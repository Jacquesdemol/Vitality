package com.vital.health

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.health.connect.client.PermissionController
import com.vital.health.healthconnect.HealthConnectRepository
import com.vital.health.notifications.VitalNotificationHelper
import com.vital.health.theme.DeepBlack
import com.vital.health.theme.VitalTheme
import com.vital.health.ui.components.VitalBottomBar
import com.vital.health.ui.components.VitalTab
import com.vital.health.ui.screens.*

class MainActivity : ComponentActivity() {

    private val repository: HealthConnectRepository by lazy {
        HealthConnectRepository(this, (application as VitalApplication).database)
    }

    // Health Connect Permission Launcher
    private val requestHealthPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        // Permissões de saúde processadas
    }

    // Android 13+ Notification Permission Launcher
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notificações concedidas ou negadas
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa canal de notificações e agendamento
        VitalNotificationHelper.createNotificationChannel(this)
        VitalNotificationHelper.schedulePeriodicHealthReminders(this)

        val prefs = getSharedPreferences("vital_health_prefs", Context.MODE_PRIVATE)
        val initialTutorialCompleted = prefs.getBoolean("has_completed_tutorial", false)

        setContent {
            VitalTheme {
                var isTutorialVisible by remember { mutableStateOf(!initialTutorialCompleted) }
                var currentTab by remember { mutableStateOf(VitalTab.HOME) }

                fun requestAllPermissions() {
                    // 1. Notificações no Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    // 2. Health Connect SDK
                    if (repository.isAvailable()) {
                        requestHealthPermissions.launch(repository.requiredPermissions)
                    }
                }

                if (isTutorialVisible) {
                    TutorialScreen(
                        onRequestPermissions = {
                            requestAllPermissions()
                        },
                        onCompleteTutorial = {
                            prefs.edit().putBoolean("has_completed_tutorial", true).apply()
                            isTutorialVisible = false
                        },
                        onTestNotification = {
                            requestAllPermissions()
                            VitalNotificationHelper.sendSampleNotification(this@MainActivity, "motivation")
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DeepBlack,
                        bottomBar = {
                            VitalBottomBar(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                                when (tab) {
                                    VitalTab.HOME -> HomeScreen(
                                        userName = "Iury",
                                        currentSteps = 7842,
                                        targetSteps = 10000,
                                        currentBpm = 72,
                                        activeCalories = 612,
                                        workoutDaysDone = 3,
                                        workoutDaysTarget = 5,
                                        onNavigateToExercise = {
                                            currentTab = VitalTab.EXERCISE
                                        },
                                        onTriggerHealthTip = {
                                            VitalNotificationHelper.sendSampleNotification(
                                                this@MainActivity,
                                                "motivation"
                                            )
                                        }
                                    )
                                    VitalTab.SLEEP -> SleepScreen()
                                    VitalTab.EXERCISE -> ExerciseScreen()
                                    VitalTab.SETTINGS -> SettingsScreen(
                                        onReplayTutorial = {
                                            isTutorialVisible = true
                                        },
                                        onRequestPermissions = {
                                            requestAllPermissions()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
