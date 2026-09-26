# Configurar o chat

1. Preencha a constante `OPENAI_API_KEY` em `src/main/kotlin/ClienteOpenAI.kt` com sua chave da API da OpenAI. Mantenha a chave somente no backend e não publique seu valor no Git.
2. Na raiz do projeto, execute `./gradlew.bat :backend:run` (Windows) ou `./gradlew :backend:run`.
3. Inicie o aplicativo. No emulador Android, o chat acessa `http://10.0.2.2:8080/api/chat-medico`. Para um dispositivo físico, ajuste `URL_BASE_CHAT` em `app/src/chat_medico/ChatMedico.kt` para o endereço do computador que executa o backend.

O backend usa a API Responses da OpenAI e o modelo definido em `MODELO_OPENAI` (`gpt-4.1-mini`). Texto e imagens de exames são enviados pelo backend; a chave não integra o aplicativo Android. Imagens aceitas: PNG, JPEG, WEBP e GIF, até 10 MB.

Referência: [documentação oficial da API](https://developers.openai.com/api/docs/guides/text).

Os testes `./gradlew.bat :backend:test` usam respostas simuladas e não precisam de uma chave real.
