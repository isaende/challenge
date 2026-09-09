package home_page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import chat_medico.TelaChatMedico
import chat_medico.criarRepositorioChatMedico
import java.text.Normalizer
import kotlinx.coroutines.launch

// mapa das telas do aplicativo que decide a tela atual

private enum class TelaAtual {
    LOGIN,
    CADASTRO,
    INICIO,
    TELEMEDICINA,
    CHAT_MEDICO,
    EXAMES,
    MEDICAMENTOS,
    EMERGENCIA,
    HIDRATACAO,
    EXERCICIOS,
    TREINO,
    METAS,
    PERFIL
}

class AtividadePrincipal : ComponentActivity() {
    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        val repositorio = criarRepositorioChatMedico()
       
       // criação das interfaces visual pelo Jetpack compose
        setContent {
            val repositorioSessao = remember { RepositorioSessao(this@AtividadePrincipal) }
            var sessao by remember { mutableStateOf(repositorioSessao.carregar()) }
            var telaAtual by remember { mutableStateOf(if (sessao == null) TelaAtual.LOGIN else TelaAtual.INICIO) }
            var voltarTreinoParaMetas by remember { mutableStateOf(false) }
            var telaAnteriorPerfil by remember { mutableStateOf(TelaAtual.INICIO) }
            var dadosSaude by remember { mutableStateOf(DadosSaude()) }
            var statusSaude by remember { mutableStateOf("Verificando Health Connect...") }
            var mostrarBotaoConectarSaude by remember { mutableStateOf(false) }
            var textoBotaoSaude by remember { mutableStateOf("Conectar") }
            val escopo = rememberCoroutineScope()
            val repositorioSaude = remember {
                RepositorioSaudeHealthConnect(this@AtividadePrincipal)
            }

            // atualiza os dados com base no health connect se ele estiver instalado, se não estiver ele solicita
            // a instalação ou atualização
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

                    // verifica se tem permissões para usar os dados do health connect
                    if (!repositorioSaude.temPermissoes()) {
                        statusSaude = "Permissao necessaria para ler seus dados"
                        mostrarBotaoConectarSaude = true
                        textoBotaoSaude = "Conectar"
                        return@launch
                    }

                    // atualiza os dados de saúde toda vez que o aplicativo inicia
                    statusSaude = "Atualizando dados..."
                    mostrarBotaoConectarSaude = false
                    try {
                        dadosSaude = repositorioSaude.lerDadosRecentes()
                        sessao?.let { usuario ->
                            runCatching { ApiAda.salvarResumo(usuario.perfilId, dadosSaude) }
                        }
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

            //conecta e verifica se o health connect esta disponivel
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

            // diz ao aplicativo Se a tela atual for início, renderize a TelaInicio. Se for exames, renderize a TelaExames
            val densidadeAtual = LocalDensity.current
            CompositionLocalProvider(
                LocalAbrirPerfil provides {
                    if (telaAtual != TelaAtual.PERFIL) telaAnteriorPerfil = telaAtual
                    telaAtual = TelaAtual.PERFIL
                },
                LocalDensity provides Density(
                    density = densidadeAtual.density,
                    fontScale = densidadeAtual.fontScale * 1.19f
                )
            ) {
                MaterialTheme(typography = TipografiaAplicativo) {
                    when (telaAtual) {
                    TelaAtual.LOGIN -> TelaLogin(
                        aoEntrar = { novaSessao ->
                            repositorioSessao.salvar(novaSessao)
                            sessao = novaSessao
                            telaAtual = TelaAtual.INICIO
                        },
                        aoCriarConta = { telaAtual = TelaAtual.CADASTRO }
                    )

                    TelaAtual.CADASTRO -> TelaCadastro(
                        aoCadastrar = { novaSessao ->
                            repositorioSessao.salvar(novaSessao)
                            sessao = novaSessao
                            telaAtual = TelaAtual.INICIO
                        },
                        aoFazerLogin = { telaAtual = TelaAtual.LOGIN }
                    )

                    TelaAtual.INICIO -> TelaInicio(
                        aoAbrirChatMedico = { telaAtual = TelaAtual.CHAT_MEDICO },
                        aoAbrirTelemedicina = { telaAtual = TelaAtual.TELEMEDICINA },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                        aoAbrirMedicamentos = { telaAtual = TelaAtual.MEDICAMENTOS },
                        aoAbrirEmergencia = { telaAtual = TelaAtual.EMERGENCIA },
                        aoAbrirHidratacao = { telaAtual = TelaAtual.HIDRATACAO },
                        aoAbrirExercicios = { telaAtual = TelaAtual.EXERCICIOS },
                        aoAbrirMetas = { telaAtual = TelaAtual.METAS },
                        dadosSaude = dadosSaude,
                        statusSaude = statusSaude,
                        aoConectarSaude = { conectarHealthConnect() },
                        mostrarBotaoConectarSaude = mostrarBotaoConectarSaude,
                        textoBotaoSaude = textoBotaoSaude,
                        nomeUsuario = sessao?.nomeCompleto.orEmpty(),
                        aoAbrirPerfil = {
                            telaAnteriorPerfil = TelaAtual.INICIO
                            telaAtual = TelaAtual.PERFIL
                        }
                    )

                    TelaAtual.PERFIL -> TelaPerfil(
                        perfilId = sessao?.perfilId ?: 0,
                        aoVoltar = { telaAtual = telaAnteriorPerfil },
                        aoAbrirInicio = { telaAtual = TelaAtual.INICIO },
                        aoAbrirMonitorar = { telaAtual = TelaAtual.EXERCICIOS },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                        aoAdicionar = { telaAtual = TelaAtual.METAS },
                        aoPerfilCarregado = { atualizada ->
                            repositorioSessao.salvar(atualizada)
                            sessao = atualizada
                        }
                    )

                    TelaAtual.CHAT_MEDICO -> TelaChatMedico(
                        repositorio = repositorio,
                        perfilId = sessao?.perfilId ?: 0,
                        aoVoltar = { telaAtual = TelaAtual.INICIO }
                    )

                    TelaAtual.TELEMEDICINA -> TelaTelemedicina(
                        aoVoltarInicio = { telaAtual = TelaAtual.INICIO },
                        aoAbrirChatAda = { telaAtual = TelaAtual.CHAT_MEDICO },
                        aoAbrirVideo = { url ->
                            if (url.isNotBlank()) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    )

                    TelaAtual.EXAMES -> TelaExames(
                        aoVoltarInicio = { telaAtual = TelaAtual.INICIO },
                        perfilId = sessao?.perfilId ?: 0
                    )

                    TelaAtual.MEDICAMENTOS -> TelaMedicamentos(
                        aoVoltarInicio = { telaAtual = TelaAtual.INICIO },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                        perfilId = sessao?.perfilId ?: 0
                    )

                    TelaAtual.EMERGENCIA -> TelaEmergencia(
                        aoVoltarInicio = { telaAtual = TelaAtual.INICIO },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES }
                    )

                        TelaAtual.HIDRATACAO -> TelaHidratacao(
                            aoVoltar = { telaAtual = TelaAtual.INICIO },
                            perfilId = sessao?.perfilId ?: 0,
                            metaDiariaMl = sessao?.metaAguaMl ?: 2500
                        )

                        TelaAtual.EXERCICIOS -> TelaExercicios(
                            passosHoje = dadosSaude.passos ?: 0L,
                            carregando = dadosSaude.passos == null,
                            aoAtualizar = { atualizarDadosSaude() },
                            aoIniciarTreino = { voltarTreinoParaMetas = false; telaAtual = TelaAtual.TREINO },
                            aoVoltar = { telaAtual = TelaAtual.INICIO },
                            metaPassos = sessao?.metaPassos ?: 8000
                        )

                        TelaAtual.TREINO -> TelaTreino(
                            aoVoltar = { telaAtual = if (voltarTreinoParaMetas) TelaAtual.METAS else TelaAtual.EXERCICIOS },
                            aoVoltarInicio = { telaAtual = TelaAtual.INICIO },
                            perfilId = sessao?.perfilId ?: 0
                        )

                        TelaAtual.METAS -> TelaMetas(
                            perfilId = sessao?.perfilId ?: 0,
                            metaAguaInicial = sessao?.metaAguaMl ?: 2500,
                            metaPassosInicial = sessao?.metaPassos ?: 8000,
                            passosHoje = dadosSaude.passos,
                            statusPassos = statusSaude,
                            aoMetasSalvas = { agua, passos ->
                                sessao?.let { atual ->
                                    val atualizada = atual.copy(metaAguaMl = agua, metaPassos = passos)
                                    repositorioSessao.salvar(atualizada)
                                    sessao = atualizada
                                }
                            },
                            aoVoltar = { telaAtual = TelaAtual.INICIO },
                            aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                            aoAbrirTreino = { voltarTreinoParaMetas = true; telaAtual = TelaAtual.TREINO },
                            aoAtualizarPassos = { conectarHealthConnect() }
                        )
                    }
                }
            }
        }
    }
}

// função para normalizar o texto para evitar erros
private fun String.normalizado(): String {
    return Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .replace("\\s+".toRegex(), " ")
        .trim()
}
