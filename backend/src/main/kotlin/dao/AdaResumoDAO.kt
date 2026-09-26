package dao

import factory.ConnectionFactory
import model.AdaResumo
import java.sql.PreparedStatement
import java.sql.Types

class AdaResumoDAO {
    fun salvar(item: AdaResumo): AdaResumo = ConnectionFactory.transaction { con ->
        val existente = con.prepareStatement("SELECT id FROM ADA_RESUMO WHERE ADA_PERFIL_id=? AND dt_resumo=?").use { ps ->
            ps.setLong(1, item.perfilId)
            ps.setDate(2, java.sql.Date.valueOf(item.dataResumo))
            ps.executeQuery().use { if (it.next()) it.getLong(1) else 0 }
        }
        val id = if (existente > 0) {
            con.prepareStatement("UPDATE ADA_RESUMO SET passos=COALESCE(?,passos),sonoMin=COALESCE(?,sonoMin),batimentosUltimos=COALESCE(?,batimentosUltimos),origem=?,pressaoSistolica=COALESCE(?,pressaoSistolica),pressaoDiastolica=COALESCE(?,pressaoDiastolica),sincronizadoEm=SYSTIMESTAMP WHERE id=?").use { ps ->
                ps.preencherDados(item)
                ps.setLong(7, existente)
                ps.executeUpdate()
            }
            existente
        } else ConnectionFactory.inserirRetornandoId(
            con,
            "INSERT INTO ADA_RESUMO (passos,sonoMin,batimentosUltimos,origem,pressaoSistolica,pressaoDiastolica,dt_resumo,sincronizadoEm,ADA_PERFIL_id) VALUES (?,?,?,?,?,?,?,SYSTIMESTAMP,?)"
        ) { ps ->
            ps.preencherDados(item)
            ps.setDate(7, java.sql.Date.valueOf(item.dataResumo))
            ps.setLong(8, item.perfilId)
        }
        item.copy(id = id)
    }

    private fun PreparedStatement.preencherDados(item: AdaResumo) {
        setObject(1, item.passos, Types.NUMERIC)
        setObject(2, item.sonoMinutos, Types.NUMERIC)
        setObject(3, item.batimentosUltimos, Types.INTEGER)
        setString(4, item.origem)
        setObject(5, item.pressaoSistolica, Types.NUMERIC)
        setObject(6, item.pressaoDiastolica, Types.NUMERIC)
    }

    fun listarPorPerfil(perfilId: Long): List<AdaResumo> = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("SELECT * FROM ADA_RESUMO WHERE ADA_PERFIL_id=? ORDER BY dt_resumo DESC").use { ps ->
            ps.setLong(1, perfilId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(AdaResumo(
                        id = rs.getLong("id"),
                        dataResumo = rs.data("dt_resumo"),
                        passos = rs.getLong("passos").let { if (rs.wasNull()) null else it },
                        sonoMinutos = rs.getLong("sonoMin").let { if (rs.wasNull()) null else it },
                        batimentosUltimos = rs.getInt("batimentosUltimos").let { if (rs.wasNull()) null else it },
                        origem = rs.getString("origem"),
                        sincronizadoEm = rs.instante("sincronizadoEm"),
                        perfilId = rs.getLong("ADA_PERFIL_id"),
                        pressaoSistolica = rs.getDouble("pressaoSistolica").let { if (rs.wasNull()) null else it },
                        pressaoDiastolica = rs.getDouble("pressaoDiastolica").let { if (rs.wasNull()) null else it }
                    ))
                }
            }
        }
    }
}
