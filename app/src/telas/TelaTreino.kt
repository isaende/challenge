package home_page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private data class TreinoDoDia(
    val titulo: String,
    val detalhe: String,
    val imagem: Int? = null,
    val descanso: Boolean = false
)

@Composable
fun TelaTreino(
    aoVoltar: () -> Unit,
    aoVoltarInicio: () -> Unit,
    perfilId: Long,
    modifier: Modifier = Modifier
) {
    var hoje by remember { mutableStateOf(LocalDate.now()) }
    val treino = remember(hoje) { treinoPara(hoje.dayOfWeek) }
    var concluido by remember(perfilId, hoje) { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var tentativa by remember { mutableStateOf(0) }
    val escopo = rememberCoroutineScope()
    BackHandler(onBack = aoVoltar)
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            hoje = LocalDate.now()
        }
    }
    LaunchedEffect(perfilId, hoje, tentativa) {
        carregando = true
        erro = null
        try {
            concluido = ApiAda.listarTreinos(perfilId).any { it.dataTreino == hoje.toString() && it.concluidoEm != null }
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (_: Exception) {
            erro = "Não foi possível carregar o treino. Tente novamente."
        } finally {
            carregando = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.MONITORAR,
                aoAbrirInicio = aoVoltarInicio
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
            CabecalhoTreino(aoVoltar)
            Text(
                text = "Meu treino",
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = textoPrimario,
                fontWeight = FontWeight.Bold,
                style = TipografiaAplicativo.titleLarge,
                textAlign = TextAlign.Center
            )
            CartaoPlanoPersonalizado()
            ConteudoTreinoDoDia(treino)
            if (carregando || salvando) LinearProgressIndicator(Modifier.fillMaxWidth(), color = verdePrincipal)
            erro?.let {
                Text(it, color = vermelhoPrincipal, style = TipografiaAplicativo.bodySmall)
                TextButton(onClick = { tentativa++ }) { Text("Tentar novamente") }
            }
            Button(
                onClick = {
                    salvando = true
                    escopo.launch {
                        try {
                            val registros = ApiAda.marcarTreino(perfilId, hoje, !concluido)
                            concluido = registros.any { it.dataTreino == hoje.toString() && it.concluidoEm != null }
                        } catch (cancelamento: CancellationException) {
                            throw cancelamento
                        } catch (_: Exception) {
                            erro = "Não foi possível salvar o treino. Tente novamente."
                        } finally {
                            salvando = false
                        }
                    }
                },
                enabled = !carregando && !salvando && erro == null && !treino.descanso,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 18.dp).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (concluido) verdeEscuro else verdePrincipal
                )
            ) {
                Text(
                    if (concluido) "Treino concluído" else "Marcar como concluído",
                    fontWeight = FontWeight.Bold,
                    style = TipografiaAplicativo.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CabecalhoTreino(aoVoltar: () -> Unit) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().height(58.dp)) {
        BotaoSetaAplicativo(aoClicar = aoVoltar, modifier = Modifier.align(Alignment.CenterStart))
        Text(
            text = "TREINOS PERSONALIZADOS",
            modifier = Modifier.align(Alignment.Center),
            color = verdePrincipal,
            fontWeight = FontWeight.Bold,
            style = TipografiaAplicativo.titleSmall
        )
        Text(
            text = "⋮",
            modifier = Modifier.align(Alignment.CenterEnd),
            color = verdePrincipal,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun CartaoPlanoPersonalizado() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(132.dp),
        shape = RoundedCornerShape(8.dp),
        color = verdePrincipal
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Plano personalizado", color = branco, style = TipografiaAplicativo.titleMedium)
                Text("Semanal", color = verdeClaro, style = TipografiaAplicativo.bodySmall, modifier = Modifier.padding(top = 4.dp))
                Surface(
                    modifier = Modifier.padding(top = 12.dp).width(112.dp).height(36.dp),
                    color = transparente,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borda)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Ver detalhes", color = branco, style = TipografiaAplicativo.labelMedium, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConteudoTreinoDoDia(treino: TreinoDoDia) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        color = branco,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text("Treino de hoje", color = textoPrimario, style = TipografiaAplicativo.titleMedium)
            Text(treino.titulo, color = verdePrincipal, style = TipografiaAplicativo.bodyMedium, modifier = Modifier.padding(top = 3.dp))
            Text(treino.detalhe, color = textoSecundario, style = TipografiaAplicativo.bodySmall, modifier = Modifier.padding(top = 3.dp, bottom = 12.dp))
            treino.imagem?.let { imagem ->
                Image(
                    painter = painterResource(imagem),
                    contentDescription = treino.titulo,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(branco)
                )
            }
            if (treino.descanso) {
                Text(
                    text = "Recupere as energias para o próximo treino.",
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    color = textoSecundario,
                    style = TipografiaAplicativo.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun treinoPara(dia: DayOfWeek): TreinoDoDia {
    return when (dia) {
        DayOfWeek.MONDAY -> TreinoDoDia("Treino funcional", "20–25 min • Intermediário", R.drawable.treino_segunda)
        DayOfWeek.TUESDAY -> TreinoDoDia("Força e estabilidade", "20–25 min • Intermediário", R.drawable.treino_terca)
        DayOfWeek.WEDNESDAY -> TreinoDoDia("Dia de descanso", "Recuperação e bem-estar", descanso = true)
        DayOfWeek.THURSDAY -> TreinoDoDia("Cardio", "45–50 min • Intermediário", R.drawable.treino_quinta)
        DayOfWeek.FRIDAY -> TreinoDoDia("Força e mobilidade", "20–25 min • Intermediário", R.drawable.treino_sexta)
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY -> TreinoDoDia("Dia de descanso", "Recuperação e bem-estar", descanso = true)
    }
}
