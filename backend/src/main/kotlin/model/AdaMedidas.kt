package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaMedidas(
    val id: Long = 0,
    val pesoKg: Double,
    val alturaCm: Double,
    val imcCalculado: Double,
    val origem: String = "CADASTRO",
    val medidoEm: String? = null,
    val perfilId: Long
)
