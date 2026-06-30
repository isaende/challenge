// Representa uma mensagem do chat, indicando se ela veio do usuário ou da assistente.
package com.ada.chatmedico.dominio

import java.util.UUID

data class MensagemChat(
    val id: String = UUID.randomUUID().toString(),
    val texto: String,
    val papel: PapelMensagem,
    val tipo: TipoRespostaMedica = TipoRespostaMedica.EDUCATIVO,
    val carregando: Boolean = false
)
