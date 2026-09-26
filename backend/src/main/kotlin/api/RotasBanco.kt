package api

import dao.*
import factory.ConnectionFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import java.nio.file.Files
import java.nio.file.Path
import java.io.File
import java.security.MessageDigest
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.*

fun Route.rotasBanco() {
    val cadastroService=CadastroService(); val perfilDAO=AdaPerfilDAO(); val metasDAO=AdaMetasDAO()
    val aguaDAO=AdaAguaDAO(); val resumoDAO=AdaResumoDAO(); val medicamentosDAO=AdaMedicamentosDAO()
    val agendaDAO=AdaAgendaDAO(); val dosesDAO=AdaDosesDAO(); val examesDAO=AdaExamesDAO()
    val treinosDAO = AdaTreinosDAO()

    get("/api/perfis/{perfilId}/desafios") { executar(call) {
        val id = perfilId(call)
        val resumos = resumoDAO.listarPorPerfil(id)
        mapOf(
            "HIDRATACAO" to aguaDAO.diasComMeta(id),
            "EXERCICIOS" to treinosDAO.listarPorPerfil(id).filter { it.concluidoEm != null }.map { it.dataTreino }.distinct(),
            "CAMINHADA" to resumos.filter { (it.passos ?: 0) >= 10_000 }.map { it.dataResumo },
            "SONO" to resumos.filter { (it.sonoMinutos ?: 0) >= 420 }.map { it.dataResumo }
        )
    } }

    get("/api/perfis/{perfilId}/habitos") { executar(call) {
        val inicio = try {
            LocalDate.parse(call.request.queryParameters["inicio"] ?: LocalDate.now().toString())
        } catch (_: java.time.format.DateTimeParseException) {
            throw IllegalArgumentException("Data inicial invalida")
        }
        AdaHabitosDAO().listarSemana(perfilId(call), inicio)
    } }

    get("/api/perfis/{perfilId}/metas") { executar(call) { metasDAO.listarPorPerfil(perfilId(call)) } }
    post("/api/perfis/{perfilId}/metas") { executar(call) {
        val req = call.receive<MetasRequest>()
        req.validar()
        metasDAO.salvarManuais(perfilId(call), req.aguaMl, req.passos, req.treinosSemana)
    } }
    get("/api/perfis/{perfilId}/treinos") { executar(call) { treinosDAO.listarPorPerfil(perfilId(call)) } }
    patch("/api/perfis/{perfilId}/treinos/conclusao") { executar(call) {
        val req = call.receive<TreinoDiaRequest>()
        val dia = req.validar()
        treinosDAO.marcarDia(perfilId(call), dia.toString(), req.concluido)
        treinosDAO.listarPorPerfil(perfilId(call))
    } }

    post("/api/auth/cadastro") { executar(call) { cadastroService.cadastrar(call.receive()) } }
    post("/api/auth/login") { executar(call) { cadastroService.login(call.receive()) } }
    get("/api/perfis/{perfilId}") { executar(call) {
        val id=perfilId(call); val perfil=perfilDAO.buscarPorId(id)?:error("Perfil nao encontrado")
        val usuario=AdaUsuariosDAO().buscarPorId(perfil.usuarioId)?:error("Usuario nao encontrado")
        val metas=metasDAO.listarPorPerfil(id)
        val medidas = AdaMedidasDAO().listarPorPerfil(id).firstOrNull()
        DadosPerfilResponse(AuthResponse(usuario.codUsuario,id,perfil.nomeCompleto,usuario.email,metas.firstOrNull{it.tipo=="AGUA"}?.valorAlvo?.toInt()?:0,metas.firstOrNull{it.tipo=="PASSOS"}?.valorAlvo?.toLong()?:0),metas,perfil,medidas)
    } }
    get("/api/perfis/{perfilId}/agua") { executar(call) { val itens=aguaDAO.listarPorPerfilEData(perfilId(call),call.request.queryParameters["data"]?:LocalDate.now().toString());AguaResponse(itens,itens.sumOf(AdaAgua::quantidadeMl)) } }
    post("/api/perfis/{perfilId}/agua") { executar(call,HttpStatusCode.Created) { val req=call.receive<AguaRequest>();require(req.quantidadeMl in 1..10_000){"Quantidade de agua invalida"};aguaDAO.inserir(AdaAgua(quantidadeMl=req.quantidadeMl,perfilId=perfilId(call))) } }
    get("/api/perfis/{perfilId}/resumos") { executar(call) {
        resumoDAO.listarPorPerfil(perfilId(call))
    } }
    post("/api/perfis/{perfilId}/resumos") { executar(call) {
        val req = call.receive<ResumoRequest>()
        ResumoResponse(resumoDAO.salvar(AdaResumo(
            dataResumo = req.dataResumo, passos = req.passos, sonoMinutos = req.sonoMinutos,
            batimentosUltimos = req.batimentosUltimos, perfilId = perfilId(call),
            pressaoSistolica = req.pressaoSistolica, pressaoDiastolica = req.pressaoDiastolica
        )))
    } }
    get("/api/perfis/{perfilId}/medicamentos") { executar(call) { medicamentosDAO.listarPorPerfil(perfilId(call)).map { med -> val agenda=agendaDAO.buscarPorMedicamento(med.id).firstOrNull();val dose=agenda?.let{dosesDAO.listarPorAgenda(it.id).firstOrNull{d->d.tomadaEm?.startsWith(LocalDate.now().toString())==true}};MedicamentoResponse(med.id,med.nomeMedicamento,med.dosagemTexto,agenda?.horarioLocal?:"08:00",agenda?.lembrete?:false,dose?.tomadaEm) } } }
    post("/api/perfis/{perfilId}/medicamentos") { executar(call,HttpStatusCode.Created) { val req=call.receive<MedicamentoRequest>();ConnectionFactory.transaction { con -> val med=medicamentosDAO.inserir(AdaMedicamentos(nomeMedicamento=req.nome,dosagemTexto=req.dosagem,inicio=LocalDate.now().toString(),perfilId=perfilId(call)),con);val agenda=agendaDAO.inserir(AdaAgenda(horarioLocal=req.horario,lembrete=req.alarmeAtivo,medicamentoId=med.id),con);MedicamentoResponse(med.id,med.nomeMedicamento,med.dosagemTexto,agenda.horarioLocal,agenda.lembrete) } } }
    patch("/api/perfis/{perfilId}/medicamentos/{medicamentoId}/tomada") { executar(call) { val medId=call.parameters["medicamentoId"]?.toLongOrNull()?:error("Medicamento invalido");val agenda=agendaDAO.buscarPorMedicamento(medId).firstOrNull()?:error("Agenda nao encontrada");dosesDAO.inserir(AdaDoses(previsao=LocalDateTime.now().toString(),tomadaEm=LocalDateTime.now().toString(),status="TOMADA",agendaId=agenda.id)) } }
    get("/api/perfis/{perfilId}/exames") { executar(call) { ExamesResponse(examesDAO.listarPorPerfil(perfilId(call))) } }
    get("/api/perfis/{perfilId}/exames/{exameId}/arquivo") {
        val perfil=perfilId(call);val id=call.parameters["exameId"]?.toLongOrNull()?:error("Exame invalido")
        val exame=withContext(Dispatchers.IO){examesDAO.buscarPorId(id)}
        if(exame==null||exame.perfilId!=perfil)call.respond(HttpStatusCode.NotFound,ErroResponse("Exame nao encontrado"))
        else { val arquivo=File(exame.armazenamento);if(!arquivo.isFile)call.respond(HttpStatusCode.NotFound,ErroResponse("Arquivo nao encontrado")) else call.respondFile(arquivo) }
    }
    post("/api/perfis/{perfilId}/exames") { executar(call,HttpStatusCode.Created) { val req=call.receive<ExameRequest>();val bytes=Base64.getDecoder().decode(req.arquivoBase64);require(bytes.size<=10*1024*1024){"Arquivo excede 10 MB"};val extensao=req.nomeArquivo.substringAfterLast('.',"").filter(Char::isLetterOrDigit).take(8);val diretorio=Path.of("backend","uploads","exames",perfilId(call).toString());Files.createDirectories(diretorio);val destino=diretorio.resolve("${UUID.randomUUID()}${if(extensao.isBlank())"" else ".$extensao"}");Files.write(destino,bytes);val checksum=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)};examesDAO.inserir(AdaExames(nomeExame=req.nome,dataExame=req.data,tipoMime=req.tipoMime,nomeArquivo=req.nomeArquivo,armazenamento=destino.toAbsolutePath().toString(),checksum=checksum,perfilId=perfilId(call))) } }
}

private fun perfilId(call:io.ktor.server.application.ApplicationCall)=call.parameters["perfilId"]?.toLongOrNull()?:error("Perfil invalido")
private suspend fun executar(call:io.ktor.server.application.ApplicationCall,status:HttpStatusCode=HttpStatusCode.OK,bloco:suspend()->Any)=try { val resposta=withContext(Dispatchers.IO){bloco()};call.respond(status,resposta) } catch(erro:IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest,ErroResponse(erro.message?:"Dados invalidos")) } catch(erro:IllegalStateException) { call.respond(HttpStatusCode.Unauthorized,ErroResponse(erro.message?:"Operacao nao autorizada")) } catch(erro:SQLException) { call.application.environment.log.error("Falha ao executar operacao no Oracle",erro);call.respond(HttpStatusCode.InternalServerError,ErroResponse("Nao foi possivel salvar os dados no Oracle. Verifique os valores e tente novamente.")) }
