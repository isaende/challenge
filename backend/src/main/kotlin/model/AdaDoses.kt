package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaDoses(
    val id: Long = 0,
    val previsao: String,
    val tomadaEm: String? = null,
    val status: String = "PENDENTE",
    val agendaId: Long
)
