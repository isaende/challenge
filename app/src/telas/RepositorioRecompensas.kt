package home_page

import android.content.Context
import java.time.LocalDate

internal class RepositorioRecompensas(contexto: Context, perfilId: Long) {
    private val preferencias = contexto.getSharedPreferences("recompensas_$perfilId", Context.MODE_PRIVATE)

    fun carregar(): Map<DesafioRecompensa, ProgressoRecompensa> = DesafioRecompensa.entries.associateWith { desafio ->
        ProgressoRecompensa(preferencias.getStringSet(desafio.name, emptySet()).orEmpty().mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.toSet())
    }

    fun salvar(desafio: DesafioRecompensa, progresso: ProgressoRecompensa) {
        preferencias.edit().putStringSet(desafio.name, progresso.dias.map { it.toString() }.toSet()).apply()
    }
}
