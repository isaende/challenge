package api

import kotlin.test.Test
import kotlin.test.assertEquals

class CadastroServiceTest {
    private val service = CadastroService()

    @Test fun `meta de passos prioriza perda de peso`() {
        assertEquals(10_000, service.calcularMetaPassos(70, "Perda de peso"))
    }

    @Test fun `meta de passos muda aos sessenta anos`() {
        assertEquals(8_000, service.calcularMetaPassos(59, "Saude"))
        assertEquals(6_000, service.calcularMetaPassos(60, "Saude"))
    }

    @Test fun `meta de agua muda acima de sessenta e cinco anos`() {
        assertEquals(2_800, service.calcularMetaAgua(65, 70.0))
        assertEquals(1_750, service.calcularMetaAgua(66, 70.0))
    }
}
