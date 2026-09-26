package home_page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(aoEntrar:(SessaoUsuario)->Unit,aoCriarConta:()->Unit,modifier:Modifier=Modifier) {
    var email by remember{mutableStateOf("")};var senha by remember{mutableStateOf("")};var senhaVisivel by remember{mutableStateOf(false)}
    var erro by remember{mutableStateOf("")};var carregando by remember{mutableStateOf(false)};val escopo=rememberCoroutineScope()
    Scaffold(containerColor=branco,contentWindowInsets=WindowInsets(0.dp),modifier=modifier.fillMaxSize()) { espaco ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espaco)
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MarcaAda()
            Spacer(Modifier.height(10.dp));Text("Bem-vindo(a)!",fontSize=24.sp,fontWeight=FontWeight.Bold,color=textoPrimario)
            Text("Faça login para continuar\ncuidando da sua saúde.",color=textoSecundario,style=TipografiaAplicativo.bodyLarge)
            Spacer(Modifier.height(18.dp))
            CampoComImagem(email,{email=it},"E-mail ou CPF",R.drawable.o_email)
            OutlinedTextField(value=senha,onValueChange={senha=it},placeholder={Text("Senha")},singleLine=true,visualTransformation=if(senhaVisivel)VisualTransformation.None else PasswordVisualTransformation(),leadingIcon={Image(painterResource(R.drawable.senha),null,Modifier.size(22.dp))},trailingIcon={Text(if(senhaVisivel)"Ocultar" else "Ver",Modifier.clickable{senhaVisivel=!senhaVisivel},color=verdePrincipal,style=TipografiaAplicativo.labelSmall)},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
            Text("Esqueci minha senha",color=verdeEscuro,fontWeight=FontWeight.SemiBold,style=TipografiaAplicativo.bodySmall,modifier=Modifier.align(Alignment.End))
            if(erro.isNotBlank())Text(erro,color=vermelhoPrincipal,style=TipografiaAplicativo.bodySmall)
            Button(onClick={if(email.isBlank()||senha.isBlank()){erro="Informe e-mail e senha."}else escopo.launch{carregando=true;erro="";runCatching{ApiAda.login(email.trim(),senha)}.onSuccess(aoEntrar).onFailure{erro=it.message?:"Não foi possível entrar."};carregando=false}},enabled=!carregando,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=verdeEscuro)){Text(if(carregando)"Entrando..." else "Entrar",fontWeight=FontWeight.Bold)}
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){HorizontalDivider(Modifier.weight(1f),color=borda);Text("  ou  ",color=textoSecundario);HorizontalDivider(Modifier.weight(1f),color=borda)}
            OutlinedButton(onClick=aoCriarConta,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp),border=androidx.compose.foundation.BorderStroke(1.dp,verdeEscuro)){Text("Criar conta",color=verdeEscuro,fontWeight=FontWeight.Bold)}
        }
    }
}

@Composable
internal fun MarcaAda() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.logo_app),
            contentDescription = "Logo AdaUniCare",
            modifier = Modifier.size(62.dp)
        )
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = textoPrimario, fontWeight = FontWeight.Bold)) { append("Ada") }
                withStyle(SpanStyle(color = verdePrincipal, fontWeight = FontWeight.Bold)) { append("UniCare") }
            },
            fontSize = 28.sp
        )
    }
}

@Composable internal fun CampoComImagem(valor:String,aoMudar:(String)->Unit,dica:String,icone:Int,modifier:Modifier=Modifier){OutlinedTextField(value=valor,onValueChange=aoMudar,placeholder={Text(dica)},singleLine=true,leadingIcon={Image(painterResource(icone),null,Modifier.size(22.dp))},shape=RoundedCornerShape(16.dp),modifier=modifier.fillMaxWidth())}
