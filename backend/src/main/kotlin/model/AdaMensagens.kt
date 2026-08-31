package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaMensagens(
    val id: Long = 0,
    val tipoResposta: Int,
    val conteudo: String,
    val criadoEm: String? = null,
    val chatId: Long
)
