package home_page

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val ConversorMedicamentos = Json { ignoreUnknownKeys = true }
private val FormatoHoraMedicamento = DateTimeFormatter.ofPattern("HH:mm")
private const val CANAL_LEMBRETES_MEDICAMENTOS = "lembretes_medicamentos"

@Serializable
private data class MedicamentoSalvo(
    val codigo: String,
    val nome: String,
    val dosagem: String,
    val horario: String,
    val alarmeAtivo: Boolean,
    val tomadoEm: String = ""
)

private class RepositorioMedicamentos(contexto: Context) {
    private val preferencias = contexto.getSharedPreferences("medicamentos", Context.MODE_PRIVATE)

    fun listar(): List<MedicamentoSalvo> {
        val textoSalvo = preferencias.getString(CHAVE_LISTA, null)
        if (textoSalvo.isNullOrBlank()) return emptyList()
        val medicamentosSalvos = runCatching {
            ConversorMedicamentos.decodeFromString(ListSerializer(MedicamentoSalvo.serializer()), textoSalvo)
        }.getOrDefault(emptyList())
        val medicamentosDoUsuario = medicamentosSalvos.filterNot { it.codigo in CODIGOS_DEMONSTRACAO }
        if (medicamentosDoUsuario.size != medicamentosSalvos.size) {
            salvarTodos(medicamentosDoUsuario)
        }
        return medicamentosDoUsuario
    }

    fun salvarTodos(medicamentos: List<MedicamentoSalvo>) {
        preferencias.edit()
            .putString(CHAVE_LISTA, ConversorMedicamentos.encodeToString(medicamentos))
            .apply()
    }

    fun adicionar(medicamento: MedicamentoSalvo): List<MedicamentoSalvo> {
        val atualizados = (listar() + medicamento).sortedBy { minutosDoDia(it.horario) }
        salvarTodos(atualizados)
        return atualizados
    }

    fun atualizar(medicamento: MedicamentoSalvo): List<MedicamentoSalvo> {
        val atualizados = listar().map { atual ->
            if (atual.codigo == medicamento.codigo) medicamento else atual
        }.sortedBy { minutosDoDia(it.horario) }
        salvarTodos(atualizados)
        return atualizados
    }

    companion object {
        private const val CHAVE_LISTA = "lista"
        private val CODIGOS_DEMONSTRACAO = setOf("losartana", "metformina", "vitamina-d")
    }
}

@Composable
fun TelaMedicamentos(
    aoVoltarInicio: () -> Unit,
    aoAbrirExames: () -> Unit,
    modificador: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioMedicamentos(contexto) }
    var medicamentos by remember { mutableStateOf(repositorio.listar()) }
    var exibindoFormulario by remember { mutableStateOf(false) }
    val pedirPermissaoNotificacao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    if (exibindoFormulario) {
        TelaAdicionarMedicamento(
            aoVoltar = { exibindoFormulario = false },
            aoSalvar = { nome, dosagem, horario, alarmeAtivo ->
                if (alarmeAtivo) {
                    pedirPermissaoNotificacaoMedicamento(contexto, pedirPermissaoNotificacao::launch)
                }
                val novoMedicamento = MedicamentoSalvo(
                    codigo = UUID.randomUUID().toString(),
                    nome = nome.ifBlank { "Medicamento" },
                    dosagem = dosagem.ifBlank { "Dosagem nao informada" },
                    horario = horario.ifBlank { "08:00" },
                    alarmeAtivo = alarmeAtivo
                )
                medicamentos = repositorio.adicionar(novoMedicamento)
                if (alarmeAtivo) agendarLembreteMedicamento(contexto, novoMedicamento)
                exibindoFormulario = false
            }
        )
        return
    }

    val proximoMedicamento = proximoMedicamento(medicamentos)

    Scaffold(
        modifier = modificador.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.NENHUM,
                aoAbrirInicio = aoVoltarInicio,
                aoAdicionar = { exibindoFormulario = true },
                aoAbrirExames = aoAbrirExames
            )
        }
    ) { espaco ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(espaco)
                .statusBarsPadding()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            item {
                CabecalhoMedicamentos(aoVoltarInicio = aoVoltarInicio)
                CartaoProximaDose(
                    medicamento = proximoMedicamento,
                    aoMarcarTomado = {
                        val medicamento = proximoMedicamento ?: return@CartaoProximaDose
                        val atualizado = medicamento.copy(tomadoEm = LocalDate.now().toString())
                        medicamentos = repositorio.atualizar(atualizado)
                    }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Meus medicamentos",
                    color = textoPrimario,
                    fontWeight = FontWeight.Bold,
                    style = TipografiaAplicativo.titleSmall,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                    color = branco,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, borda)
                ) {
                    Column(Modifier.fillMaxHeight()) {
                        if (medicamentos.isEmpty()) {
                            EstadoVazioMedicamentos()
                        } else {
                            medicamentos.forEachIndexed { indice, medicamento ->
                                LinhaMedicamento(medicamento = medicamento)
                                if (indice != medicamentos.lastIndex) DivisorMedicamentos()
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { exibindoFormulario = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)
                        ) {
                            Text("+", color = branco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Adicionar medicamento", color = branco, style = TipografiaAplicativo.labelLarge)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
                CartaoDicaMedicamentos()
            }
        }
    }
}

@Composable
private fun CabecalhoMedicamentos(aoVoltarInicio: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "MEDICAMENTOS",
            color = verdeEscuro,
            style = TipografiaAplicativo.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = aoVoltarInicio,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(branco)
            ) {
                Text("<", color = verdePrincipal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Medicamentos", color = verdeEscuro, style = TipografiaAplicativo.titleLarge)
                Text("Dona Maria", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            }
            Spacer(Modifier.size(38.dp))
        }
    }
}

@Composable
private fun CartaoProximaDose(
    medicamento: MedicamentoSalvo?,
    aoMarcarTomado: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .shadow(1.dp, RoundedCornerShape(18.dp)),
        color = branco,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(end = 108.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            Text(
                "Proxima dose",
                color = verdePrincipal,
                style = TipografiaAplicativo.labelMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = verdeClaro,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.comprimido),
                            contentDescription = "Comprimido",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        medicamento?.nome ?: "Nenhum medicamento",
                        color = textoPrimario,
                        style = TipografiaAplicativo.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        medicamento?.dosagem ?: "Adicione um lembrete",
                        color = textoSecundario,
                        style = TipografiaAplicativo.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (medicamento != null) "Hoje as ${medicamento.horario}" else "--:--",
                        color = verdePrincipal,
                        style = TipografiaAplicativo.labelSmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Button(
                onClick = aoMarcarTomado,
                enabled = medicamento != null,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verdePrincipal,
                    disabledContainerColor = borda
                )
            ) {
                Text("Marcar como tomado", color = branco, style = TipografiaAplicativo.labelLarge)
            }
            }
            Image(
                painter = painterResource(R.drawable.onca_comprimido),
                contentDescription = "Ada com medicamento",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(108.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun LinhaMedicamento(medicamento: MedicamentoSalvo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val corFundo = when {
            medicamento.nome.contains("metformina", ignoreCase = true) -> vermelhoClaro
            medicamento.nome.contains("vitamina", ignoreCase = true) -> amareloClaro
            else -> verdeClaro
        }
        Surface(
            modifier = Modifier.size(38.dp),
            color = corFundo,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.comprimido),
                    contentDescription = medicamento.nome,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    medicamento.nome,
                    color = textoPrimario,
                    fontWeight = FontWeight.Bold,
                    style = TipografiaAplicativo.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (medicamento.tomadoEm == LocalDate.now().toString()) {
                    Text("Tomado", color = verdePrincipal, style = TipografiaAplicativo.labelSmall)
                }
            }
            Text(
                "${medicamento.dosagem} - ${medicamento.horario}",
                color = textoSecundario,
                style = TipografiaAplicativo.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(">", color = textoPrimario, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EstadoVazioMedicamentos() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.comprimido),
            contentDescription = "Comprimido",
            modifier = Modifier.size(44.dp)
        )
        Text("Nenhum medicamento cadastrado", color = textoPrimario, style = TipografiaAplicativo.titleSmall)
        Text(
            "Adicione o primeiro lembrete para acompanhar seus horarios.",
            color = textoSecundario,
            style = TipografiaAplicativo.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun DivisorMedicamentos() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 14.dp)
            .height(1.dp)
            .background(borda)
    )
}

@Composable
private fun CartaoDicaMedicamentos() {
    BalaoDicaAda(
        titulo = "Dica da Ada para Dona Maria",
        texto = "Tome seus medicamentos sempre nos horários certos e cuide da sua saúde!",
        imagem = R.drawable.onca_comprimido
    )
}

@Composable
private fun TelaAdicionarMedicamento(
    aoVoltar: () -> Unit,
    aoSalvar: (String, String, String, Boolean) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var dosagem by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("08:00") }
    var alarmeAtivo by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf("") }

    Scaffold(
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp)
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(branco)
                ) {
                    Text("<", color = verdePrincipal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Adicionar medicamento", color = verdeEscuro, style = TipografiaAplicativo.titleLarge)
                    Text("Configure nome, dosagem e horario.", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = branco,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome do medicamento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dosagem,
                        onValueChange = { dosagem = it },
                        label = { Text("Dosagem") },
                        placeholder = { Text("Ex: 1 comprimido") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = horario,
                        onValueChange = { horario = it.take(5) },
                        label = { Text("Horario") },
                        placeholder = { Text("HH:mm") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Alarme de lembrete", color = textoPrimario, style = TipografiaAplicativo.titleSmall)
                            Text("Receber aviso no horario informado", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                        }
                        Switch(
                            checked = alarmeAtivo,
                            onCheckedChange = { alarmeAtivo = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = verdePrincipal)
                        )
                    }
                    if (erro.isNotBlank()) {
                        Text(erro, color = vermelhoPrincipal, style = TipografiaAplicativo.bodySmall)
                    }
                    Button(
                        onClick = {
                            erro = when {
                                nome.isBlank() -> "Informe o nome do medicamento."
                                !horarioValido(horario) -> "Informe o horario no formato HH:mm."
                                else -> ""
                            }
                            if (erro.isBlank()) aoSalvar(nome.trim(), dosagem.trim(), horario.trim(), alarmeAtivo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)
                    ) {
                        Text("Salvar medicamento", color = branco, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

class LembreteMedicamentoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val codigo = intent.getStringExtra(EXTRA_CODIGO_MEDICAMENTO).orEmpty()
        val nome = intent.getStringExtra(EXTRA_NOME_MEDICAMENTO).orEmpty()
        val dosagem = intent.getStringExtra(EXTRA_DOSAGEM_MEDICAMENTO).orEmpty()
        val horario = intent.getStringExtra(EXTRA_HORARIO_MEDICAMENTO).orEmpty()
        if (nome.isBlank()) return

        notificarMedicamento(context, codigo, nome, dosagem)
        agendarLembreteMedicamento(
            context = context,
            medicamento = MedicamentoSalvo(
                codigo = codigo,
                nome = nome,
                dosagem = dosagem,
                horario = horario.ifBlank { "08:00" },
                alarmeAtivo = true
            ),
            somenteAmanha = true
        )
    }
}

private fun pedirPermissaoNotificacaoMedicamento(
    contexto: Context,
    pedirPermissao: (String) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        contexto.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        pedirPermissao(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun agendarLembreteMedicamento(
    context: Context,
    medicamento: MedicamentoSalvo,
    somenteAmanha: Boolean = false
) {
    if (!medicamento.alarmeAtivo || !horarioValido(medicamento.horario)) return
    val alarme = context.getSystemService(AlarmManager::class.java)
    val intencao = Intent(context, LembreteMedicamentoReceiver::class.java).apply {
        putExtra(EXTRA_CODIGO_MEDICAMENTO, medicamento.codigo)
        putExtra(EXTRA_NOME_MEDICAMENTO, medicamento.nome)
        putExtra(EXTRA_DOSAGEM_MEDICAMENTO, medicamento.dosagem)
        putExtra(EXTRA_HORARIO_MEDICAMENTO, medicamento.horario)
    }
    val pendente = PendingIntent.getBroadcast(
        context,
        medicamento.codigo.hashCode(),
        intencao,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val calendario = calendarioParaHorario(medicamento.horario)
    if (somenteAmanha) {
        calendario.add(Calendar.DAY_OF_YEAR, 1)
    }
    alarme.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendario.timeInMillis, pendente)
}

private fun notificarMedicamento(
    context: Context,
    codigo: String,
    nome: String,
    dosagem: String
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val gerenciador = context.getSystemService(NotificationManager::class.java)
    val canal = NotificationChannel(
        CANAL_LEMBRETES_MEDICAMENTOS,
        "Lembretes de medicamentos",
        NotificationManager.IMPORTANCE_HIGH
    )
    gerenciador.createNotificationChannel(canal)

    val abrirApp = PendingIntent.getActivity(
        context,
        codigo.hashCode(),
        Intent(context, AtividadePrincipal::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notificacao = android.app.Notification.Builder(context, CANAL_LEMBRETES_MEDICAMENTOS)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle("Hora do medicamento")
        .setContentText("$nome - $dosagem")
        .setContentIntent(abrirApp)
        .setAutoCancel(true)
        .build()

    gerenciador.notify(codigo.hashCode(), notificacao)
}

private fun proximoMedicamento(medicamentos: List<MedicamentoSalvo>): MedicamentoSalvo? {
    if (medicamentos.isEmpty()) return null
    val hoje = LocalDate.now().toString()
    val agora = LocalTime.now()
    val pendentesHoje = medicamentos
        .filter { it.tomadoEm != hoje }
        .sortedBy { minutosDoDia(it.horario) }

    return pendentesHoje.firstOrNull { horario ->
        runCatching { LocalTime.parse(horario.horario, FormatoHoraMedicamento) }
            .getOrDefault(LocalTime.MIN) >= agora
    } ?: pendentesHoje.firstOrNull() ?: medicamentos.sortedBy { minutosDoDia(it.horario) }.firstOrNull()
}

private fun calendarioParaHorario(horario: String): Calendar {
    val partes = horario.split(":")
    val hora = partes.getOrNull(0)?.toIntOrNull() ?: 8
    val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}

private fun minutosDoDia(horario: String): Int {
    val partes = horario.split(":")
    val hora = partes.getOrNull(0)?.toIntOrNull() ?: 0
    val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0
    return hora * 60 + minuto
}

private fun horarioValido(horario: String): Boolean {
    val partes = horario.split(":")
    val hora = partes.getOrNull(0)?.toIntOrNull()
    val minuto = partes.getOrNull(1)?.toIntOrNull()
    return partes.size == 2 && hora != null && minuto != null && hora in 0..23 && minuto in 0..59
}

private const val EXTRA_CODIGO_MEDICAMENTO = "codigo_medicamento"
private const val EXTRA_NOME_MEDICAMENTO = "nome_medicamento"
private const val EXTRA_DOSAGEM_MEDICAMENTO = "dosagem_medicamento"
private const val EXTRA_HORARIO_MEDICAMENTO = "horario_medicamento"
