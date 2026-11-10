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
import com.vital.health.data.UserProfile
import com.vital.health.data.UserProfileManager
import com.vital.health.healthconnect.HealthConnectRepository
import com.vital.health.healthconnect.LiveHealthData
import com.vital.health.notifications.VitalNotificationHelper
import com.vital.health.theme.DeepBlack
import com.vital.health.theme.VitalTheme
import com.vital.health.ui.components.VitalBottomBar
import com.vital.health.ui.components.VitalTab
import com.vital.health.ui.screens.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    private val repository: HealthConnectRepository by lazy {
        HealthConnectRepository(this, (application as VitalApplication).database)
    }

    private val profileManager: UserProfileManager by lazy {
        UserProfileManager(this)
    }

    private var onPermissionsResultCallback: (() -> Unit)? = null

    // Health Connect Permission Launcher
    private val requestHealthPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        onPermissionsResultCallback?.invoke()
    }

    // Android 13+ Notification Permission Launcher
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notificações concedidas ou negadas
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa canal de notificações e agendamento com citações reais
        VitalNotificationHelper.createNotificationChannel(this)
        VitalNotificationHelper.schedulePeriodicHealthReminders(this)

        val prefs = getSharedPreferences("vital_health_prefs", Context.MODE_PRIVATE)
        val initialTutorialCompleted = prefs.getBoolean("has_completed_tutorial", false)

        setContent {
            VitalTheme {
                val coroutineScope = rememberCoroutineScope()

                var isTutorialVisible by remember { mutableStateOf(!initialTutorialCompleted) }
                var currentTab by remember { mutableStateOf(VitalTab.HOME) }

                var userProfile by remember { mutableStateOf(profileManager.getProfile()) }
                var liveHealthData by remember { mutableStateOf(LiveHealthData()) }
                var isSyncing by remember { mutableStateOf(false) }
                var lastSyncTimeStr by remember { mutableStateOf("Nunca") }

                fun syncHealthConnect() {
                    coroutineScope.launch {
                        isSyncing = true
                        try {
                            val data = repository.fetchAllLiveHealthData()
                            liveHealthData = data
                            lastSyncTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: Exception) {
                            // Erro de sincronização tratado graciosamente
                        } finally {
                            isSyncing = false
                        }
                    }
                }

                // Sincroniza dados do Health Connect ao iniciar
                LaunchedEffect(Unit) {
                    syncHealthConnect()
                }

                onPermissionsResultCallback = {
                    syncHealthConnect()
                }

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
                        initialName = userProfile.name,
                        onRequestPermissions = {
                            requestAllPermissions()
                        },
                        onCompleteTutorial = { createdProfile ->
                            profileManager.saveProfile(createdProfile)
                            userProfile = createdProfile
                            prefs.edit().putBoolean("has_completed_tutorial", true).apply()
                            isTutorialVisible = false
                            requestAllPermissions()
                            syncHealthConnect()
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
                                        userProfile = userProfile,
                                        liveData = liveHealthData,
                                        isSyncing = isSyncing,
                                        onSync = { syncHealthConnect() },
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
                                    VitalTab.SLEEP -> SleepScreen(
                                        liveData = liveHealthData,
                                        userProfile = userProfile
                                    )
                                    VitalTab.EXERCISE -> ExerciseScreen(
                                        liveData = liveHealthData,
                                        userProfile = userProfile
                                    )
                                    VitalTab.SETTINGS -> SettingsScreen(
                                        userProfile = userProfile,
                                        onUpdateProfile = { updated ->
                                            profileManager.saveProfile(updated)
                                            userProfile = updated
                                        },
                                        onSyncNow = { syncHealthConnect() },
                                        lastSyncTimeStr = lastSyncTimeStr,
                                        isSyncing = isSyncing,
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
