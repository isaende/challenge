package home_page

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import chat_medico.RepositorioChatMedico
import chat_medico.RespostaChatMedico
import chat_medico.TelaChatMedico
import chat_medico.TipoRespostaMedica
import chat_medico.criarRepositorioChatMedico
import java.text.Normalizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val USAR_SERVIDOR_LOCAL = true
private const val URL_SERVIDOR_LOCAL = "http://10.0.2.2:8080"

private enum class TelaAtual {
    INICIO,
    CHAT_MEDICO,
    EXAMES
}

class AtividadePrincipal : ComponentActivity() {
    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        val repositorio = if (USAR_SERVIDOR_LOCAL) criarRepositorioChatMedico(URL_SERVIDOR_LOCAL) else RepositorioDemo
        setContent {
            var telaAtual by remember { mutableStateOf(TelaAtual.INICIO) }
            var dadosSaude by remember { mutableStateOf(DadosSaude()) }
            var statusSaude by remember { mutableStateOf("Verificando Health Connect...") }
            var mostrarBotaoConectarSaude by remember { mutableStateOf(false) }
            var textoBotaoSaude by remember { mutableStateOf("Conectar") }
            val escopo = rememberCoroutineScope()
            val repositorioSaude = remember {
                RepositorioSaudeHealthConnect(this@AtividadePrincipal)
            }

            fun atualizarDadosSaude() {
                escopo.launch {
                    if (!repositorioSaude.healthConnectDisponivel()) {
                        statusSaude = when (repositorioSaude.statusSdk()) {
                            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                                "Instale ou atualize o Health Connect"
                            else -> "Health Connect indisponivel neste aparelho"
                        }
                        mostrarBotaoConectarSaude = false
                        return@launch
                    }

                    if (!repositorioSaude.temPermissoes()) {
                        statusSaude = "Permissao necessaria para ler seus dados"
                        mostrarBotaoConectarSaude = true
                        textoBotaoSaude = "Conectar"
                        return@launch
                    }

                    statusSaude = "Atualizando dados..."
                    mostrarBotaoConectarSaude = false
                    try {
                        dadosSaude = repositorioSaude.lerDadosRecentes()
                        statusSaude = "Atualizado agora pelo Health Connect"
                        mostrarBotaoConectarSaude = true
                        textoBotaoSaude = "Atualizar"
                    } catch (_: Exception) {
                        statusSaude = "Nao foi possivel ler o Health Connect"
                        mostrarBotaoConectarSaude = true
                        textoBotaoSaude = "Tentar"
                    }
                }
            }

            val solicitadorPermissoesSaude = rememberLauncherForActivityResult(
                contract = PermissionController.createRequestPermissionResultContract()
            ) { permissoesConcedidas ->
                if (permissoesConcedidas.containsAll(RepositorioSaudeHealthConnect.PERMISSOES)) {
                    atualizarDadosSaude()
                } else {
                    statusSaude = "Permissao negada no Health Connect"
                    mostrarBotaoConectarSaude = true
                    textoBotaoSaude = "Conectar"
                }
            }

            fun conectarHealthConnect() {
                if (!repositorioSaude.healthConnectDisponivel()) {
                        statusSaude = when (repositorioSaude.statusSdk()) {
                            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                                "Instale ou atualize o Health Connect"
                            else -> "Health Connect indisponivel neste aparelho"
                        }
                        mostrarBotaoConectarSaude = false
                        return
                    }
                escopo.launch {
                    if (repositorioSaude.temPermissoes()) {
                        atualizarDadosSaude()
                    } else {
                        solicitadorPermissoesSaude.launch(RepositorioSaudeHealthConnect.PERMISSOES)
                    }
                }
            }

            LaunchedEffect(Unit) {
                atualizarDadosSaude()
            }

            MaterialTheme {
                when (telaAtual) {
                    TelaAtual.INICIO -> TelaInicio(
                        aoAbrirChatMedico = { telaAtual = TelaAtual.CHAT_MEDICO },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                        dadosSaude = dadosSaude,
                        statusSaude = statusSaude,
                        aoConectarSaude = { conectarHealthConnect() },
                        mostrarBotaoConectarSaude = mostrarBotaoConectarSaude,
                        textoBotaoSaude = textoBotaoSaude
                    )

                    TelaAtual.CHAT_MEDICO -> TelaChatMedico(
                        repositorio = repositorio,
                        aoVoltar = { telaAtual = TelaAtual.INICIO }
                    )

                    TelaAtual.EXAMES -> TelaExames(
                        aoVoltarInicio = { telaAtual = TelaAtual.INICIO }
                    )
                }
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
