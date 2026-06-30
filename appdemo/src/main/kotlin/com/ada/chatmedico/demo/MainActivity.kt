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
import java.text.Normalizer
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

        val textoNormalizado = mensagem.normalizarParaBusca()

        return when {
            textoNormalizado.temAlgumTermo(
                "dor no peito",
                "falta de ar",
                "desmaio",
                "convulsao",
                "sangramento intenso",
                "vontade de morrer"
            ) -> RespostaChatMedico(
                texto = "Atenção: pelos sinais descritos, procure atendimento de urgência imediatamente ou acione o serviço de emergência local. Esta resposta não substitui avaliação médica.",
                tipo = TipoRespostaMedica.URGENCIA
            )

            textoNormalizado.temAlgumTermo(
                "qual dose",
                "dosagem",
                "prescricao",
                "posso parar",
                "trocar medicamento",
                "o que eu tenho",
                "tratamento para mim"
            ) -> RespostaChatMedico(
                texto = "Não posso diagnosticar, prescrever medicamentos, indicar dose ou orientar mudança de tratamento. Posso explicar informações gerais, mas para uma orientação segura procure um profissional de saúde.",
                tipo = TipoRespostaMedica.RECUSA_SEGURA
            )

            textoNormalizado.temAlgumTermo(
                "qual seu nome",
                "qual e seu nome",
                "quem e voce"
            ) -> RespostaChatMedico(
                texto = "Eu sou a Ada, uma assistente de informações educativas em saúde. Posso explicar termos de saúde e exames de forma geral, mas não faço diagnóstico, não prescrevo medicamentos e não substituo avaliação profissional.",
                tipo = TipoRespostaMedica.EDUCATIVO
            )

            textoNormalizado.contains("ldl") ||
                textoNormalizado.contains("colesterol alto") ||
                textoNormalizado.contains("colesterol ldl alto") -> RespostaChatMedico(
                texto = "LDL é uma fração do colesterol conhecida popularmente como colesterol ruim. Quando está alto, pode estar associado a maior risco cardiovascular ao longo do tempo, mas o significado depende do valor do exame, histórico de saúde e outros fatores. Em geral, alimentação equilibrada, atividade física e acompanhamento profissional ajudam no cuidado. Esta resposta é apenas educativa e não substitui avaliação de um profissional de saúde.",
                tipo = TipoRespostaMedica.EDUCATIVO
            )

            else -> RespostaChatMedico(
                texto = "Posso explicar esse tema de forma geral. Esta resposta é apenas educativa e não substitui avaliação de um profissional de saúde. Para decisões sobre sintomas, exames ou medicamentos, procure avaliação profissional.",
                tipo = TipoRespostaMedica.EDUCATIVO
            )
        }
    }

    private fun String.temAlgumTermo(vararg termos: String): Boolean {
        return termos.any { termo -> contains(termo) }
    }

    private fun String.normalizarParaBusca(): String {
        val semAcentos = Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return semAcentos.replace("\\s+".toRegex(), " ").trim()
    }
}
