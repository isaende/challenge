package home_page

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import java.text.NumberFormat
import java.util.Locale

private const val MetaPassos = 10_000L
private val VerdePassosTela = Color(0xFF006C43)
private val TrilhoPassos = Color(0xFFDCE7E1)
private val FundoPassos = Color(0xFFF7FAF8)
private val TextoPassos = Color(0xFF17211C)
private val TextoPassosSecundario = Color(0xFF61706A)

@Composable
fun TelaPassos(
    passosHoje: Long,
    carregando: Boolean,
    aoAtualizar: () -> Unit,
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val progresso = (passosHoje.toFloat() / MetaPassos).coerceIn(0f, 1f)
    val passosRestantes = (MetaPassos - passosHoje).coerceAtLeast(0)

    LaunchedEffect(Unit) { aoAtualizar() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FundoPassos,
        bottomBar = { BarraInferiorPassos(aoVoltar) }
    ) { espacamento ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacamento)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            CabecalhoPassos(aoVoltar)
            Text(
                text = "Atividade física",
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = TextoPassos,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            SeletorHoje()
            IndicadorPassos(passosHoje, progresso, carregando)
            ResumoPassos(passosRestantes, progresso)
            BotaoTreinosPersonalizados {
                Toast.makeText(contexto, "Treinos personalizados em breve", Toast.LENGTH_SHORT).show()
            }
            DicaAdaPassos()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CabecalhoPassos(aoVoltar: () -> Unit) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().height(58.dp)) {
        Surface(
            modifier = Modifier.size(38.dp).align(Alignment.CenterStart).clickable(onClick = aoVoltar),
            shape = CircleShape,
            color = Color(0xFFEAF2ED)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("‹", color = VerdePassosTela, fontSize = 32.sp, lineHeight = 32.sp)
            }
        }
        Text(
            "PASSOS",
            modifier = Modifier.align(Alignment.Center),
            color = VerdePassosTela,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Image(
            painter = painterResource(R.drawable.relatorio_treino),
            contentDescription = "Relatório de passos",
            modifier = Modifier.size(30.dp).align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun SeletorHoje() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(38.dp),
        color = Color(0xFFEEF1EF),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.weight(1f).padding(3.dp).height(32.dp),
                color = VerdePassosTela,
                shape = RoundedCornerShape(18.dp)
            ) { Box(contentAlignment = Alignment.Center) { Text("Hoje", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }
            Text("7 dias", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = TextoPassosSecundario)
            Text("30 dias", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = TextoPassosSecundario)
        }
    }
}

@Composable
private fun IndicadorPassos(passos: Long, progresso: Float, carregando: Boolean) {
    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(176.dp)) {
            val largura = 11.dp.toPx()
            val margem = largura / 2
            val tamanhoArco = Size(size.width - largura, size.height - largura)
            drawArc(
                color = TrilhoPassos,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(margem, margem),
                size = tamanhoArco,
                style = Stroke(largura)
            )
            if (progresso > 0f) {
                drawArc(
                    color = VerdePassosTela,
                    startAngle = -90f,
                    sweepAngle = 360f * progresso,
                    useCenter = false,
                    topLeft = Offset(margem, margem),
                    size = tamanhoArco,
                    style = Stroke(largura, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.sapato), "Passos", Modifier.size(38.dp))
            Text(
                if (carregando) "--" else formatarNumero(passos),
                color = TextoPassos,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text("passos", color = TextoPassos, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Meta: ${formatarNumero(MetaPassos)}", color = TextoPassosSecundario, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Text("${(progresso * 100).toInt()}%", color = VerdePassosTela, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun ResumoPassos(passosRestantes: Long, progresso: Float) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CartaoResumoPassos("Passos restantes", formatarNumero(passosRestantes), R.drawable.sapato, Modifier.weight(1f))
        CartaoResumoPassos("Meta concluída", "${(progresso * 100).toInt()}%", R.drawable.relatorio_treino, Modifier.weight(1f))
    }
}

@Composable
private fun CartaoResumoPassos(titulo: String, valor: String, icone: Int, modifier: Modifier = Modifier) {
    Surface(modifier.height(102.dp), RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(icone), null, Modifier.size(25.dp))
                Text(titulo, color = TextoPassosSecundario, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Text(valor, color = TextoPassos, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BotaoTreinosPersonalizados(aoClicar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(74.dp).clickable(onClick = aoClicar),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3EAE6))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF126B4B))
                    .padding(9.dp)
            ) {
                Image(painterResource(R.drawable.treino), "Treinos personalizados", Modifier.fillMaxSize())
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Treinos personalizados", color = TextoPassos, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Atividades pensadas para você", color = TextoPassosSecundario, fontSize = 11.sp)
            }
            Text("›", color = VerdePassosTela, fontSize = 27.sp)
        }
    }
}

@Composable
private fun DicaAdaPassos() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        color = Color(0xFFF0F4F1),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.onca_joia),
                contentDescription = "Dica da Ada AI",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(58.dp)
            )
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text("Dica da Ada AI", color = VerdePassosTela, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Continue se exercitando, o exercício físico é vital para uma vida saudável.",
                    color = Color(0xFF42534B),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun BarraInferiorPassos(aoVoltar: () -> Unit) {
    Surface(Modifier.fillMaxWidth().navigationBarsPadding(), color = Color.White, shadowElevation = 6.dp) {
        Row(Modifier.fillMaxWidth().height(62.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            listOf("⌂" to "Início", "⌁" to "Monitorar", "+" to "Adicionar", "▤" to "Exames", "●" to "Perfil").forEachIndexed { indice, item ->
                Column(
                    Modifier.width(58.dp).clickable(enabled = indice == 0, onClick = aoVoltar),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.first, color = if (indice == 1) VerdePassosTela else TextoPassosSecundario, fontSize = if (indice == 2) 25.sp else 20.sp, fontWeight = FontWeight.Bold)
                    Text(item.second, color = if (indice == 1) VerdePassosTela else TextoPassosSecundario, fontSize = 9.sp)
                }
            }
        }
    }
}

private fun formatarNumero(valor: Long): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(valor)
