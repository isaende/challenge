// Centraliza a URL base e o caminho do endpoint usado pelo chat médico.
package com.ada.chatmedico.configuracao

object ConfiguracaoApiChatMedico {
    /*
     * Nenhuma chave ou acesso direto ao modelo deve ficar no app Android.
     * O aplicativo chama apenas o backend, que conversa com o servidor privado.
     * Em producao, configure esta URL por BuildConfig, flavors ou injecao de dependencia,
     * usando sempre HTTPS.
     */
    const val API_BASE_URL = "https://meu-backend.com"
    const val CAMINHO_CHAT_MEDICO = "/api/chat-medico"
}
