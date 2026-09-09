package dao

import factory.ConnectionFactory
import model.AdaTreinos

class AdaTreinosDAO {
    fun marcarDia(perfilId: Long, data: String, concluido: Boolean) = ConnectionFactory.transaction { con ->
        con.prepareStatement("SELECT id FROM ADA_PERFIL WHERE id=? FOR UPDATE").use { ps ->
            ps.setLong(1, perfilId)
            ps.executeQuery().use { require(it.next()) { "Perfil nao encontrado" } }
        }
        val quantidade = con.prepareStatement("UPDATE ADA_TREINOS SET concluidoEm=? WHERE ADA_PERFIL_id=? AND dt_treino=?").use { ps ->
            ps.setTimestampNulo(1, if (concluido) java.time.LocalDateTime.now().toString() else null)
            ps.setLong(2, perfilId)
            ps.setDate(3, java.sql.Date.valueOf(data))
            ps.executeUpdate()
        }
        if (quantidade == 0 && concluido) {
            ConnectionFactory.inserirRetornandoId(con, "INSERT INTO ADA_TREINOS (titulo,nivel,duracaoMin,dt_treino,concluidoEm,ADA_PERFIL_id) VALUES (?,?,?,?,SYSTIMESTAMP,?)") { ps ->
                ps.setString(1, "Treino do dia")
                ps.setString(2, "Intermediario")
                ps.setInt(3, 0)
                ps.setDate(4, java.sql.Date.valueOf(data))
                ps.setLong(5, perfilId)
            }
        }
    }

    fun inserir(item:AdaTreinos):AdaTreinos=ConnectionFactory.transaction { con -> val id=ConnectionFactory.inserirRetornandoId(con,"INSERT INTO ADA_TREINOS (titulo,nivel,duracaoMin,dt_treino,concluidoEm,ADA_PERFIL_id) VALUES (?,?,?,?,?,?)") { ps -> ps.setString(1,item.titulo);ps.setString(2,item.nivel);ps.setInt(3,item.duracaoMinutos);ps.setDate(4,java.sql.Date.valueOf(item.dataTreino));ps.setTimestampNulo(5,item.concluidoEm);ps.setLong(6,item.perfilId) };item.copy(id=id) }
    fun listarPorPerfil(perfilId:Long): List<AdaTreinos> = ConnectionFactory.getConnection().use { con -> con.prepareStatement("SELECT * FROM ADA_TREINOS WHERE ADA_PERFIL_id=? ORDER BY dt_treino DESC").use { ps -> ps.setLong(1,perfilId);ps.executeQuery().use { rs -> buildList { while(rs.next())add(AdaTreinos(rs.getLong("id"),rs.getString("titulo"),rs.getString("nivel"),rs.getInt("duracaoMin"),rs.data("dt_treino"),rs.instante("concluidoEm"),rs.getLong("ADA_PERFIL_id"))) } } } }
}
