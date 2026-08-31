package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaExames(
    val id: Long = 0,
    val nomeExame: String,
    val dataExame: String,
    val tipoMime: String,
    val nomeArquivo: String,
    val armazenamento: String,
    val checksum: String,
    val enviadoEm: String? = null,
    val perfilId: Long
)
