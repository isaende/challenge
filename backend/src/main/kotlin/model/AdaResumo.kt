package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi

@Serializable
data class AdaResumo(
    val id: Long = 0,
    val dataResumo: String,
    val passos: Long?,
    val sonoMinutos: Long?,
    val batimentosUltimos: Int? = null,
    val origem: String = "HEALTH_CONNECT",
    val sincronizadoEm: String? = null,
    val perfilId: Long,
    val pressaoSistolica: Double? = null,
    val pressaoDiastolica: Double? = null
) {
    // Derivada dos passos para manter a estimativa consistente ao atualizar o dia.
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    val caloriasGastas: Double? = passos?.times(0.4)
}
