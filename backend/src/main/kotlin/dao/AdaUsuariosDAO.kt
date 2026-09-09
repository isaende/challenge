package dao

import factory.ConnectionFactory
import java.sql.Connection
import model.AdaUsuarios

class AdaUsuariosDAO {
    fun inserir(usuario: AdaUsuarios, conexao: Connection? = null): AdaUsuarios {
        val executar: (Connection) -> AdaUsuarios = { con ->
            val id = ConnectionFactory.inserirRetornandoId(con, """
                INSERT INTO ADA_USUARIOS
                (email, senhaHash, papel, status, emailVerificadoEm, ultimoLoginEm, criadoEm)
                VALUES (?, ?, ?, ?, NULL, NULL, SYSTIMESTAMP)
            """.trimIndent(), "COD_USUARIO") { ps ->
                ps.setString(1, usuario.email.lowercase()); ps.setString(2, usuario.senhaHash)
                ps.setString(3, usuario.papel); ps.setString(4, usuario.status)
            }
            usuario.copy(codUsuario = id)
        }
        return conexao?.let(executar) ?: ConnectionFactory.transaction(executar)
    }

    fun buscarPorEmail(email: String, conexao: Connection? = null): AdaUsuarios? {
        val executar: (Connection) -> AdaUsuarios? = { con ->
            con.prepareStatement("SELECT * FROM ADA_USUARIOS WHERE LOWER(email) = LOWER(?)").use { ps ->
                ps.setString(1, email.trim()); ps.executeQuery().use { rs -> if (rs.next()) mapear(rs) else null }
            }
        }
        return conexao?.let(executar) ?: ConnectionFactory.getConnection().use(executar)
    }

    fun buscarPorId(id: Long): AdaUsuarios? = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("SELECT * FROM ADA_USUARIOS WHERE cod_usuario = ?").use { ps ->
            ps.setLong(1, id); ps.executeQuery().use { rs -> if (rs.next()) mapear(rs) else null }
        }
    }

    fun registrarLogin(id: Long) = ConnectionFactory.getConnection().use { con ->
        con.prepareStatement("UPDATE ADA_USUARIOS SET ultimoLoginEm = SYSTIMESTAMP WHERE cod_usuario = ?").use {
            it.setLong(1, id); it.executeUpdate()
        }
    }

    private fun mapear(rs: java.sql.ResultSet) = AdaUsuarios(
        rs.getLong("cod_usuario"), rs.getString("email"), rs.getString("senhaHash"),
        rs.getString("papel"), rs.getString("status"), rs.instante("emailVerificadoEm"),
        rs.instante("ultimoLoginEm"), rs.instante("criadoEm")
    )
}
