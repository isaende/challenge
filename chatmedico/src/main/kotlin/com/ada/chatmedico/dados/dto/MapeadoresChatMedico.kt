// Converte os DTOs do backend para modelos de domínio usados pela tela.
package com.ada.chatmedico.dados.dto

import com.ada.chatmedico.dominio.RespostaChatMedico
import com.ada.chatmedico.dominio.TipoRespostaMedica

internal fun RespostaChatMedicoDto.paraDominio(): RespostaChatMedico {
    val tipoDominio = when (tipo.trim().lowercase()) {
        "educativo" -> TipoRespostaMedica.EDUCATIVO
        "urgencia" -> TipoRespostaMedica.URGENCIA
        "recusa_segura" -> TipoRespostaMedica.RECUSA_SEGURA
        "erro" -> TipoRespostaMedica.ERRO
        else -> TipoRespostaMedica.ERRO
    }

    return RespostaChatMedico(
        texto = resposta.ifBlank { "Nao foi possivel responder agora. Tente novamente." },
        tipo = tipoDominio
    )
}
