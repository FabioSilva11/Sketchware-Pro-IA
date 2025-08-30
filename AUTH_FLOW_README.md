# Fluxo de Autenticação - Sketchware Pro IA

## Visão Geral

Este documento descreve o fluxo de autenticação implementado no Sketchware Pro IA, baseado no modelo do GetNinjas mas com Activities separadas (sem ViewPager).

## Estrutura de Telas (Activities)

### 1. SplashActivity
- **Localização**: `pro.sketchware.activities.splash.SplashActivity`
- **Layout**: `activity_splash.xml`
- **Função**: 
  - Verifica no `FirebaseAuth` se já existe usuário logado
  - Se SIM → abre direto a `MainActivity`
  - Se NÃO → abre a `AuthChoiceActivity`

### 2. AuthChoiceActivity
- **Localização**: `pro.sketchware.activities.auth.AuthChoiceActivity`
- **Layout**: `activity_auth_choice.xml`
- **Função**:
  - Mostra logo, título, subtítulo e três botões:
    - **Entrar** → abre `LoginActivity`
    - **Criar Conta** → abre `RegisterActivity`
    - **Pular** → abre direto `MainActivity` sem autenticação

### 3. LoginActivity
- **Localização**: `pro.sketchware.activities.auth.LoginActivity`
- **Layout**: `activity_login.xml`
- **Função**:
  - Tela simples de login com Firebase (`email` + `senha`)
  - Se sucesso → abre `MainActivity`

### 4. RegisterActivity
- **Localização**: `pro.sketchware.activities.auth.RegisterActivity`
- **Layout**: `activity_register.xml`
- **Função**:
  - Tela de cadastro com Firebase Authentication (`email` + `senha`)
  - Salva os dados extras do usuário no `Firebase Database`
  - Se sucesso → abre `MainActivity`

### 5. MainActivity
- **Localização**: `pro.sketchware.activities.main.activities.MainActivity`
- **Função**: Tela principal do app (existente, não modificada)

## Regras do Fluxo

1. **Pular Autenticação**: O usuário pode pular login/cadastro e acessar direto a `MainActivity`
2. **Persistência**: Se o usuário se cadastrar, na próxima vez que abrir o app → `SplashActivity` detecta login ativo e pula direto para `MainActivity`
3. **Dados Completos**: O cadastro salva todos os campos do JSON no **Firebase Database**
4. **Design**: Segue referência no GetNinjas, mas simplificado

## Estrutura de Dados do Usuário

O cadastro salva os seguintes campos no Firebase Database, organizados em seções:

### **1. Informações Pessoais**
- `name`: Nome completo do usuário
- `phone_number`: Número de telefone
- `email`: Endereço de e-mail
- `password`: Senha (armazenada no Firebase Auth)
- `pin`: PIN de acesso (6 dígitos)

### **2. Documentos**
- `cpf`: CPF (pessoa física) - máximo 14 caracteres
- `cnpj`: CNPJ (pessoa jurídica) - máximo 18 caracteres
- `curp`: CURP (México) - máximo 18 caracteres
- `rfc`: RFC (México) - máximo 13 caracteres

### **3. Dados Demográficos**
- `birthday`: Data de nascimento
- `gender`: Gênero (seleção: Masculino, Feminino, Não-binário, Prefiro não informar)

### **4. Localização**
- `cep`: CEP do local de trabalho - máximo 9 caracteres
- `home_cep`: CEP da residência - máximo 9 caracteres

### **5. Informações Empresariais**
- `compane_size`: Tamanho da empresa (seleção: 1-10, 11-50, 51-200, 201-1000, 1000+, Não aplicável)
- `razao_social`: Razão social
- `founded_at`: Data de fundação

### **6. Campos do Sistema**
- `sub_category_ids`: Array com IDs das subcategorias selecionadas
- `token`: Token de sessão
- `utm_campaign`, `utm_medium`, `utm_term`, `utm_content`, `utm_source`, `utm_gclid`: Parâmetros UTM para analytics
- `created_at`, `updated_at`: Timestamps de criação e atualização

### **Estrutura JSON Completa**
```json
{
  "name": "string",
  "email": "string",
  "phone_number": "string",
  "cpf": "string",
  "cnpj": "string",
  "curp": "string",
  "rfc": "string",
  "birthday": "string",
  "gender": "string",
  "cep": "string",
  "home_cep": "string",
  "compane_size": "string",
  "razao_social": "string",
  "founded_at": "string",
  "sub_category_ids": ["array"],
  "token": "string",
  "pin": "string",
  "utm_campaign": "string",
  "utm_medium": "string",
  "utm_term": "string",
  "utm_content": "string",
  "utm_source": "string",
  "utm_gclid": "string",
  "created_at": "timestamp",
  "updated_at": "timestamp"
}
```

## Classes Utilitárias

### AuthManager
- **Localização**: `pro.sketchware.activities.auth.AuthManager`
- **Função**: Singleton para gerenciar autenticação Firebase
- **Métodos**:
  - `getInstance()`: Retorna instância única
  - `getAuth()`: Retorna FirebaseAuth
  - `getDatabase()`: Retorna DatabaseReference
  - `getCurrentUser()`: Retorna usuário atual
  - `isUserLoggedIn()`: Verifica se usuário está logado
  - `signOut()`: Faz logout
  - `saveUserData()`: Salva dados do usuário

### CategoryManager
- **Localização**: `pro.sketchware.activities.auth.CategoryManager`
- **Função**: Singleton para gerenciar categorias de desenvolvimento
- **Categorias Principais**:
  - **Desenvolvimento Mobile**: Android, iOS, Cross-Platform, Flutter, React Native
  - **Desenvolvimento Web**: Frontend, Backend, Full Stack, React, Vue, Angular
  - **Design e UI/UX**: UI Design, UX Design, Design Gráfico, Ícones, Logos, Prototipagem
  - **Desenvolvimento de Jogos**: Mobile, PC, Unity, Unreal Engine, Game Art
  - **Inteligência Artificial**: Machine Learning, Deep Learning, NLP, Computer Vision, Apps com IA
  - **DevOps e Infraestrutura**: Cloud Computing, Containerização, CI/CD, Monitoramento, Segurança
  - **Blockchain e Web3**: Smart Contracts, DeFi, NFTs, DApps, Criptomoedas
- **Métodos**:
  - `getAllCategories()`: Retorna todas as categorias
  - `getCategoryById()`: Retorna categoria por ID
  - `getSubCategoryById()`: Retorna subcategoria por ID
  - `getAllSubCategoryIds()`: Retorna todos os IDs de subcategorias

## Configuração no AndroidManifest.xml

As novas activities foram adicionadas ao manifest:

```xml
<!-- Auth Activities -->
<activity
    android:name="pro.sketchware.activities.auth.AuthChoiceActivity"
    android:exported="false"
    android:theme="@style/Theme.SketchwarePro" />

<activity
    android:name="pro.sketchware.activities.auth.LoginActivity"
    android:exported="false"
    android:theme="@style/Theme.SketchwarePro"
    android:windowSoftInputMode="adjustResize" />

<activity
    android:name="pro.sketchware.activities.auth.RegisterActivity"
    android:exported="false"
    android:theme="@style/Theme.SketchwarePro"
    android:windowSoftInputMode="adjustResize" />
```

## Strings de Autenticação

Todas as strings relacionadas à autenticação foram centralizadas em `strings.xml`:

- `auth_choice_title`: "Entre no incrível mundo Dev!"
- `auth_login_title`: "Entrar"
- `auth_register_title`: "Criar Conta"
- `auth_skip_text`: "Pular"
- `auth_forgot_password`: "Esqueci a senha"
- `auth_no_account`: "Não tem uma conta? Cadastre-se"
- `auth_has_account`: "Já tem uma conta? Faça login"
- E outras strings de validação e mensagens

## Dependências Firebase

O projeto já possui as dependências Firebase necessárias:

```gradle
implementation platform("com.google.firebase:firebase-bom:33.14.0")
implementation "com.google.firebase:firebase-auth"
implementation "com.google.firebase:firebase-database"
```

## Fluxo de Navegação

```
SplashActivity
    ↓ (usuário logado)
MainActivity
    ↓ (usuário não logado)
AuthChoiceActivity
    ↓ (Entrar)
LoginActivity → MainActivity
    ↓ (Criar Conta)
RegisterActivity → CategorySelectionActivity → MainActivity
    ↓ (Pular)
MainActivity
```

## Funcionalidades Implementadas

✅ **SplashActivity** com verificação de login
✅ **AuthChoiceActivity** com três opções
✅ **LoginActivity** com autenticação Firebase
✅ **RegisterActivity** com formulário completo de cadastro
✅ **Formulário com todas as seções solicitadas**:
  - Informações Pessoais (nome, telefone, email, senha, PIN)
  - Documentos (CPF, CNPJ, CURP, RFC)
  - Dados Demográficos (nascimento, gênero)
  - Localização (CEP trabalho, CEP residência)
  - Informações Empresariais (tamanho empresa, razão social, fundação)
✅ **Sistema de Categorias** com seleção de especialidades
✅ **Spinners para seleção** de gênero e tamanho da empresa
✅ **Validações** de campos obrigatórios e formatos
✅ **AuthManager** para gerenciamento centralizado
✅ **Strings centralizadas** para internacionalização
✅ **Tratamento de erros** com mensagens amigáveis
✅ **Progress indicators** durante operações
✅ **Navegação fluida** entre telas
✅ **Salvamento completo** no Firebase Database

## Funcionalidades Futuras

🔄 **Recuperação de senha** (placeholder implementado)
🔄 **Validação de email** mais robusta
🔄 **Biometria** para login rápido
🔄 **Perfil do usuário** com edição de dados
🔄 **Logout** da MainActivity
