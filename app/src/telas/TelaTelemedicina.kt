package home_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

// Preencha esta constante quando o atendimento por vídeo estiver disponível.
const val URL_VIDEO_TELECONSULTA = ""

@Composable
fun TelaTelemedicina(
    aoVoltarInicio: () -> Unit,
    aoAbrirChatAda: () -> Unit,
    aoAbrirVideo: (String) -> Unit,
    urlVideo: String = URL_VIDEO_TELECONSULTA
) {
    Scaffold(
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.NENHUM,
                aoAbrirInicio = aoVoltarInicio
            )
        }
    ) { espacoTela ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacoTela)
                .verticalScroll(rememberScrollState())
        ) {
            CabecalhoTelemedicina(aoVoltarInicio)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CartaoDuvidasComAda(aoAbrirChatAda)
                CartaoTeleconsulta(
                    urlDisponivel = urlVideo.isNotBlank(),
                    aoIniciar = { aoAbrirVideo(urlVideo) }
                )
                CartaoDicaAda()
            }
        }
    }
}

@Composable
private fun CabecalhoTelemedicina(aoVoltar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(86.dp)
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = aoVoltar,
            modifier = Modifier
                .size(42.dp)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(verdeClaro)
        ) {
            Text("<", color = verdeEscuro, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TELEMEDICINA",
                color = verdeEscuro,
                style = TipografiaAplicativo.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Fale com um médico quando precisar",
                color = textoSecundario,
                style = TipografiaAplicativo.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CartaoDuvidasComAda(aoAbrirChatAda: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(252.dp),
        color = branco,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borda)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .padding(end = 126.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = verdeClaro,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Mais usado por você",
                        color = verdeEscuro,
                        style = TipografiaAplicativo.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
                Text(
                    text = "Dúvidas com a Ada",
                    color = textoPrimario,
                    style = TipografiaAplicativo.titleLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Receba informações educativas sobre saúde, exames e medicamentos.",
                    color = textoSecundario,
                    style = TipografiaAplicativo.bodySmall
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = aoAbrirChatAda,
                    modifier = Modifier.height(44.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Conversar com a Ada",
                        color = branco,
                        style = TipografiaAplicativo.labelLarge
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.onca_duvida),
                contentDescription = "Ada, assistente de saúde",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )
        }
    }
}

@Composable
private fun CartaoTeleconsulta(
    urlDisponivel: Boolean,
    aoIniciar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(282.dp),
        color = verdeClaro,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borda)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .padding(end = 142.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Atendimento online",
                    color = verdePrincipal,
                    style = TipografiaAplicativo.labelMedium
                )
                Text(
                    text = "Teleconsulta com médico",
                    color = textoPrimario,
                    style = TipografiaAplicativo.titleLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Converse por vídeo com um profissional de saúde.",
                    color = textoSecundario,
                    style = TipografiaAplicativo.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = aoIniciar,
                    enabled = urlDisponivel,
                    modifier = Modifier.height(44.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = verdePrincipal,
                        disabledContainerColor = verdePrincipal.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (urlDisponivel) "Iniciar teleconsulta" else "Disponível em breve",
                        color = branco,
                        style = TipografiaAplicativo.labelLarge
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.medico),
                contentDescription = "Medico da teleconsulta",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(184.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp)
            )
        }
    }
}

@Composable
private fun CartaoDicaAda() {
    BalaoDicaAda(
        titulo = "Dica da Ada AI",
        texto = "Tire suas dúvidas rápidas antes que pequenos sintomas se tornem grandes problemas."
    )
}
