package home_page

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R

@Composable
fun TelaPerfilAdaptativo(
    perfilId: Long,
    nomeUsuario: String,
    aoVoltar: () -> Unit,
    aoAbrirMedico: () -> Unit,
    aoAbrirMedicamentos: () -> Unit,
    aoAbrirEmergencia: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val preferencias = remember(contexto, perfilId) {
        contexto.getSharedPreferences("preferencias_perfil_$perfilId", Context.MODE_PRIVATE)
    }
    var ativo by remember(perfilId) { mutableStateOf(preferencias.getBoolean("adaptativo", true)) }
    var mostrarInterface by rememberSaveable { mutableStateOf(false) }
    var mostrarAcompanhamento by rememberSaveable { mutableStateOf(false) }
    val verde = Color(0xFF2F583C)
    val fundoVerde = Color(0xFFF3F6EF)
    val fundoRosa = Color(0xFFFCF2EF)

    fun mudarModo(valor: Boolean) {
        ativo = valor
        preferencias.edit().putBoolean("adaptativo", valor).apply()
    }

    BackHandler(onBack = aoVoltar)
    Column(
        modifier.fillMaxSize().background(branco).safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "PERFIL / ADAPTATIVO (IDOSO)", modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            color = verde, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        )
        Surface(color = fundoVerde, shape = RoundedCornerShape(20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotaoSetaAplicativo(aoClicar = aoVoltar, descricao = "Voltar ao perfil")
                Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    Text(nomeUsuario.ifBlank { "Usuário" }, color = verde, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Modo Sênior", color = textoPrimario, fontSize = 12.sp)
                }
                Column(Modifier.padding(end = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked = ativo, onCheckedChange = ::mudarModo,
                        modifier = Modifier.semantics { contentDescription = "Modo Sênior" },
                        colors = SwitchDefaults.colors(checkedThumbColor = branco, checkedTrackColor = verde)
                    )
                    Text(if (ativo) "Ativo" else "Inativo", color = verde, fontSize = 11.sp)
                }
            }
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoAdaptativo("Falar com o médico", "Tire dúvidas e receba orientações", R.drawable.medica,
                fundoVerde, ativo, aoAbrirMedico, Modifier.weight(1f).fillMaxHeight())
            CartaoAdaptativo("Meus remédios", "Veja seus medicamentos e horários", R.drawable.comprimido,
                fundoRosa, ativo, aoAbrirMedicamentos, Modifier.weight(1f).fillMaxHeight())
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoAdaptativo("Interface simplificada", "Customize as telas para melhor uso", R.drawable.user_friendly,
                Color(0xFFFCF9EA), ativo, { mostrarInterface = true }, Modifier.weight(1f).fillMaxHeight())
            CartaoAdaptativo("SOS Família", "Peça ajuda em caso de emergência", R.drawable.sos,
                fundoRosa, ativo, aoAbrirEmergencia, Modifier.weight(1f).fillMaxHeight())
        }
        Surface(onClick = { mostrarAcompanhamento = true }, color = fundoVerde, shape = RoundedCornerShape(22.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(start = 18.dp, end = 12.dp, top = 20.dp, bottom = 20.dp)) {
                    Text("Acompanhamento", color = verde, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Cuidador e apoio no cuidado com sua saúde", color = textoPrimario, fontSize = 12.sp, lineHeight = 17.sp)
                }
                Image(painterResource(R.drawable.cuidador), contentDescription = null,
                    modifier = Modifier.weight(1f).aspectRatio(1f), contentScale = ContentScale.Crop)
            }
        }
    }
    if (mostrarInterface) {
        AlertDialog(
            onDismissRequest = { mostrarInterface = false }, containerColor = branco,
            title = { Text("Interface simplificada", color = verde) },
            text = { Text("O Modo Sênior amplia os ícones e textos dos atalhos desta tela para facilitar a leitura.") },
            confirmButton = {
                TextButton(onClick = { mudarModo(!ativo); mostrarInterface = false }) {
                    Text(if (ativo) "Desativar Modo Sênior" else "Ativar Modo Sênior", color = verde)
                }
            },
            dismissButton = { TextButton(onClick = { mostrarInterface = false }) { Text("Fechar", color = verde) } }
        )
    }
    if (mostrarAcompanhamento) {
        AlertDialog(
            onDismissRequest = { mostrarAcompanhamento = false }, containerColor = branco,
            title = { Text("Acompanhamento", color = verde) },
            text = { Text("O cadastro e o vínculo de cuidadores ainda não estão disponíveis no aplicativo.") },
            confirmButton = { TextButton(onClick = { mostrarAcompanhamento = false }) { Text("Fechar", color = verde) } }
        )
    }
}

@Composable
private fun CartaoAdaptativo(
    titulo: String,
    descricao: String,
    imagem: Int,
    cor: Color,
    modoSenior: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(onClick = aoClicar, modifier = modifier, color = cor, shape = RoundedCornerShape(22.dp)) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(painterResource(imagem), contentDescription = null,
                modifier = Modifier.size(if (modoSenior) 88.dp else 72.dp), contentScale = ContentScale.Fit)
            Text(titulo, color = Color(0xFF2F583C), fontSize = if (modoSenior) 15.sp else 13.sp,
                lineHeight = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(descricao, color = textoPrimario, fontSize = if (modoSenior) 12.sp else 11.sp,
                lineHeight = 16.sp, textAlign = TextAlign.Center)
        }
    }
}
