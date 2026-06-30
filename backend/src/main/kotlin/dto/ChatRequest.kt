// DTO com a mensagem enviada pelo app Android para o backend.
package dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val mensagem: String
)
