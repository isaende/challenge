// DTO recebido do backend com a resposta textual e seu tipo.
package com.ada.chatmedico.dados.dto

import kotlinx.serialization.Serializable

@Serializable
data class RespostaChatMedicoDto(
    val resposta: String,
    val tipo: String
)
