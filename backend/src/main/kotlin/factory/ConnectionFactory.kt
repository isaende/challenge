package factory

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.Properties

object ConnectionFactory {
    private val propriedades: Properties by lazy {
        Properties().apply {
            ConnectionFactory::class.java.classLoader
                .getResourceAsStream("database.properties")
                ?.use(::load)
        }
    }

    private fun configuracao(variavel: String, propriedade: String): String =
        System.getenv(variavel)?.takeIf(String::isNotBlank)
            ?: propriedades.getProperty(propriedade)?.takeIf(String::isNotBlank)
            ?: error("Configure $variavel ou $propriedade em database.properties")

    init {
        DriverManager.setLoginTimeout(
            System.getenv("DB_CONNECT_TIMEOUT_SECONDS")?.toIntOrNull()
                ?: propriedades.getProperty("database.connectTimeoutSeconds")?.toIntOrNull()
                ?: 10
        )
    }

    fun getConnection(): Connection = DriverManager.getConnection(
        configuracao("DB_URL", "database.url"),
        configuracao("DB_USER", "database.user"),
        configuracao("DB_PASSWORD", "database.password")
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
