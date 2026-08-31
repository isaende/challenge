package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaMetas

class AdaMetasDAO {
    fun inserir(item: AdaMetas, conexao: Connection? = null): AdaMetas {
        val executar: (Connection) -> AdaMetas = { con ->
            val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_METAS")
            con.prepareStatement("""INSERT INTO ADA_METAS
                (id,tipo,valorAlvo,unidade,origem,versaoAlgoritmos,calculos,validaDesde,validaAte,ADA_PERFIL_id)
                VALUES (?,?,?,?,?,?,?,?,?,?)""").use { ps ->
                ps.setLong(1,id);ps.setString(2,item.tipo);ps.setDouble(3,item.valorAlvo);ps.setString(4,item.unidade);ps.setString(5,item.origem)
                ps.setString(6,item.versaoAlgoritmo);ps.setString(7,item.calculos);ps.setDate(8,java.sql.Date.valueOf(item.validaDesde));ps.setDataNula(9,item.validaAte);ps.setLong(10,item.perfilId);ps.executeUpdate()
            }; item.copy(id=id)
        }; return conexao?.let(executar) ?: ConnectionFactory.transaction(executar)
    }
    fun listarPorPerfil(perfilId: Long): List<AdaMetas> = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("SELECT * FROM ADA_METAS WHERE ADA_PERFIL_id=? AND (validaAte IS NULL OR validaAte>=TRUNC(SYSDATE)) ORDER BY validaDesde DESC").use { ps ->
            ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next()) add(AdaMetas(rs.getLong("id"),rs.getString("tipo"),rs.getDouble("valorAlvo"),rs.getString("unidade"),rs.getString("origem"),rs.getString("versaoAlgoritmos"),rs.getString("calculos"),rs.data("validaDesde"),rs.dataNula("validaAte"),rs.getLong("ADA_PERFIL_id"))) } }
        }
    }
}
