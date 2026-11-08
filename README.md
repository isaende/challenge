# Ada Chat Médico

Aplicativo Android em Kotlin e Jetpack Compose, com backend Ktor e banco Oracle. O chat recebe texto e imagens pelo backend, onde ficam as credenciais de acesso.

## Executar localmente

Use JDK 17 ou superior e Android Studio com o SDK exigido em `app/build.gradle.kts`. O Gradle Wrapper está incluído no projeto.

Configure estas variáveis no ambiente do processo do backend:

| Variável | Finalidade |
| --- | --- |
| `DB_URL` | URL JDBC do seu Oracle, por exemplo `jdbc:oracle:thin:@localhost:1521:FREEPDB1` |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `OPENAI_API_KEY` | Chave da OpenAI para habilitar o chat |
| `DB_CONNECT_TIMEOUT_SECONDS` | Tempo de conexão com o banco, padrão 10 segundos |
| `PORT` | Porta HTTP do backend, padrão 8080 |

O arquivo `.env.example` contém somente exemplos. O backend lê variáveis de ambiente; não carrega `.env` ou `database.properties` automaticamente. Os três valores `DB_*` de conexão são obrigatórios para as operações do banco.

No PowerShell, configure o banco e informe os segredos sem gravá-los no histórico do terminal:

```powershell
$env:DB_URL = 'jdbc:oracle:thin:@localhost:1521:FREEPDB1'
$env:DB_USER = Read-Host 'Usuario do Oracle'
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Senha do Oracle' -AsSecureString)).Password
$env:OPENAI_API_KEY = [System.Net.NetworkCredential]::new('', (Read-Host 'Chave da OpenAI' -AsSecureString)).Password
.\gradlew.bat :backend:run
```

No Linux e macOS, defina as mesmas variáveis e execute `./gradlew :backend:run`.

Abra o projeto no Android Studio para executar o aplicativo. Com o dispositivo ou emulador conectado via ADB, execute `adb reverse tcp:8080 tcp:8080` para acessar o backend local. Mais detalhes em [Configurar o chat](backend/CHAT.md).

## Testes

```powershell
.\gradlew.bat :backend:test
```

Os testes do backend usam respostas simuladas, sem chamadas reais à OpenAI e sem conexão com o Oracle.

## Publicação no GitHub

Credenciais são fornecidas por variáveis de ambiente. Arquivos `.env`, configurações locais, chaves privadas, arquivos de assinatura Android, builds e diretórios `uploads` são ignorados pelo Git. Use apenas dados fictícios em exemplos e mantenha exames e dados de usuários fora do repositório.

**O histórico anterior contém uma chave da OpenAI e arquivos de exames. Um commit removendo esses arquivos não apaga as versões antigas.** Revogue a chave exposta e gere outra antes da publicação. Altere também a senha do banco se ela tiver sido compartilhada em outro local.

Para publicar uma cópia sem esse histórico, execute na raiz:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/export-public.ps1
```

O arquivo `public-source.zip` contém os arquivos atuais selecionados pelo Git, sem `.git`, credenciais locais, uploads e builds. Extraia em outra pasta e crie um **novo repositório** a partir desse conteúdo. Não copie a pasta `.git` original para o novo repositório. Para manter o histórico no repositório existente, remova primeiro os segredos e exames de todos os commits e referências antes de mudar sua visibilidade.
