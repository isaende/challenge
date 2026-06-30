// Lista os tipos de resposta que o backend pode retornar para o chat médico.
package com.ada.chatmedico.dominio

enum class TipoRespostaMedica {
    EDUCATIVO,
    URGENCIA,
    RECUSA_SEGURA,
    ERRO
}
