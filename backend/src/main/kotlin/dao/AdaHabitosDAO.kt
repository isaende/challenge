package dao

import factory.ConnectionFactory
import java.sql.Date
import java.time.LocalDate
import model.AdaHabitoDia

class AdaHabitosDAO {
    fun listarSemana(perfilId: Long, inicio: LocalDate): List<AdaHabitoDia> =
        ConnectionFactory.getConnection().use { con ->
            val fim = inicio.plusDays(7)
            val agua = con.prepareStatement("""
                SELECT TRUNC(consumidoEm) dia, SUM(qtdMl) total
                FROM ADA_AGUA
                WHERE ADA_PERFIL_id=? AND consumidoEm>=? AND consumidoEm<?
                GROUP BY TRUNC(consumidoEm)
            """.trimIndent()).use { ps ->
                ps.setLong(1, perfilId)
                ps.setDate(2, Date.valueOf(inicio))
                ps.setDate(3, Date.valueOf(fim))
                ps.executeQuery().use { rs -> buildMap {
                    while (rs.next()) put(rs.data("dia"), rs.getLong("total"))
                } }
            }
            val passos = con.prepareStatement("""
                SELECT dt_resumo, passos FROM ADA_RESUMO
                WHERE ADA_PERFIL_id=? AND dt_resumo>=? AND dt_resumo<?
            """.trimIndent()).use { ps ->
                ps.setLong(1, perfilId)
                ps.setDate(2, Date.valueOf(inicio))
                ps.setDate(3, Date.valueOf(fim))
                ps.executeQuery().use { rs -> buildMap {
                    while (rs.next()) put(rs.data("dt_resumo"), rs.getLong("passos").let { if (rs.wasNull()) null else it })
                } }
            }
            (0L..6L).map { indice ->
                val data = inicio.plusDays(indice).toString()
                AdaHabitoDia(data, agua[data] ?: 0, passos[data])
            }
        }
}
