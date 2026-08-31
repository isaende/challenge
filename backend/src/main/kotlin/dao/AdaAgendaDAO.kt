package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaAgenda

class AdaAgendaDAO {
    fun inserir(item:AdaAgenda,conexao:Connection?=null):AdaAgenda { val executar:(Connection)->AdaAgenda={ con -> val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_AGENDA");con.prepareStatement("INSERT INTO ADA_AGENDA (id,horarioLocal,recorrencia,lembrete,ADA_MEDICAMENTOS_id) VALUES (?,?,?,?,?)").use { ps -> ps.setLong(1,id);ps.setString(2,item.horarioLocal);ps.setString(3,item.recorrencia);ps.setInt(4,if(item.lembrete)1 else 0);ps.setLong(5,item.medicamentoId);ps.executeUpdate() };item.copy(id=id) };return conexao?.let(executar)?:ConnectionFactory.transaction(executar) }
    fun buscarPorMedicamento(medicamentoId:Long): List<AdaAgenda> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_AGENDA WHERE ADA_MEDICAMENTOS_id=?").use { ps -> ps.setLong(1,medicamentoId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaAgenda(rs.getLong("id"),rs.getString("horarioLocal"),rs.getString("recorrencia"),rs.getInt("lembrete")==1,rs.getLong("ADA_MEDICAMENTOS_id"))) } } } }
}
