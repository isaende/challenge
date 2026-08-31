package dao

import factory.ConnectionFactory
import model.AdaChat

class AdaChatDAO {
    fun inserir(item:AdaChat):AdaChat=ConnectionFactory.transaction { con -> val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_CHAT");con.prepareStatement("INSERT INTO ADA_CHAT (id,inicio,fim,expiracaoPos,ADA_PERFIL_id) VALUES (?,SYSTIMESTAMP,?,?,?)").use { ps -> ps.setLong(1,id);ps.setTimestampNulo(2,item.fim);ps.setInt(3,if(item.expiracaoPos)1 else 0);ps.setLong(4,item.perfilId);ps.executeUpdate() };item.copy(id=id) }
    fun listarPorPerfil(perfilId:Long): List<AdaChat> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_CHAT WHERE ADA_PERFIL_id=? ORDER BY inicio DESC").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaChat(rs.getLong("id"),rs.instante("inicio"),rs.instante("fim"),rs.getInt("expiracaoPos")==1,rs.getLong("ADA_PERFIL_id"))) } } } }
}
