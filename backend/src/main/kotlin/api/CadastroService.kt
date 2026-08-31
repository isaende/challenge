package api

import dao.AdaMedidasDAO
import dao.AdaMetasDAO
import dao.AdaPerfilDAO
import dao.AdaUsuariosDAO
import factory.ConnectionFactory
import java.time.LocalDate
import java.time.Period
import model.AdaMedidas
import model.AdaMetas
import model.AdaPerfil
import model.AdaUsuarios

class CadastroService(
    private val usuariosDAO:AdaUsuariosDAO=AdaUsuariosDAO(),
    private val perfilDAO:AdaPerfilDAO=AdaPerfilDAO(),
    private val medidasDAO:AdaMedidasDAO=AdaMedidasDAO(),
    private val metasDAO:AdaMetasDAO=AdaMetasDAO()
) {
    fun cadastrar(dados:CadastroRequest):AuthResponse=ConnectionFactory.transaction { con ->
        require(dados.nomeCompleto.isNotBlank()){"Informe o nome completo"}
        require(dados.email.contains('@')){"E-mail invalido"}
        require(dados.senha.length>=6){"A senha precisa ter pelo menos 6 caracteres"}
        require(dados.pesoKg>0 && dados.alturaCm>0){"Peso e altura devem ser maiores que zero"}
        require(usuariosDAO.buscarPorEmail(dados.email,con)==null){"E-mail ja cadastrado"}
        val nascimento=LocalDate.parse(dados.dataNascimento)
        require(!nascimento.isAfter(LocalDate.now())){"Data de nascimento invalida"}
        val idade=Period.between(nascimento,LocalDate.now()).years
        val usuario=usuariosDAO.inserir(AdaUsuarios(email=dados.email.trim(),senhaHash=SegurancaSenha.gerarHash(dados.senha)),con)
        val perfil=perfilDAO.inserir(AdaPerfil(nomeCompleto=dados.nomeCompleto.trim(),dataNascimento=dados.dataNascimento,genero=dados.genero,objetivoPrincipal=dados.objetivoPrincipal,usuarioId=usuario.codUsuario),con)
        val alturaMetros=dados.alturaCm/100.0
        medidasDAO.inserir(AdaMedidas(pesoKg=dados.pesoKg,alturaCm=dados.alturaCm,imcCalculado=dados.pesoKg/(alturaMetros*alturaMetros),perfilId=perfil.id),con)
        val metaPassos=calcularMetaPassos(idade,dados.objetivoPrincipal)
        val metaAgua=calcularMetaAgua(idade,dados.pesoKg)
        val hoje=LocalDate.now().toString()
        metasDAO.inserir(AdaMetas(tipo="PASSOS",valorAlvo=metaPassos.toDouble(),unidade="passos/dia",calculos="idade=$idade; objetivo=${dados.objetivoPrincipal}",validaDesde=hoje,perfilId=perfil.id),con)
        metasDAO.inserir(AdaMetas(tipo="AGUA",valorAlvo=metaAgua.toDouble(),unidade="ml/dia",calculos="idade=$idade; pesoKg=${dados.pesoKg}",validaDesde=hoje,perfilId=perfil.id),con)
        AuthResponse(usuario.codUsuario,perfil.id,perfil.nomeCompleto,usuario.email,metaAgua,metaPassos)
    }

    fun login(dados:LoginRequest):AuthResponse {
        val usuario=usuariosDAO.buscarPorEmail(dados.email)?:error("E-mail ou senha invalidos")
        require(usuario.status=="ATIVO" && SegurancaSenha.verificar(dados.senha,usuario.senhaHash)){"E-mail ou senha invalidos"}
        val perfil=perfilDAO.buscarPorUsuario(usuario.codUsuario)?:error("Perfil nao encontrado")
        val metas=metasDAO.listarPorPerfil(perfil.id)
        usuariosDAO.registrarLogin(usuario.codUsuario)
        return AuthResponse(usuario.codUsuario,perfil.id,perfil.nomeCompleto,usuario.email,metas.firstOrNull{it.tipo=="AGUA"}?.valorAlvo?.toInt()?:0,metas.firstOrNull{it.tipo=="PASSOS"}?.valorAlvo?.toLong()?:0)
    }

    internal fun calcularMetaPassos(idade:Int,objetivo:String):Long=when {
        objetivo.contains("perda",true)->10_000L
        idade>=60->6_000L
        else->8_000L
    }
    internal fun calcularMetaAgua(idade:Int,pesoKg:Double):Int=((if(idade>65)25 else 40)*pesoKg).toInt()
}
