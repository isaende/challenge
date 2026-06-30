// Implementa a chamada HTTP ao backend do chat médico usando Ktor Client.
package com.ada.chatmedico.dados.rede

import com.ada.chatmedico.configuracao.ConfiguracaoApiChatMedico
import com.ada.chatmedico.dados.dto.RespostaChatMedicoDto
import com.ada.chatmedico.dados.dto.SolicitacaoChatMedicoDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiChatMedicoKtor(
    private val clienteHttp: HttpClient = criarClienteHttp(),
    private val urlBase: String = ConfiguracaoApiChatMedico.API_BASE_URL
) : ApiChatMedico {

    override suspend fun enviarMensagem(mensagem: String): RespostaChatMedicoDto {
        return clienteHttp.post(urlEndpointChat()) {
            contentType(ContentType.Application.Json)
            setBody(SolicitacaoChatMedicoDto(mensagem = mensagem))
        }.body()
    }

    private fun urlEndpointChat(): String {
        return urlBase.trimEnd('/') + ConfiguracaoApiChatMedico.CAMINHO_CHAT_MEDICO
    }

    companion object {
        fun criarClienteHttp(): HttpClient {
            return HttpClient(OkHttp) {
                expectSuccess = true

                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }

                install(HttpTimeout) {
                    connectTimeoutMillis = 15_000
                    requestTimeoutMillis = 30_000
                    socketTimeoutMillis = 30_000
                }
            }
        }
    }
}
