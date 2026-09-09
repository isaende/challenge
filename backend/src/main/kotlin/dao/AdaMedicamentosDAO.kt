package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaMedicamentos

class AdaMedicamentosDAO {
    fun inserir(item:AdaMedicamentos,conexao:Connection?=null):AdaMedicamentos { val executar:(Connection)->AdaMedicamentos={ con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_MEDICAMENTOS (nm_medicamento,dosagemTxt,inicio,fim,ativo,ADA_PERFIL_id) VALUES (?,?,?,?,?,?)") { ps -> ps.setString(1,item.nomeMedicamento);ps.setString(2,item.dosagemTexto);ps.setDate(3,java.sql.Date.valueOf(item.inicio));ps.setDataNula(4,item.fim);ps.setInt(5,if(item.ativo)1 else 0);ps.setLong(6,item.perfilId) };item.copy(id=id) };return conexao?.let(executar)?:ConnectionFactory.transaction(executar) }
    fun listarPorPerfil(perfilId:Long): List<AdaMedicamentos> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_MEDICAMENTOS WHERE ADA_PERFIL_id=? AND ativo=1 ORDER BY nm_medicamento").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaMedicamentos(rs.getLong("id"),rs.getString("nm_medicamento"),rs.getString("dosagemTxt"),rs.data("inicio"),rs.dataNula("fim"),rs.getInt("ativo")==1,rs.getLong("ADA_PERFIL_id"))) } } } }
    fun desativar(id:Long)=ConnectionFactory.getConnection().use { con -> con.prepareStatement("UPDATE ADA_MEDICAMENTOS SET ativo=0,fim=TRUNC(SYSDATE) WHERE id=?").use { it.setLong(1,id);it.executeUpdate() } }
}
