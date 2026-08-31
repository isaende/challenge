package dao

import factory.ConnectionFactory
import model.AdaMensagens

class AdaMensagensDAO {
    fun inserir(item:AdaMensagens):AdaMensagens=ConnectionFactory.transaction { con -> val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_MENSAGENS");con.prepareStatement("INSERT INTO ADA_MENSAGENS (id,tipoResposta,conteudo,criadoEm,ADA_CHAT_id) VALUES (?,?,?,SYSTIMESTAMP,?)").use { ps -> ps.setLong(1,id);ps.setInt(2,item.tipoResposta);ps.setString(3,item.conteudo);ps.setLong(4,item.chatId);ps.executeUpdate() };item.copy(id=id) }
    fun listarPorChat(chatId:Long): List<AdaMensagens> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_MENSAGENS WHERE ADA_CHAT_id=? ORDER BY criadoEm").use { ps -> ps.setLong(1,chatId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaMensagens(rs.getLong("id"),rs.getInt("tipoResposta"),rs.getString("conteudo"),rs.instante("criadoEm"),rs.getLong("ADA_CHAT_id"))) } } } }
}
