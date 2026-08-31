package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaAgua(
    val id: Long = 0,
    val quantidadeMl: Int,
    val origem: String = "APP",
    val consumidoEm: String? = null,
    val perfilId: Long
)
