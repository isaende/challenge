package dao

import factory.ConnectionFactory
import model.AdaCondicoes

class AdaCondicoesDAO {
    fun inserir(item:AdaCondicoes):AdaCondicoes=ConnectionFactory.transaction { con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_CONDICOES (descricao,ativa,limiteHidratacao,limiteAtividades,validadoProfissional,ADA_PERFIL_id) VALUES (?,?,?,?,?,?)") { ps -> ps.setString(1,item.descricao);ps.setInt(2,if(item.ativa)1 else 0);ps.setInt(3,if(item.limiteHidratacao)1 else 0);ps.setInt(4,if(item.limiteAtividades)1 else 0);ps.setInt(5,if(item.validadoProfissional)1 else 0);ps.setLong(6,item.perfilId) };item.copy(id=id) }
    fun listarPorPerfil(perfilId:Long): List<AdaCondicoes> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_CONDICOES WHERE ADA_PERFIL_id=?").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaCondicoes(rs.getLong("id"),rs.getString("descricao"),rs.getInt("ativa")==1,rs.getInt("limiteHidratacao")==1,rs.getInt("limiteAtividades")==1,rs.getInt("validadoProfissional")==1,rs.getLong("ADA_PERFIL_id"))) } } } }
}
