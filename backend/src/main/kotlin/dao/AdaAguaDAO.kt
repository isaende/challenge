package dao

import factory.ConnectionFactory
import model.AdaAgua

class AdaAguaDAO {
    fun diasComMeta(perfilId: Long): List<String> = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("SELECT TRUNC(consumidoEm) dia FROM ADA_AGUA WHERE ADA_PERFIL_id=? GROUP BY TRUNC(consumidoEm) HAVING SUM(qtdMl)>=2000 ORDER BY dia").use { ps ->
            ps.setLong(1, perfilId)
            ps.executeQuery().use { rs -> buildList { while (rs.next()) add(rs.data("dia")) } }
        }
    }
    fun inserir(item:AdaAgua):AdaAgua=ConnectionFactory.transaction { con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_AGUA (qtdMl,origem,consumidoEm,ADA_PERFIL_id) VALUES (?,?,SYSTIMESTAMP,?)") { ps -> ps.setInt(1,item.quantidadeMl);ps.setString(2,item.origem);ps.setLong(3,item.perfilId) };item.copy(id=id) }
    fun listarPorPerfilEData(perfilId:Long,data:String): List<AdaAgua> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_AGUA WHERE ADA_PERFIL_id=? AND TRUNC(consumidoEm)=? ORDER BY consumidoEm").use { ps -> ps.setLong(1,perfilId);ps.setDate(2,java.sql.Date.valueOf(data));ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaAgua(rs.getLong("id"),rs.getInt("qtdMl"),rs.getString("origem"),rs.instante("consumidoEm"),rs.getLong("ADA_PERFIL_id"))) } } } }
}
