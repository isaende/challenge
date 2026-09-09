package home_page

import android.content.Context
import android.net.Uri
import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import java.time.LocalDate
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// No emulador do Android Studio, 10.0.2.2 aponta para o computador host.
private const val URL_API_ADA = "http://10.0.2.2:8080"

@Serializable data class CadastroApi(val nomeCompleto:String,val email:String,val dataNascimento:String,val pesoKg:Double,val alturaCm:Double,val genero:String,val objetivoPrincipal:String,val senha:String)
@Serializable data class LoginApi(val email:String,val senha:String)
@Serializable data class SessaoUsuario(val usuarioId:Long,val perfilId:Long,val nomeCompleto:String,val email:String,val metaAguaMl:Int,val metaPassos:Long)
@Serializable data class PerfilApi(val nomeCompleto: String, val dataNascimento: String, val genero: String, val objetivoPrincipal: String)
@Serializable data class MedidasPerfilApi(val pesoKg: Double, val alturaCm: Double)
@Serializable data class DadosPerfilApi(val auth: SessaoUsuario, val perfil: PerfilApi, val medidas: MedidasPerfilApi? = null)
@Serializable data class MedicamentoApi(val id:Long,val nome:String,val dosagem:String,val horario:String,val alarmeAtivo:Boolean,val tomadoEm:String?=null)
@Serializable data class MedicamentoNovoApi(val nome:String,val dosagem:String,val horario:String,val alarmeAtivo:Boolean)
@Serializable data class AguaNovaApi(val quantidadeMl:Int)
@Serializable data class RegistroAguaApi(val id:Long,val quantidadeMl:Int,val origem:String,val consumidoEm:String?=null,val perfilId:Long)
@Serializable data class AguaRespostaApi(val registros:List<RegistroAguaApi>,val totalMl:Int)
@Serializable data class ResumoNovoApi(val dataResumo:String,val passos:Long,val sonoMinutos:Long,val batimentosUltimos:Int?=null)
@Serializable data class ExameNovoApi(val nome:String,val data:String,val tipoMime:String,val nomeArquivo:String,val arquivoBase64:String)
@Serializable data class ExameApi(val id:Long,val nomeExame:String,val dataExame:String,val tipoMime:String,val nomeArquivo:String,val armazenamento:String,val checksum:String,val enviadoEm:String?=null,val perfilId:Long)
@Serializable data class ExamesRespostaApi(val exames:List<ExameApi>)
@Serializable data class MetaApi(val tipo: String, val valorAlvo: Double)
@Serializable data class MetasNovasApi(val aguaMl: Int, val passos: Long, val treinosSemana: Int)
@Serializable data class TreinoApi(val dataTreino: String, val concluidoEm: String? = null)
@Serializable data class HabitoDiaApi(val data: String, val aguaMl: Long = 0, val passos: Long? = null)
@Serializable data class TreinoDiaApi(val data: String, val concluido: Boolean)
@Serializable private data class ErroApi(val mensagem:String)

private suspend inline fun <reified T> HttpResponse.corpoOuErro():T {
    if(status.isSuccess()) return body()
    val mensagem=runCatching { body<ErroApi>().mensagem }.getOrDefault("Falha na comunicacao com o servidor")
    throw IllegalArgumentException(mensagem)
}

object ApiAda {
    private val cliente=HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys=true }) }
    }

    suspend fun carregarPerfil(perfilId: Long): DadosPerfilApi =
        cliente.get("$URL_API_ADA/api/perfis/$perfilId").corpoOuErro()

    suspend fun listarMetas(perfilId: Long): List<MetaApi> =
        cliente.get("$URL_API_ADA/api/perfis/$perfilId/metas").corpoOuErro()
    suspend fun salvarMetas(perfilId: Long, metas: MetasNovasApi): List<MetaApi> =
        cliente.post("$URL_API_ADA/api/perfis/$perfilId/metas") {
            contentType(ContentType.Application.Json); setBody(metas)
        }.corpoOuErro()
    suspend fun listarTreinos(perfilId: Long): List<TreinoApi> =
        cliente.get("$URL_API_ADA/api/perfis/$perfilId/treinos").corpoOuErro()
    suspend fun listarHabitosSemana(perfilId: Long, inicio: LocalDate): List<HabitoDiaApi> =
        cliente.get("$URL_API_ADA/api/perfis/$perfilId/habitos?inicio=$inicio").corpoOuErro()
    suspend fun marcarTreino(perfilId: Long, data: LocalDate, concluido: Boolean): List<TreinoApi> =
        cliente.patch("$URL_API_ADA/api/perfis/$perfilId/treinos/conclusao") {
            contentType(ContentType.Application.Json); setBody(TreinoDiaApi(data.toString(), concluido))
        }.corpoOuErro()

    suspend fun cadastrar(dados:CadastroApi):SessaoUsuario=cliente.post("$URL_API_ADA/api/auth/cadastro") { contentType(ContentType.Application.Json);setBody(dados) }.corpoOuErro()
    suspend fun login(email:String,senha:String):SessaoUsuario=cliente.post("$URL_API_ADA/api/auth/login") { contentType(ContentType.Application.Json);setBody(LoginApi(email,senha)) }.corpoOuErro()
    suspend fun listarMedicamentos(perfilId:Long): List<MedicamentoApi> = cliente.get("$URL_API_ADA/api/perfis/$perfilId/medicamentos").body()
    suspend fun adicionarMedicamento(perfilId:Long,item:MedicamentoNovoApi):MedicamentoApi=cliente.post("$URL_API_ADA/api/perfis/$perfilId/medicamentos") { contentType(ContentType.Application.Json);setBody(item) }.body()
    suspend fun marcarMedicamentoTomado(perfilId:Long,medicamentoId:Long) {
        cliente.patch("$URL_API_ADA/api/perfis/$perfilId/medicamentos/$medicamentoId/tomada")
    }
    suspend fun listarAgua(perfilId:Long):AguaRespostaApi=cliente.get("$URL_API_ADA/api/perfis/$perfilId/agua?data=${LocalDate.now()}").body()
    suspend fun adicionarAgua(perfilId:Long,quantidadeMl:Int):RegistroAguaApi=cliente.post("$URL_API_ADA/api/perfis/$perfilId/agua") { contentType(ContentType.Application.Json);setBody(AguaNovaApi(quantidadeMl)) }.body()
    suspend fun salvarResumo(perfilId:Long,dados:DadosSaude)=cliente.post("$URL_API_ADA/api/perfis/$perfilId/resumos") { contentType(ContentType.Application.Json);setBody(ResumoNovoApi(LocalDate.now().toString(),dados.passos?:0,dados.sonoMinutos?:0,dados.batimentos)) }
    suspend fun listarExames(perfilId:Long): List<ExameApi> = cliente.get("$URL_API_ADA/api/perfis/$perfilId/exames").body<ExamesRespostaApi>().exames
    suspend fun baixarExame(contexto:Context,perfilId:Long,exame:ExameApi):String {
        val bytes=cliente.get("$URL_API_ADA/api/perfis/$perfilId/exames/${exame.id}/arquivo").body<ByteArray>()
        val extensao=exame.nomeArquivo.substringAfterLast('.',"").filter(Char::isLetterOrDigit).take(8)
        val arquivo=File(contexto.filesDir,"exames/${exame.id}${if(extensao.isBlank())"" else ".$extensao"}")
        arquivo.parentFile?.mkdirs();arquivo.writeBytes(bytes);return arquivo.absolutePath
    }
    suspend fun enviarExame(contexto:Context,perfilId:Long,nome:String,data:String,tipoMime:String,nomeArquivo:String,uri:Uri):ExameApi {
        val bytes=contexto.contentResolver.openInputStream(uri)?.use { it.readBytes() }?:error("Nao foi possivel ler o arquivo")
        require(bytes.size<=10*1024*1024){"O arquivo deve ter no maximo 10 MB"}
        return cliente.post("$URL_API_ADA/api/perfis/$perfilId/exames") { contentType(ContentType.Application.Json);setBody(ExameNovoApi(nome,data,tipoMime,nomeArquivo,Base64.encodeToString(bytes,Base64.NO_WRAP))) }.body()
    }
}

class RepositorioSessao(contexto:Context) {
    private val preferencias=contexto.getSharedPreferences("sessao_usuario",Context.MODE_PRIVATE)
    fun carregar():SessaoUsuario? { val perfil=preferencias.getLong("perfilId",0);if(perfil==0L)return null;return SessaoUsuario(preferencias.getLong("usuarioId",0),perfil,preferencias.getString("nomeCompleto","").orEmpty(),preferencias.getString("email","").orEmpty(),preferencias.getInt("metaAguaMl",0),preferencias.getLong("metaPassos",0)) }
    fun salvar(sessao:SessaoUsuario){preferencias.edit().putLong("usuarioId",sessao.usuarioId).putLong("perfilId",sessao.perfilId).putString("nomeCompleto",sessao.nomeCompleto).putString("email",sessao.email).putInt("metaAguaMl",sessao.metaAguaMl).putLong("metaPassos",sessao.metaPassos).apply()}
    fun limpar(){preferencias.edit().clear().apply()}
}
