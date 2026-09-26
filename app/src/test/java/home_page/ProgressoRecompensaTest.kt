package home_page

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ProgressoRecompensaTest {
    private val hoje = LocalDate.of(2026, 9, 10)
    private val desafio = DesafioRecompensa.HIDRATACAO

    @Test fun novoUsuarioComecaSemPontosOuConclusoes() {
        val progresso = ProgressoRecompensa()
        assertEquals(0, progresso.total(desafio, hoje))
        assertEquals(0, progresso.pontos(desafio, hoje))
        assertFalse(progresso.concluido(desafio, hoje))
    }

    @Test fun registrarDuasVezesNoMesmoDiaContaUmaVez() {
        val progresso = ProgressoRecompensa().registrarHoje(desafio, hoje).registrarHoje(desafio, hoje)
        assertEquals(1, progresso.total(desafio, hoje))
        assertEquals(0, progresso.pontos(desafio, hoje))
    }

    @Test fun recompensaSoELiberadaAoCompletarTodosOsDias() {
        val quase = ProgressoRecompensa((1L..4L).map { hoje.minusDays(it) }.toSet())
        assertEquals(0, quase.pontos(desafio, hoje))
        val completo = quase.registrarHoje(desafio, hoje)
        assertTrue(completo.concluido(desafio, hoje))
        assertEquals(80, completo.pontos(desafio, hoje))
        assertEquals(completo, completo.registrarHoje(desafio, hoje.plusDays(1)))
    }

    @Test fun datasFuturasNaoContamEProgressoNaoUltrapassaMeta() {
        val progresso = ProgressoRecompensa((0L..8L).map { hoje.minusDays(it) }.toSet() + hoje.plusDays(1))
        assertEquals(5, progresso.total(desafio, hoje))
        assertEquals(0, ProgressoRecompensa(setOf(hoje.plusDays(1))).total(desafio, hoje))
    }

    @Test fun quatroDesafiosConcluidosSomam320Pontos() {
        val progresso = ProgressoRecompensa((0L..6L).map { hoje.minusDays(it) }.toSet())
        assertEquals(320, DesafioRecompensa.entries.sumOf { progresso.pontos(it, hoje) })
    }
}
