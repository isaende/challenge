// Verifica regras locais de urgência e recusa segura antes de chamar o modelo.
package safety

import java.text.Normalizer

object MedicalSafetyChecker {
    private val sinaisUrgencia = listOf(
        "dor no peito",
        "falta de ar intensa",
        "desmaio",
        "confusao mental",
        "convulsao",
        "fraqueza subita em um lado do corpo",
        "dificuldade subita para falar",
        "sangramento intenso",
        "febre alta persistente",
        "dor intensa",
        "piora rapida",
        "vomitos persistentes",
        "ideacao suicida",
        "vontade de morrer",
        "risco de autoagressao",
        "gravidez com sangramento",
        "gravidez com dor intensa",
        "reducao de movimentos fetais",
        "glicemia extremamente alta",
        "glicemia extremamente baixa",
        "pressao muito alta com sintomas"
    )

    private val pedidosRecusaSegura = listOf(
        "qual dose",
        "dose de",
        "dosagem",
        "prescricao",
        "prescrever",
        "receita de",
        "pode parar medicamento",
        "posso parar",
        "parar de tomar",
        "trocar medicamento",
        "mudar medicamento",
        "alterar tratamento",
        "diagnostico definitivo",
        "qual meu diagnostico",
        "o que eu tenho",
        "tratamento individualizado",
        "tratamento para mim",
        "que remedio devo tomar",
        "qual remedio devo tomar"
    )

    fun contemSinalUrgencia(mensagem: String): Boolean {
        val texto = mensagem.normalizado()
        return sinaisUrgencia.any { sinal -> texto.contains(sinal) }
    }

    fun deveRecusarPorSeguranca(mensagem: String): Boolean {
        val texto = mensagem.normalizado()
        return pedidosRecusaSegura.any { pedido -> texto.contains(pedido) }
    }

    private fun String.normalizado(): String {
        val semAcentos = Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return semAcentos.replace("\\s+".toRegex(), " ").trim()
    }
}
