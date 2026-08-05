package com.ada.chatmedico.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.ada.chatmedico.TelaChatMedico
import com.ada.chatmedico.criarRepositorioChatMedico

class AtividadePrincipal : ComponentActivity() {
    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        val repositorio = criarRepositorioChatMedico()
        setContent {
            MaterialTheme {
                TelaChatMedico(repositorio)
            }
        }
    }
}
