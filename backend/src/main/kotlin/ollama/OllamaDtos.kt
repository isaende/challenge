// DTOs usados na chamada do backend para o Ollama.
package ollama

import kotlinx.serialization.Serializable

@Serializable
data class OllamaChatRequest(
    val model: String,
    val stream: Boolean,
    val messages: List<OllamaMessage>,
    val options: OllamaOptions
)

@Serializable
data class OllamaMessage(
    val role: String,
    val content: String
)

@Serializable
data class OllamaOptions(
    val temperature: Double
)

@Serializable
data class OllamaChatResponse(
    val message: OllamaMessage? = null
)
