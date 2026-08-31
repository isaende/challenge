package dao

import factory.ConnectionFactory
import model.AdaResumo

class AdaResumoDAO {
    fun salvar(item: AdaResumo): AdaResumo = ConnectionFactory.transaction { con ->
        val existente=con.prepareStatement("SELECT id FROM ADA_RESUMO WHERE ADA_PERFIL_id=? AND dt_resumo=?").use { ps -> ps.setLong(1,item.perfilId);ps.setDate(2,java.sql.Date.valueOf(item.dataResumo));ps.executeQuery().use { if(it.next()) it.getLong(1) else 0 } }
        val id=if(existente>0) existente else ConnectionFactory.proximoId(con,"ADA_RESUMO")
        if(existente>0) con.prepareStatement("UPDATE ADA_RESUMO SET passos=?,sonoMin=?,batimentosUltimos=?,origem=?,sincronizadoEm=SYSTIMESTAMP WHERE id=?").use { ps -> ps.setLong(1,item.passos);ps.setLong(2,item.sonoMinutos);if(item.batimentosUltimos==null)ps.setNull(3,java.sql.Types.INTEGER)else ps.setInt(3,item.batimentosUltimos);ps.setString(4,item.origem);ps.setLong(5,id);ps.executeUpdate() }
        else con.prepareStatement("INSERT INTO ADA_RESUMO (id,dt_resumo,passos,sonoMin,batimentosUltimos,origem,sincronizadoEm,ADA_PERFIL_id) VALUES (?,?,?,?,?,?,SYSTIMESTAMP,?)").use { ps -> ps.setLong(1,id);ps.setDate(2,java.sql.Date.valueOf(item.dataResumo));ps.setLong(3,item.passos);ps.setLong(4,item.sonoMinutos);if(item.batimentosUltimos==null)ps.setNull(5,java.sql.Types.INTEGER)else ps.setInt(5,item.batimentosUltimos);ps.setString(6,item.origem);ps.setLong(7,item.perfilId);ps.executeUpdate() }
        item.copy(id=id)
    }
    fun listarPorPerfil(perfilId:Long):List<AdaResumo> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_RESUMO WHERE ADA_PERFIL_id=? ORDER BY dt_resumo DESC").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next()) add(AdaResumo(rs.getLong("id"),rs.data("dt_resumo"),rs.getLong("passos"),rs.getLong("sonoMin"),rs.getInt("batimentosUltimos").let{if(rs.wasNull())null else it},rs.getString("origem"),rs.instante("sincronizadoEm"),rs.getLong("ADA_PERFIL_id"))) } } } }
}
