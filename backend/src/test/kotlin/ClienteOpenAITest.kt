import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class ClienteOpenAITest {
    @Test fun `envia texto e imagem com autenticacao e extrai todos os textos`() = runBlocking {
        val imagem = "iVBORw0KGgo="
        val engine = MockEngine { pedido ->
            assertEquals("https://api.openai.com/v1/responses", pedido.url.toString())
            assertEquals("Bearer chave-teste", pedido.headers[HttpHeaders.Authorization])
            val corpo = Json.parseToJsonElement(
                (pedido.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
            ).jsonObject
            assertEquals(false, corpo["store"]!!.jsonPrimitive.boolean)
            assertEquals("gpt-4.1-mini", corpo["model"]!!.jsonPrimitive.content)
            val conteudo = corpo["input"]!!.jsonArray.single().jsonObject["content"]!!.jsonArray
            assertEquals("Explique o exame", conteudo[0].jsonObject["text"]!!.jsonPrimitive.content)
            assertEquals("data:image/png;base64,$imagem", conteudo[1].jsonObject["image_url"]!!.jsonPrimitive.content)
            respond(
                """{"status":"completed","output":[{"type":"reasoning"},{"type":"message","content":[{"type":"output_text","text":"Primeiro."},{"type":"output_text","text":"Segundo."}]}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        cliente(engine).use { http ->
            assertEquals("Primeiro.\nSegundo.", ClienteOpenAI("chave-teste", http).responder("Explique o exame", imagem))
        }
    }

    @Test fun `texto sem anexo e recusa da API sao aceitos`() = runBlocking {
        val engine = MockEngine { pedido ->
            val corpo = Json.parseToJsonElement((pedido.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject
            assertEquals(1, corpo["input"]!!.jsonArray.single().jsonObject["content"]!!.jsonArray.size)
            respond("""{"status":"completed","output":[{"type":"message","content":[{"type":"refusal","refusal":"Não posso responder."}]}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        cliente(engine).use { http ->
            assertEquals("Não posso responder.", ClienteOpenAI("teste", http).responder("Olá"))
        }
    }

    @Test fun `chave vazia e imagem invalida impedem chamada externa`() = runBlocking {
        cliente(MockEngine { error("Não deveria chamar a API") }).use { http ->
            assertFailsWith<IllegalStateException> { ClienteOpenAI("", http).responder("Olá") }
            assertFailsWith<IllegalArgumentException> { ClienteOpenAI("teste", http).responder("Olá", "invalida") }
        }
    }

    @Test fun `resposta incompleta e erro HTTP nao viram resposta educativa`() = runBlocking {
        cliente(MockEngine {
            respond("""{"status":"incomplete","output":[]}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }).use { http ->
            assertFailsWith<IllegalStateException> { ClienteOpenAI("teste", http).responder("Olá") }
        }
        cliente(MockEngine { respond("{}", HttpStatusCode.Unauthorized) }).use { http ->
            assertFailsWith<io.ktor.client.plugins.ClientRequestException> { ClienteOpenAI("teste", http).responder("Olá") }
        }
    }

    private fun cliente(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json() }
    }
}
