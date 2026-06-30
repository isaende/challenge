package com.ada.chatmedico.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.ada.chatmedico.RepositorioChatMedico
import com.ada.chatmedico.RespostaChatMedico
import com.ada.chatmedico.TelaChatMedico
import com.ada.chatmedico.TipoRespostaMedica
import com.ada.chatmedico.criarRepositorioChatMedico
import java.text.Normalizer
import kotlinx.coroutines.delay

private const val USAR_SERVIDOR_LOCAL = false
private const val URL_SERVIDOR_LOCAL = "http://10.0.2.2:8080"

class AtividadePrincipal : ComponentActivity() {
    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        val repositorio = if (USAR_SERVIDOR_LOCAL) criarRepositorioChatMedico(URL_SERVIDOR_LOCAL) else RepositorioDemo
        setContent {
            MaterialTheme {
                TelaChatMedico(repositorio)
            }
        }
    }
}

private object RepositorioDemo : RepositorioChatMedico {
    override suspend fun enviarMensagem(mensagem: String): RespostaChatMedico {
        delay(700)
        val texto = mensagem.normalizado()
        return when {
            texto.contains("dor no peito") || texto.contains("falta de ar") ->
                RespostaChatMedico("Atenção: procure atendimento de urgência imediatamente. Esta resposta não substitui avaliação médica.", TipoRespostaMedica.URGENCIA)
            texto.contains("dose") || texto.contains("remedio") || texto.contains("o que eu tenho") ->
                RespostaChatMedico("Não posso diagnosticar, prescrever medicamentos ou indicar dose. Posso explicar informações gerais, mas procure um profissional de saúde.", TipoRespostaMedica.RECUSA_SEGURA)
            texto.contains("nome") || texto.contains("quem e voce") ->
                RespostaChatMedico("Eu sou a Ada, assistente de informações educativas em saúde.", TipoRespostaMedica.EDUCATIVO)
            texto.contains("ldl") || texto.contains("colesterol") ->
                RespostaChatMedico("LDL é uma fração do colesterol conhecida como colesterol ruim. Quando está alto, pode estar associado a maior risco cardiovascular, mas precisa ser interpretado com seu histórico e outros exames. Esta resposta é apenas educativa.", TipoRespostaMedica.EDUCATIVO)
            else ->
                RespostaChatMedico("Posso explicar esse tema de forma geral. Esta resposta é apenas educativa e não substitui avaliação de um profissional de saúde.", TipoRespostaMedica.EDUCATIVO)
        }
    }
}

private fun String.normalizado(): String {
    return Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .replace("\\s+".toRegex(), " ")
        .trim()
}
