package home_page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
class RepositorioExames(
    contexto: Context
) {
    private val preferencias = contexto.getSharedPreferences("exames_enviados", Context.MODE_PRIVATE)

    fun listar(): List<ExameEnviado> {
        val textoSalvo = preferencias.getString(CHAVE_LISTA_EXAMES, null) ?: return emptyList()
        return runCatching {
            ConversorExames.decodeFromString(ListSerializer(ExameEnviado.serializer()), textoSalvo)
        }.getOrDefault(emptyList())
    }

    fun salvar(exame: ExameEnviado): List<ExameEnviado> {
        val atualizados = (listOf(exame) + listar()).distinctBy { it.codigo }
        preferencias.edit()
            .putString(CHAVE_LISTA_EXAMES, ConversorExames.encodeToString(atualizados))
            .apply()
        return atualizados
    }

    companion object {
        private const val CHAVE_LISTA_EXAMES = "lista"
    }
}

// função principal que controla o funcionamento da tela
@Composable
fun TelaExames(
    aoVoltarInicio: () -> Unit,
    modificador: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioExames(contexto) }
    var exames by remember { mutableStateOf(repositorio.listar()) }
    var exameSelecionado by remember { mutableStateOf<ExameEnviado?>(null) }
    var arquivoEscolhido by remember { mutableStateOf<Uri?>(null) }
    var tipoArquivoEscolhido by remember { mutableStateOf("") }
    var nomeArquivoEscolhido by remember { mutableStateOf("") }
    var nomeDigitado by remember { mutableStateOf("") }
    var dataDigitada by remember { mutableStateOf(LocalDate.now().format(FormatoDataExame)) }

    // permite dar upload no arquivo (imagem ou pdf)
    val escolherArquivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { enderecoArquivo ->
        if (enderecoArquivo != null) {
            runCatching {
                contexto.contentResolver.takePersistableUriPermission(
                    enderecoArquivo,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            arquivoEscolhido = enderecoArquivo
            tipoArquivoEscolhido = contexto.contentResolver.getType(enderecoArquivo).orEmpty()
            nomeArquivoEscolhido = nomeArquivo(contexto, enderecoArquivo)
            nomeDigitado = nomeArquivoEscolhido.substringBeforeLast('.', nomeArquivoEscolhido)
                .ifBlank { "Novo exame" }
            dataDigitada = LocalDate.now().format(FormatoDataExame)
        }
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
                aoAdicionar = { escolherArquivo.launch(arrayOf("image/*", "application/pdf")) }
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
                aoVoltarInicio = aoVoltarInicio,
                aoNovoExame = { escolherArquivo.launch(arrayOf("image/*", "application/pdf")) }
            )
            AbasExames()
            Spacer(Modifier.height(14.dp))
            CartaoAvisoExames()
            Spacer(Modifier.height(10.dp))
            ListaExamesEnviados(
                exames = exames,
                aoSelecionar = { exameSelecionado = it },
                modificador = Modifier.weight(1f)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { escolherArquivo.launch(arrayOf("image/*", "application/pdf")) },
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
                Text("Enviar novo exame", color = branco, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            CartaoAjudaExames()
        }
    }


    if (arquivoEscolhido != null) {
        DialogoSalvarExame(
            nome = nomeDigitado,
            data = dataDigitada,
            aoMudarNome = { nomeDigitado = it },
            aoMudarData = { dataDigitada = it },
            aoCancelar = { arquivoEscolhido = null },
            aoSalvar = {
                val enderecoArquivo = arquivoEscolhido ?: return@DialogoSalvarExame
                val codigo = UUID.randomUUID().toString()
                val caminhoCopia = salvarCopiaExame(
                    contexto = contexto,
                    enderecoArquivo = enderecoArquivo,
                    codigo = codigo,
                    nomeArquivo = nomeArquivoEscolhido
                )
                val exame = ExameEnviado(
                    codigo = codigo,
                    nome = nomeDigitado.ifBlank { "Exame sem nome" },
                    data = dataDigitada.ifBlank { LocalDate.now().format(FormatoDataExame) },
                    enderecoOriginal = enderecoArquivo.toString(),
                    tipoArquivo = tipoArquivoEscolhido.ifBlank { "application/octet-stream" },
                    nomeArquivo = nomeArquivoEscolhido.ifBlank { "arquivo" },
                    caminhoCopia = caminhoCopia
                )
                exames = repositorio.salvar(exame)
                arquivoEscolhido = null
            }
        )
    }
}

// parte de cima da tela, alterar "dona maria" para o nome usuario que estiver no banco de dados
@Composable
private fun CabecalhoExames(
    aoVoltarInicio: () -> Unit,
    aoNovoExame: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = aoVoltarInicio,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(verdeClaro)
        ) {
            Text("<", color = verdePrincipal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Meus Exames", color = verdeEscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Dona Maria, acompanhe seus resultados.", color = textoSecundario, fontSize = 11.sp)
        }
        IconButton(
            onClick = aoNovoExame,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(verdeClaro)
        ) {
            Text("^", color = verdePrincipal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
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
                fontSize = 12.sp
            )
        }
    }
}

// avisos da tela
@Composable
private fun CartaoAvisoExames() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = branco,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(branco, verdeClaro)
                    )
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.onca_joia),
                    contentDescription = "Ada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(verdeClaro)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Mantenha seus exames em dia!",
                        color = textoPrimario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Ada ajuda você ter acesso rápido ao seus exames para monitorar a saúde.",
                        color = textoSecundario,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(verdePrincipal),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.heart_green),
                        contentDescription = "Saude",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

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
                    fontSize = 12.sp,
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
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconeArquivo(tipoArquivo = exame.tipoArquivo)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                exame.nome,
                color = textoPrimario,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(exame.data, color = textoSecundario, fontSize = 12.sp)
            Text(
                if (exame.tipoArquivo.startsWith("image/")) "Foto enviada" else "PDF enviado",
                color = verdePrincipal,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
        Text(">", color = textoPrimario, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                fontSize = 10.sp
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
                IconButton(
                    onClick = aoVoltar,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(verdeClaro)
                ) {
                    Text("<", color = verdePrincipal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Resultado do exame", color = verdeEscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(exame.data, color = textoSecundario, fontSize = 12.sp)
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
                    Text(exame.nome, color = textoPrimario, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Arquivo: ${exame.nomeArquivo}", color = textoSecundario, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    Text("Data: ${exame.data}", color = textoSecundario, fontSize = 12.sp)
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
            .height(62.dp),
        color = branco,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
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
            Column(Modifier.weight(1f)) {
                Text("Precisa de ajuda?", color = verdePrincipal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Nossa equipe esta pronta para te ajudar.", color = textoSecundario, fontSize = 11.sp)
            }
            Text(">", color = textoPrimario, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
