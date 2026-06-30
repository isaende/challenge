// Modelo de domínio com o texto e o tipo da resposta recebida pelo chat.
package com.ada.chatmedico.dominio

data class RespostaChatMedico(
    val texto: String,
    val tipo: TipoRespostaMedica
)
