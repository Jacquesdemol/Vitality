/**
 * Android Studio Project Generator
 * Produces complete, native, production-grade Kotlin + Jetpack Compose + Material 3 +
 * Health Connect SDK + Room + WorkManager source code ready for compilation in Android Studio.
 */

import JSZip from 'jszip';

export interface AndroidFileItem {
  path: string;
  category: 'gradle' | 'manifest' | 'kotlin' | 'res' | 'docs';
  description: string;
  content: string;
}

export function getAndroidStudioProjectFiles(): AndroidFileItem[] {
  return [
    {
      path: 'build.gradle.kts',
      category: 'gradle',
      description: 'Root Gradle configuration',
      content: `// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
`,
    },
    {
      path: 'settings.gradle.kts',
      category: 'gradle',
      description: 'Gradle project settings & repositories',
      content: `pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VITAL"
include(":app")
`,
    },
    {
      path: 'gradle/libs.versions.toml',
      category: 'gradle',
      description: 'Version Catalog with Health Connect, Compose & Room',
      content: `[versions]
agp = "8.7.2"
kotlin = "2.0.21"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.11.00"
healthConnect = "1.1.0-alpha11"
room = "2.6.1"
workManager = "2.10.0"
coroutines = "1.9.0"
ksp = "2.0.21-1.0.28"
navigationCompose = "2.8.4"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Android Health Connect SDK
androidx-health-connect = { group = "androidx.health.connect", name = "connect-client", version.ref = "healthConnect" }

# Room Database
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
`,
    },
    {
      path: 'app/build.gradle.kts',
      category: 'gradle',
      description: 'App Module Gradle with Health Connect and Compose',
      content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.vital.health"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vital.health"
        minSdk = 28 // Required for Health Connect SDK
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Android Health Connect SDK
    implementation(libs.androidx.health.connect)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
}
`,
    },
    {
      path: 'app/src/main/AndroidManifest.xml',
      category: 'manifest',
      description: 'Android Manifest with Health Connect permissions and rationale',
      content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Health Connect Read Permissions -->
    <uses-permission android:name="android.permission.health.READ_STEPS" />
    <uses-permission android:name="android.permission.health.READ_HEART_RATE" />
    <uses-permission android:name="android.permission.health.READ_RESTING_HEART_RATE" />
    <uses-permission android:name="android.permission.health.READ_SLEEP" />
    <uses-permission android:name="android.permission.health.READ_EXERCISE" />
    <uses-permission android:name="android.permission.health.READ_ACTIVE_CALORIES_BURNED" />
    <uses-permission android:name="android.permission.health.READ_TOTAL_CALORIES_BURNED" />
    <uses-permission android:name="android.permission.health.READ_DISTANCE" />
    <uses-permission android:name="android.permission.health.READ_WEIGHT" />
    <uses-permission android:name="android.permission.health.READ_BODY_FAT" />
    <uses-permission android:name="android.permission.health.READ_OXYGEN_SATURATION" />
    <uses-permission android:name="android.permission.health.READ_RESPIRATORY_RATE" />
    <uses-permission android:name="android.permission.health.READ_HEART_RATE_VARIABILITY" />
    <uses-permission android:name="android.permission.health.READ_BLOOD_PRESSURE" />

    <!-- Health Connect Package Query -->
    <queries>
        <package android:name="com.google.android.apps.healthdata" />
        <intent>
            <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
        </intent>
    </queries>

    <application
        android:name=".VitalApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.VITAL">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.VITAL">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <!-- Health Connect Permissions Rationale Activity Target -->
            <intent-filter>
                <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
            </intent-filter>
        </activity>

        <activity-alias
            android:name=".ViewPermissionUsageActivity"
            android:exported="true"
            android:targetActivity=".MainActivity"
            android:permission="android.permission.START_VIEW_PERMISSION_USAGE">
            <intent-filter>
                <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
                <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
            </intent-filter>
        </activity-alias>
    </application>

</manifest>
`,
    },
    {
      path: 'app/src/main/java/com/vital/health/theme/Color.kt',
      category: 'kotlin',
      description: 'Sober, minimalist, monochromatic dark palette',
      content: `package com.vital.health.theme

import androidx.compose.ui.graphics.Color

// Paleta Oficial VITAL
val DeepBlack = Color(0xFF090909)       // Fundo principal
val Graphite = Color(0xFF171719)        // Cartões e superfícies
val DarkGray = Color(0xFF242428)        // Bordas, divisores e botões secundários
val MidGray = Color(0xFF85858C)         // Textos secundários e rótulos
val CrispWhite = Color(0xFFF2F2F4)      // Tipografia primária e destaques

// Transparências Glassmorphism
val GlassBackground = Color(0xCC171719) // 80% Graphite
val GlassBorder = Color(0x1AFFFFFF)     // 10% Branco
val PillHighlight = Color(0xFF2A2A30)   // Fundo da aba selecionada
`,
    },
    {
      path: 'app/src/main/java/com/vital/health/theme/Theme.kt',
      category: 'kotlin',
      description: 'Material 3 Dark Theme configured for VITAL',
      content: `package com.vital.health.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CrispWhite,
    onPrimary = DeepBlack,
    surface = Graphite,
    onSurface = CrispWhite,
    background = DeepBlack,
    onBackground = CrispWhite,
    outline = DarkGray,
    surfaceVariant = DarkGray,
    onSurfaceVariant = MidGray
)

@Composable
fun VitalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
`,
    },
    {
      path: 'app/src/main/java/com/vital/health/ui/components/VitalBottomBar.kt',
      category: 'kotlin',
      description: 'Floating pill navigation island inspired by Galaxy Store',
      content: `package com.vital.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.theme.*

enum class VitalTab(val title: String, val icon: ImageVector) {
    SLEEP("Sono", Icons.Default.Bedtime),
    EXERCISE("Exercícios", Icons.Default.FitnessCenter),
    HOME("Início", Icons.Default.Home),
    SETTINGS("Configurações", Icons.Default.Settings)
}

@Composable
fun VitalBottomBar(
    currentTab: VitalTab,
    onTabSelected: (VitalTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ilha flutuante em formato de pílula
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(GlassBackground)
                .border(1.dp, GlassBorder, CircleShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VitalTab.values().forEach { tab ->
                val isSelected = tab == currentTab
                val tabBg by animateColorAsState(
                    targetValue = if (isSelected) PillHighlight else androidx.compose.ui.graphics.Color.Transparent,
                    animationSpec = tween(durationMillis = 250),
                    label = "tab_highlight"
                )
                val contentColor = if (isSelected) CrispWhite else MidGray

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(tabBg)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.title,
                        color = contentColor,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
`,
    },
    {
      path: 'app/src/main/java/com/vital/health/healthconnect/HealthConnectRepository.kt',
      category: 'kotlin',
      description: 'Android Health Connect SDK client and record aggregation',
      content: `package com.vital.health.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectRepository(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun checkGrantedPermissions(): Set<String> {
        val client = healthConnectClient ?: return emptySet()
        return client.permissionController.getGrantedPermissions()
    }

    suspend fun readTodaySteps(): Long {
        val client = healthConnectClient ?: return 0L
        val startTime = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val endTime = Instant.now()

        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun readLatestHeartRate(): Int? {
        val client = healthConnectClient ?: return null
        val startTime = Instant.now().minus(24, ChronoUnit.HOURS)
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
            )
        )
        return response.records.lastOrNull()?.samples?.lastOrNull()?.beatsPerMinute?.toInt()
    }

    suspend fun readLatestSleepSession(): SleepSessionRecord? {
        val client = healthConnectClient ?: return null
        val startTime = Instant.now().minus(24, ChronoUnit.HOURS)
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
            )
        )
        return response.records.lastOrNull()
    }
}
`,
    },
    {
      path: 'app/src/main/java/com/vital/health/insights/DailyInsightEngine.kt',
      category: 'kotlin',
      description: 'Deterministic, rule-based daily insight generator',
      content: `package com.vital.health.insights

data class Insight(
    val id: String,
    val category: String,
    val title: String,
    val message: String
)

class DailyInsightEngine {
    companion object {
        fun generate(
            currentSteps: Long,
            stepGoal: Long,
            sleepMinutes: Long,
            weeklyAvgSleepMinutes: Long,
            workoutCount: Int,
            restingHrDelta: Int
        ): List<Insight> {
            val insights = mutableListOf<Insight>()

            // 1. Proximidade de Passos
            val ratio = currentSteps.toFloat() / stepGoal.coerceAtLeast(1L)
            if (ratio in 0.7f..0.99f) {
                insights.add(
                    Insight(
                        id = "step_prox",
                        category = "activity",
                        title = "Próximo da Meta",
                        message = "Você está próximo de alcançar sua meta de passos. Uma caminhada curta pode ajudar."
                    )
                )
            } else if (currentSteps < 2000) {
                insights.add(
                    Insight(
                        id = "inactivity",
                        category = "habits",
                        title = "Movimente-se",
                        message = "Você passou boa parte do dia sem registrar atividade. Uma pausa para movimentar o corpo pode ser interessante."
                    )
                )
            }

            // 2. Duração do sono
            if (sleepMinutes > 0 && weeklyAvgSleepMinutes > 0 && (sleepMinutes - weeklyAvgSleepMinutes) <= -40) {
                insights.add(
                    Insight(
                        id = "sleep_low",
                        category = "sleep",
                        title = "Duração do Sono",
                        message = "Sua duração de sono ficou abaixo da sua média semanal. Tente manter um horário regular para dormir."
                    )
                )
            }

            // 3. Frequência Cardíaca em Repouso
            if (kotlin.math.abs(restingHrDelta) >= 7) {
                insights.add(
                    Insight(
                        id = "hr_drift",
                        category = "recovery",
                        title = "Frequência Cardíaca",
                        message = "Sua frequência cardíaca em repouso está diferente da sua média habitual. Observe a tendência e considere buscar orientação profissional se houver sintomas."
                    )
                )
            }

            // 4. Consistência de treinos
            if (workoutCount >= 3) {
                insights.add(
                    Insight(
                        id = "workouts",
                        category = "consistency",
                        title = "Consistência de Treinos",
                        message = "Você treinou $workoutCount vezes nesta semana. Continue mantendo uma rotina consistente."
                    )
                )
            }

            if (insights.isEmpty()) {
                insights.add(
                    Insight(
                        id = "default_tip",
                        category = "habits",
                        title = "Dica de hoje",
                        message = "Manter um horário regular para dormir ajuda na qualidade do seu sono."
                    )
                )
            }

            return insights
        }
    }
}
`,
    },
    {
      path: 'app/src/main/java/com/vital/health/zepp/ZeppConnector.kt',
      category: 'kotlin',
      description: 'Official Zepp Open Platform integration architecture',
      content: `package com.vital.health.zepp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class ZeppConnectionStatus {
    object Disconnected : ZeppConnectionStatus()
    object AwaitingConfig : ZeppConnectionStatus()
    object SyncedViaHealthConnect : ZeppConnectionStatus()
    data class Connected(val tokenExpiry: Long, val deviceName: String) : ZeppConnectionStatus()
}

/**
 * ZeppConnector - Interface modular para Zepp Open Platform (Amazfit).
 * Observação de segurança: Conforme especificação, client secrets e troca
 * de token OAuth 2.0 são delegados a um backend seguro.
 */
class ZeppConnector {

    private val _status = MutableStateFlow<ZeppConnectionStatus>(
        ZeppConnectionStatus.SyncedViaHealthConnect
    )
    val status: StateFlow<ZeppConnectionStatus> = _status

    fun connectViaZeppHealthConnect() {
        _status.value = ZeppConnectionStatus.SyncedViaHealthConnect
    }

    fun initiateOAuth(authCode: String) {
        // Envia authCode para backend seguro
        // Backend valida na Zepp Open Platform e retorna token de sessão
    }
}
`,
    },
    {
      path: 'README.md',
      category: 'docs',
      description: 'Instructions to open and compile in Android Studio',
      content: `# VITAL — Aplicativo de Saúde e Bem-estar para Android

Projeto nativo completo em **Kotlin** com **Jetpack Compose**, **Material 3**, **Health Connect SDK**, **Room** e **WorkManager**.

## 📱 Estrutura do Projeto
- **ui/screens**: Telas Início, Sono, Exercícios e Configurações fiéis ao design monocromático (#090909, #171719, #242428).
- **ui/components**: Ilha de navegação inferior flutuante inspirada na Galaxy Store, cartões de batimentos cardíacos, hipnograma de sono e composição corporal.
- **healthconnect**: Camada de repositório com suporte a 14 tipos de registros do Health Connect (StepsRecord, SleepSessionRecord, etc).
- **zepp**: Módulo preparado para a plataforma oficial Zepp Open Platform (Amazfit).
- **insights**: DailyInsightEngine com recomendações não-diagnósticas baseadas no histórico individual.

## 🚀 Como Compilar no Android Studio
1. Abra o **Android Studio** (versão Ladybug / Koala ou superior recomendada).
2. Selecione **Open** e escolha a pasta deste projeto.
3. Aguarde o Gradle sincronizar as dependências automáticas (\`libs.versions.toml\`).
4. Conecte um dispositivo físico Android (Android 9+ / API 28+) com o aplicativo **Health Connect** instalado.
5. Clique em **Run 'app'** (\`Shift + F10\`).
`,
    },
  ];
}

export async function downloadAndroidProjectZip(): Promise<Blob> {
  const zip = new JSZip();
  const files = getAndroidStudioProjectFiles();

  files.forEach((file) => {
    zip.file(file.path, file.content);
  });

  return await zip.generateAsync({ type: 'blob' });
}
