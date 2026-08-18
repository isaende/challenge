package home_page

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R

enum class ItemBarraInferior {
    INICIO,
    MONITORAR,
    EXAMES,
    PERFIL,
    NENHUM
}

private data class ItemMenuPadrao(
    val titulo: String,
    val icone: Int,
    val selecionado: Boolean,
    val redondo: Boolean = false,
    val aoClicar: () -> Unit
)

@Composable
fun BarraInferiorPadrao(
    itemSelecionado: ItemBarraInferior,
    aoAbrirInicio: () -> Unit = {},
    aoAbrirMonitorar: () -> Unit = {},
    aoAdicionar: () -> Unit = {},
    aoAbrirExames: () -> Unit = {},
    aoAbrirPerfil: () -> Unit = {}
) {
    val itensMenu = listOf(
        ItemMenuPadrao("Inicio", R.drawable.house_building, itemSelecionado == ItemBarraInferior.INICIO, aoClicar = aoAbrirInicio),
        ItemMenuPadrao("Monitorar", R.drawable.arrow, itemSelecionado == ItemBarraInferior.MONITORAR, aoClicar = aoAbrirMonitorar),
        ItemMenuPadrao("Exames", R.drawable.list, itemSelecionado == ItemBarraInferior.EXAMES, aoClicar = aoAbrirExames),
        ItemMenuPadrao("Perfil", R.drawable.user, itemSelecionado == ItemBarraInferior.PERFIL, redondo = true, aoClicar = aoAbrirPerfil)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = branco,
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
            BotaoBarraInferior(itensMenu[0])
            BotaoBarraInferior(itensMenu[1])
            FloatingActionButton(
                onClick = aoAdicionar,
                containerColor = verdePrincipal,
                contentColor = branco,
                modifier = Modifier.size(48.dp)
            ) {
                Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light)
            }
            BotaoBarraInferior(itensMenu[2])
            BotaoBarraInferior(itensMenu[3])
        }
    }
}

@Composable
private fun BotaoBarraInferior(item: ItemMenuPadrao) {
    val corItem = if (item.selecionado) verdePrincipal else textoSecundario
    val formatoIcone = if (item.redondo) CircleShape else RoundedCornerShape(4.dp)

    Column(
        modifier = Modifier
            .width(56.dp)
            .clickable(onClick = item.aoClicar),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(formatoIcone),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(item.icone),
                contentDescription = item.titulo,
                contentScale = if (item.redondo) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.titulo,
            color = corItem,
            style = TipografiaAplicativo.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
