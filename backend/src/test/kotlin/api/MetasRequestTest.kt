package api

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MetasRequestTest {
    @Test fun `metas invalidas sao recusadas antes de gravar`() {
        listOf(
            MetasRequest(0, 8000, 4), MetasRequest(-250, 8000, 4),
            MetasRequest(2500, 0, 4), MetasRequest(2500, Long.MAX_VALUE, 4),
            MetasRequest(2500, 8000, 0), MetasRequest(2500, 8000, 8)
        ).forEach { dados -> assertFailsWith<IllegalArgumentException> { dados.validar() } }
    }

    @Test fun `conclusao permite historico mas impede datas futuras ou invalidas`() {
        val hoje = LocalDate.of(2026, 9, 9)
        assertEquals(hoje, TreinoDiaRequest("2026-09-09", true).validar(hoje))
        assertEquals(hoje.minusWeeks(1), TreinoDiaRequest("2026-09-02", false).validar(hoje))
        listOf("2026-09-10", "2026-02-30", "09/09/2026", "").forEach { data ->
            assertFailsWith<IllegalArgumentException> { TreinoDiaRequest(data, true).validar(hoje) }
        }
    }
}
