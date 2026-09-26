package home_page

import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class PersistenciaProgressoTest {
    @Test fun aguaComOrigemOmitidaPeloServidorContinuaLegivel() {
        val agua = Json.decodeFromString<AguaRespostaApi>("""{"registros":[{"id":1,"quantidadeMl":250,"perfilId":2}],"totalMl":250}""")
        assertEquals(250, agua.totalMl)
        assertEquals("APP", agua.registros.single().origem)
    }

    @Test fun registrosAutomaticosNaoDuplicamDiasManuaisENemMantemTreinoDesmarcado() {
        val dia = LocalDate.of(2026, 9, 12)
        val manuais = DesafioRecompensa.entries.associateWith { ProgressoRecompensa() }.toMutableMap()
        manuais[DesafioRecompensa.HIDRATACAO] = ProgressoRecompensa(setOf(dia))
        val sincronizado = manuais.comRegistrosAutomaticos(mapOf(
            "HIDRATACAO" to listOf(dia.toString(), dia.toString()),
            "EXERCICIOS" to listOf(dia.toString())
        ))
        assertEquals(1, sincronizado.getValue(DesafioRecompensa.HIDRATACAO).total(DesafioRecompensa.HIDRATACAO, dia))
        assertEquals(setOf(dia), sincronizado.getValue(DesafioRecompensa.EXERCICIOS).dias)
        assertTrue(manuais.comRegistrosAutomaticos(emptyMap()).getValue(DesafioRecompensa.EXERCICIOS).dias.isEmpty())
    }
}
