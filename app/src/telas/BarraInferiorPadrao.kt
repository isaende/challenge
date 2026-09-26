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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
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

enum class ItemBarraInferior {
    INICIO,
    MONITORAR,
    EXAMES,
    PERFIL,
    RECOMPENSAS,
    NENHUM
}

// Mantém o acesso ao perfil em todas as telas que usam o menu compartilhado.
val LocalAbrirPerfil = staticCompositionLocalOf<() -> Unit> { {} }
val LocalAbrirMonitorar = staticCompositionLocalOf<() -> Unit> { {} }
val LocalAbrirRecompensas = staticCompositionLocalOf<() -> Unit> { {} }
val LocalAbrirMetas = staticCompositionLocalOf<() -> Unit> { {} }

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
    aoAbrirMonitorar: () -> Unit = LocalAbrirMonitorar.current,
    aoAdicionar: () -> Unit = LocalAbrirMetas.current,
    aoAbrirExames: () -> Unit = {},
    aoAbrirPerfil: () -> Unit = LocalAbrirPerfil.current
) {
    var menuAberto by rememberSaveable { mutableStateOf(false) }
    val abrirRecompensas = LocalAbrirRecompensas.current
    val abrirMetas = LocalAbrirMetas.current
    val itensMenu = listOf(
        ItemMenuPadrao("Inicio", R.drawable.house_building, itemSelecionado == ItemBarraInferior.INICIO, aoClicar = aoAbrirInicio),
        ItemMenuPadrao("Monitorar", R.drawable.arrow, itemSelecionado == ItemBarraInferior.MONITORAR, aoClicar = aoAbrirMonitorar),
        ItemMenuPadrao("Exames", R.drawable.list, itemSelecionado == ItemBarraInferior.EXAMES, aoClicar = aoAbrirExames),
        ItemMenuPadrao("Menu", R.drawable.ic_menu, menuAberto || itemSelecionado in setOf(ItemBarraInferior.PERFIL, ItemBarraInferior.RECOMPENSAS), aoClicar = { menuAberto = true })
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = branco,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotaoBarraInferior(itensMenu[0], Modifier.weight(1f))
            BotaoBarraInferior(itensMenu[1], Modifier.weight(1f))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = aoAdicionar,
                    containerColor = verdePrincipal,
                    contentColor = branco,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Text("+", fontSize = 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.Light)
                }
            }
            BotaoBarraInferior(itensMenu[2], Modifier.weight(1f))
            Box(Modifier.weight(1f)) {
                BotaoBarraInferior(itensMenu[3], Modifier.fillMaxWidth())
                DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }, containerColor = branco) {
                    DropdownMenuItem(
                        text = { Text("Meu perfil") },
                        leadingIcon = { Image(painterResource(R.drawable.user), null, Modifier.size(24.dp)) },
                        onClick = { menuAberto = false; aoAbrirPerfil() }
                    )
                    DropdownMenuItem(
                        text = { Text("Metas & Hábitos") },
                        leadingIcon = { Image(painterResource(R.drawable.metas), null, Modifier.size(24.dp)) },
                        onClick = { menuAberto = false; abrirMetas() }
                    )
                    DropdownMenuItem(
                        text = { Text("Recompensas") },
                        leadingIcon = { Image(painterResource(R.drawable.trofeu), null, Modifier.size(24.dp)) },
                        onClick = { menuAberto = false; abrirRecompensas() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BotaoBarraInferior(item: ItemMenuPadrao, modifier: Modifier = Modifier) {
    val corItem = if (item.selecionado) verdePrincipal else textoSecundario
    val formatoIcone = if (item.redondo) CircleShape else RoundedCornerShape(4.dp)

    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = item.aoClicar)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
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
        Spacer(Modifier.height(5.dp))
        Text(
            text = item.titulo,
            color = corItem,
            style = TipografiaAplicativo.bodyLarge,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = if (item.selecionado) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
