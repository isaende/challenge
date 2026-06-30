<!-- Guia rápido do módulo de chat médico e de como testar ou integrar a tela. -->

# Chat Medico Educativo

Modulo Android nativo em Kotlin com Jetpack Compose para a tela de chat "Fale com a Ada".

Este repositorio entrega apenas um modulo Android Library (`:chatmedico`). Ele nao cria app completo, login, cadastro, banco de dados, pagamento, tela inicial, upload de exames ou navegacao completa.

## O que foi incluido

- Tela Compose `TelaChatMedico`.
- `ChatMedicoViewModel` com `StateFlow`.
- Repository e API Ktor Client.
- DTOs e modelos de dominio.
- Validacao local de 2000 caracteres.
- Loading, erro amigavel e destaque para resposta de urgencia.
- Mensagens apenas em memoria enquanto a tela estiver aberta.
- Exemplo pequeno de integracao com NavHost.
- Imagem `ada_onca` da mascote verde no modulo.

## Conectar ao backend

Configure a URL base em `ConfiguracaoApiChatMedico.API_BASE_URL` ou injete um repositorio com a URL correta:

```kotlin
val repositorio = ProvedorChatMedico.criarRepositorio(
    urlBase = "https://seu-backend.com"
)
```

O endpoint esperado e:

`POST /api/chat-medico`

```json
{
  "mensagem": "texto digitado pelo usuario"
}
```

Resposta:

```json
{
  "resposta": "texto gerado pelo assistente",
  "tipo": "educativo"
}
```

Tipos aceitos: `educativo`, `urgencia`, `recusa_segura`, `erro`.

## Integracao Compose

```kotlin
TelaChatMedicoComRepositorio(
    repositorio = repositorio,
    aoVoltar = { navController.popBackStack() },
    aoEscolherExamesGuardados = {
        // Futuramente abrir seletor de exames ja guardados.
    }
)
```

## Testar no Android Studio

Para rodar sem backend, selecione a configuracao `appdemo` no Android Studio e clique em Run. O modulo demo usa um repositorio simulado somente para testar a interface.

Para testar com o backend local, abra `appdemo/src/main/kotlin/com/ada/chatmedico/demo/MainActivity.kt` e altere `USAR_BACKEND_LOCAL` para `true`.

## Mascote

A tela usa `chatmedico/src/main/res/drawable-nodpi/ada_onca.jpeg`. Para trocar a arte depois, mantenha o mesmo nome do recurso (`ada_onca`).

## Privacidade e seguranca

- O app Android nao chama Ollama nem modelo diretamente.
- O app chama apenas o backend Ktor.
- O MVP nao persiste conversas.
- O codigo nao registra conteudo medico em logs.
- Em producao, use HTTPS e configure a URL por build config/flavors.

## Backend MedGemma com Ollama

O modulo `:backend` expoe `POST /api/chat-medico` e chama o Ollama local em `http://localhost:11434/api/chat` usando `medgemma1.5:4b`.

Para rodar localmente:

```powershell
ollama pull medgemma1.5:4b
ollama serve
.\gradlew.bat :backend:run
```

Teste com curl:

```powershell
curl -X POST http://localhost:8080/api/chat-medico `
  -H "Content-Type: application/json" `
  -d "{\"mensagem\":\"O que significa glicemia alta?\"}"
```

No emulador Android, use `http://10.0.2.2:8080` como URL base para acessar o backend rodando no computador.
