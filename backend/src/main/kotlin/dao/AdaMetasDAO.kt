package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaMetas

class AdaMetasDAO {
    fun salvarManuais(perfilId: Long, aguaMl: Int, passos: Long, treinos: Int): List<AdaMetas> =
        ConnectionFactory.transaction { con ->
            // Serializa alterações do mesmo perfil e preserva as metas anteriores.
            con.prepareStatement("SELECT id FROM ADA_PERFIL WHERE id=? FOR UPDATE").use { ps ->
                ps.setLong(1, perfilId)
                ps.executeQuery().use { require(it.next()) { "Perfil nao encontrado" } }
            }
            con.prepareStatement("UPDATE ADA_METAS SET validaAte=TRUNC(SYSDATE)-1 WHERE ADA_PERFIL_id=? AND tipo IN ('AGUA','PASSOS','TREINOS') AND (validaAte IS NULL OR validaAte>=TRUNC(SYSDATE))").use { ps ->
                ps.setLong(1, perfilId)
                ps.executeUpdate()
            }
            listOf(
                Triple("AGUA", aguaMl.toDouble(), "ml/dia"),
                Triple("PASSOS", passos.toDouble(), "passos/dia"),
                Triple("TREINOS", treinos.toDouble(), "treinos/semana")
            ).map { (tipo, alvo, unidade) ->
                inserir(AdaMetas(tipo = tipo, valorAlvo = alvo, unidade = unidade,
                    origem = "MANUAL", validaDesde = java.time.LocalDate.now().toString(), perfilId = perfilId), con)
            }
        }

    fun inserir(item: AdaMetas, conexao: Connection? = null): AdaMetas {
        val executar: (Connection) -> AdaMetas = { con ->
            val id=ConnectionFactory.inserirRetornandoId(con,"""INSERT INTO ADA_METAS
                (tipo,valorAlvo,unidade,origem,versaoAlgoritmos,calculos,validaDesde,validaAte,ADA_PERFIL_id)
                VALUES (?,?,?,?,?,?,?,?,?)""") { ps ->
                ps.setString(1,item.tipo);ps.setDouble(2,item.valorAlvo);ps.setString(3,item.unidade);ps.setString(4,item.origem)
                ps.setString(5,item.versaoAlgoritmo);ps.setString(6,item.calculos);ps.setDate(7,java.sql.Date.valueOf(item.validaDesde));ps.setDataNula(8,item.validaAte);ps.setLong(9,item.perfilId)
            }; item.copy(id=id)
        }; return conexao?.let(executar) ?: ConnectionFactory.transaction(executar)
    }
    fun listarPorPerfil(perfilId: Long): List<AdaMetas> = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("SELECT * FROM ADA_METAS WHERE ADA_PERFIL_id=? AND (validaAte IS NULL OR validaAte>=TRUNC(SYSDATE)) ORDER BY validaDesde DESC").use { ps ->
            ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next()) add(AdaMetas(rs.getLong("id"),rs.getString("tipo"),rs.getDouble("valorAlvo"),rs.getString("unidade"),rs.getString("origem"),rs.getString("versaoAlgoritmos"),rs.getString("calculos"),rs.data("validaDesde"),rs.dataNula("validaAte"),rs.getLong("ADA_PERFIL_id"))) } }
        }
    }
}
