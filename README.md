# VITAL — Aplicativo de Saúde e Bem-estar para Android

**Repositório Oficial no GitHub**: [https://github.com/Jacquesdemol/Vitality](https://github.com/Jacquesdemol/Vitality)

**VITAL** é um aplicativo Android nativo desenvolvido em **Kotlin** e **Jetpack Compose** com arquitetura **MVVM**, projetado como um visualizador prático e soberano de saúde física, atividade esportiva e sono sincronizado diretamente com o **Android Health Connect SDK** e dispositivos **Zepp OS / Amazfit**.

---

## 🚀 Principais Funcionalidades

1. **Health Connect 100% Real (Sem Dados Fictícios)**:
   - Extração rigorosa de **estágios de sono reais** (`DEEP`, `LIGHT`, `REM`, `AWAKE`) a partir de `SleepSessionRecord.stages`, eliminando quaisquer porcentagens artificiais.
   - Separação estrita entre **Frequência Cardíaca instantânea** (`HeartRateRecord`) e **Frequência Cardíaca de Repouso** (`RestingHeartRateRecord`).
   - Leitura real de sessões de treino (`ExerciseSessionRecord`) com paginação resiliente, tipo de atividade traduzido, calorias correlacionadas, distância e zonas de frequência cardíaca.
   - Sincronização de passos (`StepsRecord`), calorias ativas (`ActiveCaloriesBurnedRecord`) e distância agregada (`DistanceRecord`).

2. **Ofensiva de Atividade (Estilo Duolingo)**:
   - Contador de dias consecutivos de treino e atividade.
   - Registro de recorde pessoal de ofensiva (maior sequência dos últimos 30 dias).
   - Acompanhamento semanal de metas com barra de progresso dinâmica e mensagens personalizadas ("Você treinou 5 dias nesta semana. Imparável!").

3. **Painel Interativo de Atividades (`ExerciseScreen.kt`)**:
   - Gráfico de barras semanal de 7 dias com navegação entre semanas anteriores e atual (`<` e `>`).
   - Seleção interativa dia a dia.
   - Lista detalhada de sessões reais do dia selecionado com duração, calorias, distância, batimentos médios e identificação do app de origem.
   - Estado vazio limpo e sem placeholders quando não houver treinos no dia.

4. **Painel Interativo de Qualidade do Sono (`SleepScreen.kt`)**:
   - Gráfico de duração do sono dos 7 dias da semana com navegação por semanas.
   - Seleção de noites individuais com horários reais de adormecer e acordar.
   - Comparação instantânea com a média semanal de descanso.
   - Hipnograma cronológico com estágios biométricos reais e aviso transparente quando o dispositivo registrar apenas o tempo total sem estágios.

5. **Painel de Distância Percorrida**:
   - Visualizador de 7 dias com total acumulado em quilômetros e média diária.
   - Indicador de proveniência do Health Connect e sensores de passos.

6. **Sistema Completo de Temas & Personalização ("Aparência & Cores")**:
   - **Modos de Exibição**: Seguir Sistema, Escuro (*Dark Mode* profundo minimalista) e Claro (*Light Mode* elegante).
   - **6 Paletas de Cores**:
     - *Monocromático* (Padrão Galaxy Store: `#090909`, `#171719`, `#242428`, `#85858C`, `#F2F2F4`)
     - *Azul Oceano*
     - *Verde Esmeralda*
     - *Roxo Ametista*
     - *Laranja Pôr do Sol*
     - *Material You / Monet* (Cores dinâmicas do Android 12+)
   - Persistência imediata via `SharedPreferences` sem necessidade de reiniciar o aplicativo.

7. **Citações Motivacionais de Pessoas Reais**:
   - Frases autênticas e inspiradoras de lendas do esporte e do pensamento: **Ayrton Senna**, **Muhammad Ali**, **Kobe Bryant**, **Usain Bolt**, **Marco Aurélio**, **Eliud Kipchoge**, **Serena Williams**, **Bernardinho** e **Sêneca**.
   - Integração com o sistema de notificações periódicas do Android via **WorkManager**.

8. **Governança de Perfil e Dispositivos**:
   - Edição de perfil pessoal (Nome, Idade, Altura, Peso e cálculo em tempo real de IMC).
   - Personalização de metas de saúde (Passos, Calorias, Horas de sono e Dias de treino).
   - Guia passo a passo de integração com pulseiras e relógios Amazfit através do app oficial Zepp.
   - Opção de rever o tutorial de boas-vindas a qualquer momento.

---

## 📂 Estrutura de Arquivos

```text
├── build.gradle.kts                      # Script de build raiz
├── settings.gradle.kts                   # Configuração de repositórios e módulos
├── gradle.properties                     # Configurações de JVM e AndroidX
├── AndroidManifest.xml                   # Cópia raiz do manifesto
├── VitalBottomBar.kt                     # Ilha flutuante estilo Galaxy Store (cópia raiz)
├── Theme.kt                              # Sistema de temas e CompositionLocalProvider (cópia raiz)
├── HealthConnectRepository.kt            # Repositório oficial do Health Connect (cópia raiz)
├── ZeppConnector.kt                      # Conector Zepp Open Platform (cópia raiz)
├── README.md                             # Documentação técnica e arquitetura
├── gradle/
│   └── libs.versions.toml                # Version Catalog das dependências
└── app/
    ├── build.gradle.kts                  # Configurações do módulo :app
    └── src/
        └── main/
            ├── AndroidManifest.xml       # Manifesto Android com 14 permissões de saúde
            ├── res/
            │   ├── mipmap-*/             # Ícones do app (ic_launcher e ic_launcher_round)
            │   └── values/
            │       ├── strings.xml
            │       └── themes.xml        # Theme.VITAL compatível com Compose
            └── java/com/vital/health/
                ├── VitalApplication.kt   # Inicialização do app
                ├── MainActivity.kt       # Activity principal com suporte a temas e estados
                ├── model/
                │   └── HealthModels.kt   # Modelos de domínio (SleepStage, Streak, Activity)
                ├── theme/
                │   ├── Color.kt          # Tokens de cores primárias
                │   ├── Theme.kt          # Material 3 ColorScheme builder
                │   ├── VitalThemeSystem.kt # Modos, paletas e ThemePreferencesManager
                │   └── Type.kt           # Tipografia moderna
                ├── ui/
                │   ├── components/
                │   │   └── VitalBottomBar.kt # Navegação inferior em pílula
                │   └── screens/
                │       ├── HomeScreen.kt     # Início com Ofensiva, Batimentos, Calorias e Passos
                │       ├── SleepScreen.kt    # Painel interativo de sono e hipnograma
                │       ├── ExerciseScreen.kt # Painel interativo de atividades com navegação semanal
                │       ├── SettingsScreen.kt # Configurações, temas, perfil e metas
                │       └── TutorialScreen.kt # Onboarding em 3 passos
                ├── healthconnect/
                │   └── HealthConnectRepository.kt # Leitura de dados biométricos reais
                ├── zepp/
                │   └── ZeppConnector.kt      # Conector modular Amazfit
                ├── notifications/
                │   └── VitalNotificationHelper.kt # Notificações com citações reais
                ├── insights/
                │   └── DailyInsightEngine.kt # Motor de insights e citações
                └── data/
                    ├── UserProfile.kt        # Modelo de perfil e metas
                    ├── UserProfileManager.kt # Gerenciador de persistência do perfil
                    └── local/
                        ├── VitalDatabase.kt  # Banco de dados local Room
                        ├── HealthDao.kt      # DAO de consultas
                        └── HealthEntity.kt   # Entidade Room para cache off-line
```

---

## 🛠️ Tecnologias e Dependências

- **Linguagem**: Kotlin 2.0.21
- **Interface**: Jetpack Compose com Material 3 (BOM 2024.11.00)
- **SDK de Saúde**: `androidx.health.connect:connect-client:1.1.0-alpha11`
- **Banco de Dados Local**: Android Room 2.6.1 com KSP
- **Tarefas em Segundo Plano**: Android WorkManager 2.10.0
- **Concorrência**: Kotlin Coroutines 1.9.0 e StateFlow
- **Versão Mínima do Android**: Android 9.0 (API 28+)

---

## 🚀 Como Compilar o APK no Android Studio

### Pré-requisitos
- **Android Studio Ladybug (2024.2+)** ou superior instalado.
- **JDK 17** configurado no Android Studio (*Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*).
- Dispositivo físico com Android 9+ ou emulador com a imagem **Google Play System Image** (com o app **Health Connect** instalado via Google Play ou pré-instalado a partir do Android 14).

### Passo a Passo
1. Clone este repositório no seu computador:
   ```bash
   git clone https://github.com/Jacquesdemol/Vitality.git
   cd Vitality
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

O repositório já inclui o workflow `.github/workflows/android.yml` configurado para gerar e disponibilizar o arquivo `vital-debug-apk` nos artefatos da aba *Actions* a cada commit.

---

## 🛡️ Privacidade e Soberania de Dados

- **100% On-Device**: Todos os dados consultados são persistidos exclusivamente no SQLite/Room local do dispositivo Android.
- **Zero Venda de Dados**: O aplicativo não envia métricas para servidores de terceiros ou nuvens privadas.
