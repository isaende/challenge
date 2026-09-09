package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaMedidas

class AdaMedidasDAO {
    fun inserir(item: AdaMedidas, conexao: Connection? = null): AdaMedidas {
        val executar: (Connection) -> AdaMedidas = { con ->
            val id = ConnectionFactory.inserirRetornandoId(con, """INSERT INTO ADA_MEDIDAS
                (pesoKg, alturaCm, imcCalculado, origem, medidoEm, ADA_PERFIL_id)
                VALUES (?, ?, ?, ?, SYSTIMESTAMP, ?)""") { ps ->
                ps.setDouble(1,item.pesoKg); ps.setDouble(2,item.alturaCm); ps.setDouble(3,item.imcCalculado)
                ps.setString(4,item.origem); ps.setLong(5,item.perfilId)
            }; item.copy(id=id)
        }; return conexao?.let(executar) ?: ConnectionFactory.transaction(executar)
    }
    fun listarPorPerfil(perfilId: Long): List<AdaMedidas> = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("SELECT * FROM ADA_MEDIDAS WHERE ADA_PERFIL_id=? ORDER BY medidoEm DESC").use { ps ->
            ps.setLong(1,perfilId); ps.executeQuery().use { rs -> buildList { while(rs.next()) add(AdaMedidas(rs.getLong("id"),rs.getDouble("pesoKg"),rs.getDouble("alturaCm"),rs.getDouble("imcCalculado"),rs.getString("origem"),rs.instante("medidoEm"),rs.getLong("ADA_PERFIL_id"))) } }
        }
    }
}
