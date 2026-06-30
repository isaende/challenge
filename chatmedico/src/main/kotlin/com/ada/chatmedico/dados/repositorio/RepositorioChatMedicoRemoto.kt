// Repositório que busca respostas reais chamando a API remota do chat médico.
package com.ada.chatmedico.dados.repositorio

import com.ada.chatmedico.dados.dto.paraDominio
import com.ada.chatmedico.dados.rede.ApiChatMedico
import com.ada.chatmedico.dominio.RespostaChatMedico

class RepositorioChatMedicoRemoto(
    private val apiChatMedico: ApiChatMedico
) : RepositorioChatMedico {

    override suspend fun enviarMensagem(mensagem: String): RespostaChatMedico {
        return apiChatMedico.enviarMensagem(mensagem).paraDominio()
    }
}
