package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaMedicamentos(
    val id: Long = 0,
    val nomeMedicamento: String,
    val dosagemTexto: String,
    val inicio: String,
    val fim: String? = null,
    val ativo: Boolean = true,
    val perfilId: Long
)
