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

private const val LINK_VIDEO_PRIMEIROS_SOCORROS = "https://youtu.be/1kW8i3aXhz0?si=jzxXxGSF7KscDWHY"
private const val LINK_MANUAL_PRIMEIROS_SOCORROS = "https://www.samunoroestepr.com.br/materia/dicas/1-dicas-de-primeiros-socorros"

// controla o comportamento da tela e o vídeo e o manual
@Composable
fun TelaEmergencia(
    aoVoltarInicio: () -> Unit,
    aoAbrirExames: () -> Unit,
    modificador: Modifier = Modifier
) {
    val contexto = LocalContext.current

    Scaffold(
        modifier = modificador.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.NENHUM,
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
                titulo = "Vídeo de primeiros socorros",
                subtitulo = "Assista orientações rápidas para agir com segurança em casos de emergência.",
                icone = R.drawable.multimidia,
                corFundoIcone = vermelhoClaro,
                aoClicar = { abrirLinkEmergencia(contexto, LINK_VIDEO_PRIMEIROS_SOCORROS) }
            )
            BotaoLinkEmergencia(
                titulo = "Manual de primeiros socorros",
                subtitulo = "Consulte dicas para diferentes situações.",
                icone = R.drawable.manual_medico,
                corFundoIcone = verdeClaro,
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
                .background(branco)
        ) {
            Text("<", color = textoPrimario, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Emergência",
            color = verdeEscuro,
            style = TipografiaAplicativo.titleLarge,
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
            .height(142.dp)
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        color = vermelhoPrincipal,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(vermelhoPrincipal, vermelhoEscuro)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 100.dp)
            ) {
                Text("Precisa de ajuda?", color = branco, style = TipografiaAplicativo.titleMedium)
                Text(
                    "Acesso rápido em caso de emergências.",
                    color = branco,
                    style = TipografiaAplicativo.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .size(74.dp)
                    .align(Alignment.CenterEnd),
                color = branco,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("SOS", color = vermelhoPrincipal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// numeros de emergencia para ligar
@Composable
private fun NumerosEmergencia() {
    Column {
        Text(
            "Ligar para emergencia",
            color = textoPrimario,
            style = TipografiaAplicativo.titleSmall,
            modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CartaoNumeroEmergencia("SAMU", "192", R.drawable.samu192, Modifier.weight(1f))
            CartaoNumeroEmergencia("Bombeiros", "193", R.drawable.fogueira, Modifier.weight(1f))
            CartaoNumeroEmergencia("Polícia", "190", R.drawable.sirene, Modifier.weight(1f))
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
            color = branco,
            shape = CircleShape,
            border = BorderStroke(1.dp, borda),
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
            color = textoPrimario,
            style = TipografiaAplicativo.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(numero, color = vermelhoPrincipal, style = TipografiaAplicativo.titleSmall)
    }
}

// direciona os links 
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
            .height(94.dp)
            .clickable(onClick = aoClicar),
        color = branco,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borda),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(titulo, color = textoPrimario, style = TipografiaAplicativo.titleSmall)
                Text(subtitulo, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            }
            Text(">", color = verdePrincipal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// texto de aviso da tela
@Composable
private fun CartaoAvisoSeguranca() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        color = vermelhoClaro,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = branco,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("!", color = vermelhoPrincipal, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("Sua segurança é nossa prioridade.", color = textoPrimario, style = TipografiaAplicativo.titleSmall)
                Text("Estamos aqui para te ajudar.", color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            }
        }
    }
}

private fun abrirLinkEmergencia(contexto: Context, url: String) {
    val intencao = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching {
        contexto.startActivity(intencao)
    }
}
