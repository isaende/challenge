// Cria as dependências simples do módulo, como API, repositório e factory da ViewModel.
package com.ada.chatmedico.di

import com.ada.chatmedico.configuracao.ConfiguracaoApiChatMedico
import com.ada.chatmedico.dados.rede.ApiChatMedico
import com.ada.chatmedico.dados.rede.ApiChatMedicoKtor
import com.ada.chatmedico.dados.repositorio.RepositorioChatMedico
import com.ada.chatmedico.dados.repositorio.RepositorioChatMedicoRemoto
import com.ada.chatmedico.ui.ChatMedicoViewModelFactory

object ProvedorChatMedico {
    fun criarApi(
        urlBase: String = ConfiguracaoApiChatMedico.API_BASE_URL
    ): ApiChatMedico {
        return ApiChatMedicoKtor(urlBase = urlBase)
    }

    fun criarRepositorio(
        urlBase: String = ConfiguracaoApiChatMedico.API_BASE_URL
    ): RepositorioChatMedico {
        return RepositorioChatMedicoRemoto(apiChatMedico = criarApi(urlBase = urlBase))
    }

    fun criarViewModelFactory(
        repositorio: RepositorioChatMedico = criarRepositorio()
    ): ChatMedicoViewModelFactory {
        return ChatMedicoViewModelFactory(repositorio = repositorio)
    }
}
