// passa para analisar se a IA pode responder a pergunta, 
// primeiro classifica se é caso de urgencia, se ele deve recusar ou der algum erro

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationCliente
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.text.Normalizer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val MODELO_OLLAMA = "medgemma1.5:4b"
private const val URL_OLLAMA = "http://localhost:11434/api/chat"
private const val LIMITE_MENSAGEM = 2000
private const val RESPOSTA_URGENCIA = "Atenção: pelos sinais descritos, procure atendimento de urgência imediatamente ou acione o serviço de emergência local. Esta resposta não substitui avaliação médica."
private const val RESPOSTA_RECUSA = "Não posso diagnosticar, prescrever medicamentos, indicar dose ou orientar mudança de tratamento. Posso explicar informações gerais, mas para uma orientação segura procure um profissional de saúde."
private const val RESPOSTA_ERRO = "Não foi possível responder agora. Tente novamente."

fun main() {
    embeddedServer(Netty, host = "0.0.0.0", port = System.getenv("PORT")?.toIntOrNull() ?: 8080) {
        modulo()
    }.start(wait = true)
}

fun Application.modulo() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(StatusPages) {
        exception<Throwable> { call, erro ->
            this@modulo.environment.log.error("Falha tecnica no chat medico: ${erro::class.simpleName}")
            call.respond(HttpStatusCode.InternalServerError, RespostaChat(RESPOSTA_ERRO, "erro"))
        }
    }
    routing {
        post("/api/chat-medico") {
            val pedido = runCatching { call.receive<PedidoChat>() }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, RespostaChat("Requisição inválida.", "erro"))
                return@post
            }
            val mensagem = pedido.mensagem.trim()
            when {
                mensagem.isBlank() -> call.respond(HttpStatusCode.BadRequest, RespostaChat("Mensagem vazia.", "erro"))
                mensagem.length > LIMITE_MENSAGEM -> call.respond(HttpStatusCode.BadRequest, RespostaChat("Mensagem muito longa. Resuma sua dúvida.", "erro"))
                mensagem.temUrgencia() -> call.respond(RespostaChat(RESPOSTA_URGENCIA, "urgencia"))
                mensagem.deveRecusar() -> call.respond(RespostaChat(RESPOSTA_RECUSA, "recusa_segura"))
                else -> {
                    val resposta = runCatching { ClienteOllama.responder(mensagem) }.getOrDefault("")
                    call.respond(
                        if (resposta.isBlank()) RespostaChat(RESPOSTA_ERRO, "erro")
                        else RespostaChat(resposta, "educativo")
                    )
                }
            }
        }
    }
}

@Serializable
data class PedidoChat(val mensagem: String)

@Serializable
data class RespostaChat(val resposta: String, val tipo: String)

private object ClienteOllama {
    private val cliente = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiationCliente) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
    }

    suspend fun responder(mensagem: String): String {
        val resposta = cliente.post(System.getenv("OLLAMA_CHAT_URL") ?: URL_OLLAMA) {
            contentType(ContentType.Application.Json)
            setBody(
                PedidoOllama(
                    modelo = System.getenv("OLLAMA_MODEL") ?: MODELO_OLLAMA,
                    transmitirFluxo = false,
                    mensagens = listOf(
                        MensagemOllama("system", PROMPT_MEDICO),
                        MensagemOllama("user", mensagem)
                    ),
                    opcoes = OpcoesOllama(0.2)
                )
            )
        }.body<RespostaOllama>()
        return resposta.mensagem?.conteudo.orEmpty().trim()
    }
}

@Serializable
private data class PedidoOllama(
    @SerialName("model") val modelo: String,
    @SerialName("stream") val transmitirFluxo: Boolean,
    @SerialName("messages") val mensagens: List<MensagemOllama>,
    @SerialName("options") val opcoes: OpcoesOllama
)

@Serializable
private data class MensagemOllama(
    @SerialName("role") val papel: String,
    @SerialName("content") val conteudo: String
)

@Serializable
private data class OpcoesOllama(
    @SerialName("temperature") val temperatura: Double
)

@Serializable
private data class RespostaOllama(
    @SerialName("message") val mensagem: MensagemOllama? = null
)

private fun String.temUrgencia(): Boolean {
    val texto = normalizado()
    return listOf(
        "dor no peito",
        "falta de ar intensa",
        "desmaio",
        "confusao mental",
        "convulsao",
        "fraqueza subita em um lado do corpo",
        "dificuldade subita para falar",
        "sangramento intenso",
        "febre alta persistente",
        "dor intensa",
        "piora rapida",
        "vomitos persistentes",
        "ideacao suicida",
        "vontade de morrer",
        "risco de autoagressao",
        "gravidez com sangramento",
        "gravidez com dor intensa",
        "reducao de movimentos fetais",
        "glicemia extremamente alta",
        "glicemia extremamente baixa",
        "pressao muito alta com sintomas"
    ).any { texto.contains(it) }
}

private fun String.deveRecusar(): Boolean {
    val texto = normalizado()
    return listOf(
        "qual dose",
        "dose de",
        "dosagem",
        "prescricao",
        "prescrever",
        "receita de",
        "posso parar",
        "parar de tomar",
        "trocar medicamento",
        "mudar medicamento",
        "alterar tratamento",
        "diagnostico definitivo",
        "qual meu diagnostico",
        "o que eu tenho",
        "tratamento individualizado",
        "tratamento para mim",
        "que remedio devo tomar",
        "qual remedio devo tomar"
    ).any { texto.contains(it) }
}

private fun String.normalizado(): String {
    return Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .replace("\\s+".toRegex(), " ")
        .trim()
}

private const val PROMPT_MEDICO = """
Você é um assistente de educação em saúde.

Responda dúvidas gerais sobre saúde em português do Brasil, explique termos médicos de forma simples e sempre deixe claro que a resposta não substitui avaliação médica.

Limites obrigatórios:
- Não realiza diagnóstico.
- Não prescreve medicamentos.
- Não indica dose.
- Não altera tratamento.
- Não orienta parar, iniciar ou trocar medicamentos.
- Não substitui consulta médica.
- Não afirma que o usuário tem uma doença.
- Se houver emergência, oriente atendimento imediato.

Aviso obrigatório quando a resposta envolver sintomas, exames, tratamento ou medicamentos:
"Esta resposta é apenas educativa e não substitui avaliação de um profissional de saúde."
"""
