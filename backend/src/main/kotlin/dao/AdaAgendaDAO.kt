package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaAgenda

class AdaAgendaDAO {
    fun inserir(item:AdaAgenda,conexao:Connection?=null):AdaAgenda { val executar:(Connection)->AdaAgenda={ con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_AGENDA (horarioLocal,recorrencia,lembrete,ADA_MEDICAMENTOS_id) VALUES (?,?,?,?)") { ps -> ps.setString(1,item.horarioLocal);ps.setString(2,item.recorrencia);ps.setInt(3,if(item.lembrete)1 else 0);ps.setLong(4,item.medicamentoId) };item.copy(id=id) };return conexao?.let(executar)?:ConnectionFactory.transaction(executar) }
    fun buscarPorMedicamento(medicamentoId:Long): List<AdaAgenda> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_AGENDA WHERE ADA_MEDICAMENTOS_id=?").use { ps -> ps.setLong(1,medicamentoId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaAgenda(rs.getLong("id"),rs.getString("horarioLocal"),rs.getString("recorrencia"),rs.getInt("lembrete")==1,rs.getLong("ADA_MEDICAMENTOS_id"))) } } } }
}
