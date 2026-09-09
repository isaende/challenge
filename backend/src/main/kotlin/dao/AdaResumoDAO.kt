package dao

import factory.ConnectionFactory
import model.AdaResumo

class AdaResumoDAO {
    fun salvar(item: AdaResumo): AdaResumo = ConnectionFactory.transaction { con ->
        val existente=con.prepareStatement("SELECT id FROM ADA_RESUMO WHERE ADA_PERFIL_id=? AND dt_resumo=?").use { ps -> ps.setLong(1,item.perfilId);ps.setDate(2,java.sql.Date.valueOf(item.dataResumo));ps.executeQuery().use { if(it.next()) it.getLong(1) else 0 } }
        val id = if (existente > 0) {
            con.prepareStatement("UPDATE ADA_RESUMO SET passos=?,sonoMin=?,batimentosUltimos=?,origem=?,sincronizadoEm=SYSTIMESTAMP WHERE id=?").use { ps -> ps.setLong(1,item.passos);ps.setLong(2,item.sonoMinutos);if(item.batimentosUltimos==null)ps.setNull(3,java.sql.Types.INTEGER)else ps.setInt(3,item.batimentosUltimos);ps.setString(4,item.origem);ps.setLong(5,existente);ps.executeUpdate() }
            existente
        } else ConnectionFactory.inserirRetornandoId(
            con,
            "INSERT INTO ADA_RESUMO (dt_resumo,passos,sonoMin,batimentosUltimos,origem,sincronizadoEm,ADA_PERFIL_id) VALUES (?,?,?,?,?,SYSTIMESTAMP,?)"
        ) { ps -> ps.setDate(1,java.sql.Date.valueOf(item.dataResumo));ps.setLong(2,item.passos);ps.setLong(3,item.sonoMinutos);if(item.batimentosUltimos==null)ps.setNull(4,java.sql.Types.INTEGER)else ps.setInt(4,item.batimentosUltimos);ps.setString(5,item.origem);ps.setLong(6,item.perfilId) }
        item.copy(id=id)
    }
    fun listarPorPerfil(perfilId:Long):List<AdaResumo> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_RESUMO WHERE ADA_PERFIL_id=? ORDER BY dt_resumo DESC").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next()) add(AdaResumo(rs.getLong("id"),rs.data("dt_resumo"),rs.getLong("passos"),rs.getLong("sonoMin"),rs.getInt("batimentosUltimos").let{if(rs.wasNull())null else it},rs.getString("origem"),rs.instante("sincronizadoEm"),rs.getLong("ADA_PERFIL_id"))) } } } }
}
