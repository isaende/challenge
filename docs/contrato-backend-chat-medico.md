<!-- Documenta o contrato esperado entre o app Android e o backend com MedGemma via Ollama. -->

# Contrato do Backend: Chat Medico Educativo

O app Android nao chama o Ollama nem o modelo diretamente. O Android consome apenas o backend Ktor, e o backend conversa com o Ollama em ambiente local ou servidor privado.

## Endpoint

`POST /api/chat-medico`

## Request

```json
{
  "mensagem": "texto digitado pelo usuario"
}
```

## Response

```json
{
  "resposta": "texto gerado pelo assistente",
  "tipo": "educativo | urgencia | recusa_segura | erro"
}
```

## Tipos

- `educativo`: exibir normalmente.
- `urgencia`: exibir com destaque textual e orientar atendimento imediato.
- `recusa_segura`: exibir normalmente, mantendo os limites do app.
- `erro`: o app mostra a mensagem amigavel "Nao foi possivel responder agora. Tente novamente."

## Regras esperadas no backend

- Validar mensagem vazia e limite de 2000 caracteres.
- Nao salvar historico, mensagens ou respostas.
- Nao registrar conteudo medico sensivel em logs.
- Usar HTTPS em producao entre app e backend.
- Nao expor o Ollama diretamente para a internet.
- Retornar `urgencia` de forma deterministica quando houver sinais claros de urgencia.
- Retornar `recusa_segura` sem chamar o modelo quando houver pedido de diagnostico definitivo, dose, prescricao ou mudanca de tratamento.
- Chamar o Ollama em `http://localhost:11434/api/chat` usando o modelo `medgemma1.5:4b`.

## URL base no Android

A URL padrao esta em:

`chatmedico/src/main/kotlin/com/ada/chatmedico/configuracao/ConfiguracaoApiChatMedico.kt`

Para testar no emulador Android com backend local:

`http://10.0.2.2:8080`

No emulador, `10.0.2.2` aponta para o `localhost` do computador.
