package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaChat(
    val id: Long = 0,
    val inicio: String? = null,
    val fim: String? = null,
    val expiracaoPos: Boolean = false,
    val perfilId: Long
)
