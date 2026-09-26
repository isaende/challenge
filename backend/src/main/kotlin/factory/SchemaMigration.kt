package factory

/** Ajustes incrementais: preservam registros e podem ser repetidos no inicio. */
object SchemaMigration {
    fun atualizar() = ConnectionFactory.getConnection().use { con ->
        listOf("monitoramento_schema.sql", "persistencia_schema.sql").forEach { recurso ->
            val sql = checkNotNull(javaClass.classLoader.getResourceAsStream(recurso)).bufferedReader().use { it.readText() }
            sql.split(Regex("(?m)^/\\s*$")).filter { it.isNotBlank() }.forEach { bloco ->
                con.createStatement().use { it.execute(bloco.trim()) }
            }
        }
    }
}
