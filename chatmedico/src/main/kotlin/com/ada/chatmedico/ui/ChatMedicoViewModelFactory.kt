// Factory usada para criar a ViewModel com o repositório escolhido pelo app.
package com.ada.chatmedico.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ada.chatmedico.dados.repositorio.RepositorioChatMedico

class ChatMedicoViewModelFactory(
    private val repositorio: RepositorioChatMedico
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatMedicoViewModel::class.java)) {
            return ChatMedicoViewModel(repositorio = repositorio) as T
        }

        throw IllegalArgumentException("ViewModel desconhecida: ${modelClass.name}")
    }
}
