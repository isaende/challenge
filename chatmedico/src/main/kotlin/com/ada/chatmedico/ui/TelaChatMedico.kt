// Contém a interface em Jetpack Compose da tela "Fale com a Ada".
package com.ada.chatmedico.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ada.chatmedico.R
import com.ada.chatmedico.di.ProvedorChatMedico
import com.ada.chatmedico.dominio.MensagemChat
import com.ada.chatmedico.dominio.PapelMensagem
import com.ada.chatmedico.dominio.TipoRespostaMedica

private val VerdeAda = Color(0xFF007A4D)
private val VerdeAdaEscuro = Color(0xFF004E35)
private val VerdeAdaClaro = Color(0xFFE8F7EF)
private val FundoChat = Color(0xFFF7FAF8)
private val BordaSuave = Color(0xFFE0E8E3)
private val TextoPrincipal = Color(0xFF17211C)
private val TextoSecundario = Color(0xFF61706A)
private val FundoUsuario = Color(0xFF007A4D)
private val FundoAssistente = Color.White
private val FundoUrgencia = Color(0xFFFFF4E5)
private val BordaUrgencia = Color(0xFFFFA133)

@Composable
fun TelaChatMedico(
    modifier: Modifier = Modifier,
    modelo: ChatMedicoViewModel = viewModel(
        factory = ProvedorChatMedico.criarViewModelFactory()
    ),
    aoVoltar: (() -> Unit)? = null,
    aoEscolherExamesGuardados: (() -> Unit)? = null
) {
    val estado by modelo.estado.collectAsStateWithLifecycle()

    ConteudoChatMedico(
        estado = estado,
        aoAlterarTexto = modelo::alterarTextoDigitado,
        aoEnviarMensagem = modelo::enviarMensagem,
        aoVoltar = aoVoltar,
        aoEscolherExamesGuardados = aoEscolherExamesGuardados,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConteudoChatMedico(
    estado: EstadoChatMedico,
    aoAlterarTexto: (String) -> Unit,
    aoEnviarMensagem: () -> Unit,
    modifier: Modifier = Modifier,
    aoVoltar: (() -> Unit)? = null,
    aoEscolherExamesGuardados: (() -> Unit)? = null
) {
    val listaEstado = rememberLazyListState()

    LaunchedEffect(estado.mensagens.size) {
        if (estado.mensagens.isNotEmpty()) {
            listaEstado.animateScrollToItem(estado.mensagens.lastIndex)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FundoChat,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Fale com a Ada",
                        color = TextoPrincipal,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (aoVoltar != null) {
                        IconButton(onClick = aoVoltar) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = TextoPrincipal
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FundoChat
                )
            )
        },
        bottomBar = {
            CampoEnvioMensagem(
                texto = estado.textoDigitado,
                enviando = estado.enviando,
                erroEntrada = estado.erroEntrada,
                aoAlterarTexto = aoAlterarTexto,
                aoEnviarMensagem = aoEnviarMensagem
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CabecalhoAda()

            AvisoEducativo(
                aoEscolherExamesGuardados = aoEscolherExamesGuardados,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listaEstado,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = estado.mensagens,
                    key = { mensagem -> mensagem.id }
                ) { mensagem ->
                    LinhaMensagem(mensagem = mensagem)
                }
            }
        }
    }
}

@Composable
private fun CabecalhoAda() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ada_onca),
            contentDescription = "Ada, assistente de saúde",
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(VerdeAdaClaro)
                .border(width = 1.dp, color = BordaSuave, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "Olá! Eu sou a Ada.",
                color = TextoPrincipal,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Sua assistente de informações educativas em saúde.",
                color = TextoSecundario,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AvisoEducativo(
    aoEscolherExamesGuardados: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = VerdeAdaClaro,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = TEXTO_AVISO_EDUCATIVO,
                color = VerdeAdaEscuro,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (aoEscolherExamesGuardados != null) {
                OutlinedButton(
                    onClick = aoEscolherExamesGuardados,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AttachFile,
                        contentDescription = null,
                        tint = VerdeAda,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Escolher exames guardados")
                }
            }
        }
    }
}

@Composable
private fun LinhaMensagem(mensagem: MensagemChat) {
    val mensagemUsuario = mensagem.papel == PapelMensagem.USUARIO

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensagemUsuario) Arrangement.End else Arrangement.Start
    ) {
        BolhaMensagem(mensagem = mensagem)
    }
}

@Composable
private fun BolhaMensagem(mensagem: MensagemChat) {
    val mensagemUsuario = mensagem.papel == PapelMensagem.USUARIO
    val formato = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomEnd = if (mensagemUsuario) 4.dp else 16.dp,
        bottomStart = if (mensagemUsuario) 16.dp else 4.dp
    )

    val corFundo = when {
        mensagemUsuario -> FundoUsuario
        mensagem.tipo == TipoRespostaMedica.URGENCIA -> FundoUrgencia
        else -> FundoAssistente
    }

    val corTexto = if (mensagemUsuario) Color.White else TextoPrincipal
    val corBorda = if (mensagem.tipo == TipoRespostaMedica.URGENCIA) BordaUrgencia else BordaSuave

    Surface(
        modifier = Modifier.fillMaxWidth(0.86f),
        shape = formato,
        color = corFundo,
        shadowElevation = if (mensagemUsuario) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .border(width = 1.dp, color = corBorda, shape = formato)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mensagem.carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = VerdeAda
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (mensagem.tipo == TipoRespostaMedica.URGENCIA && !mensagem.carregando) {
                    Text(
                        text = "Atenção",
                        color = TextoPrincipal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = mensagem.texto,
                    color = corTexto,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun CampoEnvioMensagem(
    texto: String,
    enviando: Boolean,
    erroEntrada: String?,
    aoAlterarTexto: (String) -> Unit,
    aoEnviarMensagem: () -> Unit
) {
    val podeEnviar = texto.isNotBlank() && !enviando

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = FundoChat,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = texto,
                    onValueChange = aoAlterarTexto,
                    modifier = Modifier.weight(1f),
                    enabled = !enviando,
                    placeholder = {
                        Text(
                            text = "Digite sua dúvida...",
                            color = TextoSecundario
                        )
                    },
                    maxLines = 4,
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (podeEnviar) aoEnviarMensagem()
                        }
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = aoEnviarMensagem,
                    enabled = podeEnviar,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeAda,
                        disabledContainerColor = Color(0xFFB8C9C0)
                    )
                ) {
                    if (enviando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensagem",
                            tint = Color.White
                        )
                    }
                }
            }

            AnimatedVisibility(visible = erroEntrada != null) {
                Text(
                    text = erroEntrada.orEmpty(),
                    color = Color(0xFFB00020),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewConteudoChatMedico() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ConteudoChatMedico(
                estado = EstadoChatMedico(
                    mensagens = listOf(
                        MensagemChat(
                            id = "1",
                            texto = TEXTO_MENSAGEM_INICIAL,
                            papel = PapelMensagem.ASSISTENTE
                        ),
                        MensagemChat(
                            id = "2",
                            texto = "Meu exame veio alterado. O que significa glicemia alta?",
                            papel = PapelMensagem.USUARIO
                        ),
                        MensagemChat(
                            id = "3",
                            texto = "Glicemia alta pode indicar excesso de açúcar no sangue, mas um exame isolado não fecha diagnóstico. Procure avaliação profissional para interpretar o resultado com seu histórico.",
                            papel = PapelMensagem.ASSISTENTE,
                            tipo = TipoRespostaMedica.EDUCATIVO
                        )
                    )
                ),
                aoAlterarTexto = {},
                aoEnviarMensagem = {},
                aoEscolherExamesGuardados = {}
            )
        }
    }
}
