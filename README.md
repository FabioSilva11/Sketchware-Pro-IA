<p align="center">
  <img src="https://github.com/FabioSilva11/Sketchware-Pro-IA/blob/main/app/src/main/res/drawable-xhdpi/sketch_app_icon.png" style="width: 30%;" />
</p>

# Sketchware com IA

Este projeto é um **derivado do Sketchware Pro**.  
Ele foi criado porque, na comunidade original, **os moderadores não aceitavam recursos de inteligência artificial no código nem a adição de novos blocos**.  
Por conta disso, decidi desenvolver minha própria versão, baseada na **última versão oficial do Sketchware Pro**, para oferecer mais liberdade, inovação e recursos para criadores de apps.

---

## Compilando o aplicativo

Para compilar o app, é necessário usar o **Gradle**.  
É altamente recomendável usar o **Android Studio** para ter a melhor experiência.

### Mapa do código-fonte

| Classe                | Função                                               |
| --------------------- | ---------------------------------------------------- |
| `a.a.a.ProjectBuilder`| Auxiliar para compilar um projeto inteiro            |
| `a.a.a.Ix`            | Responsável por gerar o arquivo AndroidManifest.xml  |
| `a.a.a.Jx`            | Gera o código-fonte das atividades                   |
| `a.a.a.Lx`            | Gera o código-fonte de componentes, como listeners, etc. |
| `a.a.a.Ox`            | Responsável por gerar arquivos XML de layouts        |
| `a.a.a.qq`            | Registro das dependências das bibliotecas internas   |
| `a.a.a.tq`            | Responsável pelos diálogos de compilação             |
| `a.a.a.yq`            | Organiza os caminhos de arquivos dos projetos do Sketchware |

> 💡 **Dica:**  
> Você também pode verificar o pacote `mod`, que contém a maioria das alterações feitas pelos contribuidores.

---

## Como contribuir

Se você quiser contribuir para este mod, siga estes passos:

1. Faça um fork deste repositório.  
2. Faça as alterações no seu fork.  
3. Teste as alterações.  
4. Crie um pull request neste repositório.  
5. Seu pull request será revisado e, se aceito, será mesclado.

Aceitamos contribuições de qualquer tamanho, sejam novos recursos ou correções de bugs, mas todas as contribuições passam por revisão.

---

### Mensagem de commit

Ao alterar um ou mais arquivos, você deve fazer um commit com uma mensagem adequada.  
Siga estas orientações:

- Mantenha a mensagem curta e descritiva.  
- Use um destes prefixos para indicar o tipo de commit:
  - `feat:` para um novo recurso.
  - `fix:` para uma correção de bug.
  - `style:` para mudanças de estilo.
  - `refactor:` para refatoração de código.
  - `test:` para testes.
  - `docs:` para documentação.
  - `chore:` para manutenção (pode usar emojis também).

**Exemplos:**
- `feat: Aumentar velocidade da compilação com nova técnica`
- `fix: Corrigir travamento na inicialização em certos celulares`
- `refactor: Reformatar código no File.java`

> ⚠ **Importante:**  
> Se for adicionar novos recursos que não precisam modificar outros pacotes além de `pro.sketchware`, faça as mudanças dentro deste pacote, respeitando a estrutura de diretórios e nomes dos arquivos.  
> Embora o projeto compile com classes Kotlin, prefira escrever em Java, exceto quando for realmente necessário usar Kotlin.

---

## Agradecimentos por contribuir

Obrigado por contribuir! Sua ajuda mantém o projeto vivo.  
Cada contribuição aceita será registrada na atividade "Sobre a equipe", usando seu nome e foto de perfil do GitHub (que podem ser alterados depois).

---

## Telegram

Quer conversar conosco, discutir mudanças ou apenas bater papo? Temos um canal no Telegram para isso.

[Junte-se ao nosso canal do Telegram!](https://t.me/+8rUUdcvjZxk0YTIx)

---

## Aviso legal

Este mod é **derivado** do Sketchware Pro e mantém sua essência, mas com melhorias e novas funcionalidades.  
Ele **não foi criado com propósitos prejudiciais** ao Sketchware ou ao Sketchware Pro — pelo contrário, meu objetivo é expandir o potencial da ferramenta e permitir que desenvolvedores possam usar IA, novos blocos e recursos sem limitações impostas por moderadores.

O projeto continua sendo **gratuito** e **completamente independente**.  
Não é permitido publicar este mod, seja original ou modificado, na **Play Store** ou em qualquer outra loja de aplicativos sem autorização.

Agradeço aos criadores originais do Sketchware e do Sketchware Pro pelo excelente trabalho, que serviu de base para este projeto.
