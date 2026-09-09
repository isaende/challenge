package home_page

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R


data class ItemResumoSaude(
    val titulo: String,
    val valor: String,
    val icone: Int,
    val cor: Color
)

data class ItemAcaoRapida(
    val titulo: String,
    val icone: Int,
    val cor: Color,
    val aoClicar: () -> Unit = {}
)

@Composable
fun TelaInicio(
    aoAbrirChatMedico: () -> Unit,
    aoAbrirTelemedicina: () -> Unit = {},
    aoAbrirExames: () -> Unit = {},
    aoAbrirMedicamentos: () -> Unit = {},
    aoAbrirEmergencia: () -> Unit = {},
    aoAbrirHidratacao: () -> Unit = {},
    aoAbrirExercicios: () -> Unit = {},
    aoAbrirMetas: () -> Unit = {},
    dadosSaude: DadosSaude = DadosSaude(),
    statusSaude: String = "Conecte o Health Connect",
    aoConectarSaude: () -> Unit = {},
    mostrarBotaoConectarSaude: Boolean = true,
    textoBotaoSaude: String = "Conectar",
    nomeUsuario: String = "",
    aoAbrirPerfil: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.INICIO,
                aoAbrirExames = aoAbrirExames,
                aoAbrirPerfil = aoAbrirPerfil
            )
        }
    ) { espacoTela ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacoTela)
                .verticalScroll(rememberScrollState())
        ) {
            CabecalhoInicio(nomeUsuario, aoAbrirPerfil)
            CorpoInicio(
                aoAbrirChatMedico = aoAbrirChatMedico,
                aoAbrirTelemedicina = aoAbrirTelemedicina,
                aoAbrirExames = aoAbrirExames,
                aoAbrirMedicamentos = aoAbrirMedicamentos,
                aoAbrirEmergencia = aoAbrirEmergencia,
                aoAbrirHidratacao = aoAbrirHidratacao,
                aoAbrirExercicios = aoAbrirExercicios,
                aoAbrirMetas = aoAbrirMetas,
                dadosSaude = dadosSaude,
                statusSaude = statusSaude,
                aoConectarSaude = aoConectarSaude,
                mostrarBotaoConectarSaude = mostrarBotaoConectarSaude,
                textoBotaoSaude = textoBotaoSaude
            )
        }
    }
}

@Composable
private fun CabecalhoInicio(nomeUsuario: String, aoAbrirPerfil: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(verdeEscuro, verdePrincipal)
                )
            )
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 10.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Bom dia,",
                color = branco,
                style = TipografiaAplicativo.bodyLarge
            )
            Text(
                text = "${nomeUsuario.substringBefore(' ').ifBlank { "Usuário" }}!",
                color = branco,
                style = TipografiaAplicativo.titleLarge
            )
        }

        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CirculoComSigla(
                sigla = "!",
                corFundo = branco.copy(alpha = 0.18f),
                corTexto = branco,
                tamanho = 32.dp
            )
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Abrir meu perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(branco)
                    .clickable(onClick = aoAbrirPerfil)
            )
        }
    }
}

@Composable
private fun CorpoInicio(
    aoAbrirChatMedico: () -> Unit,
    aoAbrirTelemedicina: () -> Unit,
    aoAbrirExames: () -> Unit,
    aoAbrirMedicamentos: () -> Unit,
    aoAbrirEmergencia: () -> Unit,
    aoAbrirHidratacao: () -> Unit,
    aoAbrirExercicios: () -> Unit,
    aoAbrirMetas: () -> Unit,
    dadosSaude: DadosSaude,
    statusSaude: String,
    aoConectarSaude: () -> Unit,
    mostrarBotaoConectarSaude: Boolean,
    textoBotaoSaude: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CartaoResumoSaude(
            dadosSaude = dadosSaude,
            statusSaude = statusSaude,
            aoConectarSaude = aoConectarSaude,
            mostrarBotaoConectarSaude = mostrarBotaoConectarSaude,
            textoBotaoSaude = textoBotaoSaude
        )
        CartaoAdaAi(aoAbrirChatMedico)
        BlocoAcoesRapidas(
            aoAbrirTelemedicina = aoAbrirTelemedicina,
            aoAbrirExames = aoAbrirExames,
            aoAbrirMedicamentos = aoAbrirMedicamentos,
            aoAbrirEmergencia = aoAbrirEmergencia,
            aoAbrirHidratacao = aoAbrirHidratacao,
            aoAbrirExercicios = aoAbrirExercicios,
            aoAbrirMetas = aoAbrirMetas
        )
    }
}

@Composable
private fun CartaoResumoSaude(
    dadosSaude: DadosSaude,
    statusSaude: String,
    aoConectarSaude: () -> Unit,
    mostrarBotaoConectarSaude: Boolean,
    textoBotaoSaude: String
) {
    val itensResumo = listOf(
        ItemResumoSaude(
            "Batimentos cardiacos",
            dadosSaude.batimentos?.let { "$it bpm" } ?: "-- bpm",
            R.drawable.heart,
            vermelhoPrincipal
        ),
        ItemResumoSaude(
            "Sono",
            dadosSaude.sonoMinutos?.let { minutos -> "${minutos / 60}h ${minutos % 60}m" } ?: "--",
            R.drawable.night,
            roxoPrincipal
        ),
        ItemResumoSaude(
            "Passos",
            dadosSaude.passos?.let { "%,d".format(it).replace(",", ".") } ?: "--",
            R.drawable.footstep,
            verdePrincipal
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp)),
        color = branco,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Resumo da sua saúde",
                        color = textoPrimario,
                        fontWeight = FontWeight.Bold,
                        style = TipografiaAplicativo.titleSmall
                    )
                    Text(
                        text = statusSaude,
                        color = textoSecundario,
                        style = TipografiaAplicativo.bodySmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (mostrarBotaoConectarSaude) {
                    Button(
                        onClick = aoConectarSaude,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = textoBotaoSaude,
                            color = branco,
                            style = TipografiaAplicativo.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itensResumo.forEach { item ->
                    CartaoItemSaude(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoItemSaude(
    item: ItemResumoSaude,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(108.dp),
        color = branco,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borda)
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CirculoComImagem(
                icone = item.icone,
                descricao = item.titulo,
                corFundo = item.cor.copy(alpha = 0.12f),
                tamanho = 30.dp
            )
            Column {
                Text(
                    text = item.titulo,
                    color = textoSecundario,
                    style = TipografiaAplicativo.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = item.valor,
                    color = textoPrimario,
                    fontWeight = FontWeight.Bold,
                    style = TipografiaAplicativo.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
private fun CartaoAdaAi(aoAbrirChatMedico: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 156.dp)
            .clickable(onClick = aoAbrirChatMedico),
        color = verdeEscuro,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    listOf(verdeEscuro, verdePrincipal)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ia),
                        contentDescription = "Ícone do chatbot Ada AI",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ada AI",
                        color = branco,
                        style = TipografiaAplicativo.titleSmall,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = ">",
                        color = branco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "Tire suas dúvidas com a Ada AI, nosso chat bot de saúde personalizado para você",
                    color = branco,
                    style = TipografiaAplicativo.bodyMedium,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = aoAbrirChatMedico,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = branco),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Ver detalhes",
                        color = verdeEscuro,
                        style = TipografiaAplicativo.labelLarge,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

        }
    }
}

@Composable
private fun BlocoAcoesRapidas(
    aoAbrirTelemedicina: () -> Unit,
    aoAbrirExames: () -> Unit,
    aoAbrirMedicamentos: () -> Unit,
    aoAbrirEmergencia: () -> Unit,
    aoAbrirHidratacao: () -> Unit,
    aoAbrirExercicios: () -> Unit,
    aoAbrirMetas: () -> Unit
) {
    val acoes = listOf(
        ItemAcaoRapida("Telemedicina", R.drawable.tele_medicina, verdePrincipal, aoAbrirTelemedicina),
        ItemAcaoRapida("Exames", R.drawable.exames, azulPrincipal, aoAbrirExames),
        ItemAcaoRapida("Medicamentos", R.drawable.medicamentos, verdePrincipal, aoAbrirMedicamentos),
        ItemAcaoRapida("Emergência", R.drawable.emergencia, vermelhoPrincipal, aoAbrirEmergencia),
        ItemAcaoRapida("Hidratação", R.drawable.agua, azulPrincipal, aoAbrirHidratacao),
        ItemAcaoRapida("Exercícios", R.drawable.exercicios, verdePrincipal, aoAbrirExercicios),
        ItemAcaoRapida("Metas", R.drawable.metas, verdePrincipal, aoAbrirMetas)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = branco,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.75.dp, borda)
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ações rápidas",
                    color = textoPrimario,
                    fontWeight = FontWeight.Bold,
                    style = TipografiaAplicativo.titleLarge,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                acoes.chunked(3).forEach { linha ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        linha.forEach { acao ->
                            CartaoAcaoRapida(
                                acao = acao,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - linha.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoAcaoRapida(
    acao: ItemAcaoRapida,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 84.dp)
            .clickable(onClick = acao.aoClicar),
        color = branco,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.75.dp, borda)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(acao.icone),
                contentDescription = acao.titulo,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = acao.titulo,
                color = textoPrimario,
                style = TipografiaAplicativo.bodyLarge,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CirculoComSigla(
    sigla: String,
    corFundo: Color,
    corTexto: Color,
    tamanho: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(corFundo),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sigla,
            color = corTexto,
            fontWeight = FontWeight.Bold,
            fontSize = (tamanho.value / 2.1f).sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CirculoComImagem(
    icone: Int,
    descricao: String,
    corFundo: Color,
    tamanho: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(corFundo)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icone),
            contentDescription = descricao,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
