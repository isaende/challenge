package home_page

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val VerdeEmergencia = Color(0xFF087542)
private val VerdeEscuroEmergencia = Color(0xFF095231)
private val VerdeClaroEmergencia = Color(0xFFEAF7F0)
private val FundoEmergencia = Color(0xFFF7FAF8)
private val VermelhoEmergencia = Color(0xFFD9232E)
private val VermelhoClaroEmergencia = Color(0xFFFFF0F1)
private val TextoEmergencia = Color(0xFF17211C)
private val TextoSuaveEmergencia = Color(0xFF61706A)
private val LinhaEmergencia = Color(0xFFE7EEE9)
private const val LINK_VIDEO_PRIMEIROS_SOCORROS = "https://youtu.be/1kW8i3aXhz0?si=jzxXxGSF7KscDWHY"
private const val LINK_MANUAL_PRIMEIROS_SOCORROS = "https://www.samunoroestepr.com.br/materia/dicas/1-dicas-de-primeiros-socorros"

@Composable
fun TelaEmergencia(
    aoVoltarInicio: () -> Unit,
    aoAbrirExames: () -> Unit,
    modificador: Modifier = Modifier
) {
    val contexto = LocalContext.current

    Scaffold(
        modifier = modificador.fillMaxSize(),
        containerColor = FundoEmergencia,
        bottomBar = {
            BarraInferiorEmergencia(
                aoAbrirInicio = aoVoltarInicio,
                aoAbrirExames = aoAbrirExames
            )
        }
    ) { espaco ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espaco)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CabecalhoEmergencia(aoVoltarInicio = aoVoltarInicio)
            CartaoSosEmergencia()
            NumerosEmergencia()
            BotaoLinkEmergencia(
                titulo = "Video de primeiros socorros",
                subtitulo = "Assista orientacoes rapidas para agir com seguranca.",
                icone = R.drawable.multimidia,
                corFundoIcone = VermelhoClaroEmergencia,
                aoClicar = { abrirLinkEmergencia(contexto, LINK_VIDEO_PRIMEIROS_SOCORROS) }
            )
            BotaoLinkEmergencia(
                titulo = "Manual de primeiros socorros",
                subtitulo = "Consulte dicas para diferentes situacoes.",
                icone = R.drawable.manual_medico,
                corFundoIcone = VerdeClaroEmergencia,
                aoClicar = { abrirLinkEmergencia(contexto, LINK_MANUAL_PRIMEIROS_SOCORROS) }
            )
            CartaoAvisoSeguranca()
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CabecalhoEmergencia(aoVoltarInicio: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = aoVoltarInicio,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            Text("<", color = TextoEmergencia, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Emergencia",
            color = VerdeEscuroEmergencia,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(38.dp))
    }
}

@Composable
private fun CartaoSosEmergencia() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        color = VermelhoEmergencia,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFE12A33), Color(0xFFC91C25))
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 100.dp)
            ) {
                Text("Precisa de ajuda?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "Acesso rapido a emergencias e SOS.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .size(74.dp)
                    .align(Alignment.CenterEnd),
                color = Color.White,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("SOS", color = VermelhoEmergencia, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NumerosEmergencia() {
    Column {
        Text(
            "Ligar para emergencia",
            color = TextoEmergencia,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CartaoNumeroEmergencia("SAMU", "192", R.drawable.samu192, Modifier.weight(1f))
            CartaoNumeroEmergencia("Bombeiros", "193", R.drawable.fogueira, Modifier.weight(1f))
            CartaoNumeroEmergencia("Policia", "190", R.drawable.sirene, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CartaoNumeroEmergencia(
    titulo: String,
    numero: String,
    icone: Int,
    modificador: Modifier = Modifier
) {
    Column(
        modifier = modificador,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            color = Color.White,
            shape = CircleShape,
            border = BorderStroke(1.dp, LinhaEmergencia),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(icone),
                    contentDescription = titulo,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
        Text(
            titulo,
            color = TextoEmergencia,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(numero, color = VermelhoEmergencia, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun BotaoLinkEmergencia(
    titulo: String,
    subtitulo: String,
    icone: Int,
    corFundoIcone: Color,
    aoClicar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clickable(onClick = aoClicar),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LinhaEmergencia),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = corFundoIcone,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(icone),
                        contentDescription = titulo,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, color = TextoEmergencia, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitulo, color = TextoSuaveEmergencia, fontSize = 11.sp, lineHeight = 14.sp)
            }
            Text(">", color = VerdeEmergencia, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CartaoAvisoSeguranca() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = VermelhoClaroEmergencia,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = Color.White,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("!", color = VermelhoEmergencia, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Sua seguranca e nossa prioridade.", color = TextoEmergencia, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Estamos aqui para te ajudar.", color = TextoSuaveEmergencia, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun BarraInferiorEmergencia(
    aoAbrirInicio: () -> Unit,
    aoAbrirExames: () -> Unit
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
            ItemMenuEmergencia("Inicio", R.drawable.house_building, false, aoAbrirInicio)
            ItemMenuEmergencia("Favoritos", R.drawable.heart_green, false, {})
            FloatingActionButton(
                onClick = {},
                containerColor = VerdeEmergencia,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light)
            }
            ItemMenuEmergencia("Exames", R.drawable.list, false, aoAbrirExames)
            ItemMenuEmergencia("Menu", R.drawable.arrow, false, {})
        }
    }
}

@Composable
private fun ItemMenuEmergencia(
    titulo: String,
    icone: Int,
    selecionado: Boolean,
    aoClicar: () -> Unit
) {
    val corItem = if (selecionado) VerdeEmergencia else TextoSuaveEmergencia
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
                .clip(RoundedCornerShape(4.dp))
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

private fun abrirLinkEmergencia(contexto: Context, url: String) {
    val intencao = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching {
        contexto.startActivity(intencao)
    }
}
