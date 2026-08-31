package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaResumo(
    val id: Long = 0,
    val dataResumo: String,
    val passos: Long,
    val sonoMinutos: Long,
    val batimentosUltimos: Int? = null,
    val origem: String = "HEALTH_CONNECT",
    val sincronizadoEm: String? = null,
    val perfilId: Long
)
