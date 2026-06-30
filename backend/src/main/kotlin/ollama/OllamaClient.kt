// Cliente HTTP que envia mensagens ao Ollama usando o modelo MedGemma.
package ollama

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import prompts.MedicalPrompts

class OllamaClient(
    private val clienteHttp: HttpClient = criarClienteHttp(),
    private val urlOllama: String = System.getenv("OLLAMA_CHAT_URL")
        ?: "http://localhost:11434/api/chat",
    private val modelo: String = System.getenv("OLLAMA_MODEL")
        ?: "medgemma1.5:4b"
) {
    suspend fun gerarRespostaEducativa(mensagemDoUsuario: String): String {
        val resposta = clienteHttp.post(urlOllama) {
            contentType(ContentType.Application.Json)
            setBody(
                OllamaChatRequest(
                    model = modelo,
                    stream = false,
                    messages = listOf(
                        OllamaMessage(
                            role = "system",
                            content = MedicalPrompts.CHAT_SYSTEM_PROMPT
                        ),
                        OllamaMessage(
                            role = "user",
                            content = mensagemDoUsuario
                        )
                    ),
                    options = OllamaOptions(temperature = 0.2)
                )
            )
        }.body<OllamaChatResponse>()

        return resposta.message?.content.orEmpty().trim()
    }

    companion object {
        fun criarClienteHttp(): HttpClient {
            return HttpClient(CIO) {
                expectSuccess = true

                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }

                install(HttpTimeout) {
                    connectTimeoutMillis = 10_000
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 60_000
                }
            }
        }
    }
}
