package home_page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ada.chatmedico.demo.R
import java.time.LocalDate

@Composable
fun TelaRecompensas(
    perfilId: Long,
    nomeUsuario: String,
    aoVoltar: () -> Unit,
    aoAbrirInicio: () -> Unit,
    aoAbrirExames: () -> Unit
) {
    BackHandler(onBack = aoVoltar)
    val contexto = LocalContext.current
    val repositorio = remember(perfilId, contexto) { RepositorioRecompensas(contexto, perfilId) }
    var progressos by remember(repositorio) { mutableStateOf(repositorio.carregar()) }
    var atualizacao by remember { mutableIntStateOf(0) }
    var erroSincronizacao by remember { mutableStateOf(false) }
    LaunchedEffect(perfilId, atualizacao) {
        erroSincronizacao = false
        try {
            val registros = ApiAda.listarDesafios(perfilId)
            progressos = repositorio.carregar().comRegistrosAutomaticos(registros)
        } catch (cancelamento: kotlinx.coroutines.CancellationException) {
            throw cancelamento
        } catch (_: Exception) {
            erroSincronizacao = true
        }
    }
    var mostrarConcluidos by rememberSaveable(perfilId) { mutableStateOf(false) }
    var detalhe by rememberSaveable(perfilId) { mutableStateOf<DesafioRecompensa?>(null) }
    var hoje by remember { mutableStateOf(LocalDate.now()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { hoje = LocalDate.now(); atualizacao++ }
    val concluidos = DesafioRecompensa.entries.filter { progressos.getValue(it).concluido(it, hoje) }
    val desafios = DesafioRecompensa.entries.filter { (it in concluidos) == mostrarConcluidos }
    val pontos = DesafioRecompensa.entries.sumOf { progressos.getValue(it).pontos(it, hoje) }

    Scaffold(
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.RECOMPENSAS,
                aoAbrirInicio = aoAbrirInicio,
                aoAbrirExames = aoAbrirExames
            )
        }
    ) { espaco ->
        Column(Modifier.fillMaxSize().padding(espaco).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                BotaoSetaAplicativo(aoClicar = aoVoltar)
                Text("DESAFIOS & RECOMPENSAS", Modifier.weight(1f),
                    color = verdePrincipal, style = TipografiaAplicativo.titleSmall, textAlign = TextAlign.Center)
                Image(painterResource(R.drawable.trofeu), null, Modifier.padding(12.dp).size(24.dp))
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (erroSincronizacao) {
                    Text("Não foi possível atualizar os registros dos desafios.", color = vermelhoPrincipal)
                    TextButton(onClick = { atualizacao++ }) { Text("Tentar novamente") }
                }
                Surface(color = verdeClaro, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Olá, ${nomeUsuario.substringBefore(' ').ifBlank { "Usuário" }}! 👋",
                            color = textoPrimario, style = TipografiaAplicativo.titleMedium)
                        Text("Cada pequeno passo te leva mais longe!", color = textoSecundario, style = TipografiaAplicativo.bodyMedium)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(false to "Ativos", true to "Concluídos").forEach { (concluido, titulo) ->
                        val selecionado = mostrarConcluidos == concluido
                        Surface(
                            modifier = Modifier.weight(1f).selectable(selected = selecionado, role = Role.Tab,
                                onClick = { mostrarConcluidos = concluido }),
                            color = if (selecionado) verdePrincipal else branco,
                            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, if (selecionado) verdePrincipal else borda)
                        ) {
                            Text(titulo, Modifier.padding(vertical = 13.dp), textAlign = TextAlign.Center,
                                color = if (selecionado) branco else textoSecundario, style = TipografiaAplicativo.labelLarge)
                        }
                    }
                }
                val caminhada = DesafioRecompensa.CAMINHADA
                if (caminhada in desafios) {
                    CartaoDesafioDestaque(progressos.getValue(caminhada), hoje) { detalhe = caminhada }
                }
                val outros = desafios.filter { it != caminhada }
                if (outros.isNotEmpty()) {
                    Text(if (mostrarConcluidos) "Suas conquistas" else "Outros desafios", color = textoPrimario,
                        style = TipografiaAplicativo.titleSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        outros.forEach { desafio ->
                            CartaoDesafioRecompensa(desafio, progressos.getValue(desafio), hoje) { detalhe = desafio }
                        }
                    }
                }
                if (desafios.isEmpty()) {
                    Surface(color = branco, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, borda)) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Image(painterResource(R.drawable.trofeu), null, Modifier.size(54.dp))
                            Text(if (mostrarConcluidos) "Sua próxima conquista começa hoje" else "Todos os desafios concluídos!",
                                color = verdePrincipal, style = TipografiaAplicativo.titleMedium, textAlign = TextAlign.Center)
                            Text(if (mostrarConcluidos) "Registre suas metas nos desafios ativos. Quando completar um desafio, ele aparecerá aqui."
                                else "Você completou os quatro desafios. Confira suas conquistas na aba Concluídos.",
                                color = textoSecundario, style = TipografiaAplicativo.bodyMedium, textAlign = TextAlign.Center)
                            TextButton(onClick = { mostrarConcluidos = !mostrarConcluidos }) {
                                Text(if (mostrarConcluidos) "Ver desafios ativos" else "Ver conquistas", color = verdePrincipal)
                            }
                        }
                    }
                }
                Surface(shape = RoundedCornerShape(18.dp), color = branco, border = BorderStroke(1.dp, borda)) {
                    Row(
                        Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(verdeClaro, Color(0xFFFFF9E9))))
                            .padding(18.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Suas recompensas", color = verdePrincipal, style = TipografiaAplicativo.titleSmall)
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(pontos.toString(), color = verdePrincipal, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                Text("pontos", color = verdeEscuro, style = TipografiaAplicativo.bodyMedium, modifier = Modifier.padding(bottom = 5.dp))
                            }
                            Text("${concluidos.size} de 4 desafios concluídos", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                        }
                        Image(painterResource(R.drawable.presente_recompensas), null, Modifier.size(80.dp))
                    }
                }
                Text("?gua, treinos, passos e sono registrados atualizam os desafios automaticamente. Cada dia conta uma vez; os pontos são liberados ao concluir o desafio.",
                    color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            }
        }
    }

    detalhe?.let { desafio ->
        val progresso = progressos.getValue(desafio)
        val concluido = progresso.concluido(desafio, hoje)
        val registradoHoje = hoje in progresso.dias
        AlertDialog(
            onDismissRequest = { detalhe = null }, containerColor = branco,
            icon = { Image(painterResource(iconeDesafio(desafio)), null, Modifier.size(42.dp)) },
            title = { Text(desafio.titulo, color = verdePrincipal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(desafio.descricao)
                    Text("${progresso.total(desafio, hoje)} / ${desafio.metaDias} dias • ${desafio.pontos} pontos", color = verdeEscuro)
                    Text(when {
                        concluido -> "Parabéns! Os pontos desta conquista já estão no seu total."
                        registradoHoje -> "A meta de hoje já foi registrada. Continue amanhã!"
                        else -> "${desafio.confirmacao}? Confirme apenas depois de atingir essa meta. Os dias não precisam ser seguidos."
                    })
                }
            },
            confirmButton = {
                if (!concluido && !registradoHoje) {
                    TextButton(onClick = {
                        val dataRegistro = LocalDate.now()
                        val atualizado = repositorio.carregar().getValue(desafio).registrarHoje(desafio, dataRegistro)
                        repositorio.salvar(desafio, atualizado)
                        progressos = progressos + (desafio to progresso.copy(dias = progresso.dias + dataRegistro))
                        hoje = dataRegistro
                        detalhe = null
                    }, enabled = perfilId > 0) { Text("Confirmar meta de hoje", color = verdePrincipal) }
                } else {
                    TextButton(onClick = { detalhe = null }) { Text("Entendi", color = verdePrincipal) }
                }
            },
            dismissButton = {
                if (!concluido && !registradoHoje) TextButton(onClick = { detalhe = null }) { Text("Agora não", color = textoSecundario) }
            }
        )
    }
}

@Composable
private fun CartaoDesafioDestaque(progresso: ProgressoRecompensa, hoje: LocalDate, aoClicar: () -> Unit) {
    val desafio = DesafioRecompensa.CAMINHADA
    val dias = progresso.total(desafio, hoje)
    val fracao = dias.toFloat() / desafio.metaDias
    Surface(onClick = aoClicar, shape = RoundedCornerShape(20.dp), color = verdePrincipal) {
        Row(
            Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(verdeEscuro, verdePrincipal)))
                .padding(20.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(desafio.titulo, color = branco, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("7 dias caminhando", color = branco, style = TipografiaAplicativo.bodyLarge)
                Text("10.000 passos por dia", color = verdeClaro, style = TipografiaAplicativo.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$dias / 7 dias", color = branco, style = TipografiaAplicativo.labelMedium)
                    Text("${(fracao * 100).toInt()}%", color = verdeClaro, style = TipografiaAplicativo.labelMedium)
                }
                LinearProgressIndicator(progress = { fracao }, modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color(0xFFADD47E), trackColor = branco.copy(alpha = 0.18f))
                Text(if (dias == 7) "+100 pontos conquistados" else "+100 pontos ao concluir", color = verdeClaro,
                    style = TipografiaAplicativo.bodySmall)
            }
            Image(painterResource(R.drawable.trofeu), null, Modifier.size(84.dp))
        }
    }
}

@Composable
private fun CartaoDesafioRecompensa(desafio: DesafioRecompensa, progresso: ProgressoRecompensa, hoje: LocalDate, aoClicar: () -> Unit) {
    val cor = when (desafio) {
        DesafioRecompensa.HIDRATACAO -> azulPrincipal
        DesafioRecompensa.SONO -> roxoPrincipal
        else -> verdePrincipal
    }
    Surface(onClick = aoClicar, shape = RoundedCornerShape(16.dp), color = branco, border = BorderStroke(1.dp, borda)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(42.dp).background(cor.copy(alpha = 0.09f), CircleShape), contentAlignment = Alignment.Center) {
                Image(painterResource(iconeDesafio(desafio)), null, Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(desafio.titulo, color = textoPrimario, style = TipografiaAplicativo.labelMedium)
                Text(desafio.descricao, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            }
            Text("${progresso.total(desafio, hoje)} / ${desafio.metaDias}", color = verdePrincipal, style = TipografiaAplicativo.labelMedium)
            SetaAplicativo(modifier = Modifier.size(24.dp))
        }
    }
}

private fun iconeDesafio(desafio: DesafioRecompensa): Int = when (desafio) {
    DesafioRecompensa.CAMINHADA -> R.drawable.trofeu
    DesafioRecompensa.HIDRATACAO -> R.drawable.agua
    DesafioRecompensa.EXERCICIOS -> R.drawable.exercicios
    DesafioRecompensa.SONO -> R.drawable.sono
}
