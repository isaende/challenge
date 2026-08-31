package model

import kotlinx.serialization.Serializable

@Serializable
data class AdaMetas(
    val id: Long = 0,
    val tipo: String,
    val valorAlvo: Double,
    val unidade: String,
    val origem: String = "ALGORITMO",
    val versaoAlgoritmo: String = "1.0",
    val calculos: String? = null,
    val validaDesde: String,
    val validaAte: String? = null,
    val perfilId: Long
)
