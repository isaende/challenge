package home_page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ada.chatmedico.demo.R
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelaMetas(
    perfilId: Long,
    metaAguaInicial: Int,
    metaPassosInicial: Long,
    passosHoje: Long?,
    statusPassos: String,
    aoMetasSalvas: (Int, Long) -> Unit,
    aoVoltar: () -> Unit,
    aoAbrirExames: () -> Unit,
    aoAbrirTreino: () -> Unit,
    aoAtualizarPassos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val escopo = rememberCoroutineScope()
    var hoje by remember { mutableStateOf(LocalDate.now()) }
    var atualizacao by remember { mutableIntStateOf(0) }
    var metas by remember(perfilId) {
        mutableStateOf(MetasNovasApi(metaAguaInicial.coerceAtLeast(1), metaPassosInicial.coerceAtLeast(1), 4))
    }
    var aguaMl by remember(perfilId) { mutableStateOf<Int?>(null) }
    var treinos by remember(perfilId) { mutableStateOf<List<TreinoApi>?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var editarMetas by rememberSaveable { mutableStateOf(false) }
    var registrarAgua by rememberSaveable { mutableStateOf(false) }
    var aba by rememberSaveable { mutableIntStateOf(0) }
    var semanasAtras by rememberSaveable { mutableIntStateOf(0) }
    val inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(semanasAtras.toLong())
    val diasConcluidos = treinos.orEmpty().filter { it.concluidoEm != null }.map { it.dataTreino }.toSet()
    val treinosSemana = (0L..6L).count { inicioSemana.plusDays(it).toString() in diasConcluidos }
    val pronto = !carregando && aguaMl != null && treinos != null
    var habitosSemana by remember(perfilId, inicioSemana) { mutableStateOf<List<HabitoDiaApi>?>(null) }
    var carregandoHabitos by remember { mutableStateOf(false) }
    var erroHabitos by remember { mutableStateOf(false) }
    var tentativaHabitos by remember { mutableIntStateOf(0) }

    LaunchedEffect(perfilId, inicioSemana, hoje, atualizacao, tentativaHabitos) {
        carregandoHabitos = true
        erroHabitos = false
        habitosSemana = null
        try {
            habitosSemana = ApiAda.listarHabitosSemana(perfilId, inicioSemana)
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (_: Exception) {
            erroHabitos = true
        } finally {
            carregandoHabitos = false
        }
    }

    BackHandler(onBack = aoVoltar)
    val ciclo = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                hoje = LocalDate.now()
                atualizacao++
            }
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            hoje = LocalDate.now()
        }
    }
    LaunchedEffect(perfilId, hoje, atualizacao) {
        carregando = true
        erro = null
        try {
            val metasServidor = ApiAda.listarMetas(perfilId)
            val aguaServidor = ApiAda.listarAgua(perfilId)
            val treinosServidor = ApiAda.listarTreinos(perfilId)
            metas = MetasNovasApi(
                metasServidor.firstOrNull { it.tipo == "AGUA" }?.valorAlvo?.toInt() ?: metaAguaInicial,
                metasServidor.firstOrNull { it.tipo == "PASSOS" }?.valorAlvo?.toLong() ?: metaPassosInicial,
                metasServidor.firstOrNull { it.tipo == "TREINOS" }?.valorAlvo?.toInt() ?: 4
            )
            aguaMl = aguaServidor.totalMl
            treinos = treinosServidor
            aoMetasSalvas(metas.aguaMl, metas.passos)
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (_: Exception) {
            aguaMl = null
            treinos = null
            erro = "Não foi possível carregar suas metas. Verifique a conexão e tente novamente."
        } finally {
            carregando = false
        }
    }

    fun salvar(operacao: suspend () -> Unit) {
        if (salvando) return
        salvando = true
        erro = null
        escopo.launch {
            try {
                operacao()
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (_: Exception) {
                erro = "Não foi possível salvar. Verifique a conexão e tente novamente."
            } finally {
                salvando = false
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(), containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.NENHUM,
                aoAbrirInicio = aoVoltar, aoAbrirMonitorar = aoAtualizarPassos,
                aoAdicionar = { if (pronto && !salvando) editarMetas = true },
                aoAbrirExames = aoAbrirExames
            )
        }
    ) { espaco ->
        Column(
            Modifier.fillMaxSize().padding(espaco).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth().statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = aoVoltar, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(48.dp).semantics { contentDescription = "Voltar ao início" }) {
                    Text("‹", fontSize = 32.sp, color = verdePrincipal)
                }
                Text("METAS & HÁBITOS", Modifier.weight(1f), color = verdePrincipal,
                    style = TipografiaAplicativo.titleMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Image(painterResource(R.drawable.metas), null, Modifier.padding(12.dp).size(24.dp))
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(fundoSecundario)) {
                listOf("Metas & Hábitos", "Progresso").forEachIndexed { indice, titulo ->
                    Surface(onClick = { aba = indice }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), color = if (aba == indice) verdePrincipal else fundoSecundario) {
                        Text(titulo, Modifier.padding(vertical = 13.dp, horizontal = 4.dp),
                            color = if (aba == indice) branco else textoSecundario,
                            style = TipografiaAplicativo.labelMedium, textAlign = TextAlign.Center)
                    }
                }
            }
            if (carregando) LinearProgressIndicator(Modifier.fillMaxWidth(), color = verdePrincipal)
            erro?.let { mensagem ->
                Surface(color = vermelhoClaro, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(mensagem, color = vermelhoPrincipal, style = TipografiaAplicativo.bodySmall)
                        TextButton(onClick = { atualizacao++ }, enabled = !salvando && !carregando) { Text("Tentar novamente") }
                    }
                }
            }
            if (aba == 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Minhas metas", Modifier.weight(1f), style = TipografiaAplicativo.titleSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { editarMetas = true }, enabled = pronto && !salvando) { Text("Editar metas", color = verdePrincipal) }
                }
                CartaoMeta("Beber ${litrosMeta(metas.aguaMl)} de água", "${aguaMl?.let(::litrosMeta) ?: "—"} / ${litrosMeta(metas.aguaMl)} hoje",
                    R.drawable.agua, azulPrincipal, aguaMl?.toFloat(), metas.aguaMl.toFloat(), "Adicionar água", pronto && !salvando) { registrarAgua = true }
                CartaoMeta("Dar ${numeroMeta(metas.passos)} passos", "${passosHoje?.let(::numeroMeta) ?: "—"} / ${numeroMeta(metas.passos)} hoje",
                    R.drawable.sapato, verdePrincipal, passosHoje?.toFloat(), metas.passos.toFloat(), "Atualizar passos", !salvando, aoAtualizarPassos)
                Text("Passos registrados pelo Health Connect. Toque em Atualizar passos para conectar ou sincronizar.",
                    color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                Text(statusPassos, color = verdePrincipal, style = TipografiaAplicativo.bodySmall)
                val inicioAtual = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val totalAtual = (0L..6L).count { inicioAtual.plusDays(it).toString() in diasConcluidos }
                CartaoMeta("Treinar ${metas.treinosSemana}x por semana", "${if (treinos == null) "—" else totalAtual} / ${metas.treinosSemana} treinos nesta semana",
                    R.drawable.treino, roxoPrincipal, if (treinos == null) null else totalAtual.toFloat(), metas.treinosSemana.toFloat(), "Ver treino de hoje", !salvando, aoAbrirTreino)
            } else {
                Text("Sua constância", style = TipografiaAplicativo.titleMedium, fontWeight = FontWeight.Bold)
                Text("Acompanhe os treinos concluídos a cada semana. A comparação usa sua meta semanal atual.",
                    color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                (0L..3L).forEach { semana ->
                    val inicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(semana)
                    val total = (0L..6L).count { inicio.plusDays(it).toString() in diasConcluidos }
                    CartaoMeta("${dataMeta(inicio)} – ${dataMeta(inicio.plusDays(6))}",
                        "${if (treinos == null) "—" else total} / ${metas.treinosSemana} treinos", R.drawable.treino,
                        roxoPrincipal, if (treinos == null) null else total.toFloat(), metas.treinosSemana.toFloat(), "Ver semana", pronto) { semanasAtras = semana.toInt() }
                }
            }
            Text("Hábitos diários", style = TipografiaAplicativo.titleSmall, fontWeight = FontWeight.Bold)
            Surface(color = branco, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, borda)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { semanasAtras++ }, enabled = !salvando,
                            modifier = Modifier.semantics { contentDescription = "Semana anterior" }) { Text("‹", fontSize = 24.sp) }
                        Text("${dataMeta(inicioSemana)} – ${dataMeta(inicioSemana.plusDays(6))}", Modifier.weight(1f),
                            textAlign = TextAlign.Center, style = TipografiaAplicativo.labelMedium)
                        TextButton(onClick = { semanasAtras-- }, enabled = semanasAtras > 0 && !salvando,
                            modifier = Modifier.semantics { contentDescription = "Próxima semana" }) { Text("›", fontSize = 24.sp) }
                    }
                    if (carregandoHabitos) LinearProgressIndicator(Modifier.fillMaxWidth(), color = verdePrincipal)
                    if (erroHabitos) {
                        Text("Não foi possível carregar água e passos desta semana.", color = vermelhoPrincipal, style = TipografiaAplicativo.bodySmall)
                        TextButton(onClick = { tentativaHabitos++ }) { Text("Tentar novamente") }
                    }
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val larguraTabela = maxOf(maxWidth, 350.dp)
                        val tabelaRolavel = maxWidth < larguraTabela
                        Column(Modifier.horizontalScroll(rememberScrollState())) {
                            Column(Modifier.width(larguraTabela)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(Modifier.width(126.dp))
                                    listOf("S", "T", "Q", "Q", "S", "S", "D").forEachIndexed { indice, nome ->
                                        val dia = inicioSemana.plusDays(indice.toLong())
                                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(Modifier.size(22.dp).clip(CircleShape).background(if (dia == hoje) verdePrincipal else transparente), contentAlignment = Alignment.Center) {
                                                Text(nome, color = if (dia == hoje) branco else textoSecundario, style = TipografiaAplicativo.labelSmall)
                                            }
                                            Text(dia.dayOfMonth.toString(), color = textoSecundario, style = TipografiaAplicativo.labelSmall)
                                        }
                                    }
                                }
                                LinhaHabitoSemanal("Beber água", "${litrosMeta(metas.aguaMl)} por dia", R.drawable.agua, azulPrincipal,
                                    inicioSemana, hoje,
                                    progressoDia = { dia ->
                                        val quantidade = if (dia == hoje) aguaMl?.toLong() else habitosSemana?.firstOrNull { it.data == dia.toString() }?.aguaMl
                                        quantidade?.let { it.toFloat() / metas.aguaMl.coerceAtLeast(1) }
                                    })
                                HorizontalDivider(color = borda)
                                LinhaHabitoSemanal("Passos", "${numeroMeta(metas.passos)} por dia", R.drawable.sapato, verdePrincipal,
                                    inicioSemana, hoje,
                                    progressoDia = { dia ->
                                        val quantidade = if (dia == hoje && passosHoje != null) passosHoje else habitosSemana?.firstOrNull { it.data == dia.toString() }?.passos
                                        quantidade?.let { it.toFloat() / metas.passos.coerceAtLeast(1) }
                                    })
                                HorizontalDivider(color = borda)
                                LinhaHabitoSemanal("Treinos", "${metas.treinosSemana}x por semana", R.drawable.treino, roxoPrincipal,
                                    inicioSemana, hoje,
                                    progressoDia = { dia -> if (treinos == null) null else if (dia.toString() in diasConcluidos) 1f else 0f },
                                    aoClicarDia = if (pronto && !salvando) { dia ->
                                        salvar { treinos = ApiAda.marcarTreino(perfilId, dia, dia.toString() !in diasConcluidos) }
                                    } else null)
                            }
                            if (tabelaRolavel) Text("Deslize para ver todos os dias →", color = textoSecundario, style = TipografiaAplicativo.labelSmall)
                        }
                    }
                    Text("Água e passos são marcados ao atingir a meta diária atual. O círculo mostra o progresso; — indica falta de dados ou um dia futuro.",
                        color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                    Text(if (treinos == null) "Carregue os dados para acompanhar seus treinos."
                        else "$treinosSemana de ${metas.treinosSemana} treinos concluídos. Toque na linha Treinos para marcar ou desmarcar um dia.",
                        color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                    if (salvando) LinearProgressIndicator(Modifier.fillMaxWidth(), color = verdePrincipal)
                }
            }
            Surface(color = verdeClaro, shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (treinos != null && treinosSemana >= metas.treinosSemana) "Muito bem! Você alcançou sua meta de treinos nesta semana."
                        else "Um dia de cada vez. Cada copo, passo e treino conta!", color = verdePrincipal,
                        style = TipografiaAplicativo.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    if (editarMetas) DialogoMetas(metas, salvando, erro,
        aoFechar = { if (!salvando) editarMetas = false },
        aoSalvar = { novas -> salvar {
            ApiAda.salvarMetas(perfilId, novas)
            metas = novas
            aoMetasSalvas(novas.aguaMl, novas.passos)
            editarMetas = false
        } })
    if (registrarAgua) DialogoAguaMeta(salvando, erro,
        aoFechar = { if (!salvando) registrarAgua = false },
        aoSalvar = { quantidade -> salvar {
            ApiAda.adicionarAgua(perfilId, quantidade)
            registrarAgua = false
            // Releitura evita duplicar o registro caso a atualização falhe.
            atualizacao++
        } })
}

@Composable
private fun LinhaHabitoSemanal(
    titulo: String,
    detalhe: String,
    icone: Int,
    cor: Color,
    inicio: LocalDate,
    hoje: LocalDate,
    progressoDia: (LocalDate) -> Float?,
    aoClicarDia: ((LocalDate) -> Unit)? = null
) {
    Row(Modifier.fillMaxWidth().heightIn(min = 68.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.width(126.dp).padding(end = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(cor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icone), null, Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(titulo, color = textoPrimario, style = TipografiaAplicativo.labelSmall, fontWeight = FontWeight.SemiBold)
                Text(detalhe, color = textoSecundario, style = TipografiaAplicativo.labelSmall)
            }
        }
        (0L..6L).forEach { indice ->
            val dia = inicio.plusDays(indice)
            val futuro = dia.isAfter(hoje)
            val progresso = if (futuro) null else progressoDia(dia)?.coerceIn(0f, 1f)
            val concluido = progresso != null && progresso >= 1f
            val descricao = when {
                futuro -> "dia futuro"
                progresso == null -> "sem dados"
                concluido -> "concluído"
                else -> "${(progresso * 100).toInt()}% da meta"
            }
            Box(Modifier.weight(1f).height(56.dp)
                .then(if (aoClicarDia != null && !futuro) Modifier.clickable(
                    onClickLabel = if (concluido) "Desmarcar treino" else "Marcar treino concluído"
                ) { aoClicarDia(dia) } else Modifier)
                .semantics { contentDescription = "$titulo, ${dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}: $descricao" },
                contentAlignment = Alignment.Center) {
                if (concluido) {
                    Box(Modifier.size(20.dp).clip(CircleShape).background(verdePrincipal), contentAlignment = Alignment.Center) {
                        Text("✓", color = branco, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (progresso != null) {
                    CircularProgressIndicator(progress = { progresso }, modifier = Modifier.size(20.dp),
                        color = verdePrincipal, trackColor = borda, strokeWidth = 3.dp)
                } else {
                    Text("—", color = if (futuro) borda else textoSecundario, style = TipografiaAplicativo.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun CartaoMeta(titulo: String, detalhe: String, icone: Int, cor: Color,
    atual: Float?, alvo: Float, acao: String, habilitado: Boolean, aoClicar: () -> Unit) {
    val progresso = ((atual ?: 0f) / alvo.coerceAtLeast(1f)).coerceIn(0f, 1f)
    Surface(color = branco, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, borda)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(cor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icone), null, Modifier.size(30.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(titulo, color = textoPrimario, style = TipografiaAplicativo.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(detalhe, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(progress = { progresso }, modifier = Modifier.weight(1f).height(5.dp).clip(CircleShape), color = cor, trackColor = fundoSecundario)
                    Text(if (atual == null) "—" else "${(progresso * 100).toInt()}%", color = cor, style = TipografiaAplicativo.labelSmall)
                }
                TextButton(onClick = aoClicar, enabled = habilitado, contentPadding = PaddingValues(0.dp)) { Text(acao, color = if (habilitado) cor else textoSecundario, style = TipografiaAplicativo.labelMedium) }
            }
        }
    }
}

@Composable
private fun DialogoMetas(metas: MetasNovasApi, salvando: Boolean, erro: String?, aoFechar: () -> Unit, aoSalvar: (MetasNovasApi) -> Unit) {
    var agua by rememberSaveable { mutableStateOf(metas.aguaMl.toString()) }
    var passos by rememberSaveable { mutableStateOf(metas.passos.toString()) }
    var treinos by rememberSaveable { mutableStateOf(metas.treinosSemana.toString()) }
    val valido = (agua.toIntOrNull() ?: 0) in 1..10_000 && (passos.toLongOrNull() ?: 0L) in 1..100_000 && (treinos.toIntOrNull() ?: 0) in 1..7
    AlertDialog(onDismissRequest = aoFechar, title = { Text("Editar metas") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoNumeroMeta("Água por dia (ml)", agua, !salvando) { agua = it }
            CampoNumeroMeta("Passos por dia", passos, !salvando) { passos = it }
            CampoNumeroMeta("Treinos por semana", treinos, !salvando) { treinos = it }
            Text("Água: 1 a 10.000 ml • Passos: 1 a 100.000 • Treinos: 1 a 7", style = TipografiaAplicativo.bodySmall)
            erro?.let { Text(it, color = vermelhoPrincipal, style = TipografiaAplicativo.bodySmall) }
        }
    }, confirmButton = { TextButton(enabled = valido && !salvando, onClick = { aoSalvar(MetasNovasApi(agua.toInt(), passos.toLong(), treinos.toInt())) }) { Text(if (salvando) "Salvando…" else "Salvar") } },
        dismissButton = { TextButton(onClick = aoFechar, enabled = !salvando) { Text("Cancelar") } })
}

@Composable
private fun DialogoAguaMeta(salvando: Boolean, erro: String?, aoFechar: () -> Unit, aoSalvar: (Int) -> Unit) {
    var quantidade by rememberSaveable { mutableStateOf("250") }
    AlertDialog(onDismissRequest = aoFechar, title = { Text("Adicionar água") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoNumeroMeta("Quantidade bebida (ml)", quantidade, !salvando) { quantidade = it }
            Text("Informe entre 1 e 10.000 ml.", style = TipografiaAplicativo.bodySmall)
            erro?.let { Text(it, color = vermelhoPrincipal, style = TipografiaAplicativo.bodySmall) }
        }
    }, confirmButton = { TextButton(onClick = { aoSalvar(quantidade.toInt()) }, enabled = !salvando && (quantidade.toIntOrNull() ?: 0) in 1..10_000) { Text(if (salvando) "Salvando…" else "Adicionar") } },
        dismissButton = { TextButton(onClick = aoFechar, enabled = !salvando) { Text("Cancelar") } })
}

@Composable
private fun CampoNumeroMeta(rotulo: String, valor: String, habilitado: Boolean, aoAlterar: (String) -> Unit) {
    OutlinedTextField(value = valor, onValueChange = { aoAlterar(it.filter { caractere -> caractere in '0'..'9' }.take(6)) },
        label = { Text(rotulo) }, enabled = habilitado, singleLine = true, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

private fun numeroMeta(valor: Long): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(valor)
private fun litrosMeta(valor: Int): String = "${NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).format(valor / 1000.0)} L"
private fun dataMeta(data: LocalDate): String = data.format(DateTimeFormatter.ofPattern("dd/MM"))
