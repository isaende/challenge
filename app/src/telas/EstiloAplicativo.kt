package home_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection

enum class DirecaoSeta { VOLTAR, AVANCAR }

// Indicador visual para cards cuja superficie inteira ja recebe o clique.
@Composable
fun SetaAplicativo(
    direcao: DirecaoSeta = DirecaoSeta.AVANCAR,
    modifier: Modifier = Modifier
) {
    val apontaParaEsquerda = (direcao == DirecaoSeta.VOLTAR) !=
        (LocalLayoutDirection.current == LayoutDirection.Rtl)
    Canvas(modifier.size(34.dp).background(verdeClaro, CircleShape)) {
        val sentido = if (apontaParaEsquerda) -1f else 1f
        val metadeLargura = 2.dp.toPx()
        val metadeAltura = 4.dp.toPx()
        val caminho = Path().apply {
            moveTo(center.x - sentido * metadeLargura, center.y - metadeAltura)
            lineTo(center.x + sentido * metadeLargura, center.y)
            lineTo(center.x - sentido * metadeLargura, center.y + metadeAltura)
        }
        drawPath(
            path = caminho,
            color = verdePrincipal,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun BotaoSetaAplicativo(
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    direcao: DirecaoSeta = DirecaoSeta.VOLTAR,
    descricao: String = if (direcao == DirecaoSeta.VOLTAR) "Voltar" else "Avançar",
    habilitado: Boolean = true
) {
    IconButton(
        onClick = aoClicar,
        enabled = habilitado,
        modifier = modifier.size(48.dp).semantics { contentDescription = descricao }
    ) {
        SetaAplicativo(direcao, Modifier.alpha(if (habilitado) 1f else 0.38f))
    }
}

val TipografiaAplicativo = Typography(
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp)
)

@Composable
fun BalaoDicaAda(
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp),
        color = branco,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borda),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(titulo, color = verdePrincipal, style = TipografiaAplicativo.titleSmall)
            Text(
                texto,
                color = textoSecundario,
                style = TipografiaAplicativo.bodySmall
            )
        }
    }
}
