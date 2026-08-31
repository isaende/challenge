package dao

import factory.ConnectionFactory
import model.AdaTreinos

class AdaTreinosDAO {
    fun inserir(item:AdaTreinos):AdaTreinos=ConnectionFactory.transaction { con -> val id=if(item.id>0)item.id else ConnectionFactory.proximoId(con,"ADA_TREINOS");con.prepareStatement("INSERT INTO ADA_TREINOS (id,titulo,nivel,duracaoMin,dt_treino,concluidoEm,ADA_PERFIL_id) VALUES (?,?,?,?,?,?,?)").use { ps -> ps.setLong(1,id);ps.setString(2,item.titulo);ps.setString(3,item.nivel);ps.setInt(4,item.duracaoMinutos);ps.setDate(5,java.sql.Date.valueOf(item.dataTreino));ps.setTimestampNulo(6,item.concluidoEm);ps.setLong(7,item.perfilId);ps.executeUpdate() };item.copy(id=id) }
    fun listarPorPerfil(perfilId:Long): List<AdaTreinos> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_TREINOS WHERE ADA_PERFIL_id=? ORDER BY dt_treino DESC").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaTreinos(rs.getLong("id"),rs.getString("titulo"),rs.getString("nivel"),rs.getInt("duracaoMin"),rs.data("dt_treino"),rs.instante("concluidoEm"),rs.getLong("ADA_PERFIL_id"))) } } } }
}
