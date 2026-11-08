package factory

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

object ConnectionFactory {
    private fun configuracao(variavel: String): String =
        System.getenv(variavel)?.takeIf(String::isNotBlank)
            ?: error("Configure a variavel de ambiente $variavel no backend")

    init {
        DriverManager.setLoginTimeout(
            System.getenv("DB_CONNECT_TIMEOUT_SECONDS")?.toIntOrNull()
                ?: 10
        )
    }

    fun getConnection(): Connection = DriverManager.getConnection(
        configuracao("DB_URL"),
        configuracao("DB_USER"),
        configuracao("DB_PASSWORD")
    )

    fun testarConexao() {
        getConnection().use { conexao ->
            conexao.prepareStatement("SELECT 1 FROM DUAL").use { comando ->
                check(comando.executeQuery().use { it.next() }) { "Oracle nao respondeu ao teste" }
            }
        }
    }

    fun <T> transaction(bloco: (Connection) -> T): T = getConnection().use { conexao ->
        conexao.autoCommit = false
        try {
            bloco(conexao).also { conexao.commit() }
        } catch (erro: Throwable) {
            conexao.rollback()
            throw erro
        }
    }

    fun inserirRetornandoId(
        conexao: Connection,
        sql: String,
        colunaId: String = "ID",
        configurar: (PreparedStatement) -> Unit
    ): Long = conexao.prepareStatement(sql, arrayOf(colunaId)).use { comando ->
        configurar(comando)
        comando.executeUpdate()
        comando.generatedKeys.use { chaves ->
            check(chaves.next()) { "O Oracle nao retornou o ID gerado para $colunaId" }
            chaves.getLong(1)
        }
    }
}
