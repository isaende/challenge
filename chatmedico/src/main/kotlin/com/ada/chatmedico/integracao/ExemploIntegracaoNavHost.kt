// Mostra como integrar futuramente a tela de chat em um NavHost existente.
package com.ada.chatmedico.integracao

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ada.chatmedico.dados.repositorio.RepositorioChatMedico
import com.ada.chatmedico.ui.ChatMedicoViewModel
import com.ada.chatmedico.ui.ChatMedicoViewModelFactory
import com.ada.chatmedico.ui.TelaChatMedico

object RotasChatMedico {
    const val CHAT_MEDICO = "chat_medico"
}

fun NavGraphBuilder.rotaChatMedico(
    repositorio: RepositorioChatMedico,
    navController: NavController,
    aoEscolherExamesGuardados: (() -> Unit)? = null
) {
    composable(route = RotasChatMedico.CHAT_MEDICO) {
        TelaChatMedicoComRepositorio(
            repositorio = repositorio,
            aoVoltar = { navController.popBackStack() },
            aoEscolherExamesGuardados = aoEscolherExamesGuardados
        )
    }
}

@Composable
fun TelaChatMedicoComRepositorio(
    repositorio: RepositorioChatMedico,
    aoVoltar: (() -> Unit)? = null,
    aoEscolherExamesGuardados: (() -> Unit)? = null
) {
    val modelo: ChatMedicoViewModel = viewModel(
        factory = ChatMedicoViewModelFactory(repositorio = repositorio)
    )

    TelaChatMedico(
        modelo = modelo,
        aoVoltar = aoVoltar,
        aoEscolherExamesGuardados = aoEscolherExamesGuardados
    )
}
