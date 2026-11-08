# VITAL — Aplicativo de Saúde e Bem-estar para Android

**Repositório Oficial no GitHub**: [https://github.com/Jacquesdemol/Vitality](https://github.com/Jacquesdemol/Vitality)

**VITAL** é um aplicativo Android nativo desenvolvido em **Kotlin** e **Jetpack Compose** com arquitetura **MVVM**, projetado para monitoramento de saúde, atividade física e bem-estar inspirado na elegância do ecossistema Samsung Health e navegação em ilha flutuante da Galaxy Store.

Possui identidade visual monocromática e minimalista (#090909, #171719, #242428, #85858C, #F2F2F4), integração centralizada com o **Android Health Connect SDK**, sistema de **Notificações Inteligentes e Incentivos** com **WorkManager**, tutorial interativo de onboarding, armazenamento local com **Room** e conector para **Zepp Open Platform** (dispositivos Amazfit).

---

## 📂 Estrutura de Arquivos para o GitHub e Android Studio

```text
├── build.gradle.kts                      # Script de build raiz
├── settings.gradle.kts                   # Configuração de repositórios e módulos
├── gradle.properties                     # Configurações de JVM e AndroidX
├── AndroidManifest.xml                   # Cópia raiz do manifesto (consultas e permissões)
├── VitalBottomBar.kt                     # Ilha flutuante estilo Galaxy Store (cópia raiz)
├── Theme.kt                              # Tema Material 3 monocromático (cópia raiz)
├── HealthConnectRepository.kt            # Repositório do Health Connect (cópia raiz)
├── ZeppConnector.kt                      # Integração Zepp Open Platform (cópia raiz)
├── README.md                             # Documentação e instruções de compilação
├── gradle/
│   └── libs.versions.toml                # Version Catalog das dependências
└── app/
    ├── build.gradle.kts                  # Configurações do módulo :app
    └── src/
        └── main/
            ├── AndroidManifest.xml       # Manifesto Android com 14 permissões de saúde
            ├── res/
            │   └── values/
            │       ├── strings.xml
            │       └── colors.xml
            └── java/com/vital/health/
                ├── VitalApplication.kt   # Inicialização do app
                ├── MainActivity.kt       # Activity principal com navegação Compose
                ├── theme/
                │   ├── Color.kt          # Paleta monocromática oficial
                │   ├── Theme.kt          # Material 3 Dark Theme
                │   └── Type.kt           # Tipografia moderna e legível
                ├── ui/
                │   ├── components/
                │   │   └── VitalBottomBar.kt # Navegação inferior em pílula
                │   └── screens/
                │       ├── HomeScreen.kt     # Tela Início com widgets personalizáveis
                │       ├── SleepScreen.kt    # Tela Sono com hipnograma horizontal
                │       ├── ExerciseScreen.kt # Tela Exercícios com zonas de FC
                │       └── SettingsScreen.kt # Tela Configurações e governança
                ├── healthconnect/
                │   └── HealthConnectRepository.kt # Camada do Health Connect SDK
                ├── zepp/
                │   └── ZeppConnector.kt      # Conector modular Zepp Open Platform
                ├── insights/
                │   └── DailyInsightEngine.kt # Motor de recomendações determinísticas
                └── data/local/
                    ├── VitalDatabase.kt      # Banco de dados local Room
                    ├── HealthDao.kt          # DAO de consultas e inserções
                    └── HealthEntity.kt       # Entidades Room para cache off-line
```

---

## 🛠️ Tecnologias e Dependências

- **Linguagem**: Kotlin 2.0.21
- **Interface**: Jetpack Compose com Material 3 (BOM 2024.11.00)
- **SDK de Saúde**: `androidx.health.connect:connect-client:1.1.0-alpha11`
- **Banco de Dados Local**: Android Room 2.6.1 com KSP
- **Tarefas em Segundo Plano**: Android WorkManager 2.10.0
- **Concorrência**: Kotlin Coroutines 1.9.0 e StateFlow
- **Navegação**: Navigation Compose 2.8.4
- **Versão Mínima do Android**: Android 9.0 (API 28+)

---

## 🚀 Como Compilar o APK no Android Studio

### Pré-requisitos
- **Android Studio Ladybug (2024.2+)** ou Koala / Iguana instalado.
- **JDK 17** configurado no Android Studio (*Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*).
- Dispositivo físico com Android 9+ ou emulador com a imagem **Google Play System Image** (com o app **Health Connect** instalado via Google Play ou pré-instalado a partir do Android 14).

### Passo a Passo
1. Clone ou baixe este repositório no seu computador:
   ```bash
   git clone <URL_DO_SEU_REPOSITORIO>
   cd VITAL
   ```
2. Abra o **Android Studio**.
3. Selecione **File > Open...** e escolha a pasta raiz do projeto.
4. Aguarde a sincronização automática do Gradle (*Gradle Sync*).
5. Conecte seu dispositivo Android via cabo USB ou Wi-Fi com a **Depuração USB** ativada.
6. Clique no botão verde **Run 'app'** (`Shift + F10`) ou no menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
7. O APK compilado estará disponível em:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## ⚙️ Compilação Automática no GitHub Actions (CI/CD)

Para gerar o APK automaticamente a cada commit no seu repositório GitHub, crie o arquivo `.github/workflows/android.yml`:

```yaml
name: Build Android APK

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew || true

      - name: Build Debug APK with Gradle
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: vital-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Permissões do Android Health Connect

O aplicativo declara e gerencia de forma granular 14 tipos de registros de saúde no `AndroidManifest.xml`:

1. `StepsRecord` (Passos)
2. `HeartRateRecord` (Frequência Cardíaca contínua)
3. `RestingHeartRateRecord` (Batimentos em repouso)
4. `SleepSessionRecord` (Sessões e estágios de sono)
5. `ExerciseSessionRecord` (Sessões de treino)
6. `ActiveCaloriesBurnedRecord` (Calorias ativas)
7. `TotalCaloriesBurnedRecord` (Calorias totais)
8. `DistanceRecord` (Distância percorrida)
9. `WeightRecord` (Peso corporal)
10. `BodyFatRecord` (Gordura corporal por bioimpedância)
11. `OxygenSaturationRecord` (Saturação SpO2)
12. `RespiratoryRateRecord` (Frequência respiratória)
13. `HeartRateVariabilityRmssdRecord` (Variabilidade cardíaca - HRV)
14. `BloodPressureRecord` (Pressão arterial)

---

## ⌚ Integração com Dispositivos Zepp / Amazfit

A classe `ZeppConnector` implementa a arquitetura modular recomendada para a **Zepp Open Platform oficial**:
- A autenticação OAuth 2.0 e credenciais secretas residem exclusivamente em backend seguro (nunca no APK).
- Enquanto a integração direta não for autenticada via backend corporativo, o aplicativo utiliza os dados que o app oficial Zepp sincroniza com o **Android Health Connect**, garantindo privacidade absoluta e disponibilidade off-line.

---

## 🛡️ Privacidade e Soberania de Dados

- **100% On-Device**: Todos os dados consultados são persistidos exclusivamente no SQLite/Room local do dispositivo Android.
- **Não Diagnóstico**: O `DailyInsightEngine` produz apenas contextualizações baseadas em hábitos individuais e regularidade, sem realizar diagnósticos médicos nem prescrever medicamentos.
