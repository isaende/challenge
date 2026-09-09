package dao

import factory.ConnectionFactory
import model.AdaChat

class AdaChatDAO {
    fun inserir(item:AdaChat):AdaChat=ConnectionFactory.transaction { con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_CHAT (inicio,fim,expiracaoPos,ADA_PERFIL_id) VALUES (SYSTIMESTAMP,?,?,?)") { ps -> ps.setTimestampNulo(1,item.fim);ps.setInt(2,if(item.expiracaoPos)1 else 0);ps.setLong(3,item.perfilId) };item.copy(id=id) }
    fun listarPorPerfil(perfilId:Long): List<AdaChat> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_CHAT WHERE ADA_PERFIL_id=? ORDER BY inicio DESC").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaChat(rs.getLong("id"),rs.instante("inicio"),rs.instante("fim"),rs.getInt("expiracaoPos")==1,rs.getLong("ADA_PERFIL_id"))) } } } }
}
