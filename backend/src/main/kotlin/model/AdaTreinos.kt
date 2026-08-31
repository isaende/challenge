package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaTreinos(
    val id: Long = 0,
    val titulo: String,
    val nivel: String,
    val duracaoMinutos: Int,
    val dataTreino: String,
    val concluidoEm: String? = null,
    val perfilId: Long
)
