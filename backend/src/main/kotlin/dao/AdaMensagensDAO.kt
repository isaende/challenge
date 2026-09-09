package dao

import factory.ConnectionFactory
import model.AdaMensagens

class AdaMensagensDAO {
    fun inserir(item:AdaMensagens):AdaMensagens=ConnectionFactory.transaction { con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_MENSAGENS (tipoResposta,conteudo,criadoEm,ADA_CHAT_id) VALUES (?,?,SYSTIMESTAMP,?)") { ps -> ps.setInt(1,item.tipoResposta);ps.setString(2,item.conteudo);ps.setLong(3,item.chatId) };item.copy(id=id) }
    fun listarPorChat(chatId:Long): List<AdaMensagens> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_MENSAGENS WHERE ADA_CHAT_id=? ORDER BY criadoEm").use { ps -> ps.setLong(1,chatId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaMensagens(rs.getLong("id"),rs.getInt("tipoResposta"),rs.getString("conteudo"),rs.instante("criadoEm"),rs.getLong("ADA_CHAT_id"))) } } } }
}
