// Guarda o estado da tela de chat, textos fixos e mensagens iniciais do MVP.
package com.ada.chatmedico.ui

import com.ada.chatmedico.dominio.MensagemChat
import com.ada.chatmedico.dominio.PapelMensagem
import com.ada.chatmedico.dominio.TipoRespostaMedica

const val LIMITE_CARACTERES_MENSAGEM = 2000

internal const val TEXTO_AVISO_EDUCATIVO =
    "Este chat fornece informações educativas e não substitui avaliação de um profissional de saúde."

internal const val TEXTO_MENSAGEM_INICIAL =
    "Olá! Posso ajudar com explicações gerais sobre saúde e exames. " +
        "Não faço diagnóstico, não prescrevo medicamentos e não substituo uma consulta médica."

internal const val TEXTO_ERRO_API =
    "Não foi possível responder agora. Tente novamente."

internal const val TEXTO_ERRO_TAMANHO =
    "Mensagem muito longa. Resuma sua dúvida."

internal const val TEXTO_CARREGANDO =
    "Ada está preparando uma resposta educativa..."

data class EstadoChatMedico(
    val mensagens: List<MensagemChat> = mensagensIniciaisChatMedico(),
    val textoDigitado: String = "",
    val enviando: Boolean = false,
    val erroEntrada: String? = null
)

private fun mensagensIniciaisChatMedico(): List<MensagemChat> {
    return listOf(
        MensagemChat(
            id = "mensagem_inicial_ada",
            texto = TEXTO_MENSAGEM_INICIAL,
            papel = PapelMensagem.ASSISTENTE,
            tipo = TipoRespostaMedica.EDUCATIVO
        )
    )
}
