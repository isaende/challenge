package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaAgenda(
    val id: Long = 0,
    val horarioLocal: String,
    val recorrencia: String = "DIARIA",
    val lembrete: Boolean = true,
    val medicamentoId: Long
)
