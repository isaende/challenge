// Controla o envio de mensagens, loading, validações e atualização do estado do chat.
package com.ada.chatmedico.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ada.chatmedico.dados.repositorio.RepositorioChatMedico
import com.ada.chatmedico.dominio.MensagemChat
import com.ada.chatmedico.dominio.PapelMensagem
import com.ada.chatmedico.dominio.RespostaChatMedico
import com.ada.chatmedico.dominio.TipoRespostaMedica
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ChatMedicoViewModel(
    private val repositorio: RepositorioChatMedico
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoChatMedico())
    val estado: StateFlow<EstadoChatMedico> = _estado.asStateFlow()

    fun alterarTextoDigitado(texto: String) {
        _estado.update { estadoAtual ->
            estadoAtual.copy(
                textoDigitado = texto,
                erroEntrada = if (texto.length > LIMITE_CARACTERES_MENSAGEM) {
                    TEXTO_ERRO_TAMANHO
                } else {
                    null
                }
            )
        }
    }

    fun enviarMensagem() {
        val estadoAtual = _estado.value
        val mensagemUsuario = estadoAtual.textoDigitado.trim()

        if (estadoAtual.enviando || mensagemUsuario.isBlank()) return

        if (mensagemUsuario.length > LIMITE_CARACTERES_MENSAGEM) {
            _estado.update { it.copy(erroEntrada = TEXTO_ERRO_TAMANHO) }
            return
        }

        val idMensagemCarregando = UUID.randomUUID().toString()
        val mensagemCarregando = MensagemChat(
            id = idMensagemCarregando,
            texto = TEXTO_CARREGANDO,
            papel = PapelMensagem.ASSISTENTE,
            carregando = true
        )

        _estado.update { estado ->
            estado.copy(
                mensagens = estado.mensagens +
                    MensagemChat(
                        texto = mensagemUsuario,
                        papel = PapelMensagem.USUARIO
                    ) +
                    mensagemCarregando,
                textoDigitado = "",
                enviando = true,
                erroEntrada = null
            )
        }

        viewModelScope.launch {
            try {
                val resposta = repositorio.enviarMensagem(mensagem = mensagemUsuario)
                adicionarRespostaAssistente(
                    idMensagemCarregando = idMensagemCarregando,
                    resposta = resposta
                )
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (_: Exception) {
                adicionarErroAssistente(idMensagemCarregando = idMensagemCarregando)
            }
        }
    }

    private fun adicionarRespostaAssistente(
        idMensagemCarregando: String,
        resposta: RespostaChatMedico
    ) {
        val mensagemAssistente = when (resposta.tipo) {
            TipoRespostaMedica.ERRO -> MensagemChat(
                texto = TEXTO_ERRO_API,
                papel = PapelMensagem.ASSISTENTE,
                tipo = TipoRespostaMedica.ERRO
            )

            TipoRespostaMedica.URGENCIA -> MensagemChat(
                texto = resposta.texto.comAvisoUrgencia(),
                papel = PapelMensagem.ASSISTENTE,
                tipo = TipoRespostaMedica.URGENCIA
            )

            TipoRespostaMedica.EDUCATIVO,
            TipoRespostaMedica.RECUSA_SEGURA -> MensagemChat(
                texto = resposta.texto,
                papel = PapelMensagem.ASSISTENTE,
                tipo = resposta.tipo
            )
        }

        substituirCarregandoPorMensagem(
            idMensagemCarregando = idMensagemCarregando,
            mensagem = mensagemAssistente
        )
    }

    private fun adicionarErroAssistente(idMensagemCarregando: String) {
        substituirCarregandoPorMensagem(
            idMensagemCarregando = idMensagemCarregando,
            mensagem = MensagemChat(
                texto = TEXTO_ERRO_API,
                papel = PapelMensagem.ASSISTENTE,
                tipo = TipoRespostaMedica.ERRO
            )
        )
    }

    private fun substituirCarregandoPorMensagem(
        idMensagemCarregando: String,
        mensagem: MensagemChat
    ) {
        _estado.update { estado ->
            estado.copy(
                mensagens = estado.mensagens.map { mensagemExistente ->
                    if (mensagemExistente.id == idMensagemCarregando) mensagem else mensagemExistente
                },
                enviando = false
            )
        }
    }

    private fun String.comAvisoUrgencia(): String {
        return if (contains("atenção", ignoreCase = true)) {
            this
        } else {
            "Atenção: procure atendimento de urgência imediatamente.\n\n$this"
        }
    }
}
