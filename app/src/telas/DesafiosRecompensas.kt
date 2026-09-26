package home_page

import java.time.LocalDate

internal enum class DesafioRecompensa(
    val titulo: String,
    val descricao: String,
    val confirmacao: String,
    val metaDias: Int,
    val pontos: Int
) {
    CAMINHADA("Desafio 10K", "10 mil passos por dia, durante 7 dias", "Completei 10.000 passos hoje", 7, 100),
    HIDRATACAO("Hidratação em dia", "Beba 2 L de água por 5 dias", "Bebi 2 L de água hoje", 5, 80),
    EXERCICIOS("Meta de exercícios", "Pratique exercícios por 5 dias", "Pratiquei exercícios hoje", 5, 60),
    SONO("Noite de sono", "Durma 7 h por 5 noites", "Dormi pelo menos 7 h na última noite", 5, 80)
}

internal data class ProgressoRecompensa(val dias: Set<LocalDate> = emptySet()) {
    fun total(desafio: DesafioRecompensa, hoje: LocalDate): Int =
        dias.count { !it.isAfter(hoje) }.coerceAtMost(desafio.metaDias)

    fun concluido(desafio: DesafioRecompensa, hoje: LocalDate): Boolean = total(desafio, hoje) == desafio.metaDias

    fun pontos(desafio: DesafioRecompensa, hoje: LocalDate): Int = if (concluido(desafio, hoje)) desafio.pontos else 0

    fun registrarHoje(desafio: DesafioRecompensa, hoje: LocalDate): ProgressoRecompensa =
        if (concluido(desafio, hoje)) this else copy(dias = dias + hoje)
}

internal fun Map<DesafioRecompensa, ProgressoRecompensa>.comRegistrosAutomaticos(
    registros: Map<String, List<String>>
): Map<DesafioRecompensa, ProgressoRecompensa> = DesafioRecompensa.entries.associateWith { desafio ->
    ProgressoRecompensa(getValue(desafio).dias + registros[desafio.name].orEmpty().map(LocalDate::parse))
}
