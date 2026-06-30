// Inicia o servidor Ktor e registra as configurações principais do backend.
import dto.ChatResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import routes.configureMedicalChatRoutes

fun main() {
    val porta = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(
        factory = Netty,
        host = "0.0.0.0",
        port = porta,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }

    install(StatusPages) {
        exception<Throwable> { call, causa ->
            this@module.environment.log.error("Falha tecnica no backend de chat medico: ${causa::class.simpleName}")
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ChatResponse(
                    resposta = "Não foi possível responder agora. Tente novamente.",
                    tipo = "erro"
                )
            )
        }
    }

    configureMedicalChatRoutes()
}
