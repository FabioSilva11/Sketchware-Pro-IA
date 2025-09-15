from telethon import TelegramClient
import os
import sys
import subprocess

def get_git_commit_info():
    commit_author = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%an']).decode('utf-8')
    commit_message = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%s']).decode('utf-8')
    commit_hash = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%H']).decode('utf-8')
    commit_hash_short = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%h']).decode('utf-8')
    return commit_author, commit_message, commit_hash, commit_hash_short

def find_apk_file(base_path='app/build/outputs/apk'):
    for root, dirs, files in os.walk(base_path):
        for file in files:
            if file.endswith('.apk'):
                return os.path.join(root, file)
    return None
    

def find_apk_file_multi(base_paths):
    for path in base_paths:
        if not path:
            continue
        if os.path.isfile(path) and path.endswith('.apk'):
            return path
        if os.path.isdir(path):
            found = find_apk_file(path)
            if found:
                return found
    return None

def exit_with_error(message: str):
    print(f"[deploy_artifacts] Error: {message}")
    sys.exit(1)

def get_required_env(name: str) -> str:
    value = os.getenv(name, "").strip()
    if not value:
        exit_with_error(f"Missing required environment variable: {name}")
    return value

def get_required_env_int(name: str) -> int:
    value = get_required_env(name)
    try:
        return int(value)
    except ValueError:
        exit_with_error(f"Environment variable {name} must be an integer, got: '{value}'")

def get_entity_env(name: str):
    """CHAT_ID can be an int (-100...) or a @username. Accept both."""
    value = get_required_env(name)
    try:
        return int(value)
    except ValueError:
        return value  # treat as username or link

# Telegram API credentials (fail-fast with clear messages)
api_id = get_required_env_int("API_ID")
api_hash = get_required_env("API_HASH")
bot_token = get_required_env("BOT_TOKEN")
group_id = get_entity_env("CHAT_ID")

# Permite usar um caminho explícito (preferível no workflow), senão tenta localizar
apk_env = os.getenv("APK_PATH")
apk_candidates = [
    apk_env if apk_env else None,
    os.getenv("APK_DIR", "downloaded-artifact"),
    "./downloaded-artifact",
    "downloaded-artifact",
    "app/build/outputs/apk",
    "app/build/outputs",
]
apk_path = find_apk_file_multi(apk_candidates)
if not apk_path:
    print("APK não encontrado. Informe APK_PATH ou garanta que exista em downloaded-artifact/ ou app/build/outputs/apk.")
    exit(1)
else:
    print(f"APK encontrado: {apk_path}")

# Pega info do último commit
commit_author, commit_message, commit_hash, commit_hash_short = get_git_commit_info()

# Limpa sessão anterior
session_file = "bot_session.session"
if os.path.exists(session_file):
    os.remove(session_file)

# Cria cliente Telegram com token de bot
client = TelegramClient('bot_session', api_id, api_hash).start(bot_token=bot_token)
client.parse_mode = 'markdown'

def human_readable_size(size, decimal_places=2):
    for unit in ['B', 'KB', 'MB', 'GB', 'TB']:
        if size < 1024.0:
            break
        size /= 1024.0
    return f"{size:.{decimal_places}f} {unit}"

async def progress(current, total):
    progress_percentage = (current / total) * 100
    uploaded_size_readable = human_readable_size(current)
    total_size_readable = human_readable_size(total)
    print(f"{progress_percentage:.2f}% uploaded - {uploaded_size_readable}/{total_size_readable}", end='\r')

async def send_file(file_path):
    if not os.path.exists(file_path):
        print("Arquivo não encontrado:", file_path)
        return

    print(f"Enviando arquivo: {file_path} para o grupo Telegram")

    message = (
     f"🇧🇷 **Novo APK estável disponível** ✅\n\n"
     f"👤 **Autor do commit:** {commit_author}\n"
     f"📝 **Mensagem:** {commit_message}\n"
     f"🔖 **Hash:** #{commit_hash_short}\n"
     f"📱 **Compatibilidade:** Android 8 ou superior\n\n"
     f"ℹ️ Esta é uma **versão estável**. Caso encontre algum bug, por favor reporte no repositório.\n\n"
     f"---\n\n"
     f"🇺🇸 **New stable APK available** ✅\n\n"
     f"👤 **Commit author:** {commit_author}\n"
     f"📝 **Message:** {commit_message}\n"
     f"🔖 **Hash:** #{commit_hash_short}\n"
     f"📱 **Compatibility:** Android 8 or higher\n\n"
     f"ℹ️ This is a **stable version**. If you find any bugs, please report them in the repository.\n\n"
     f"---\n\n"
     f"🤝 Contribua com o projeto / Contribute to the project: "
     f"[Sketchware-Pro-IA no GitHub](https://github.com/FabioSilva11/Sketchware-Pro-IA)" 
    )


    try:
        await client.send_file(
            entity=group_id,
            file=file_path,
            parse_mode='markdown',
            caption=message,
            progress_callback=progress,
            reply_to=int(os.getenv("TOPIC_ID")) if os.getenv("TOPIC_ID") else None
        )
        print("\nArquivo enviado com sucesso")
    except Exception as e:
        print(f"Falha ao enviar arquivo: {e}")

try:
    with client:
        client.loop.run_until_complete(send_file(apk_path))
finally:
    if client.is_connected():
        client.loop.run_until_complete(client.disconnect())
