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
import androidx.compose.ui.graphics.Color
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

private val VerdePrincipalExames = Color(0xFF0C6A3C)
private val VerdeEscuroExames = Color(0xFF064527)
private val VerdeClaroExames = Color(0xFFEAF7F0)
private val FundoExames = Color(0xFFF7FAF8)
private val TextoPrincipalExames = Color(0xFF17211C)
private val TextoSecundarioExames = Color(0xFF61706A)
private val LinhaExames = Color(0xFFE7EEE9)
private val ConversorExames = Json { ignoreUnknownKeys = true }
private val FormatoDataExame = DateTimeFormatter.ofPattern("dd/MM/yyyy")

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

    Scaffold(
        modifier = modificador.fillMaxSize(),
        containerColor = FundoExames,
        bottomBar = {
            BarraInferiorExames(
                aoAbrirInicio = aoVoltarInicio,
                aoNovoExame = { escolherArquivo.launch(arrayOf("image/*", "application/pdf")) }
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
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipalExames)
            ) {
                Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("Enviar novo exame", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                .background(VerdeClaroExames)
        ) {
            Text("<", color = VerdePrincipalExames, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Meus Exames", color = VerdeEscuroExames, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Dona Maria, acompanhe seus resultados.", color = TextoSecundarioExames, fontSize = 11.sp)
        }
        IconButton(
            onClick = aoNovoExame,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(VerdeClaroExames)
        ) {
            Text("^", color = VerdePrincipalExames, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AbasExames() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, LinhaExames)
    ) {
        Row(Modifier.padding(3.dp)) {
            AbaExame("Todos", selecionada = false, Modifier.weight(1f))
            AbaExame("Enviados", selecionada = true, Modifier.weight(1f))
            AbaExame("Resultados", selecionada = false, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AbaExame(texto: String, selecionada: Boolean, modificador: Modifier = Modifier) {
    Surface(
        modifier = modificador.height(34.dp),
        color = if (selecionada) VerdePrincipalExames else Color.Transparent,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                color = if (selecionada) Color.White else TextoPrincipalExames,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CartaoAvisoExames() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.White, VerdeClaroExames)
                    )
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.onca1),
                    contentDescription = "Ada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(VerdeClaroExames)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Mantenha seus exames em dia!",
                        color = TextoPrincipalExames,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Exames enviados ajudam a cuidar melhor da sua saude.",
                        color = TextoSecundarioExames,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(VerdePrincipalExames),
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

@Composable
private fun ListaExamesEnviados(
    exames: List<ExameEnviado>,
    aoSelecionar: (ExameEnviado) -> Unit,
    modificador: Modifier = Modifier
) {
    Surface(
        modifier = modificador
            .fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, LinhaExames)
    ) {
        if (exames.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Nenhum exame enviado", color = TextoPrincipalExames, fontWeight = FontWeight.Bold)
                Text(
                    "Envie uma foto ou PDF para guardar aqui.",
                    color = TextoSecundarioExames,
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
                color = TextoPrincipalExames,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(exame.data, color = TextoSecundarioExames, fontSize = 12.sp)
            Text(
                if (exame.tipoArquivo.startsWith("image/")) "Foto enviada" else "PDF enviado",
                color = VerdePrincipalExames,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
        Text(">", color = TextoPrincipalExames, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(LinhaExames)
    )
}

@Composable
private fun IconeArquivo(tipoArquivo: String) {
    val cor = if (tipoArquivo.startsWith("image/")) Color(0xFFF2A51A) else VerdePrincipalExames
    Surface(
        modifier = Modifier.size(38.dp),
        color = Color.Transparent,
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
                Text("Salvar", color = VerdePrincipalExames, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) {
                Text("Cancelar", color = TextoSecundarioExames)
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
        containerColor = FundoExames,
        bottomBar = { BarraInferiorExames(aoAbrirInicio = aoVoltar, aoNovoExame = {}) }
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
                        .background(VerdeClaroExames)
                ) {
                    Text("<", color = VerdePrincipalExames, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Resultado do exame", color = VerdeEscuroExames, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(exame.data, color = TextoSecundarioExames, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 1.dp
            ) {
                Column(Modifier.padding(18.dp)) {
                    IconeArquivo(tipoArquivo = exame.tipoArquivo)
                    Spacer(Modifier.height(14.dp))
                    Text(exame.nome, color = TextoPrincipalExames, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Arquivo: ${exame.nomeArquivo}", color = TextoSecundarioExames, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    Text("Data: ${exame.data}", color = TextoSecundarioExames, fontSize = 12.sp)
                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = { abrirArquivoExame(contexto, exame) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipalExames)
                    ) {
                        Text("Abrir resultado", color = Color.White, fontWeight = FontWeight.Bold)
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
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                color = VerdeClaroExames,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("?", color = VerdePrincipalExames, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Precisa de ajuda?", color = VerdePrincipalExames, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Nossa equipe esta pronta para te ajudar.", color = TextoSecundarioExames, fontSize = 11.sp)
            }
            Text(">", color = TextoPrincipalExames, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BarraInferiorExames(
    aoAbrirInicio: () -> Unit,
    aoNovoExame: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemMenuExames("Inicio", R.drawable.house_building, false, aoAbrirInicio)
            ItemMenuExames("Monitorar", R.drawable.heart_green, false, {})
            FloatingActionButton(
                onClick = aoNovoExame,
                containerColor = VerdePrincipalExames,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light)
            }
            ItemMenuExames("Exames", R.drawable.list, true, {})
            ItemMenuExames("Perfil", R.drawable.user, false, {})
        }
    }
}

@Composable
private fun ItemMenuExames(
    titulo: String,
    icone: Int,
    selecionado: Boolean,
    aoClicar: () -> Unit
) {
    val corItem = if (selecionado) VerdePrincipalExames else TextoSecundarioExames
    Column(
        modifier = Modifier
            .width(58.dp)
            .clickable(onClick = aoClicar),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(icone),
            contentDescription = titulo,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(22.dp)
                .clip(if (titulo == "Perfil") CircleShape else RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(2.dp))
        Text(
            titulo,
            color = corItem,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
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
