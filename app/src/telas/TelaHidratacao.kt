package home_page

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class RegistroAgua(val horario: String, val quantidadeMl: Int)

@Composable
fun TelaHidratacao(aoVoltar: () -> Unit, perfilId: Long, metaDiariaMl: Int, modifier: Modifier = Modifier) {
    val escopo = rememberCoroutineScope()
    var registros by remember { mutableStateOf(emptyList<RegistroAgua>()) }
    var dataDosRegistros by remember { mutableStateOf(LocalDate.now()) }
    var mostrarValorManual by remember { mutableStateOf(false) }
    val totalMl = registros.sumOf { it.quantidadeMl }
    val progresso = (totalMl.toFloat() / metaDiariaMl.coerceAtLeast(1)).coerceIn(0f, 1f)

    fun adicionarAgua(quantidadeMl: Int) {
        if (quantidadeMl <= 0) return
        val hoje = LocalDate.now()
        val novoRegistro = RegistroAgua(
            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
            quantidadeMl
        )
        escopo.launch {
            runCatching { ApiAda.adicionarAgua(perfilId, quantidadeMl) }.onSuccess {
                registros = if (dataDosRegistros == hoje) registros + novoRegistro else listOf(novoRegistro)
                dataDosRegistros = hoje
            }
        }
    }

    LaunchedEffect(Unit) {
        runCatching { ApiAda.listarAgua(perfilId) }.onSuccess { resposta ->
            registros = resposta.registros.map { item ->
                RegistroAgua(item.consumidoEm?.substringAfter('T')?.take(5) ?: "--:--", item.quantidadeMl)
            }
        }
        while (true) {
            delay(30_000)
            val hoje = LocalDate.now()
            if (dataDosRegistros != hoje) {
                registros = emptyList()
                dataDosRegistros = hoje
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.MONITORAR,
                aoAbrirInicio = aoVoltar
            )
        }
    ) { espacamento ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacamento)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CabecalhoHidratacao(aoVoltar)
            CartaoDestaqueHidratacao()
            IndicadorHidratacao(totalMl, metaDiariaMl, progresso)
            BotoesQuantidade(
                aoAdicionar = ::adicionarAgua,
                aoDigitarOutro = { mostrarValorManual = true }
            )
            HistoricoHidratacao(registros, Modifier.weight(1f))
            DicaHidratacao()
        }
    }

    if (mostrarValorManual) {
        DialogoValorManual(
            aoFechar = { mostrarValorManual = false },
            aoAdicionar = {
                adicionarAgua(it)
                mostrarValorManual = false
            }
        )
    }
}

@Composable
private fun CabecalhoHidratacao(aoVoltar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(58.dp)
    ) {
        Surface(
            modifier = Modifier.size(38.dp).align(Alignment.CenterStart).clickable(onClick = aoVoltar),
            shape = CircleShape,
            color = verdeClaro
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("‹", color = verdePrincipal, fontSize = 32.sp, lineHeight = 32.sp)
            }
        }
        Text(
            "HIDRATAÇÃO",
            modifier = Modifier.align(Alignment.Center),
            color = verdePrincipal,
            fontWeight = FontWeight.Bold,
            style = TipografiaAplicativo.titleMedium
        )
    }
}

@Composable
private fun CartaoDestaqueHidratacao() {
    BalaoDicaAda(
        titulo = "Hidratação é energia, foco e resultados!",
        texto = "Seu corpo agradece cada gole.",
        imagem = R.drawable.onca_comprimido
    )
}

@Composable
private fun IndicadorHidratacao(totalMl: Int, metaDiariaMl: Int, progresso: Float) {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(146.dp)) {
            val largura = 8.dp.toPx()
            drawArc(borda, -90f, 360f, false, style = Stroke(largura, cap = StrokeCap.Round))
            drawArc(
                verdePrincipal,
                -90f,
                360f * progresso,
                false,
                topLeft = Offset(largura / 2, largura / 2),
                size = Size(size.width - largura, size.height - largura),
                style = Stroke(largura, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Image(painterResource(R.drawable.agua), "Gota de água", Modifier.size(32.dp))
            Text(formatarLitros(totalMl), color = textoPrimario, fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("de ${formatarLitros(metaDiariaMl)}", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            Text("${(progresso * 100).toInt()}%", color = verdePrincipal, style = TipografiaAplicativo.labelLarge)
        }
    }
}

@Composable
private fun BotoesQuantidade(aoAdicionar: (Int) -> Unit, aoDigitarOutro: () -> Unit) {
    val opcoes = listOf(
        Triple("+250 ml", R.drawable.copo_de_agua, 250),
        Triple("+500 ml", R.drawable.garrafa_de_agua, 500),
        Triple("+1 L", R.drawable.garrafa_de_agua, 1000)
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        opcoes.forEach { (texto, icone, quantidade) ->
            BotaoQuantidade(texto, icone, Modifier.weight(1f)) { aoAdicionar(quantidade) }
        }
        Surface(
            modifier = Modifier.weight(1f).height(42.dp).clickable(onClick = aoDigitarOutro),
            shape = RoundedCornerShape(22.dp),
            color = branco,
            border = androidx.compose.foundation.BorderStroke(1.dp, borda)
        ) {
            Box(contentAlignment = Alignment.Center) { Text("Outro", style = TipografiaAplicativo.labelSmall) }
        }
    }
}

@Composable
private fun BotaoQuantidade(texto: String, icone: Int, modifier: Modifier, aoClicar: () -> Unit) {
    Surface(
        modifier = modifier.height(42.dp).clickable(onClick = aoClicar),
        shape = RoundedCornerShape(22.dp),
        color = branco,
        border = androidx.compose.foundation.BorderStroke(1.dp, borda)
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(icone), texto, Modifier.size(18.dp))
            Spacer(Modifier.width(3.dp))
            Text(texto, style = TipografiaAplicativo.labelSmall)
        }
    }
}

@Composable
private fun HistoricoHidratacao(registros: List<RegistroAgua>, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), RoundedCornerShape(12.dp), color = branco, shadowElevation = 1.dp) {
        Column(Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Histórico de hoje", color = textoPrimario, style = TipografiaAplicativo.titleSmall, modifier = Modifier.weight(1f))
                Text("Meta: 2,5 L", color = verdePrincipal, style = TipografiaAplicativo.labelSmall)
            }
            if (registros.isEmpty()) {
                Text("Nenhum copo registrado ainda.", color = textoSecundario, style = TipografiaAplicativo.bodySmall, modifier = Modifier.padding(vertical = 18.dp).align(Alignment.CenterHorizontally))
            } else {
                registros.asReversed().forEach { registro ->
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.copo_de_agua), null, Modifier.size(18.dp))
                        Text(registro.horario, color = textoSecundario, style = TipografiaAplicativo.bodySmall, modifier = Modifier.padding(start = 8.dp).width(52.dp))
                        Text(formatarQuantidade(registro.quantidadeMl), style = TipografiaAplicativo.bodySmall, modifier = Modifier.weight(1f))
                        Box(Modifier.size(16.dp).clip(CircleShape).background(verdePrincipal), contentAlignment = Alignment.Center) {
                            Text("✓", color = branco, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DicaHidratacao() {
    BalaoDicaAda(
        titulo = "Dica da Ada AI",
        texto = "Você treina pesado! Mantenha sua hidratação em dia para melhorar sua performance e recuperação.",
        imagem = R.drawable.onca_comprimido
    )
}

@Composable
private fun DialogoValorManual(aoFechar: () -> Unit, aoAdicionar: (Int) -> Unit) {
    var texto by remember { mutableStateOf("") }
    val valor = texto.toIntOrNull()
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Adicionar água") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { novo -> texto = novo.filter(Char::isDigit).take(5) },
                label = { Text("Quantidade em ml") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { valor?.let(aoAdicionar) }, enabled = valor != null && valor > 0, colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)) {
                Text("Adicionar")
            }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar") } }
    )
}

private fun formatarLitros(ml: Int): String = String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f L", ml / 1000.0).replace(",00", "")
private fun formatarQuantidade(ml: Int): String = if (ml >= 1000 && ml % 1000 == 0) "${ml / 1000} L" else "$ml ml"
