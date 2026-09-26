package home_page

import org.junit.Assert.*
import org.junit.Test

class DadosSaudeTest {
    @Test fun sonoSalvoApareceMesmoSemLeituraLocal() {
        val salvo = DadosSaude(sonoMinutos = 440)
        assertEquals(salvo, DadosSaude().complementarCom(salvo))
    }

    @Test fun novasMedicoesPrevalecemSemApagarIndicadoresAusentes() {
        val salvo = DadosSaude(passos = 1000, sonoMinutos = 440, batimentos = 72)
        val combinado = DadosSaude(passos = 0, batimentos = 80).complementarCom(salvo)
        assertEquals(0L, combinado.passos)
        assertEquals(440L, combinado.sonoMinutos)
        assertEquals(80, combinado.batimentos)
        assertEquals(420L, DadosSaude(sonoMinutos = 420).complementarCom(salvo).sonoMinutos)
    }

    @Test fun ausenciaDeMedicoesContinuaNula() {
        val dados = DadosSaude().complementarCom(DadosSaude())
        assertNull(dados.sonoMinutos)
        assertFalse(dados.temDados())
    }
}
