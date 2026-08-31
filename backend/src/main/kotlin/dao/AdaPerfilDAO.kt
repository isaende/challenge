package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaPerfil

class AdaPerfilDAO {
    fun inserir(perfil: AdaPerfil, conexao: Connection? = null): AdaPerfil {
        val executar: (Connection) -> AdaPerfil = { con ->
            val id = if (perfil.id > 0) perfil.id else ConnectionFactory.proximoId(con, "ADA_PERFIL")
            con.prepareStatement("""INSERT INTO ADA_PERFIL
                (id, nm_completo, dt_nascimento, genero, objetivoPrincipal, criadoEm, ADA_USUARIOS_cod_usuario)
                VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP, ?)""").use { ps ->
                ps.setLong(1, id); ps.setString(2, perfil.nomeCompleto); ps.setDate(3, java.sql.Date.valueOf(perfil.dataNascimento))
                ps.setString(4, perfil.genero); ps.setString(5, perfil.objetivoPrincipal); ps.setLong(6, perfil.usuarioId); ps.executeUpdate()
            }; perfil.copy(id = id)
        }
        return conexao?.let(executar) ?: ConnectionFactory.transaction(executar)
    }

    fun buscarPorUsuario(usuarioId: Long, conexao: Connection? = null): AdaPerfil? {
        val executar: (Connection) -> AdaPerfil? = { con ->
            con.prepareStatement("SELECT * FROM ADA_PERFIL WHERE ADA_USUARIOS_cod_usuario = ?").use { ps ->
                ps.setLong(1, usuarioId); ps.executeQuery().use { rs -> if (rs.next()) mapear(rs) else null }
            }
        }; return conexao?.let(executar) ?: ConnectionFactory.getConnection().use(executar)
    }

    fun buscarPorId(id: Long): AdaPerfil? = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("SELECT * FROM ADA_PERFIL WHERE id = ?").use { ps ->
            ps.setLong(1, id); ps.executeQuery().use { rs -> if (rs.next()) mapear(rs) else null }
        }
    }

    private fun mapear(rs: java.sql.ResultSet) = AdaPerfil(
        rs.getLong("id"), rs.getString("nm_completo"), rs.data("dt_nascimento"),
        rs.getString("genero"), rs.getString("objetivoPrincipal"), rs.instante("criadoEm"),
        rs.getLong("ADA_USUARIOS_cod_usuario")
    )
}
