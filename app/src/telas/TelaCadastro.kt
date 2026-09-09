package home_page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ada.chatmedico.demo.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun TelaCadastro(aoCadastrar:(SessaoUsuario)->Unit,aoFazerLogin:()->Unit,modifier:Modifier=Modifier){
    var nome by remember{mutableStateOf("")};var email by remember{mutableStateOf("")};var nascimento by remember{mutableStateOf("")};var peso by remember{mutableStateOf("")};var altura by remember{mutableStateOf("")};var genero by remember{mutableStateOf("")};var objetivo by remember{mutableStateOf("")};var senha by remember{mutableStateOf("")};var confirmar by remember{mutableStateOf("")};var visivel by remember{mutableStateOf(false)};var erro by remember{mutableStateOf("")};var carregando by remember{mutableStateOf(false)};val escopo=rememberCoroutineScope()
    Scaffold(containerColor=fundoPrincipal,contentWindowInsets=WindowInsets(0.dp),modifier=modifier.fillMaxSize()){espaco->Column(Modifier.fillMaxSize().padding(espaco).statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal=24.dp,vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Text("LOGIN / CADASTRO",color=verdeEscuro,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=10.dp));Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),color=branco,border=androidx.compose.foundation.BorderStroke(1.dp,borda)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
            MarcaAda();Text("Crie sua conta!",fontSize=23.sp,fontWeight=FontWeight.Bold,color=textoPrimario);Text("Preencha os dados abaixo para começar a cuidar da sua saúde.",color=textoSecundario)
            CampoComImagem(nome,{nome=it},"Nome completo",R.drawable.usuario);CampoComImagem(email,{email=it},"E-mail",R.drawable.o_email);CampoComImagem(nascimento,{nascimento=it.take(10)},"Data de nascimento (dd/MM/aaaa)",R.drawable.agenda)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(peso,{peso=it.filter{c->c.isDigit()||c==','||c=='.'}},placeholder={Text("Peso (kg)")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),shape=RoundedCornerShape(16.dp),modifier=Modifier.weight(1f));OutlinedTextField(altura,{altura=it.filter{c->c.isDigit()||c==','||c=='.'}},placeholder={Text("Altura (m ou cm)")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),shape=RoundedCornerShape(16.dp),modifier=Modifier.weight(1f))}
            OutlinedTextField(genero,{genero=it},placeholder={Text("Gênero")},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth());OutlinedTextField(objetivo,{objetivo=it},placeholder={Text("Objetivo principal (ex.: perda de peso)")},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
            CampoSenhaCadastro(senha,{senha=it},"Senha",visivel){visivel=!visivel};CampoSenhaCadastro(confirmar,{confirmar=it},"Confirmar senha",visivel){visivel=!visivel}
            if(erro.isNotBlank())Text(erro,color=vermelhoPrincipal,style=TipografiaAplicativo.bodySmall)
            Button(onClick={val dataIso=runCatching{LocalDate.parse(nascimento,DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()}.getOrNull();val pesoNumero=peso.replace(',','.').toDoubleOrNull();val alturaNumero=altura.replace(',','.').toDoubleOrNull();erro=when{nome.isBlank()||email.isBlank()||genero.isBlank()||objetivo.isBlank()->"Preencha todos os campos.";dataIso==null->"Informe uma data válida.";pesoNumero==null||alturaNumero==null->"Informe peso e altura válidos.";senha.length<6->"A senha deve ter ao menos 6 caracteres.";senha!=confirmar->"As senhas não coincidem.";else->""};if(erro.isBlank())escopo.launch{carregando=true;runCatching{ApiAda.cadastrar(CadastroApi(nome.trim(),email.trim(),dataIso!!,pesoNumero!!,alturaNumero!!,genero.trim(),objetivo.trim(),senha))}.onSuccess(aoCadastrar).onFailure{erro=it.message?:"Não foi possível cadastrar."};carregando=false}},enabled=!carregando,modifier=Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=verdeEscuro)){Text(if(carregando)"Cadastrando..." else "Cadastrar",fontWeight=FontWeight.Bold)}
            Text("Já tem uma conta? Fazer login",color=verdeEscuro,modifier=Modifier.align(Alignment.CenterHorizontally).clickable(onClick=aoFazerLogin),style=TipografiaAplicativo.bodySmall)
        }};Text("Ao se cadastrar, você concorda com nossos Termos e Política.",color=textoSecundario,style=TipografiaAplicativo.labelSmall,modifier=Modifier.padding(12.dp))
    }}
}

@Composable private fun CampoSenhaCadastro(valor:String,aoMudar:(String)->Unit,dica:String,visivel:Boolean,aoVisibilidade:()->Unit){OutlinedTextField(valor,aoMudar,placeholder={Text(dica)},singleLine=true,visualTransformation=if(visivel)VisualTransformation.None else PasswordVisualTransformation(),leadingIcon={Image(painterResource(R.drawable.senha),null,Modifier.size(22.dp))},trailingIcon={Text(if(visivel)"Ocultar" else "Ver",Modifier.clickable(onClick=aoVisibilidade),color=verdePrincipal,style=TipografiaAplicativo.labelSmall)},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())}
