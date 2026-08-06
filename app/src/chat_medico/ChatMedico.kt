package chat_medico

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ada.chatmedico.demo.R
import home_page.amareloClaro
import home_page.borda
import home_page.branco
import home_page.fundoPrincipal
import home_page.textoPrimario
import home_page.textoSecundario
import home_page.transparente
import home_page.verdeClaro
import home_page.verdeEscuro
import home_page.verdePrincipal
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

const val URL_BASE_SERVIDOR_CHAT = "http://10.0.2.2:8080"
private const val CAMINHO_CHAT_MEDICO = "/api/chat-medico"
private const val LIMITE_MENSAGEM = 2000
private const val TEXTO_AVISO = "Este chat fornece informações educativas e não substitui avaliação de um profissional de saúde."
private const val TEXTO_INICIAL = "Olá! Posso ajudar com explicações gerais sobre saúde e exames. Não faço diagnóstico, não prescrevo medicamentos e não substituo uma consulta médica."
private const val TEXTO_ERRO = "Não foi possível responder agora. Tente novamente."
private const val TEXTO_ERRO_TAMANHO = "Mensagem muito longa. Resuma sua dúvida."
private const val TEXTO_CARREGANDO = "Ada está preparando uma resposta educativa..."

enum class PapelMensagem { USUARIO, ASSISTENTE }
enum class TipoRespostaMedica { EDUCATIVO, URGENCIA, RECUSA_SEGURA, ERRO }

data class MensagemChat(
    val id: String = UUID.randomUUID().toString(),
    val texto: String,
    val papel: PapelMensagem,
    val tipo: TipoRespostaMedica = TipoRespostaMedica.EDUCATIVO,
    val carregando: Boolean = false
)

data class RespostaChatMedico(
    val texto: String,
    val tipo: TipoRespostaMedica
)

data class EstadoChatMedico(
    val mensagens: List<MensagemChat> = listOf(
        MensagemChat(
            id = "inicio",
            texto = TEXTO_INICIAL,
            papel = PapelMensagem.ASSISTENTE
        )
    ),
    val textoDigitado: String = "",
    val enviando: Boolean = false,
    val erroEntrada: String? = null
)

@Serializable
private data class PedidoChatRede(val mensagem: String)

@Serializable
private data class RespostaChatRede(val resposta: String, val tipo: String)

interface RepositorioChatMedico {
    suspend fun enviarMensagem(mensagem: String): RespostaChatMedico
}

class RepositorioChatMedicoServidor(
    private val urlBase: String = URL_BASE_SERVIDOR_CHAT,
    private val cliente: HttpClient = criarClienteHttp()
) : RepositorioChatMedico {
    override suspend fun enviarMensagem(mensagem: String): RespostaChatMedico {
        val resposta = cliente.post(urlBase.trimEnd('/') + CAMINHO_CHAT_MEDICO) {
            contentType(ContentType.Application.Json)
            setBody(PedidoChatRede(mensagem))
        }.body<RespostaChatRede>()

        return RespostaChatMedico(
            texto = resposta.resposta.ifBlank { TEXTO_ERRO },
            tipo = when (resposta.tipo.lowercase()) {
                "educativo" -> TipoRespostaMedica.EDUCATIVO
                "urgencia" -> TipoRespostaMedica.URGENCIA
                "recusa_segura" -> TipoRespostaMedica.RECUSA_SEGURA
                else -> TipoRespostaMedica.ERRO
            }
        )
    }
}

fun criarRepositorioChatMedico(urlBase: String = URL_BASE_SERVIDOR_CHAT): RepositorioChatMedico {
    return RepositorioChatMedicoServidor(urlBase = urlBase)
}

private fun criarClienteHttp(): HttpClient {
    return HttpClient(OkHttp) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 180_000
            socketTimeoutMillis = 180_000
        }
    }
}

class ModeloVisaoChatMedico(
    private val repositorio: RepositorioChatMedico
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoChatMedico())
    val estado: StateFlow<EstadoChatMedico> = _estado.asStateFlow()

    fun alterarTextoDigitado(texto: String) {
        _estado.update {
            it.copy(
                textoDigitado = texto,
                erroEntrada = if (texto.length > LIMITE_MENSAGEM) TEXTO_ERRO_TAMANHO else null
            )
        }
    }

    fun enviarMensagem() {
        val texto = _estado.value.textoDigitado.trim()
        if (_estado.value.enviando || texto.isBlank()) return
        if (texto.length > LIMITE_MENSAGEM) {
            _estado.update { it.copy(erroEntrada = TEXTO_ERRO_TAMANHO) }
            return
        }

        val idCarregando = UUID.randomUUID().toString()
        _estado.update {
            it.copy(
                mensagens = it.mensagens +
                    MensagemChat(texto = texto, papel = PapelMensagem.USUARIO) +
                    MensagemChat(id = idCarregando, texto = TEXTO_CARREGANDO, papel = PapelMensagem.ASSISTENTE, carregando = true),
                textoDigitado = "",
                enviando = true,
                erroEntrada = null
            )
        }

        viewModelScope.launch {
            try {
                val resposta = repositorio.enviarMensagem(texto)
                trocarCarregando(
                    idCarregando,
                    MensagemChat(
                        texto = if (resposta.tipo == TipoRespostaMedica.URGENCIA) resposta.texto.comAvisoUrgencia() else resposta.texto,
                        papel = PapelMensagem.ASSISTENTE,
                        tipo = resposta.tipo
                    )
                )
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (_: Exception) {
                trocarCarregando(
                    idCarregando,
                    MensagemChat(texto = TEXTO_ERRO, papel = PapelMensagem.ASSISTENTE, tipo = TipoRespostaMedica.ERRO)
                )
            }
        }
    }

    private fun trocarCarregando(id: String, mensagem: MensagemChat) {
        _estado.update {
            it.copy(
                mensagens = it.mensagens.map { atual -> if (atual.id == id) mensagem else atual },
                enviando = false
            )
        }
    }

    private fun String.comAvisoUrgencia(): String {
        return if (contains("atenção", ignoreCase = true)) this else "Atenção: procure atendimento de urgência imediatamente.\n\n$this"
    }
}

private class FabricaModeloVisao(
    private val repositorio: RepositorioChatMedico
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ModeloVisaoChatMedico(repositorio) as T
    }
}

@Composable
fun TelaChatMedico(
    repositorio: RepositorioChatMedico = criarRepositorioChatMedico(),
    aoVoltar: () -> Unit = {}
) {
    val modelo: ModeloVisaoChatMedico = viewModel(factory = FabricaModeloVisao(repositorio))
    val estado by modelo.estado.collectAsStateWithLifecycle()
    ConteudoChatMedico(
        estado = estado,
        aoAlterarTexto = modelo::alterarTextoDigitado,
        aoEnviar = modelo::enviarMensagem,
        aoVoltar = aoVoltar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConteudoChatMedico(
    estado: EstadoChatMedico,
    aoAlterarTexto: (String) -> Unit,
    aoEnviar: () -> Unit,
    aoVoltar: () -> Unit
) {
    val lista = rememberLazyListState()
    LaunchedEffect(estado.mensagens.size) {
        if (estado.mensagens.isNotEmpty()) lista.animateScrollToItem(estado.mensagens.lastIndex)
    }

    Scaffold(
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Fale com a Ada", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Text("<", color = textoPrimario, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fundoPrincipal)
            )
        },
        bottomBar = {
            CampoMensagem(
                texto = estado.textoDigitado,
                enviando = estado.enviando,
                erro = estado.erroEntrada,
                aoAlterarTexto = aoAlterarTexto,
                aoEnviar = aoEnviar
            )
        }
    ) { espaco ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espaco)
        ) {
            Cabecalho()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = verdeClaro,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = TEXTO_AVISO,
                    modifier = Modifier.padding(12.dp),
                    color = verdeEscuro,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = lista,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.mensagens, key = { it.id }) { mensagem ->
                    LinhaMensagem(mensagem)
                }
            }
        }
    }
}

@Composable
private fun Cabecalho() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.onca),
            contentDescription = "Ada",
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(verdeClaro)
                .border(1.dp, borda, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Olá! Eu sou a Ada.", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Informações educativas em saúde.", color = textoSecundario, fontSize = 13.sp)
        }
    }
}

@Composable
private fun LinhaMensagem(mensagem: MensagemChat) {
    val ehUsuario = mensagem.papel == PapelMensagem.USUARIO
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (ehUsuario) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.86f),
            color = when {
                ehUsuario -> verdePrincipal
                mensagem.tipo == TipoRespostaMedica.URGENCIA -> amareloClaro
                else -> branco
            },
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mensagem.carregando) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = verdePrincipal)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = mensagem.texto,
                    color = if (ehUsuario) branco else textoPrimario,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun CampoMensagem(
    texto: String,
    enviando: Boolean,
    erro: String?,
    aoAlterarTexto: (String) -> Unit,
    aoEnviar: () -> Unit
) {
    val podeEnviar = texto.isNotBlank() && !enviando
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = fundoPrincipal,
        shadowElevation = 4.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = texto,
                    onValueChange = aoAlterarTexto,
                    modifier = Modifier.weight(1f),
                    enabled = !enviando,
                    placeholder = { Text("Digite sua dúvida...") },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (podeEnviar) aoEnviar() }),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = branco,
                        unfocusedContainerColor = branco,
                        disabledContainerColor = branco,
                        focusedIndicatorColor = transparente,
                        unfocusedIndicatorColor = transparente,
                        disabledIndicatorColor = transparente
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = aoEnviar,
                    enabled = podeEnviar,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)
                ) {
                    Text(if (enviando) "..." else "Enviar")
                }
            }
            AnimatedVisibility(erro != null) {
                Text(erro.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }
        }
    }
}
