package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaPerfil(
    val id: Long = 0,
    val nomeCompleto: String,
    val dataNascimento: String,
    val genero: String,
    val objetivoPrincipal: String,
    val criadoEm: String? = null,
    val usuarioId: Long
)
