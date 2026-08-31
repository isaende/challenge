package dao

import factory.ConnectionFactory
import model.AdaCondicoes

class AdaCondicoesDAO {
    fun inserir(item:AdaCondicoes):AdaCondicoes=ConnectionFactory.transaction { con -> val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_CONDICOES");con.prepareStatement("INSERT INTO ADA_CONDICOES (id,descricao,ativa,limiteHidratacao,limiteAtividades,validadoProfissional,ADA_PERFIL_id) VALUES (?,?,?,?,?,?,?)").use { ps -> ps.setLong(1,id);ps.setString(2,item.descricao);ps.setInt(3,if(item.ativa)1 else 0);ps.setInt(4,if(item.limiteHidratacao)1 else 0);ps.setInt(5,if(item.limiteAtividades)1 else 0);ps.setInt(6,if(item.validadoProfissional)1 else 0);ps.setLong(7,item.perfilId);ps.executeUpdate() };item.copy(id=id) }
    fun listarPorPerfil(perfilId:Long): List<AdaCondicoes> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_CONDICOES WHERE ADA_PERFIL_id=?").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaCondicoes(rs.getLong("id"),rs.getString("descricao"),rs.getInt("ativa")==1,rs.getInt("limiteHidratacao")==1,rs.getInt("limiteAtividades")==1,rs.getInt("validadoProfissional")==1,rs.getLong("ADA_PERFIL_id"))) } } } }
}
