package home_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.time.DayOfWeek
import java.time.LocalDate

private val VerdeTreino = Color(0xFF006C43)
private val FundoTreino = Color(0xFFF7FAF8)
private val TextoTreino = Color(0xFF17211C)
private val TextoTreinoSecundario = Color(0xFF61706A)

private data class TreinoDoDia(
    val titulo: String,
    val detalhe: String,
    val imagem: Int,
    val descanso: Boolean = false
)

@Composable
fun TelaTreino(
    aoVoltar: () -> Unit,
    aoVoltarInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val treino = remember { treinoPara(LocalDate.now().dayOfWeek) }
    var concluido by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FundoTreino,
        bottomBar = { BarraInferiorTreino(aoVoltarInicio) }
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
                color = TextoTreino,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            CartaoPlanoPersonalizado()
            ConteudoTreinoDoDia(treino)
            Button(
                onClick = { concluido = !concluido },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 18.dp).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (concluido) Color(0xFF38584A) else VerdeTreino
                )
            ) {
                Text(
                    if (concluido) "Treino concluído" else "Marcar como concluído",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CabecalhoTreino(aoVoltar: () -> Unit) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().height(58.dp)) {
        Surface(
            modifier = Modifier.size(38.dp).align(Alignment.CenterStart).clickable(onClick = aoVoltar),
            shape = CircleShape,
            color = Color(0xFFEAF2ED)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("‹", color = VerdeTreino, fontSize = 32.sp, lineHeight = 32.sp)
            }
        }
        Text(
            text = "TREINOS PERSONALIZADOS",
            modifier = Modifier.align(Alignment.Center),
            color = VerdeTreino,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = "⋮",
            modifier = Modifier.align(Alignment.CenterEnd),
            color = VerdeTreino,
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
        color = Color(0xFF087449)
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Plano personalizado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Semanal", color = Color(0xFFD7F0E4), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                Surface(
                    modifier = Modifier.padding(top = 12.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFB7E0CB))
                ) {
                    Text("Ver detalhes", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
            Image(
                painter = painterResource(R.drawable.onca_treino),
                contentDescription = "Ada treinando",
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(132.dp).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ConteudoTreinoDoDia(treino: TreinoDoDia) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text("Treino de hoje", color = TextoTreino, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(treino.titulo, color = VerdeTreino, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
            Text(treino.detalhe, color = TextoTreinoSecundario, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp, bottom = 12.dp))
            Image(
                painter = painterResource(treino.imagem),
                contentDescription = treino.titulo,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (treino.descanso) 270.dp else 360.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
            )
            if (treino.descanso) {
                Text(
                    text = "Recupere as energias para o próximo treino.",
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    color = TextoTreinoSecundario,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BarraInferiorTreino(aoVoltarInicio: () -> Unit) {
    Surface(Modifier.fillMaxWidth().navigationBarsPadding(), color = Color.White, shadowElevation = 6.dp) {
        Row(Modifier.fillMaxWidth().height(62.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            listOf("⌂" to "Início", "♡" to "Monitorar", "+" to "Adicionar", "▤" to "Exames", "♙" to "Perfil").forEachIndexed { indice, item ->
                Column(
                    modifier = Modifier.width(58.dp).clickable(enabled = indice == 0, onClick = aoVoltarInicio),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.first, color = if (indice == 1) VerdeTreino else TextoTreinoSecundario, fontSize = if (indice == 2) 25.sp else 20.sp, fontWeight = FontWeight.Bold)
                    Text(item.second, color = if (indice == 1) VerdeTreino else TextoTreinoSecundario, fontSize = 9.sp)
                }
            }
        }
    }
}

private fun treinoPara(dia: DayOfWeek): TreinoDoDia {
    return when (dia) {
        DayOfWeek.MONDAY -> TreinoDoDia("Treino funcional", "20–25 min • Intermediário", R.drawable.treino_segunda)
        DayOfWeek.TUESDAY -> TreinoDoDia("Força e estabilidade", "20–25 min • Intermediário", R.drawable.treino_terca)
        DayOfWeek.WEDNESDAY -> TreinoDoDia("Dia de descanso", "Recuperação e bem-estar", R.drawable.descanso, true)
        DayOfWeek.THURSDAY -> TreinoDoDia("Cardio", "45–50 min • Intermediário", R.drawable.treino_quinta)
        DayOfWeek.FRIDAY -> TreinoDoDia("Força e mobilidade", "20–25 min • Intermediário", R.drawable.treino_sexta)
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY -> TreinoDoDia("Dia de descanso", "Recuperação e bem-estar", R.drawable.descanso, true)
    }
}
