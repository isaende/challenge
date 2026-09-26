package home_page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch

private val ConversorExames = Json { ignoreUnknownKeys = true }
private val FormatoDataExame = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// guarda informações do exame enviado, como um código único, nome, data, etc...
@Serializable
data class ExameEnviado(
    val codigo: String,
    val nome: String,
    val data: String,
    val enderecoOriginal: String,
    val tipoArquivo: String,
    val nomeArquivo: String,
    val caminhoCopia: String = ""
)

// usa uma memória simples do Android para salvar e ler a lista de exames, futuramente aplicar o branco de dados
// função principal que controla o funcionamento da tela
@Composable
fun TelaExames(
    aoVoltarInicio: () -> Unit,
    perfilId: Long,
    modificador: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    var salvandoExame by remember { mutableStateOf(false) }
    var exames by remember { mutableStateOf(emptyList<ExameEnviado>()) }
    var exameSelecionado by remember { mutableStateOf<ExameEnviado?>(null) }
    var arquivoEscolhido by remember { mutableStateOf<Uri?>(null) }
    var tipoArquivoEscolhido by remember { mutableStateOf("") }
    var nomeArquivoEscolhido by remember { mutableStateOf("") }
    var nomeDigitado by remember { mutableStateOf("") }
    var dataDigitada by remember { mutableStateOf(LocalDate.now().format(FormatoDataExame)) }
    var mostrarOpcoesEnvio by remember { mutableStateOf(false) }

    LaunchedEffect(perfilId) {
        exames = runCatching { ApiAda.listarExames(perfilId).map { exame -> exame.paraTela().copy(caminhoCopia=runCatching { ApiAda.baixarExame(contexto,perfilId,exame) }.getOrDefault("")) } }
            .onFailure { android.widget.Toast.makeText(contexto, "Não foi possível carregar os exames.", android.widget.Toast.LENGTH_LONG).show() }
            .getOrDefault(emptyList())
    }

    fun prepararArquivo(enderecoArquivo: Uri, manterPermissao: Boolean) {
        if (manterPermissao) {
            runCatching {
                contexto.contentResolver.takePersistableUriPermission(
                    enderecoArquivo,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        arquivoEscolhido = enderecoArquivo
        tipoArquivoEscolhido = contexto.contentResolver.getType(enderecoArquivo).orEmpty()
        nomeArquivoEscolhido = nomeArquivo(contexto, enderecoArquivo)
        nomeDigitado = nomeArquivoEscolhido.substringBeforeLast('.', nomeArquivoEscolhido)
            .ifBlank { "Novo exame" }
        dataDigitada = LocalDate.now().format(FormatoDataExame)
    }

    val escolherFotoGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { enderecoArquivo ->
        if (enderecoArquivo != null) prepararArquivo(enderecoArquivo, manterPermissao = false)
    }

    val escolherPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { enderecoArquivo ->
        if (enderecoArquivo != null) prepararArquivo(enderecoArquivo, manterPermissao = true)
    }

    if (exameSelecionado != null) {
        TelaDetalheExame(
            exame = exameSelecionado ?: return,
            aoVoltar = { exameSelecionado = null }
        )
        return
    }

    // organiza aonde deve ficar cada um dos componentes da tela e cores
    Scaffold(
        modifier = modificador.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.EXAMES,
                aoAbrirInicio = aoVoltarInicio,
                aoAdicionar = { mostrarOpcoesEnvio = true }
            )
        }
    ) { espaco ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espaco)
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            CabecalhoExames(
                aoVoltarInicio = aoVoltarInicio
            )
            Spacer(Modifier.height(4.dp))
            AbasExames()
            Spacer(Modifier.height(16.dp))
            CartaoAvisoExames()
            Spacer(Modifier.height(14.dp))
            ListaExamesEnviados(
                exames = exames,
                aoSelecionar = { exameSelecionado = it },
                modificador = Modifier.weight(1f)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { mostrarOpcoesEnvio = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(2.dp, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)
            ) {
                Text("+", color = branco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("Enviar novo exame", color = branco, style = TipografiaAplicativo.labelLarge)
            }
            Spacer(Modifier.height(10.dp))
            CartaoAjudaExames()
        }
    }

    if (mostrarOpcoesEnvio) {
        AlertDialog(
            onDismissRequest = { mostrarOpcoesEnvio = false },
            title = { Text("Enviar novo exame") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Escolha de onde deseja enviar o exame.")
                    Button(
                        onClick = {
                            mostrarOpcoesEnvio = false
                            escolherFotoGaleria.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Foto da galeria")
                    }
                    Button(
                        onClick = {
                            mostrarOpcoesEnvio = false
                            escolherPdf.launch(arrayOf("application/pdf"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Arquivo PDF")
                    }
                    Text(
                        "Use a seta de voltar do seletor para cancelar e retornar ao aplicativo.",
                        color = textoSecundario,
                        style = TipografiaAplicativo.bodySmall
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarOpcoesEnvio = false }) { Text("Cancelar") }
            }
        )
    }


    if (arquivoEscolhido != null) {
        DialogoSalvarExame(
            nome = nomeDigitado,
            data = dataDigitada,
            aoMudarNome = { nomeDigitado = it },
            aoMudarData = { dataDigitada = it },
            aoCancelar = { if (!salvandoExame) arquivoEscolhido = null },
            aoSalvar = {
                if (salvandoExame) return@DialogoSalvarExame
                val enderecoArquivo = arquivoEscolhido ?: return@DialogoSalvarExame
                val codigo = UUID.randomUUID().toString()
                val caminhoCopia = salvarCopiaExame(
                    contexto = contexto,
                    enderecoArquivo = enderecoArquivo,
                    codigo = codigo,
                    nomeArquivo = nomeArquivoEscolhido
                )
                salvandoExame = true
                escopo.launch {
                    val dataIso=runCatching { LocalDate.parse(dataDigitada,FormatoDataExame).toString() }.getOrDefault(LocalDate.now().toString())
                    runCatching { ApiAda.enviarExame(contexto,perfilId,nomeDigitado.ifBlank { "Exame sem nome" },dataIso,tipoArquivoEscolhido.ifBlank { "application/octet-stream" },nomeArquivoEscolhido.ifBlank { "arquivo" },enderecoArquivo) }
                        .onSuccess { salvo -> exames=listOf(salvo.paraTela().copy(enderecoOriginal=enderecoArquivo.toString(),caminhoCopia=caminhoCopia))+exames; arquivoEscolhido = null }
                        .onFailure { android.widget.Toast.makeText(contexto, "Não foi possível salvar o exame. Tente novamente.", android.widget.Toast.LENGTH_LONG).show() }
                    salvandoExame = false
                }
            }
        )
    }
}

// parte de cima da tela
@Composable
private fun CabecalhoExames(
    aoVoltarInicio: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotaoSetaAplicativo(aoClicar = aoVoltarInicio, descricao = "Voltar ao início")
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Meus Exames", color = verdeEscuro, style = TipografiaAplicativo.titleLarge)
            Text("Acompanhe seus resultados.", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
        }
        Spacer(Modifier.size(48.dp))
    }
}


@Composable
private fun AbasExames() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = branco,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borda)
    ) {
        Row(Modifier.padding(3.dp)) {
            AbaExame("Enviados", selecionada = true, Modifier.weight(1f))
        }
    }
}


@Composable
private fun AbaExame(texto: String, selecionada: Boolean, modificador: Modifier = Modifier) {
    Surface(
        modifier = modificador.height(34.dp),
        color = if (selecionada) verdePrincipal else transparente,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                color = if (selecionada) branco else textoPrimario,
                fontWeight = FontWeight.Bold,
                style = TipografiaAplicativo.labelMedium
            )
        }
    }
}

// avisos da tela
@Composable
private fun CartaoAvisoExames() {
    BalaoDicaAda(
        titulo = "Mantenha seus exames em dia!",
        texto = "Ada ajuda você a ter acesso rápido aos seus exames para monitorar a saúde."
    )
}

internal fun ExameApi.paraTela()=ExameEnviado(
    codigo=id.toString(),
    nome=nomeExame,
    data=runCatching { LocalDate.parse(dataExame).format(FormatoDataExame) }.getOrDefault(dataExame),
    enderecoOriginal="",
    tipoArquivo=tipoMime,
    nomeArquivo=nomeArquivo
)

// lista com todos exames enviados
@Composable
private fun ListaExamesEnviados(
    exames: List<ExameEnviado>,
    aoSelecionar: (ExameEnviado) -> Unit,
    modificador: Modifier = Modifier
) {
    Surface(
        modifier = modificador
            .fillMaxWidth(),
        color = branco,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borda)
    ) {
        if (exames.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Nenhum exame enviado", color = textoPrimario, fontWeight = FontWeight.Bold)
                Text(
                    "Envie uma foto ou PDF para guardar aqui.",
                    color = textoSecundario,
                    style = TipografiaAplicativo.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(exames, key = { it.codigo }) { exame ->
                    LinhaExame(exame = exame, aoSelecionar = { aoSelecionar(exame) })
                }
            }
        }
    }
}

@Composable
private fun LinhaExame(
    exame: ExameEnviado,
    aoSelecionar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = aoSelecionar)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconeArquivo(tipoArquivo = exame.tipoArquivo)
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                exame.nome,
                color = textoPrimario,
                fontWeight = FontWeight.Bold,
                style = TipografiaAplicativo.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(exame.data, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            Text(
                if (exame.tipoArquivo.startsWith("image/")) "Foto enviada" else "PDF enviado",
                color = verdePrincipal,
                fontWeight = FontWeight.Bold,
                style = TipografiaAplicativo.labelSmall
            )
        }
        SetaAplicativo()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(borda)
    )
}

@Composable
private fun IconeArquivo(tipoArquivo: String) {
    val cor = if (tipoArquivo.startsWith("image/")) amareloPrincipal else verdePrincipal
    Surface(
        modifier = Modifier.size(38.dp),
        color = transparente,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, cor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                if (tipoArquivo.startsWith("image/")) "IMG" else "PDF",
                color = cor,
                fontWeight = FontWeight.Bold,
                style = TipografiaAplicativo.labelSmall
            )
        }
    }
}

// tela para escolher o nome ao enviar o exame
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoSalvarExame(
    nome: String,
    data: String,
    aoMudarNome: (String) -> Unit,
    aoMudarData: (String) -> Unit,
    aoCancelar: () -> Unit,
    aoSalvar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text("Salvar exame") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = aoMudarNome,
                    label = { Text("Nome do exame") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = data,
                    onValueChange = aoMudarData,
                    label = { Text("Data do exame") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = aoSalvar) {
                Text("Salvar", color = verdePrincipal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) {
                Text("Cancelar", color = textoSecundario)
            }
        }
    )
}

@Composable
private fun TelaDetalheExame(
    exame: ExameEnviado,
    aoVoltar: () -> Unit
) {
    val contexto = LocalContext.current

    Scaffold(
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.EXAMES,
                aoAbrirInicio = aoVoltar
            )
        }
    ) { espaco ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espaco)
                .statusBarsPadding()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotaoSetaAplicativo(aoClicar = aoVoltar)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Resultado do exame", color = verdeEscuro, style = TipografiaAplicativo.titleLarge)
                    Text(exame.data, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = branco,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 1.dp
            ) {
                Column(Modifier.padding(18.dp)) {
                    IconeArquivo(tipoArquivo = exame.tipoArquivo)
                    Spacer(Modifier.height(14.dp))
                    Text(exame.nome, color = textoPrimario, style = TipografiaAplicativo.titleLarge)
                    Text("Arquivo: ${exame.nomeArquivo}", color = textoSecundario, style = TipografiaAplicativo.bodySmall, modifier = Modifier.padding(top = 6.dp))
                    Text("Data: ${exame.data}", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = { abrirArquivoExame(contexto, exame) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)
                    ) {
                        Text("Abrir resultado", color = branco, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoAjudaExames() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        color = branco,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                color = verdeClaro,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("?", color = verdePrincipal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("Precisa de ajuda?", color = verdePrincipal, style = TipografiaAplicativo.titleSmall)
                Text("Nossa equipe esta pronta para te ajudar.", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            }
            SetaAplicativo()
        }
    }
}

private fun nomeArquivo(contexto: Context, enderecoArquivo: Uri): String {
    val consulta = contexto.contentResolver.query(enderecoArquivo, null, null, null, null)
    consulta?.use {
        val indice = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (indice >= 0 && it.moveToFirst()) {
            return it.getString(indice).orEmpty()
        }
    }
    return enderecoArquivo.lastPathSegment.orEmpty()
}

private fun abrirArquivoExame(contexto: Context, exame: ExameEnviado) {
    val arquivo = exame.caminhoCopia.takeIf { it.isNotBlank() }?.let { File(it) }
    val enderecoArquivo = if (arquivo != null && arquivo.exists()) {
        FileProvider.getUriForFile(
            contexto,
            "${contexto.packageName}.arquivos",
            arquivo
        )
    } else {
        Uri.parse(exame.enderecoOriginal)
    }
    val intencao = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(enderecoArquivo, exame.tipoArquivo)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        contexto.startActivity(Intent.createChooser(intencao, "Abrir exame"))
    }
}

private fun salvarCopiaExame(
    contexto: Context,
    enderecoArquivo: Uri,
    codigo: String,
    nomeArquivo: String
): String {
    return runCatching {
        val diretorio = File(contexto.filesDir, "exames").apply { mkdirs() }
        val extensao = nomeArquivo.substringAfterLast('.', "")
            .takeIf { it.isNotBlank() && it.length <= 8 }
            ?.let { ".$it" }
            ?: ""
        val destino = File(diretorio, "$codigo$extensao")

        contexto.contentResolver.openInputStream(enderecoArquivo)?.use { entrada ->
            destino.outputStream().use { saida ->
                entrada.copyTo(saida)
            }
        }
        destino.absolutePath
    }.getOrDefault("")
}
