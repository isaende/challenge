import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.util.Base64
import kotlinx.serialization.json.*

private const val MODELO_OPENAI = "gpt-4.1-mini"
private const val URL_OPENAI = "https://api.openai.com/v1/responses"
private const val LIMITE_IMAGEM_BYTES = 10 * 1024 * 1024

internal class ClienteOpenAI(
    private val chave: String = System.getenv("OPENAI_API_KEY").orEmpty(),
    private val cliente: HttpClient = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) { json() }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 180_000
            socketTimeoutMillis = 180_000
        }
    }
) {
    suspend fun responder(mensagem: String, imagemBase64: String? = null): String {
        check(chave.isNotBlank()) { "Configure a variavel de ambiente OPENAI_API_KEY no backend para habilitar o chat." }
        val pedido = buildJsonObject {
            put("model", MODELO_OPENAI)
            put("instructions", PROMPT_MEDICO)
            put("store", false)
            put("max_output_tokens", 500)
            putJsonArray("input") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "input_text")
                            put("text", mensagem)
                        }
                        imagemBase64?.let {
                            addJsonObject {
                                put("type", "input_image")
                                put("image_url", imagemDataUrl(it))
                            }
                        }
                    }
                }
            }
        }
        val resposta = cliente.post(URL_OPENAI) {
            bearerAuth(chave.trim())
            contentType(ContentType.Application.Json)
            setBody(pedido)
        }.body<JsonObject>()
        check(resposta["status"]?.jsonPrimitive?.content == "completed") {
            "A OpenAI não concluiu a resposta."
        }
        return resposta["output"]?.jsonArray.orEmpty()
            .map { it.jsonObject }
            .filter { it["type"]?.jsonPrimitive?.content == "message" }
            .flatMap { it["content"]?.jsonArray.orEmpty() }
            .map { it.jsonObject }
            .mapNotNull {
                when (it["type"]?.jsonPrimitive?.content) {
                    "output_text" -> it["text"]?.jsonPrimitive?.content
                    "refusal" -> it["refusal"]?.jsonPrimitive?.content
                    else -> null
                }
            }.joinToString("\n").trim()
    }
}

private fun imagemDataUrl(imagem: String): String {
    require(imagem.length <= ((LIMITE_IMAGEM_BYTES + 2) / 3) * 4) { "Imagem muito grande." }
    val dados = Base64.getDecoder().decode(imagem)
    require(dados.isNotEmpty() && dados.size <= LIMITE_IMAGEM_BYTES) { "Imagem inválida." }
    val cabecalho = dados.take(12).toByteArray().toString(Charsets.ISO_8859_1)
    val assinatura = dados.take(8).map { it.toInt() and 0xff }
    val tipo = when {
        assinatura.take(3) == listOf(0xff, 0xd8, 0xff) -> "image/jpeg"
        assinatura.take(8) == listOf(137, 80, 78, 71, 13, 10, 26, 10) -> "image/png"
        cabecalho.take(6) in listOf("GIF87a", "GIF89a") -> "image/gif"
        cabecalho.startsWith("RIFF") && cabecalho.drop(8) == "WEBP" -> "image/webp"
        else -> throw IllegalArgumentException("Formato de imagem não suportado.")
    }
    return "data:$tipo;base64,$imagem"
}
