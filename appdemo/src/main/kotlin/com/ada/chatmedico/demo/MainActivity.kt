// Activity do app demo que abre a tela de chat com respostas simuladas para testes.
package com.ada.chatmedico.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.ada.chatmedico.dados.repositorio.RepositorioChatMedico
import com.ada.chatmedico.di.ProvedorChatMedico
import com.ada.chatmedico.dominio.RespostaChatMedico
import com.ada.chatmedico.dominio.TipoRespostaMedica
import com.ada.chatmedico.integracao.TelaChatMedicoComRepositorio
import kotlinx.coroutines.delay

private const val USAR_BACKEND_LOCAL = false
private const val URL_BACKEND_LOCAL = "http://10.0.2.2:8080"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repositorio = if (USAR_BACKEND_LOCAL) {
            ProvedorChatMedico.criarRepositorio(urlBase = URL_BACKEND_LOCAL)
        } else {
            RepositorioChatMedicoDemo
        }

        setContent {
            MaterialTheme {
                TelaChatMedicoComRepositorio(
                    repositorio = repositorio,
                    aoEscolherExamesGuardados = {
                        // Futuramente abrir seletor de exames guardados.
                    }
                )
            }
        }
    }
}

private object RepositorioChatMedicoDemo : RepositorioChatMedico {
    override suspend fun enviarMensagem(mensagem: String): RespostaChatMedico {
        delay(900)

        val textoNormalizado = mensagem.lowercase()
        val pareceUrgencia = listOf(
            "dor no peito",
            "falta de ar",
            "desmaio",
            "convulsão",
            "sangramento intenso",
            "vontade de morrer"
        ).any { sinal -> textoNormalizado.contains(sinal) }

        return if (pareceUrgencia) {
            RespostaChatMedico(
                texto = "Atenção: pelos sinais descritos, procure atendimento de urgência imediatamente ou acione o serviço de emergência local. Esta resposta não substitui avaliação médica.",
                tipo = TipoRespostaMedica.URGENCIA
            )
        } else {
            RespostaChatMedico(
                texto = "Posso explicar esse tema de forma geral. Esta resposta é apenas educativa e não substitui avaliação de um profissional de saúde. Para decisões sobre sintomas, exames ou medicamentos, procure avaliação profissional.",
                tipo = TipoRespostaMedica.EDUCATIVO
            )
        }
    }
}
