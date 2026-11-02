# PetPulse — Automação de testes (Sprint 4)

Parte B da entrega de Compliance, QA & Tests: automação da **interface** (app Android, Katalon Studio) e da **API** (Postman + Newman), com os relatórios das execuções.

| Pasta | Ferramenta | Testes | Último resultado |
|---|---|---|---|
| `katalon/` | Katalon Studio (Mobile) | 4 test cases de interface (TC-UI-01 a TC-UI-04) | 01/11/2026 21:33 — **4/4 aprovados** |
| `postman/` | Postman + Newman | 24 requisições, 78 asserções (integração e desempenho) | 01/11/2026 — **78/78 aprovadas** |

## APK do app

`katalon/apk/PetPulse.apk`: build release do PetPulse-mobile para emulador Android x86_64 (Android 7.0+), já apontando para a API no Render.

## Relatórios

- `katalon/evidencias/relatorio-katalon.html`: relatório da suite `PetPulse UI` (também em CSV e JUnit XML)
- `postman/evidencias/relatorio-newman.html`: relatório do Newman

## Katalon (interface do app)

| Teste | Caso manual | O que valida |
|---|---|---|
| TC-UI-01 Login com senha inválida | CT17 | Mensagem "E-mail ou senha incorretos." e permanência na tela de login |
| TC-UI-02 Login do tutor exibe pets | CT17 | Home do tutor com o pet "Bidu QA" / Golden Retriever |
| TC-UI-03 Alerta inteligente na Home | CT18 | Leitura IoT com FC 195 gera alerta exibido no sino da Home |
| TC-UI-04 Veterinário cadastra histórico | CT19 | Veterinário cadastra uma Consulta, que aparece no app e é gravada na API |

Para executar: no Katalon Studio, use **File → Open Project → `katalon/PetPulse-Katalon`**. Em seguida ajuste `APK_PATH` em *Profiles → default* para o caminho completo de `katalon/apk/PetPulse.apk` nesta máquina e rode a suite `PetPulse UI` em um emulador Android. O passo a passo completo, incluindo como gerar o APK, está em `katalon/README.md`.

## Postman (API)

Importe `PetPulse-QA.postman_collection.json` e `PetPulse-Render.postman_environment.json` no Postman e rode a coleção (**Run**). Pela linha de comando:

```bash
cd postman
npm install
npm run test:relatorio
```

API testada: https://petpulse-api-j1k8.onrender.com. A primeira chamada pode demorar enquanto o serviço gratuito do Render acorda.
