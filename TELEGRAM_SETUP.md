# Configuração do Telegram para Sketchware Pro IA

## Credenciais Configuradas

### Bot "Alicy"
- **Token:** `7320764076:AAFkoDkaMMlrZMsYJVoyXoiecBUrlBO7Z04`
- **Username:** @Alicyonline_bot
- **ID:** 7320764076

### Grupo "Sketchware Pró IA"
- **ID:** `-4851663824`
- **Tipo:** group
- **Membros:** 2
- **Status do bot:** administrator

## Como obter API_ID e API_HASH

Para que o upload de APKs funcione, você precisa obter o `API_ID` e `API_HASH` do Telegram:

### 1. Acesse o site do Telegram
Vá para: https://my.telegram.org

### 2. Faça login com seu número de telefone
- Digite seu número de telefone
- Digite o código de verificação recebido

### 3. Crie um aplicativo
- Clique em "API development tools"
- Preencha os campos:
  - **App title:** Sketchware Pro IA
  - **Short name:** sketchware_ia
  - **Platform:** Android
  - **Description:** Mod do Sketchware Pro com IA

### 4. Copie as credenciais
Você receberá:
- **api_id:** Um número (ex: 123456)
- **api_hash:** Uma string longa (ex: abcdef123456789...)

### 5. Atualize o arquivo deploy_artifacts.py
Substitua no arquivo `.github/workflows/deploy_artifacts.py`:

```python
api_id = 123456  # Substitua pelo seu API_ID
api_hash = "seu_api_hash_aqui"  # Substitua pelo seu API_HASH
```

## Arquivos Modificados

- **`.github/workflows/notify_telegram.py`** - Credenciais do bot para notificações
- **`.github/workflows/deploy_artifacts.py`** - Credenciais para upload de APKs
- **`.github/workflows/android.yml`** - Removidas referências aos GitHub Secrets

## Funcionalidades

✅ **Notificações de commits** - Funciona com as credenciais atuais  
⏳ **Upload de APKs** - Precisa do API_ID e API_HASH para funcionar

## Segurança

⚠️ **Atenção:** As credenciais estão agora no código. Certifique-se de:
- Não compartilhar o repositório publicamente com as credenciais
- Usar um bot dedicado para este projeto
- Monitorar o uso do bot regularmente
