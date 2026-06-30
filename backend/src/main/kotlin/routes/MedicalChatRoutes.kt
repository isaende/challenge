// Define a rota POST /api/chat-medico consumida pelo aplicativo Android.
package routes

import dto.ChatRequest
import dto.ChatResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import ollama.OllamaClient
import safety.MedicalSafetyChecker

private const val LIMITE_MENSAGEM = 2000

private const val RESPOSTA_URGENCIA =
    "Atenção: pelos sinais descritos, procure atendimento de urgência imediatamente ou acione o serviço de emergência local. Esta resposta não substitui avaliação médica."

private const val RESPOSTA_RECUSA_SEGURA =
    "Não posso diagnosticar, prescrever medicamentos, indicar dose ou orientar mudança de tratamento. Posso explicar informações gerais, mas para uma orientação segura procure um profissional de saúde."

private const val RESPOSTA_ERRO =
    "Não foi possível responder agora. Tente novamente."

fun Application.configureMedicalChatRoutes(
    ollamaClient: OllamaClient = OllamaClient()
) {
    routing {
        post("/api/chat-medico") {
            val request = runCatching { call.receive<ChatRequest>() }.getOrElse {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ChatResponse(
                        resposta = "Requisição inválida.",
                        tipo = "erro"
                    )
                )
                return@post
            }

            val mensagem = request.mensagem.trim()

            when {
                mensagem.isBlank() -> {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ChatResponse(
                            resposta = "Mensagem vazia.",
                            tipo = "erro"
                        )
                    )
                }

                mensagem.length > LIMITE_MENSAGEM -> {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ChatResponse(
                            resposta = "Mensagem muito longa. Resuma sua dúvida.",
                            tipo = "erro"
                        )
                    )
                }

                MedicalSafetyChecker.contemSinalUrgencia(mensagem) -> {
                    call.respond(
                        ChatResponse(
                            resposta = RESPOSTA_URGENCIA,
                            tipo = "urgencia"
                        )
                    )
                }

                MedicalSafetyChecker.deveRecusarPorSeguranca(mensagem) -> {
                    call.respond(
                        ChatResponse(
                            resposta = RESPOSTA_RECUSA_SEGURA,
                            tipo = "recusa_segura"
                        )
                    )
                }

                else -> {
                    val respostaModelo = runCatching {
                        ollamaClient.gerarRespostaEducativa(mensagemDoUsuario = mensagem)
                    }.getOrElse { erro ->
                        this@configureMedicalChatRoutes.environment.log.warn("Falha tecnica ao consultar Ollama: ${erro::class.simpleName}")
                        ""
                    }

                    if (respostaModelo.isBlank()) {
                        call.respond(
                            ChatResponse(
                                resposta = RESPOSTA_ERRO,
                                tipo = "erro"
                            )
                        )
                    } else {
                        call.respond(
                            ChatResponse(
                                resposta = respostaModelo,
                                tipo = "educativo"
                            )
                        )
                    }
                }
            }
        }
    }
}
