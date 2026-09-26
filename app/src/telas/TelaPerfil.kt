package home_page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.Period
import java.util.Locale
import kotlinx.coroutines.CancellationException

@Composable
fun TelaPerfil(
    perfilId: Long,
    aoVoltar: () -> Unit,
    aoAbrirInicio: () -> Unit,
    aoAbrirMonitorar: () -> Unit,
    aoAbrirExames: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAbrirPerfilAdaptativo: () -> Unit,
    aoPerfilCarregado: (SessaoUsuario) -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dados by remember(perfilId) { mutableStateOf<DadosPerfilApi?>(null) }
    var carregando by remember(perfilId) { mutableStateOf(true) }
    var erro by remember(perfilId) { mutableStateOf(false) }
    var tentativa by remember(perfilId) { mutableIntStateOf(0) }
    var mostrarConta by rememberSaveable(perfilId) { mutableStateOf(false) }
    val aoCarregarAtual by rememberUpdatedState(aoPerfilCarregado)

    BackHandler(onBack = aoVoltar)
    LaunchedEffect(perfilId, tentativa) {
        carregando = true
        erro = false
        try {
            val resposta = ApiAda.carregarPerfil(perfilId)
            dados = resposta
            aoCarregarAtual(resposta.auth)
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (_: Exception) {
            erro = true
        } finally {
            carregando = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = fundoPrincipal,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            Column {
                OutlinedButton(
                    onClick = aoSair,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, vermelhoPrincipal),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = transparente,
                        contentColor = vermelhoPrincipal
                    )
                ) {
                    Text("Sair da conta", fontWeight = FontWeight.SemiBold)
                }
                BarraInferiorPadrao(
                    itemSelecionado = ItemBarraInferior.PERFIL,
                    aoAbrirInicio = aoAbrirInicio,
                    aoAbrirMonitorar = aoAbrirMonitorar,
                    aoAdicionar = aoAdicionar,
                    aoAbrirExames = aoAbrirExames
                )
            }
        }
    ) { espaco ->
        Column(Modifier.fillMaxSize().padding(espaco)) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(verdeEscuro, verdePrincipal)))
                    .statusBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotaoSetaAplicativo(aoClicar = aoVoltar)
                Text(
                    "Meu Perfil", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    color = branco, fontSize = 22.sp, fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = { mostrarConta = true }, enabled = dados != null,
                    modifier = Modifier.semantics { contentDescription = "Informações da conta" }
                ) {
                    Text("⚙", color = branco, fontSize = 25.sp)
                }
            }

            when {
                carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = verdePrincipal)
                        Text("Carregando seu perfil…", color = textoSecundario)
                    }
                }
                erro -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Não foi possível carregar seu perfil. Verifique a conexão e tente novamente.",
                            color = textoSecundario, textAlign = TextAlign.Center
                        )
                        Button(onClick = { tentativa++ }, colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal)) {
                            Text("Tentar novamente")
                        }
                    }
                }
                else -> dados?.let { perfil ->
                    ConteudoPerfil(perfil, aoAbrirPerfilAdaptativo)
                }
            }
        }
    }

    if (mostrarConta) {
        AlertDialog(
            onDismissRequest = { mostrarConta = false },
            containerColor = branco,
            title = { Text("Minha conta", color = verdePrincipal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(dados?.perfil?.nomeCompleto.orEmpty(), color = textoPrimario)
                    Text(dados?.auth?.email.orEmpty(), color = textoSecundario)
                    Text("Seus dados de perfil são os informados no cadastro.", color = textoSecundario)
                }
            },
            confirmButton = { TextButton(onClick = { mostrarConta = false }) { Text("Fechar", color = verdePrincipal) } }
        )
    }
}

@Composable
private fun ConteudoPerfil(dados: DadosPerfilApi, aoAbrirPerfilAdaptativo: () -> Unit) {
    val perfil = dados.perfil
    val localidade = Locale.forLanguageTag("pt-BR")
    val idade = runCatching {
        val nascimento = LocalDate.parse(perfil.dataNascimento)
        val hoje = LocalDate.now()
        if (nascimento.isAfter(hoje)) null else Period.between(nascimento, hoje).years
    }.getOrNull()
    val altura = dados.medidas?.alturaCm?.takeIf { it.isFinite() && it > 0 }?.let {
        String.format(localidade, "%.2f m", it / 100)
    } ?: "Não informada"
    val peso = dados.medidas?.pesoKg?.takeIf { it.isFinite() && it > 0 }?.let {
        "${NumberFormat.getNumberInstance(localidade).format(it)} kg"
    } ?: "Não informado"

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(verdeClaro.copy(alpha = 0.65f), fundoPrincipal)))
            .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = CircleShape, color = branco, border = BorderStroke(3.dp, branco), modifier = Modifier.size(88.dp)) {
                Image(
                    painterResource(R.drawable.user), contentDescription = "Avatar do usuário",
                    contentScale = ContentScale.Crop, modifier = Modifier.padding(3.dp).clip(CircleShape)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    perfil.nomeCompleto.ifBlank { "Usuário" }, color = verdePrincipal,
                    fontSize = 22.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold
                )
                Surface(shape = RoundedCornerShape(24.dp), color = verdeClaro) {
                    Text("Usuário", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = verdeEscuro, style = TipografiaAplicativo.bodySmall)
                }
            }
        }
        Text(
            "Construindo uma vida mais saudável, equilibrada e com propósito. 🌿",
            color = verdeEscuro, style = TipografiaAplicativo.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        CartaoPerfil {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InformacaoPerfil("Idade", idade?.let { "$it ${if (it == 1) "ano" else "anos"}" } ?: "Não informada", R.drawable.agenda_perfil)
                InformacaoPerfil("Altura", altura, R.drawable.altura)
                InformacaoPerfil("Peso", peso, R.drawable.peso)
                InformacaoPerfil("Gênero", perfil.genero.ifBlank { "Não informado" }, R.drawable.genero)
            }
        }
        CartaoPerfil {
            InformacaoPerfil("Localização", "São Paulo - SP", R.drawable.localizacao)
        }
        CartaoPerfil {
            InformacaoPerfil("Objetivo", perfil.objetivoPrincipal.ifBlank { "Não informado" }, R.drawable.metas)
        }
        Surface(
            onClick = aoAbrirPerfilAdaptativo,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = branco
        ) {
            Row(
                Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Perfil adaptativo", color = verdePrincipal, style = TipografiaAplicativo.titleMedium)
                    Text(
                        "Acesse o Modo Sênior e os atalhos para cuidar da sua saúde.",
                        color = textoSecundario, style = TipografiaAplicativo.bodySmall
                    )
                }
                SetaAplicativo()
            }
        }
    }
}

@Composable
private fun CartaoPerfil(conteudo: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = branco) {
        Box(Modifier.padding(18.dp)) { conteudo() }
    }
}

@Composable
private fun InformacaoPerfil(titulo: String, valor: String, icone: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(48.dp).background(verdeClaro, CircleShape), contentAlignment = Alignment.Center) {
            Image(
                painterResource(icone), contentDescription = null,
                modifier = Modifier.size(27.dp), contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(verdeEscuro)
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(titulo, color = textoSecundario, style = TipografiaAplicativo.bodySmall)
            Text(valor, color = textoPrimario, style = TipografiaAplicativo.bodyLarge)
        }
    }
}
