// Define o contrato de rede que envia mensagens para o backend do chat médico.
package com.ada.chatmedico.dados.rede

import com.ada.chatmedico.dados.dto.RespostaChatMedicoDto

interface ApiChatMedico {
    suspend fun enviarMensagem(mensagem: String): RespostaChatMedicoDto
}
