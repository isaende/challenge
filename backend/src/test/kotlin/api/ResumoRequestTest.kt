package api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import model.AdaResumo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResumoRequestTest {
    @Test fun `resumos anteriores continuam aceitos sem pressao`() {
        val resumo = Json.decodeFromString<ResumoRequest>(
            """{"dataResumo":"2026-09-09","passos":1000,"sonoMinutos":420,"batimentosUltimos":72}"""
        )
        assertEquals(1000L, resumo.passos)
        assertNull(resumo.pressaoSistolica)
        assertNull(resumo.pressaoDiastolica)
    }

    @Test fun `ausencia de dados nao vira zero e pressao mantem as duas medidas`() {
        val resumo = Json.decodeFromString<ResumoRequest>(
            """{"dataResumo":"2026-09-09","passos":null,"sonoMinutos":null,"pressaoSistolica":120.5,"pressaoDiastolica":80.0}"""
        )
        assertNull(resumo.passos)
        assertNull(resumo.sonoMinutos)
        assertNull(resumo.batimentosUltimos)
        assertEquals(120.5, resumo.pressaoSistolica)
        assertEquals(80.0, resumo.pressaoDiastolica)
    }

    @Test fun `calorias acompanham atualizacao dos passos inclusive zero e ausencia`() {
        val resumo = AdaResumo(dataResumo = "2026-09-09", passos = 1000, sonoMinutos = null, perfilId = 1)
        assertEquals(400.0, resumo.caloriasGastas)
        assertEquals(0.4, resumo.copy(passos = 1).caloriasGastas)
        assertEquals(0.0, resumo.copy(passos = 0).caloriasGastas)
        assertNull(resumo.copy(passos = null).caloriasGastas)
        val resposta = Json.parseToJsonElement(Json.encodeToString(resumo)).jsonObject
        assertEquals("400.0", resposta.getValue("caloriasGastas").jsonPrimitive.content)
    }
}
