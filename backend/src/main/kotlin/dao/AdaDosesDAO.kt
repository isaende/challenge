package dao

import factory.ConnectionFactory
import model.AdaDoses

class AdaDosesDAO {
    fun inserir(item:AdaDoses):AdaDoses=ConnectionFactory.transaction { con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_DOSES (previsao,tomadaEm,status,ADA_AGENDA_id) VALUES (?,?,?,?)") { ps -> ps.setTimestamp(1,java.sql.Timestamp.valueOf(item.previsao.replace('T',' ')));ps.setTimestampNulo(2,item.tomadaEm);ps.setString(3,item.status);ps.setLong(4,item.agendaId) };item.copy(id=id) }
    fun marcarTomada(id:Long)=ConnectionFactory.getConnection().use { con -> con.prepareStatement("UPDATE ADA_DOSES SET tomadaEm=SYSTIMESTAMP,status='TOMADA' WHERE id=?").use { it.setLong(1,id);it.executeUpdate() } }
    fun listarPorAgenda(agendaId:Long): List<AdaDoses> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_DOSES WHERE ADA_AGENDA_id=? ORDER BY previsao DESC").use { ps -> ps.setLong(1,agendaId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaDoses(rs.getLong("id"),rs.instante("previsao")!!,rs.instante("tomadaEm"),rs.getString("status"),rs.getLong("ADA_AGENDA_id"))) } } } }
}
