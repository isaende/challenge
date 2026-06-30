// DTO com a resposta que o backend devolve para o app Android.
package dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val resposta: String,
    val tipo: String
)
