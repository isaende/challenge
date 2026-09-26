package home_page

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LeituraSaudeTest {
    @Test fun falhaEmOutroIndicadorNaoInterrompeBatimentos() = runBlocking {
        val falhas = mutableListOf<Exception>()
        val pressao = lerIndicadorSaude<Int>({ throw IllegalStateException("indisponivel") }, { falhas.add(it) })
        val batimentos = lerIndicadorSaude({ 72 }, { falhas.add(it) })
        assertNull(pressao)
        assertEquals(72, batimentos)
        assertEquals(1, falhas.size)
    }

    @Test fun ausenciaDeRegistroNaoEhFalha() = runBlocking {
        var falhou = false
        assertNull(lerIndicadorSaude<Int>({ null }, { falhou = true }))
        assertFalse(falhou)
        val leitura = LeituraSaude(DadosSaude(), emptySet(), emptySet(), true)
        assertEquals("Sem batimentos registrados hoje no Health Connect", leitura.statusBatimentos())
    }

    @Test fun cancelamentoNaoEhConvertidoEmAusenciaDeDados() = runBlocking {
        var falhou = false
        try {
            lerIndicadorSaude<Int>({ throw CancellationException("cancelado") }, { falhou = true })
            fail("Deveria propagar o cancelamento")
        } catch (_: CancellationException) {
            assertFalse(falhou)
        }
    }

    @Test fun permissaoNegadaEFalhaSaoDiferentesDeRegistroAusente() {
        val leitura = LeituraSaude(DadosSaude(), setOf("batimentos"), emptySet(), true)
        assertEquals("Nao foi possivel ler os batimentos do Health Connect", leitura.statusBatimentos())
        assertEquals("Autorize a leitura de batimentos no Health Connect",
            leitura.copy(permissaoBatimentos = false).statusBatimentos())
        assertNull(leitura.copy(dados = DadosSaude(batimentos = 72), falhas = emptySet()).statusBatimentos())
    }
    @Test fun pressaoAusenteOuSemPermissaoTemMensagemPropria() {
        val leitura = LeituraSaude(DadosSaude(batimentos = 72), emptySet(), emptySet(), true)
        assertEquals("Sem pressao registrada hoje no Health Connect", leitura.statusSinaisVitais())
        assertEquals("Autorize a leitura de pressao no Health Connect",
            leitura.copy(permissaoPressao = false).statusSinaisVitais())
        assertEquals("Nao foi possivel ler a pressao do Health Connect",
            leitura.copy(falhas = setOf("pressao")).statusSinaisVitais())
        assertNull(leitura.copy(dados = DadosSaude(batimentos = 72,
            pressaoSistolica = 120.0, pressaoDiastolica = 80.0)).statusSinaisVitais())
    }
}
