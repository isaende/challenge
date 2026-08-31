package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaUsuarios(
    val codUsuario: Long = 0,
    val email: String,
    val senhaHash: String,
    val papel: String = "USUARIO",
    val status: String = "ATIVO",
    val emailVerificadoEm: String? = null,
    val ultimoLoginEm: String? = null,
    val criadoEm: String? = null
)
