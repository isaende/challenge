package chat_medico

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import home_page.ExameEnviado
import home_page.fundoPrincipal
import home_page.ApiAda
import home_page.TipografiaAplicativo
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

const val URL_BASE_OLLAMA = "http://10.0.2.2:11434"
private const val CAMINHO_CHAT_OLLAMA = "/api/chat"
private const val MODELO_OLLAMA = "medgemma1.5:4b"
private const val LIMITE_MENSAGEM = 2000
private const val LIMITE_IMAGEM_BYTES = 10 * 1024 * 1024
private const val TEXTO_AVISO = "Este chat fornece informações educativas e não substitui avaliação de um profissional de saúde."
private const val TEXTO_INICIAL = "Olá! Posso ajudar com explicações gerais sobre saúde e exames. Qual a sua dúvida?"
private const val TEXTO_ERRO = "Não foi possível responder agora. Tente novamente."
private const val TEXTO_ERRO_TAMANHO = "Mensagem muito longa. Resuma sua dúvida."
private const val TEXTO_CARREGANDO = "Ada está preparando uma resposta educativa..."
private const val TEXTO_ANALISAR_EXAME = "Explique os dados visíveis neste exame em linguagem simples."
private const val TEXTO_ERRO_IMAGEM = "Não foi possível carregar esta imagem. Escolha um arquivo de até 10 MB."
private val ConversorResposta = Json { ignoreUnknownKeys = true }

enum class PapelMensagem { USUARIO, ASSISTENTE }
enum class TipoRespostaMedica { EDUCATIVO, URGENCIA, RECUSA_SEGURA, ERRO }

data class MensagemChat(
    val id: String = UUID.randomUUID().toString(),
    val texto: String,
    val papel: PapelMensagem,
    val tipo: TipoRespostaMedica = TipoRespostaMedica.EDUCATIVO,
    val carregando: Boolean = false,
    val nomeAnexo: String? = null
)

data class AnexoExame(
    val nome: String,
    val imagemBase64: String
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
    val anexo: AnexoExame? = null,
    val enviando: Boolean = false,
    val erroEntrada: String? = null
)

@Serializable
private data class PedidoOllama(
    val model: String,
    val stream: Boolean,
    val format: String,
    val messages: List<MensagemOllama>,
    val options: OpcoesOllama
)

@Serializable
private data class MensagemOllama(
    val role: String,
    val content: String,
    val images: List<String>? = null
)

@Serializable
private data class OpcoesOllama(
    val temperature: Double,
    @kotlinx.serialization.SerialName("num_predict") val numPredict: Int,
    @kotlinx.serialization.SerialName("repeat_penalty") val repeatPenalty: Double
)

@Serializable
private data class RespostaOllama(val message: MensagemOllama? = null)

@Serializable
private data class ConteudoResposta(val resposta: String)

interface RepositorioChatMedico {
    suspend fun enviarMensagem(mensagem: String, imagemBase64: String? = null): RespostaChatMedico
}

class RepositorioChatMedicoOllama(
    private val urlBase: String = URL_BASE_OLLAMA,
    private val cliente: HttpClient = criarClienteHttp()
) : RepositorioChatMedico {
    override suspend fun enviarMensagem(mensagem: String, imagemBase64: String?): RespostaChatMedico {
        val resposta = cliente.post(urlBase.trimEnd('/') + CAMINHO_CHAT_OLLAMA) {
            contentType(ContentType.Application.Json)
            setBody(
                PedidoOllama(
                    model = MODELO_OLLAMA,
                    stream = false,
                    format = "json",
                    messages = listOf(
                        MensagemOllama("system", PROMPT_MEDICO),
                        MensagemOllama(
                            role = "user",
                            content = mensagem,
                            images = imagemBase64?.let(::listOf)
                        )
                    ),
                    options = OpcoesOllama(
                        temperature = 0.1,
                        numPredict = 300,
                        repeatPenalty = 1.2
                    )
                )
            )
        }.body<RespostaOllama>()

        val texto = resposta.message?.content.orEmpty().somenteRespostaFinal()

        return RespostaChatMedico(
            texto = texto.ifBlank { TEXTO_ERRO },
            tipo = if (texto.isBlank()) TipoRespostaMedica.ERRO else TipoRespostaMedica.EDUCATIVO
        )
    }
}

fun criarRepositorioChatMedico(urlBase: String = URL_BASE_OLLAMA): RepositorioChatMedico {
    return RepositorioChatMedicoOllama(urlBase = urlBase)
}

private fun String.somenteRespostaFinal(): String {
    val conteudoLimpo = substringAfterLast("<unused95>", this)
        .replace("<unused\\d+>".toRegex(), "")
        .trim()
    return runCatching {
        ConversorResposta.decodeFromString<ConteudoResposta>(conteudoLimpo).resposta
    }.getOrDefault(conteudoLimpo)
}

private const val PROMPT_MEDICO = """
Voce e Ada, uma assistente de educacao em saude.

Responda em portugues do Brasil, de forma clara e objetiva. Forneca apenas informacoes gerais e educativas.
Nao realize diagnosticos, nao prescreva medicamentos, nao indique doses e nao oriente mudancas de tratamento.
Quando houver sinais de emergencia, recomende atendimento imediato. Em temas de saude, deixe claro que a resposta
nao substitui a avaliacao de um profissional de saude.
Ao receber a imagem de um exame, descreva somente o que estiver legivel, explique termos e valores de referencia
sem concluir diagnosticos e avise quando a qualidade da imagem impedir uma leitura confiavel.
Nao mostre raciocinio interno e nao repita frases. Responda com no maximo 120 palavras, somente como JSON valido
no formato {"resposta":"texto final para o usuario"}.
"""

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

    fun anexarExame(anexo: AnexoExame) {
        _estado.update { it.copy(anexo = anexo, erroEntrada = null) }
    }

    fun removerAnexo() {
        _estado.update { it.copy(anexo = null) }
    }

    fun informarErroImagem() {
        _estado.update { it.copy(erroEntrada = TEXTO_ERRO_IMAGEM) }
    }

    fun enviarMensagem() {
        val estadoAtual = _estado.value
        val textoDigitado = estadoAtual.textoDigitado.trim()
        val anexo = estadoAtual.anexo
        if (estadoAtual.enviando || (textoDigitado.isBlank() && anexo == null)) return
        val texto = textoDigitado.ifBlank { TEXTO_ANALISAR_EXAME }
        if (texto.length > LIMITE_MENSAGEM) {
            _estado.update { it.copy(erroEntrada = TEXTO_ERRO_TAMANHO) }
            return
        }

        val idCarregando = UUID.randomUUID().toString()
        _estado.update {
            it.copy(
                mensagens = it.mensagens +
                    MensagemChat(
                        texto = texto,
                        papel = PapelMensagem.USUARIO,
                        nomeAnexo = anexo?.nome
                    ) +
                    MensagemChat(id = idCarregando, texto = TEXTO_CARREGANDO, papel = PapelMensagem.ASSISTENTE, carregando = true),
                textoDigitado = "",
                anexo = null,
                enviando = true,
                erroEntrada = null
            )
        }

        viewModelScope.launch {
            try {
                val resposta = repositorio.enviarMensagem(texto, anexo?.imagemBase64)
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
    perfilId: Long = 0,
    aoVoltar: () -> Unit = {}
) {
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    var examesSalvos by remember { mutableStateOf(emptyList<ExameEnviado>()) }
    var mostrarOpcoesAnexo by remember { mutableStateOf(false) }
    var mostrarExamesSalvos by remember { mutableStateOf(false) }
    val modelo: ModeloVisaoChatMedico = viewModel(factory = FabricaModeloVisao(repositorio))
    val estado by modelo.estado.collectAsStateWithLifecycle()

    fun carregarExame(nome: String, abrirDados: () -> ByteArray?) {
        escopo.launch {
            val anexo = withContext(Dispatchers.IO) {
                abrirDados()?.takeIf { it.isNotEmpty() && it.size <= LIMITE_IMAGEM_BYTES }?.let { dados ->
                    AnexoExame(nome, Base64.encodeToString(dados, Base64.NO_WRAP))
                }
            }
            if (anexo != null) modelo.anexarExame(anexo) else modelo.informarErroImagem()
        }
    }

    val escolherImagem = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { endereco ->
        if (endereco != null) {
            val nome = nomeArquivo(contexto, endereco)
            carregarExame(nome) {
                contexto.contentResolver.openInputStream(endereco)?.use(::lerImagemLimitada)
            }
        }
    }

    ConteudoChatMedico(
        estado = estado,
        aoAlterarTexto = modelo::alterarTextoDigitado,
        aoEnviar = modelo::enviarMensagem,
        aoAnexar = { mostrarOpcoesAnexo = true },
        aoRemoverAnexo = modelo::removerAnexo,
        aoVoltar = aoVoltar
    )

    if (mostrarOpcoesAnexo) {
        DialogoOpcoesExame(
            aoFechar = { mostrarOpcoesAnexo = false },
            aoEnviarImagem = {
                mostrarOpcoesAnexo = false
                escolherImagem.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            aoEscolherSalvo = {
                mostrarOpcoesAnexo = false
                escopo.launch {
                    examesSalvos = runCatching {
                        ApiAda.listarExames(perfilId).map { exame ->
                            ExameEnviado(
                                codigo = exame.id.toString(), nome = exame.nomeExame,
                                data = exame.dataExame, enderecoOriginal = "",
                                tipoArquivo = exame.tipoMime, nomeArquivo = exame.nomeArquivo,
                                caminhoCopia = ApiAda.baixarExame(contexto, perfilId, exame)
                            )
                        }
                    }.getOrDefault(emptyList())
                    mostrarExamesSalvos = true
                }
            }
        )
    }

    if (mostrarExamesSalvos) {
        DialogoExamesSalvos(
            exames = examesSalvos.filter { it.tipoArquivo.startsWith("image/") },
            aoFechar = { mostrarExamesSalvos = false },
            aoEscolher = { exame ->
                mostrarExamesSalvos = false
                carregarExame(exame.nome) { lerDadosExame(contexto, exame) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConteudoChatMedico(
    estado: EstadoChatMedico,
    aoAlterarTexto: (String) -> Unit,
    aoEnviar: () -> Unit,
    aoAnexar: () -> Unit,
    aoRemoverAnexo: () -> Unit,
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
                title = { Text("Fale com a Ada", style = TipografiaAplicativo.titleLarge) },
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
                anexo = estado.anexo,
                aoAlterarTexto = aoAlterarTexto,
                aoEnviar = aoEnviar,
                aoAnexar = aoAnexar,
                aoRemoverAnexo = aoRemoverAnexo
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
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = TEXTO_AVISO,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    color = verdeEscuro,
                    style = TipografiaAplicativo.bodyMedium
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Olá! Eu sou a Ada.", style = TipografiaAplicativo.titleLarge)
            Text("Informações educativas em saúde.", color = textoSecundario, style = TipografiaAplicativo.bodyMedium)
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
            Column(modifier = Modifier.padding(14.dp)) {
                mensagem.nomeAnexo?.let { nome ->
                    Text(
                        text = "Imagem anexada: $nome",
                        color = if (ehUsuario) branco else verdeEscuro,
                        style = TipografiaAplicativo.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mensagem.carregando) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = verdePrincipal)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = mensagem.texto,
                        color = if (ehUsuario) branco else textoPrimario,
                        style = TipografiaAplicativo.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoMensagem(
    texto: String,
    enviando: Boolean,
    erro: String?,
    anexo: AnexoExame?,
    aoAlterarTexto: (String) -> Unit,
    aoEnviar: () -> Unit,
    aoAnexar: () -> Unit,
    aoRemoverAnexo: () -> Unit
) {
    val podeEnviar = (texto.isNotBlank() || anexo != null) && !enviando
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = fundoPrincipal,
        shadowElevation = 4.dp
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            anexo?.let {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = verdeClaro,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Exame: ${it.nome}", modifier = Modifier.weight(1f), color = verdeEscuro, style = TipografiaAplicativo.bodyMedium)
                        TextButton(onClick = aoRemoverAnexo, enabled = !enviando) {
                            Text("Remover", color = verdeEscuro)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = aoAnexar, enabled = !enviando) {
                    Text("+", color = verdePrincipal, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                TextField(
                    value = texto,
                    onValueChange = aoAlterarTexto,
                    modifier = Modifier.weight(1f),
                    enabled = !enviando,
                    placeholder = { Text("Digite sua dúvida ou anexe um exame...", style = TipografiaAplicativo.bodyMedium) },
                    textStyle = TipografiaAplicativo.bodyMedium,
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
                    Text(if (enviando) "..." else "Enviar", style = TipografiaAplicativo.labelLarge)
                }
            }
            AnimatedVisibility(erro != null) {
                Text(erro.orEmpty(), color = MaterialTheme.colorScheme.error, style = TipografiaAplicativo.bodySmall, modifier = Modifier.padding(start = 8.dp, top = 6.dp))
            }
        }
    }
}

@Composable
private fun DialogoOpcoesExame(
    aoFechar: () -> Unit,
    aoEnviarImagem: () -> Unit,
    aoEscolherSalvo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Anexar exame") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Escolha uma foto da galeria ou use um exame que já está salvo no aplicativo.", style = TipografiaAplicativo.bodyMedium)
                Button(onClick = aoEnviarImagem, modifier = Modifier.fillMaxWidth()) {
                    Text("Foto da galeria")
                }
                Button(onClick = aoEscolherSalvo, modifier = Modifier.fillMaxWidth()) {
                    Text("Escolher exame salvo")
                }
                Text("Nesta versão, o chat lê imagens. Arquivos PDF continuam disponíveis na tela de exames.", style = TipografiaAplicativo.bodySmall, color = textoSecundario)
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoExamesSalvos(
    exames: List<ExameEnviado>,
    aoFechar: () -> Unit,
    aoEscolher: (ExameEnviado) -> Unit
) {
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Exames salvos") },
        text = {
            if (exames.isEmpty()) {
                Text("Nenhum exame foi salvo ainda.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exames, key = { it.codigo }) { exame ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { aoEscolher(exame) },
                            color = verdeClaro,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(exame.nome, color = verdeEscuro, style = TipografiaAplicativo.titleSmall)
                                Text(exame.data, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = aoFechar) { Text("Fechar") } }
    )
}

private fun nomeArquivo(contexto: Context, endereco: Uri): String {
    return contexto.contentResolver.query(endereco, null, null, null, null)?.use { cursor ->
        val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (indice >= 0 && cursor.moveToFirst()) cursor.getString(indice) else null
    } ?: endereco.lastPathSegment ?: "imagem do exame"
}

private fun lerDadosExame(contexto: Context, exame: ExameEnviado): ByteArray? {
    return runCatching {
        val copia = exame.caminhoCopia.takeIf { it.isNotBlank() }?.let(::File)
        if (copia != null && copia.exists()) {
            copia.inputStream().use(::lerImagemLimitada)
        } else {
            contexto.contentResolver.openInputStream(Uri.parse(exame.enderecoOriginal))?.use(::lerImagemLimitada)
        }
    }.getOrNull()
}

private fun lerImagemLimitada(entrada: InputStream): ByteArray? {
    val saida = ByteArrayOutputStream()
    val bloco = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val quantidade = entrada.read(bloco)
        if (quantidade < 0) break
        total += quantidade
        if (total > LIMITE_IMAGEM_BYTES) return null
        saida.write(bloco, 0, quantidade)
    }
    return saida.toByteArray()
}
