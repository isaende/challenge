package dao

import factory.ConnectionFactory
import model.AdaAgua

class AdaAguaDAO {
    fun inserir(item:AdaAgua):AdaAgua=ConnectionFactory.transaction { con -> val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_AGUA");con.prepareStatement("INSERT INTO ADA_AGUA (id,qtdMl,origem,consumidoEm,ADA_PERFIL_id) VALUES (?,?,?,SYSTIMESTAMP,?)").use { ps -> ps.setLong(1,id);ps.setInt(2,item.quantidadeMl);ps.setString(3,item.origem);ps.setLong(4,item.perfilId);ps.executeUpdate() };item.copy(id=id) }
    fun listarPorPerfilEData(perfilId:Long,data:String): List<AdaAgua> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_AGUA WHERE ADA_PERFIL_id=? AND TRUNC(consumidoEm)=? ORDER BY consumidoEm").use { ps -> ps.setLong(1,perfilId);ps.setDate(2,java.sql.Date.valueOf(data));ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaAgua(rs.getLong("id"),rs.getInt("qtdMl"),rs.getString("origem"),rs.instante("consumidoEm"),rs.getLong("ADA_PERFIL_id"))) } } } }
}
