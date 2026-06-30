// DTO enviado no corpo da requisição para o endpoint de chat médico.
package com.ada.chatmedico.dados.dto

import kotlinx.serialization.Serializable

@Serializable
data class SolicitacaoChatMedicoDto(
    val mensagem: String
)
