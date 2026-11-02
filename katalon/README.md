# Automação de interface — Katalon Studio (app PetPulse-mobile)

São 4 testes automatizados das telas do app Android [PetPulse-mobile](https://github.com/Fiap-2tdspg-Challenge/PetPulse-mobile), feitos no **Katalon Studio (Mobile Testing)**. Eles complementam os testes de API em Postman.

| Teste | Caso manual | O que valida |
|---|---|---|
| TC-UI-01 Login com senha inválida | CT17 | Diálogo "E-mail ou senha incorretos." e permanência na tela de login |
| TC-UI-02 Login do tutor exibe pets | CT17 | Home "Olá, Tutor!", seção "Seus pets", pet "Bidu QA" / Golden Retriever e perfil do pet |
| TC-UI-03 Alerta inteligente na Home | CT18 | Leitura IoT com FC 195 gera alerta, exibido no sino da Home com mensagem e recomendação |
| TC-UI-04 Veterinário cadastra histórico | CT19 | Painel do veterinário: busca do pet, cadastro de Consulta com retorno 10/01/2027, registro listado no app e gravado na API |

Os dados são controlados. Antes de cada teste, o script cria pela API o pet "Bidu QA" (e, no TC-UI-03, a leitura que gera o alerta). No fim, remove tudo, mesmo se o teste falhar. O app é reinstalado a cada teste, então sempre começa deslogado.

```text
automacao-katalon/
├── PetPulse-Katalon/                   Projeto Katalon pronto (File → Open Project)
├── apk/PetPulse.apk                    APK do app (release, x86_64, aponta para a API no Render)
├── Keywords/petpulse/Helpers.groovy    Localizadores, login no app e preparação/limpeza via API
└── Scripts/                            Script de cada Test Case
```

## ⚡ Caminho rápido (computador do Gabriel, já configurado)

Já estão instalados e testados: Android SDK em `%LOCALAPPDATA%\Android\Sdk`, emulador `PetPulse_Pixel7` (Android 14), Appium 3 com driver UiAutomator2 e o APK em `apk/PetPulse.apk`. Os 4 testes foram executados neste emulador com os mesmos localizadores e passos: **2 execuções seguidas, 8/8 aprovados**, sem deixar dados na API.

1. Defina as variáveis de ambiente do usuário (uma vez só) e abra um **novo** PowerShell:

   ```powershell
   setx ANDROID_HOME "$env:LOCALAPPDATA\Android\Sdk"
   setx JAVA_HOME "$env:USERPROFILE\ppb-jdk"
   ```

2. Ligue o emulador:

   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd PetPulse_Pixel7
   ```

3. Instale o [Katalon Studio](https://katalon.com/download) e entre com sua conta Katalon.
4. **Window → Katalon Studio Preferences → Katalon → Mobile**: em *Appium Directory* informe `C:\Users\gnlos\AppData\Roaming\npm\node_modules\appium`.
5. **File → Open Project** → selecione a pasta `automacao-katalon/PetPulse-Katalon`. O Profile, a keyword, os 4 Test Cases e a suite `PetPulse UI` já estão lá.
6. Abra a suite `PetPulse UI` → seta ao lado de **Run** → **Android** → `emulator-5554`.

Se o Katalon não abrir o projeto, crie um projeto Mobile vazio e siga as seções 4.1 a 4.4 abaixo (copiar e colar). As seções 1 a 3 só são necessárias em outro computador.

---

## 1. Instalar o Android Studio e criar o emulador

1. Instale o [Android Studio](https://developer.android.com/studio).
2. Em **More Actions → SDK Manager**, deixe instalados *Android SDK Platform 35*, *Android SDK Build-Tools* e *Android SDK Platform-Tools*.
3. Em **More Actions → Virtual Device Manager → Create device**, escolha **Pixel 7** com a imagem **API 34 (Google APIs)** e inicie o emulador.
4. Configure as variáveis de ambiente do Windows (Iniciar → "Editar as variáveis de ambiente do sistema"):
   - `ANDROID_HOME` = `%LOCALAPPDATA%\Android\Sdk`
   - `JAVA_HOME` = `C:\Program Files\Android\Android Studio\jbr`
   - Em `Path`, adicione `%ANDROID_HOME%\platform-tools`
5. Abra um novo PowerShell e confira se o emulador aparece:

```powershell
adb devices
```

Deve listar algo como `emulator-5554   device`.

## 2. Gerar o APK do app

O APK precisa apontar para a API no Render. É o padrão do app quando não existe `.env`.

**Opção A — build local (não precisa de conta Expo):**

```powershell
git clone https://github.com/Fiap-2tdspg-Challenge/PetPulse-mobile.git
cd PetPulse-mobile
npm install
npx expo prebuild -p android
cd android
.\gradlew.bat assembleRelease -PreactNativeArchitectures=x86_64
```

O APK sai em `android\app\build\outputs\apk\release\app-release.apk`. Copie para `automacao-katalon\apk\PetPulse.apk`. A primeira compilação demora de 10 a 20 minutos. O parâmetro `x86_64` compila só para o emulador, o que deixa o build mais rápido; para um celular físico, remova-o.

> Clone o app e aponte o `JAVA_HOME` para pastas **sem acento nem espaço** (ex.: `C:\Users\<você>\ppb`). Caminhos como "Área de Trabalho" quebram a etapa nativa do build (`prefab_command.bat ... O sistema não pode encontrar o caminho especificado`).

**Opção B — EAS Build (na nuvem):** quem é dono do projeto no Expo (`pietrowilhelm`) roda `npx eas-cli build -p android --profile preview`, baixa o APK pelo link gerado e salva no mesmo caminho.

Para conferir o APK, instale com `adb install apk\PetPulse.apk` e abra o app no emulador. Faça um login manual uma vez, para "acordar" a API do Render.

## 3. Instalar o Appium (motor de automação do Katalon)

```powershell
npm install -g appium
appium driver install uiautomator2
```

## 4. Configurar o Katalon Studio

1. Baixe o [Katalon Studio](https://katalon.com/download) (a versão gratuita serve) e entre com uma conta Katalon.
2. **Window → Katalon Studio Preferences → Katalon → Mobile**: em *Appium Directory* informe `C:\Users\<seu usuário>\AppData\Roaming\npm\node_modules\appium`.
3. **File → New → Project**: nome `PetPulse-Katalon`, tipo **Mobile**.

### 4.1 Variáveis do projeto

Em **Profiles → default**, adicione:

| Nome | Tipo | Valor |
|---|---|---|
| `APK_PATH` | String | caminho completo do APK, ex.: `C:\Users\gnlos\OneDrive\Área de Trabalho\SPRINT 4\automacao-katalon\apk\PetPulse.apk` |
| `API_URL` | String | `https://petpulse-api-j1k8.onrender.com` |
| `TUTOR_EMAIL` | String | `security.tutor@petpulse.com` |
| `TUTOR_ID` | Number | `23` |
| `VET_EMAIL` | String | `carlos.andrade@vetcare.com` |
| `SENHA` | String | `123456` |

### 4.2 Keyword compartilhada

1. Clique com o botão direito em **Keywords → New → Package** e dê o nome `petpulse`.
2. Clique com o botão direito no pacote → **New → Keyword** → nome `Helpers`.
3. Apague o conteúdo gerado, cole o de [Keywords/petpulse/Helpers.groovy](Keywords/petpulse/Helpers.groovy) e salve (Ctrl+S).

### 4.3 Test Cases

Para cada arquivo em [Scripts/](Scripts):

1. Clique com o botão direito em **Test Cases → New → Test Case**, com o mesmo nome do arquivo (ex.: `TC-UI-01 Login com senha invalida`).
2. Abra a aba **Script** (embaixo do editor), apague o conteúdo, cole o do arquivo e salve.

Depois de salvar, a aba **Manual** mostra os passos em forma de tabela, o que é bom para mostrar no vídeo.

### 4.4 Test Suite

Clique com o botão direito em **Test Suites → New → Test Suite** → nome `PetPulse UI`. Em seguida clique em **Add** e selecione os 4 test cases, na ordem.

## 5. Executar

1. Deixe o emulador aberto (`adb devices` mostrando `device`).
2. Abra a suite `PetPulse UI` e, na barra de ferramentas, clique na seta ao lado de **Run** → **Android** → selecione o emulador.
3. O Katalon instala o app, executa os 4 testes e mostra o resultado em **Log Viewer** / **Job Progress**.
4. O relatório fica em **Reports**. Clique com o botão direito na execução → **Export as** → HTML ou PDF e salve em `automacao-katalon/evidencias/`.

Cada teste leva de 1 a 2 minutos. O primeiro pode demorar mais se a API do Render estiver dormindo.

## 6. Record & Playback (para o vídeo)

O enunciado cita gravação Record & Playback. Para mostrar:

1. **Tools → Mobile Recorder** (ou o ícone *Record Mobile*), escolha o emulador e o APK.
2. No espelho do app, toque em **Tutor**, digite o e-mail e a senha `000000` e toque em **Login**. Cada ação vira um passo na lista.
3. Selecione o texto da mensagem de erro e adicione a ação **Verify Element Text**.
4. **Save Script** → nome `TC-UI-00 Gravado - login invalido`. Rode o teste gravado e mostre que ele passa.

Os 4 testes da suite foram escritos em script porque preparam e limpam os dados pela API. O teste gravado serve para demonstrar a ferramenta.

## Solução de problemas

| Problema | O que fazer |
|---|---|
| `Unable to find element` | Abra **Tools → Spy Mobile**, capture a tela e confira o texto/atributo do elemento. Os localizadores ficam em `Helpers.groovy` (`texto`, `campo`) ou direto no script (sino da Home e botão "+"). |
| Diálogo de permissão de notificações | `permitirNotificacoes()` já aceita; se aparecer outro (localização), toque em *Permitir* uma vez manualmente. |
| Timeout no login ou na preparação | A API do Render estava dormindo. Abra https://petpulse-api-j1k8.onrender.com/actuator/health no navegador, espere `UP` e rode de novo. |
| Aparecem dois pets "Bidu QA" | Sobrou um pet de uma execução interrompida (ex.: Katalon fechado no meio). Remova o mais antigo pelo Swagger (`DELETE` de alertas, leituras, histórico, coleira e pet). |
| Appium não encontrado | Confira o caminho em *Preferences → Katalon → Mobile* e rode `appium --version`. |
