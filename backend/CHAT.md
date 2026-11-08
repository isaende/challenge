# Configurar o chat

Defina `OPENAI_API_KEY` como variável de ambiente do processo que executa o backend. A chave permanece no servidor. Consulte o [README](../README.md) para configurar o banco e executar o projeto.

Na raiz, execute `./gradlew.bat :backend:run` no Windows ou `./gradlew :backend:run` no Linux e macOS. Sem a chave, as chamadas ao chat retornam a mensagem de indisponibilidade; as demais rotas continuam disponíveis conforme a configuração do banco.

O aplicativo usa `http://127.0.0.1:8080`. Para desenvolvimento por USB ou emulador, execute `adb reverse tcp:8080 tcp:8080`. Para usar o endereço de rede do computador, ajuste `URL_BASE_CHAT` em `app/src/chat_medico/ChatMedico.kt` e `URL_API_ADA` em `app/src/telas/ApiAda.kt`.

O backend usa a API Responses e o modelo definido em `MODELO_OPENAI`. Aceita imagens PNG, JPEG, WEBP e GIF de até 10 MB. A chave não integra o aplicativo Android.

Os testes `./gradlew.bat :backend:test` usam respostas simuladas e dispensam uma chave real.
