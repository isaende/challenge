package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaCondicoes(
    val id: Long = 0,
    val descricao: String,
    val ativa: Boolean,
    val limiteHidratacao: Boolean,
    val limiteAtividades: Boolean,
    val validadoProfissional: Boolean,
    val perfilId: Long
)
