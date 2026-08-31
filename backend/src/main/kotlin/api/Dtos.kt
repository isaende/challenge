package api

import kotlinx.serialization.Serializable
import model.AdaAgua
import model.AdaExames
import model.AdaMetas
import model.AdaResumo

@Serializable data class CadastroRequest(val nomeCompleto:String,val email:String,val dataNascimento:String,val pesoKg:Double,val alturaCm:Double,val genero:String,val objetivoPrincipal:String,val senha:String)
@Serializable data class LoginRequest(val email:String,val senha:String)
@Serializable data class AuthResponse(val usuarioId:Long,val perfilId:Long,val nomeCompleto:String,val email:String,val metaAguaMl:Int,val metaPassos:Long)
@Serializable data class ErroResponse(val mensagem:String)
@Serializable data class MedicamentoRequest(val nome:String,val dosagem:String,val horario:String,val alarmeAtivo:Boolean=true)
@Serializable data class MedicamentoResponse(val id:Long,val nome:String,val dosagem:String,val horario:String,val alarmeAtivo:Boolean,val tomadoEm:String?=null)
@Serializable data class AguaRequest(val quantidadeMl:Int)
@Serializable data class ResumoRequest(val dataResumo:String,val passos:Long,val sonoMinutos:Long,val batimentosUltimos:Int?=null)
@Serializable data class ExameRequest(val nome:String,val data:String,val tipoMime:String,val nomeArquivo:String,val arquivoBase64:String)
@Serializable data class DadosPerfilResponse(val auth:AuthResponse,val metas:List<AdaMetas>)
@Serializable data class AguaResponse(val registros:List<AdaAgua>,val totalMl:Int)
@Serializable data class ExamesResponse(val exames:List<AdaExames>)
@Serializable data class ResumoResponse(val resumo:AdaResumo)
