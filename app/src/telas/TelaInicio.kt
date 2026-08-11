package home_page

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
    dadosSaude: DadosSaude = DadosSaude(),
    statusSaude: String = "Conecte o Health Connect",
    aoConectarSaude: () -> Unit = {},
    mostrarBotaoConectarSaude: Boolean = true,
    textoBotaoSaude: String = "Conectar",
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            BarraInferiorPadrao(
                itemSelecionado = ItemBarraInferior.INICIO,
                aoAbrirExames = aoAbrirExames
            )
        }
    ) { espacoTela ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacoTela)
                .verticalScroll(rememberScrollState())
        ) {
            CabecalhoInicio()
            CorpoInicio(
                aoAbrirChatMedico = aoAbrirChatMedico,
                aoAbrirTelemedicina = aoAbrirTelemedicina,
                aoAbrirExames = aoAbrirExames,
                aoAbrirMedicamentos = aoAbrirMedicamentos,
                aoAbrirEmergencia = aoAbrirEmergencia,
                aoAbrirHidratacao = aoAbrirHidratacao,
                aoAbrirExercicios = aoAbrirExercicios,
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
private fun CabecalhoInicio() {
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
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = "Bom dia,",
                color = branco,
                fontSize = 14.sp
            )
            Text(
                text = "Maria!",
                color = branco,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
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
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(branco)
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
            aoAbrirExercicios = aoAbrirExercicios
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
            .shadow(3.dp, RoundedCornerShape(8.dp)),
        color = branco,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Resumo da sua saude",
                        color = textoPrimario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = statusSaude,
                        color = textoSecundario,
                        fontSize = 10.sp,
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        modifier = modifier.height(96.dp),
        color = branco,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borda)
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
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
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = item.valor,
                    color = textoPrimario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
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
            .height(200.dp)
            .clickable(onClick = aoAbrirChatMedico),
        color = verdeEscuro,
        shape = RoundedCornerShape(8.dp)
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
                    .fillMaxSize()
                    .padding(14.dp)
                    .padding(end = 116.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CirculoComSigla(
                        sigla = "A",
                        corFundo = branco.copy(alpha = 0.20f),
                        corTexto = branco,
                        tamanho = 26.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ada AI",
                        color = branco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = ">",
                        color = branco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Text(
                    text = "Detectamos alteracao leve no seu padrao de sono esta semana.",
                    color = branco,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = aoAbrirChatMedico,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = branco),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Ver detalhes",
                        color = verdeEscuro,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Image(
                painter = painterResource(R.drawable.onca),
                contentDescription = "Ada AI",
                modifier = Modifier
                    .size(104.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .clip(CircleShape)
                    .background(verdeClaro)
            )
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
    aoAbrirExercicios: () -> Unit
) {
    val acoes = listOf(
        ItemAcaoRapida("Telemedicina", R.drawable.buy_online, verdePrincipal, aoAbrirTelemedicina),
        ItemAcaoRapida("Exames", R.drawable.doctor_notes, azulPrincipal, aoAbrirExames),
        ItemAcaoRapida("Medicamentos", R.drawable.drugs, verdePrincipal, aoAbrirMedicamentos),
        ItemAcaoRapida("Emergencia", R.drawable.medicine, vermelhoPrincipal, aoAbrirEmergencia),
        ItemAcaoRapida("Hidratação", R.drawable.agua, azulPrincipal, aoAbrirHidratacao),
        ItemAcaoRapida("Exercícios", R.drawable.relatorio_treino, verdePrincipal, aoAbrirExercicios)
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Acoes rapidas",
                color = textoPrimario,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Editar",
                color = verdePrincipal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            acoes.chunked(4).forEach { linha ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    linha.forEach { acao ->
                        CartaoAcaoRapida(
                            acao = acao,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - linha.size) {
                        Spacer(Modifier.weight(1f))
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
            .height(98.dp)
            .clickable(onClick = acao.aoClicar),
        color = branco,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CirculoComImagem(
                icone = acao.icone,
                descricao = acao.titulo,
                corFundo = acao.cor.copy(alpha = 0.12f),
                tamanho = 40.dp
            )
            Text(
                text = acao.titulo,
                color = textoPrimario,
                fontSize = 9.sp,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Clip
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
