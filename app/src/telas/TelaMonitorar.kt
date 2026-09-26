package home_page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TelaMonitorar(
    dadosSaude: DadosSaude,
    statusSaude: String,
    mostrarBotaoConectarSaude: Boolean,
    textoBotaoSaude: String,
    nomeUsuario: String,
    metaPassos: Long,
    aoConectarSaude: () -> Unit,
    aoVoltar: () -> Unit,
    aoAbrirExames: () -> Unit,
    aoAdicionar: () -> Unit
) {
    BackHandler(onBack = aoVoltar)
    val numeros = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
        maximumFractionDigits = 1
    }
    Scaffold(
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.MONITORAR,
                aoAbrirInicio = aoVoltar,
                aoAbrirExames = aoAbrirExames,
                aoAdicionar = aoAdicionar
            )
        }
    ) { espaco ->
        Column(Modifier.fillMaxSize().padding(espaco)) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(verdeEscuro, verdePrincipal)))
                    .statusBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotaoSetaAplicativo(aoClicar = aoVoltar, descricao = "Voltar ao início")
                Text("Monitoramento", color = branco, style = TipografiaAplicativo.titleMedium)
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        "Olá, ${nomeUsuario.substringBefore(' ').ifBlank { "Usuário" }}!",
                        style = TipografiaAplicativo.titleMedium, color = textoPrimario
                    )
                    Text("Acompanhe seus sinais vitais e cuide da sua saúde.",
                        style = TipografiaAplicativo.bodyMedium, color = textoSecundario)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Seus dados de hoje", color = verdePrincipal,
                            style = TipografiaAplicativo.titleSmall)
                        Text(statusSaude, color = textoSecundario,
                            style = TipografiaAplicativo.bodySmall)
                    }
                    if (mostrarBotaoConectarSaude) {
                        Button(onClick = aoConectarSaude,
                            colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal),
                            shape = RoundedCornerShape(24.dp)) {
                            Text(textoBotaoSaude, style = TipografiaAplicativo.labelMedium)
                        }
                    }
                }
                CartaoMonitoramento(
                    "Frequência cardíaca", dadosSaude.batimentos?.let { "$it bpm" },
                    "Última medição de hoje", R.drawable.batimento, vermelhoPrincipal
                )
                val pressao = if (dadosSaude.pressaoSistolica != null && dadosSaude.pressaoDiastolica != null) {
                    "${numeros.format(dadosSaude.pressaoSistolica)} / ${numeros.format(dadosSaude.pressaoDiastolica)} mmHg"
                } else null
                CartaoMonitoramento("Pressão arterial", pressao,
                    "Última medição de hoje", R.drawable.pressao_arterial, verdePrincipal)
                CartaoMonitoramento("Calorias gastas", dadosSaude.caloriasGastas?.let { "${numeros.format(it)} kcal" },
                    "Estimativa: 0,4 kcal por passo", R.drawable.calorias, amareloPrincipal)
                CartaoMonitoramento("Sono", dadosSaude.sonoMinutos?.let { "${it / 60}h ${it % 60}min" },
                    "Total nas últimas 24 horas", R.drawable.sono, roxoPrincipal)
                CartaoMonitoramento("Passos", dadosSaude.passos?.let { "${numeros.format(it)} passos" },
                    if (dadosSaude.passos != null && metaPassos > 0 && dadosSaude.passos >= metaPassos)
                        "Meta de hoje alcançada"
                    else "Meta diária: ${numeros.format(metaPassos)} passos",
                    R.drawable.sapato, verdePrincipal)
                BalaoDicaAda(
                    titulo = "Dica da Ada AI",
                    texto = "Acompanhe seus registros diariamente para conhecer melhor sua rotina."
                )
            }
        }
    }
}

@Composable
private fun CartaoMonitoramento(titulo: String, valor: String?, detalhe: String, icone: Int, cor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = branco,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borda)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = cor.copy(alpha = 0.09f)) {
                Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                    Image(painterResource(icone), contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(titulo, color = verdePrincipal, style = TipografiaAplicativo.labelMedium)
                Text(valor ?: "Sem dados", color = if (valor == null) textoSecundario else textoPrimario,
                    style = TipografiaAplicativo.titleSmall, fontWeight = FontWeight.Bold)
                Text(detalhe, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            }
        }
    }
}
