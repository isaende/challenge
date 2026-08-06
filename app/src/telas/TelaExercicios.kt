package home_page

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.text.NumberFormat
import java.util.Locale

// meta de passos fixa, futuramente ajustar de acordo com a idade do usuario
private const val MetaPassos = 10_000L

@Composable
fun TelaExercicios(
    passosHoje: Long,
    carregando: Boolean,
    aoAtualizar: () -> Unit,
    aoIniciarTreino: () -> Unit,
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {

    //calcula o progresso dividindo os passos de hoje pela meta e os passos restantes subtraindo os passos de voce pela meta, o 
    val progresso = (passosHoje.toFloat() / MetaPassos).coerceIn(0f, 1f)
    val passosRestantes = (MetaPassos - passosHoje).coerceAtLeast(0)

// atualiza o health connect ao abrir a tela
    LaunchedEffect(Unit) { aoAtualizar() }


// esqueleto da tela, contem a cor de fundo base, espaçamentos, fontes, etc
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            CabecalhoExercicios(aoVoltar)
            Text(
                text = "Atividade física",
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = textoPrimario,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            SeletorHoje()
            IndicadorPassos(passosHoje, progresso, carregando)
            ResumoPassos(passosRestantes, progresso)
            BotaoIniciarTreino(aoIniciarTreino)
            DicaAdaExercicios()
            Spacer(Modifier.height(16.dp))
        }
    }
}

// titulo com icone ao lado
@Composable
private fun CabecalhoExercicios(aoVoltar: () -> Unit) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().height(58.dp)) {
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
            "EXERCÍCIOS",
            modifier = Modifier.align(Alignment.Center),
            color = verdePrincipal,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Image(
            painter = painterResource(R.drawable.relatorio_treino),
            contentDescription = "Relatório de exercícios",
            modifier = Modifier.size(30.dp).align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun SeletorHoje() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(38.dp),
        color = fundoSecundario,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.weight(1f).padding(3.dp).height(32.dp),
                color = verdePrincipal,
                shape = RoundedCornerShape(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Hoje", color = branco, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Text("7 dias", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = textoSecundario)
            Text("30 dias", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = textoSecundario)
        }
    }
}


// indicador de passos com gráfico circular que acompanha a quantidade
@Composable
private fun IndicadorPassos(passos: Long, progresso: Float, carregando: Boolean) {
    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(176.dp)) {
            val largura = 11.dp.toPx()
            val margem = largura / 2
            val tamanhoArco = Size(size.width - largura, size.height - largura)
            drawArc(
                color = borda,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(margem, margem),
                size = tamanhoArco,
                style = Stroke(largura)
            )
            if (progresso > 0f) {
                drawArc(
                    color = verdePrincipal,
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
                color = textoPrimario,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text("passos", color = textoPrimario, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Meta: ${formatarNumero(MetaPassos)}", color = textoSecundario, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Text("${(progresso * 100).toInt()}%", color = verdePrincipal, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

// mostra quantos passos faltam
@Composable
private fun ResumoPassos(passosRestantes: Long, progresso: Float) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CartaoResumoPassos("Passos restantes", formatarNumero(passosRestantes), R.drawable.sapato, Modifier.weight(1f))
        CartaoResumoPassos("Meta concluída", "${(progresso * 100).toInt()}%", R.drawable.relatorio_treino, Modifier.weight(1f))
    }
}


@Composable
private fun CartaoResumoPassos(titulo: String, valor: String, icone: Int, modifier: Modifier = Modifier) {
    Surface(modifier.height(102.dp), RoundedCornerShape(12.dp), color = branco, shadowElevation = 1.dp) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(icone), null, Modifier.size(25.dp))
                Text(titulo, color = textoSecundario, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Text(valor, color = textoPrimario, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BotaoIniciarTreino(aoClicar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(74.dp).clickable(onClick = aoClicar),
        color = branco,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borda)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(verdeEscuro)
                    .padding(9.dp)
            ) {
                Image(painterResource(R.drawable.treino), "Treinos personalizados", Modifier.fillMaxSize())
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Iniciar treino", color = textoPrimario, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Abra seu treino personalizado de hoje", color = textoSecundario, fontSize = 11.sp)
            }
            Text("›", color = verdePrincipal, fontSize = 27.sp)
        }
    }
}

@Composable
private fun DicaAdaExercicios() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        color = fundoSecundario,
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
                Text("Dica da Ada AI", color = verdePrincipal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Continue se exercitando, o exercício físico é vital para uma vida saudável.",
                    color = textoSecundario,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

// função para transforma 10000 em 10.000 para facilicar a visualização
private fun formatarNumero(valor: Long): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(valor)
