// Define a interface que a ViewModel usa para enviar mensagens sem depender da rede diretamente.
package com.ada.chatmedico.dados.repositorio

import com.ada.chatmedico.dominio.RespostaChatMedico

interface RepositorioChatMedico {
    suspend fun enviarMensagem(mensagem: String): RespostaChatMedico
}
