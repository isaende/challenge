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

private val VerdePrincipal = Color(0xFF007A4D)
private val VerdeEscuro = Color(0xFF005C3A)
private val VerdeClaro = Color(0xFFE8F7EF)
private val FundoTela = Color(0xFFF7FAF8)
private val TextoPrincipal = Color(0xFF17211C)
private val TextoSecundario = Color(0xFF61706A)
private val VermelhoSaude = Color(0xFFE84B4B)
private val RoxoSono = Color(0xFF6750A4)
private val VerdePassos = Color(0xFF168C67)

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

data class ItemMenuInicio(
    val titulo: String,
    val icone: Int,
    val selecionado: Boolean = false,
    val redondo: Boolean = false,
    val aoClicar: () -> Unit = {}
)

@Composable
fun TelaInicio(
    aoAbrirChatMedico: () -> Unit,
    aoAbrirExames: () -> Unit = {},
    dadosSaude: DadosSaude = DadosSaude(),
    statusSaude: String = "Conecte o Health Connect",
    aoConectarSaude: () -> Unit = {},
    mostrarBotaoConectarSaude: Boolean = true,
    textoBotaoSaude: String = "Conectar",
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FundoTela,
        bottomBar = { BarraMenuInferior(aoAbrirExames = aoAbrirExames) }
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
                aoAbrirExames = aoAbrirExames,
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
                    listOf(VerdeEscuro, VerdePrincipal)
                )
            )
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 10.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = "Bom dia,",
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "Maria!",
                color = Color.White,
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
                corFundo = Color.White.copy(alpha = 0.18f),
                corTexto = Color.White,
                tamanho = 32.dp
            )
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun CorpoInicio(
    aoAbrirChatMedico: () -> Unit,
    aoAbrirExames: () -> Unit,
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
        BlocoAcoesRapidas(aoAbrirExames = aoAbrirExames)
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
            VermelhoSaude
        ),
        ItemResumoSaude(
            "Sono",
            dadosSaude.sonoMinutos?.let { minutos -> "${minutos / 60}h ${minutos % 60}m" } ?: "--",
            R.drawable.night,
            RoxoSono
        ),
        ItemResumoSaude(
            "Passos",
            dadosSaude.passos?.let { "%,d".format(it).replace(",", ".") } ?: "--",
            R.drawable.footstep,
            VerdePassos
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(8.dp)),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Resumo da sua saude",
                        color = TextoPrincipal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = statusSaude,
                        color = TextoSecundario,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (mostrarBotaoConectarSaude) {
                    Button(
                        onClick = aoConectarSaude,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = textoBotaoSaude,
                            color = Color.White,
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
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6ECE9))
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
                    color = TextoSecundario,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = item.valor,
                    color = TextoPrincipal,
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
        color = VerdeEscuro,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF004B33), Color(0xFF00875A))
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
                        corFundo = Color.White.copy(alpha = 0.20f),
                        corTexto = Color.White,
                        tamanho = 26.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ada AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = ">",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Text(
                    text = "Detectamos alteracao leve no seu padrao de sono esta semana.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = aoAbrirChatMedico,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Ver detalhes",
                        color = VerdeEscuro,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Image(
                painter = painterResource(R.drawable.ada_onca),
                contentDescription = "Ada AI",
                modifier = Modifier
                    .size(104.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .clip(CircleShape)
                    .background(VerdeClaro)
            )
        }
    }
}

@Composable
private fun BlocoAcoesRapidas(aoAbrirExames: () -> Unit) {
    val acoes = listOf(
        ItemAcaoRapida("Telemedicina", R.drawable.buy_online, VerdePrincipal),
        ItemAcaoRapida("Exames", R.drawable.doctor_notes, Color(0xFF1F7A8C), aoAbrirExames),
        ItemAcaoRapida("Medicamentos", R.drawable.drugs, Color(0xFF2F7D32)),
        ItemAcaoRapida("Emergencia", R.drawable.medicine, Color(0xFFD32F2F))
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Acoes rapidas",
                color = TextoPrincipal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Editar",
                color = VerdePrincipal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            acoes.forEach { acao ->
                CartaoAcaoRapida(
                    acao = acao,
                    modifier = Modifier.weight(1f)
                )
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
        color = Color.White,
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
                color = TextoPrincipal,
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
private fun BarraMenuInferior(aoAbrirExames: () -> Unit) {
    val itensMenu = listOf(
        ItemMenuInicio("Inicio", R.drawable.house_building, true),
        ItemMenuInicio("Monitorar", R.drawable.arrow),
        ItemMenuInicio("Adicionar", R.drawable.logo, true, redondo = true),
        ItemMenuInicio("Exames", R.drawable.list, aoClicar = aoAbrirExames),
        ItemMenuInicio("Perfil", R.drawable.user, redondo = true)
    )

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
            itensMenu.forEach { item ->
                if (item.titulo == "Adicionar") {
                    FloatingActionButton(
                        onClick = item.aoClicar,
                        containerColor = VerdePrincipal,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light)
                    }
                } else {
                    BotaoMenuInferior(item)
                }
            }
        }
    }
}

@Composable
private fun BotaoMenuInferior(item: ItemMenuInicio) {
    val corItem = if (item.selecionado) VerdePrincipal else TextoSecundario
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
            fontSize = 9.sp,
            lineHeight = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
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
