package home_page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import chat_medico.TelaChatMedico
import chat_medico.criarRepositorioChatMedico
import com.ada.chatmedico.demo.R
import java.text.Normalizer
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

// mapa das telas do aplicativo que decide a tela atual

private enum class TelaAtual {
    LOGIN,
    CADASTRO,
    INICIO,
    MONITORAR,
    RECOMPENSAS,
    TELEMEDICINA,
    CHAT_MEDICO,
    EXAMES,
    MEDICAMENTOS,
    EMERGENCIA,
    HIDRATACAO,
    EXERCICIOS,
    TREINO,
    METAS,
    PERFIL,
    PERFIL_ADAPTATIVO
}

class AtividadePrincipal : ComponentActivity() {
    override fun onCreate(estadoSalvo: Bundle?) {
        setTheme(R.style.Theme_AdaUniCare)
        super.onCreate(estadoSalvo)
        val repositorio = criarRepositorioChatMedico()
       
       // criação das interfaces visual pelo Jetpack compose
        setContent {
            val repositorioSessao = remember { RepositorioSessao(this@AtividadePrincipal) }
            var sessao by remember { mutableStateOf(repositorioSessao.carregar()) }
            var telaAtual by rememberSaveable { mutableStateOf(if (sessao == null) TelaAtual.LOGIN else TelaAtual.INICIO) }
            var voltarTreinoParaMetas by remember { mutableStateOf(false) }
            var telaAnteriorPerfil by remember { mutableStateOf(TelaAtual.INICIO) }
            var origemAtalhoAdaptativo by rememberSaveable { mutableStateOf<TelaAtual?>(null) }
            var telaAnteriorRecompensas by rememberSaveable { mutableStateOf(TelaAtual.INICIO) }
            var dadosSaude by remember { mutableStateOf(DadosSaude()) }
            var statusSaude by remember { mutableStateOf("Verificando Health Connect...") }
            var mostrarBotaoConectarSaude by remember { mutableStateOf(false) }
            var textoBotaoSaude by remember { mutableStateOf("Conectar") }
            val escopo = rememberCoroutineScope()
            var atualizacaoSaude by remember { mutableStateOf<Job?>(null) }
            val repositorioSaude = remember {
                RepositorioSaudeHealthConnect(this@AtividadePrincipal)
            }

            fun atualizarDadosSaude() {
                val usuario = sessao ?: return
                atualizacaoSaude?.cancel()
                atualizacaoSaude = escopo.launch {
                    statusSaude = "Atualizando dados..."
                    mostrarBotaoConectarSaude = false
                    var dadosSalvos = DadosSaude()
                    try {
                        dadosSalvos = ApiAda.carregarResumoHoje(usuario.perfilId)
                        if (sessao?.perfilId != usuario.perfilId) return@launch
                        dadosSaude = dadosSalvos
                    } catch (cancelamento: CancellationException) {
                        throw cancelamento
                    } catch (_: Exception) {
                        // A leitura local continua disponivel se o servidor estiver offline.
                    }
                    try {
                        if (!repositorioSaude.healthConnectDisponivel()) {
                            dadosSaude = dadosSalvos
                            statusSaude = when (repositorioSaude.statusSdk()) {
                                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                                    "Instale ou atualize o Health Connect"
                                else -> "Health Connect indisponível neste aparelho"
                            }
                            if (dadosSalvos.temDados()) statusSaude = "Resumo de hoje salvo no banco"
                            return@launch
                        }
                        val leitura = repositorioSaude.lerDadosRecentes()
                        val novosDados = leitura.dados
                        if (sessao?.perfilId != usuario.perfilId) return@launch
                        dadosSaude = novosDados.complementarCom(dadosSalvos)
                        try {
                            if (novosDados.temDados()) ApiAda.salvarResumo(usuario.perfilId, dadosSaude)
                            statusSaude = "Dados atualizados e salvos"
                        } catch (cancelamento: CancellationException) {
                            throw cancelamento
                        } catch (_: Exception) {
                            statusSaude = "Dados lidos. Não foi possível salvar; tente atualizar."
                        }
                        mostrarBotaoConectarSaude = true
                        textoBotaoSaude = if (leitura.permissoesPendentes.isEmpty()) "Atualizar" else "Conectar"
                        leitura.statusSinaisVitais()?.let { statusSaude = it }
                        if (leitura.falhas.isNotEmpty() && leitura.statusSinaisVitais() == null) {
                            statusSaude = "Dados parciais: nao foi possivel ler ${leitura.falhas.joinToString()}"
                        }
                    } catch (cancelamento: CancellationException) {
                        throw cancelamento
                    } catch (_: Exception) {
                        dadosSaude = dadosSalvos
                        statusSaude = if (dadosSalvos.temDados()) "Resumo de hoje salvo no banco"
                            else "Nao foi possivel ler o Health Connect"
                        mostrarBotaoConectarSaude = true
                        textoBotaoSaude = "Tentar"
                    }
                }
            }

            val solicitadorPermissoesSaude = rememberLauncherForActivityResult(
                contract = PermissionController.createRequestPermissionResultContract()
            ) { _ ->
                atualizarDadosSaude()
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
                    try {
                        if (repositorioSaude.temPermissoes()) {
                            atualizarDadosSaude()
                        } else {
                            solicitadorPermissoesSaude.launch(RepositorioSaudeHealthConnect.PERMISSOES)
                        }
                    } catch (cancelamento: CancellationException) {
                        throw cancelamento
                    } catch (_: Exception) {
                        statusSaude = "Não foi possível conectar ao Health Connect"
                        mostrarBotaoConectarSaude = true
                        textoBotaoSaude = "Tentar"
                    }
                }
            }

            LaunchedEffect(sessao?.perfilId) {
                atualizacaoSaude?.cancel()
                dadosSaude = DadosSaude()
                if (sessao != null) atualizarDadosSaude()
            }
            LaunchedEffect(telaAtual == TelaAtual.MONITORAR) {
                if (telaAtual == TelaAtual.MONITORAR) atualizarDadosSaude()
            }
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                atualizarDadosSaude()
            }

            // diz ao aplicativo Se a tela atual for início, renderize a TelaInicio. Se for exames, renderize a TelaExames
            val densidadeAtual = LocalDensity.current
            CompositionLocalProvider(
                LocalAbrirMonitorar provides { telaAtual = TelaAtual.MONITORAR },
                LocalAbrirMetas provides { telaAtual = TelaAtual.METAS },
                LocalAbrirRecompensas provides {
                    if (telaAtual != TelaAtual.RECOMPENSAS) telaAnteriorRecompensas = telaAtual
                    telaAtual = TelaAtual.RECOMPENSAS
                },
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
                    val voltarDoAtalho: () -> Unit = {
                        telaAtual = if (origemAtalhoAdaptativo == telaAtual) TelaAtual.PERFIL_ADAPTATIVO else TelaAtual.INICIO
                        origemAtalhoAdaptativo = null
                    }
                    BackHandler(enabled = origemAtalhoAdaptativo == telaAtual, onBack = voltarDoAtalho)
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

                    TelaAtual.MONITORAR -> TelaMonitorar(
                        dadosSaude = dadosSaude,
                        statusSaude = statusSaude,
                        mostrarBotaoConectarSaude = mostrarBotaoConectarSaude,
                        textoBotaoSaude = textoBotaoSaude,
                        nomeUsuario = sessao?.nomeCompleto.orEmpty(),
                        metaPassos = sessao?.metaPassos ?: 8000,
                        aoConectarSaude = { conectarHealthConnect() },
                        aoVoltar = { telaAtual = TelaAtual.INICIO },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                        aoAdicionar = { telaAtual = TelaAtual.METAS }
                    )

                    TelaAtual.RECOMPENSAS -> TelaRecompensas(
                        perfilId = sessao?.perfilId ?: 0,
                        nomeUsuario = sessao?.nomeCompleto.orEmpty(),
                        aoVoltar = { telaAtual = telaAnteriorRecompensas },
                        aoAbrirInicio = { telaAtual = TelaAtual.INICIO },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES }
                    )

                    TelaAtual.PERFIL -> TelaPerfil(
                        perfilId = sessao?.perfilId ?: 0,
                        aoAbrirPerfilAdaptativo = { telaAtual = TelaAtual.PERFIL_ADAPTATIVO },
                        aoVoltar = { telaAtual = telaAnteriorPerfil },
                        aoAbrirInicio = { telaAtual = TelaAtual.INICIO },
                        aoAbrirMonitorar = { telaAtual = TelaAtual.MONITORAR },
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                        aoAdicionar = { telaAtual = TelaAtual.METAS },
                        aoPerfilCarregado = { atualizada ->
                            if (sessao?.perfilId == atualizada.perfilId && telaAtual == TelaAtual.PERFIL) {
                                repositorioSessao.salvar(atualizada)
                                sessao = atualizada
                            }
                        },
                        aoSair = {
                            repositorioSessao.limpar()
                            sessao = null
                            telaAnteriorPerfil = TelaAtual.INICIO
                            telaAnteriorRecompensas = TelaAtual.INICIO
                            voltarTreinoParaMetas = false
                            origemAtalhoAdaptativo = null
                            telaAtual = TelaAtual.LOGIN
                        }
                    )

                    TelaAtual.PERFIL_ADAPTATIVO -> TelaPerfilAdaptativo(
                        perfilId = sessao?.perfilId ?: 0,
                        nomeUsuario = sessao?.nomeCompleto.orEmpty(),
                        aoVoltar = { telaAtual = TelaAtual.PERFIL },
                        aoAbrirMedico = {
                            origemAtalhoAdaptativo = TelaAtual.TELEMEDICINA
                            telaAtual = TelaAtual.TELEMEDICINA
                        },
                        aoAbrirMedicamentos = {
                            origemAtalhoAdaptativo = TelaAtual.MEDICAMENTOS
                            telaAtual = TelaAtual.MEDICAMENTOS
                        },
                        aoAbrirEmergencia = {
                            origemAtalhoAdaptativo = TelaAtual.EMERGENCIA
                            telaAtual = TelaAtual.EMERGENCIA
                        }
                    )

                    TelaAtual.CHAT_MEDICO -> TelaChatMedico(
                        repositorio = repositorio,
                        perfilId = sessao?.perfilId ?: 0,
                        aoVoltar = { telaAtual = TelaAtual.INICIO }
                    )

                    TelaAtual.TELEMEDICINA -> TelaTelemedicina(
                        aoVoltarInicio = voltarDoAtalho,
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
                        aoVoltarInicio = voltarDoAtalho,
                        aoAbrirExames = { telaAtual = TelaAtual.EXAMES },
                        perfilId = sessao?.perfilId ?: 0
                    )

                    TelaAtual.EMERGENCIA -> TelaEmergencia(
                        aoVoltarInicio = voltarDoAtalho,
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
