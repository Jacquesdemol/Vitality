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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vital.health.data.UserProfile
import com.vital.health.data.UserProfileManager
import com.vital.health.healthconnect.HealthConnectLogger
import com.vital.health.healthconnect.HealthConnectRepository
import com.vital.health.healthconnect.LiveHealthData
import com.vital.health.model.*
import com.vital.health.notifications.VitalNotificationHelper
import com.vital.health.theme.*
import com.vital.health.ui.components.VitalBottomBar
import com.vital.health.ui.components.VitalTab
import com.vital.health.ui.screens.*
import com.vital.health.util.AppLifecycleTracker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class MainActivity : ComponentActivity() {

    private val repository: HealthConnectRepository by lazy {
        HealthConnectRepository(this, (application as VitalApplication).database)
    }

    private val profileManager: UserProfileManager by lazy {
        UserProfileManager(this)
    }

    private val themePrefs: ThemePreferencesManager by lazy {
        ThemePreferencesManager(this)
    }

    private var onPermissionsResultCallback: (() -> Unit)? = null
    private var syncJob: Job? = null
    private var activityWeekJob: Job? = null
    private var sleepWeekJob: Job? = null

    // Health Connect Permission Launcher
    private val requestHealthPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        HealthConnectLogger.logQuerySuccess("PERM_CALLBACK", "PermissionController", "MainActivity", 0, granted.size)
        onPermissionsResultCallback?.invoke()
    }

    // Android 13+ Notification Permission Launcher
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa canal de notificações e agendamento
        VitalNotificationHelper.createNotificationChannel(this)
        VitalNotificationHelper.schedulePeriodicHealthReminders(this)

        val prefs = getSharedPreferences("vital_health_prefs", Context.MODE_PRIVATE)
        val initialTutorialCompleted = prefs.getBoolean("has_completed_tutorial", false)

        setContent {
            var currentThemeMode by remember { mutableStateOf(themePrefs.getThemeMode()) }
            var currentThemePalette by remember { mutableStateOf(themePrefs.getThemePalette()) }

            VitalTheme(mode = currentThemeMode, palette = currentThemePalette) {
                var isTutorialVisible by remember { mutableStateOf(!initialTutorialCompleted) }
                var currentTab by remember { mutableStateOf(VitalTab.HOME) }

                var userProfile by remember { mutableStateOf(profileManager.getProfile()) }
                var liveHealthData by remember { mutableStateOf(LiveHealthData()) }
                var syncStatus by remember { mutableStateOf(HealthSyncStatus.CONNECTED) }
                var streakData by remember {
                    mutableStateOf(
                        StreakData(
                            currentStreakDays = 0,
                            bestStreakDays = 0,
                            activeDaysThisWeek = 0,
                            weeklyGoalDays = userProfile.goals.weeklyWorkoutDays,
                            streakMessage = "Sincronize o Health Connect para rastrear sua consistência.",
                            isGoalMet = false
                        )
                    )
                }

                // Weekly datasets
                var currentActivityWeekOffset by remember { mutableLongStateOf(0L) }
                var currentSleepWeekOffset by remember { mutableLongStateOf(0L) }

                var weeklyActivity by remember { mutableStateOf<List<DayActivitySummary>>(emptyList()) }
                var weeklySleep by remember { mutableStateOf<List<NightSleepSummary>>(emptyList()) }
                var weeklyDistance by remember { mutableStateOf<WeeklyDistanceSummary?>(null) }

                var isSyncing by remember { mutableStateOf(false) }
                var lastSyncTimeStr by remember { mutableStateOf("Nunca") }

                fun loadWeeklyActivityData(offset: Long) {
                    if (!AppLifecycleTracker.isForeground) return
                    activityWeekJob?.cancel(CancellationException("NewActivityWeekRequested"))
                    activityWeekJob = lifecycleScope.launch {
                        try {
                            val today = LocalDate.now().plusWeeks(offset)
                            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            val result = repository.queryWeeklyActivity(monday, "MainActivity")
                            weeklyActivity = result
                        } catch (ce: CancellationException) {
                            // Cancelado por mudança rápida ou ciclo de vida
                        } catch (e: Exception) {
                            HealthConnectLogger.logQueryError("ACT_WEEK", "DayActivitySummary", "MainActivity", 0, e)
                        }
                    }
                }

                fun loadWeeklySleepData(offset: Long) {
                    if (!AppLifecycleTracker.isForeground) return
                    sleepWeekJob?.cancel(CancellationException("NewSleepWeekRequested"))
                    sleepWeekJob = lifecycleScope.launch {
                        try {
                            val today = LocalDate.now().plusWeeks(offset)
                            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            val result = repository.queryWeeklySleep(monday, "MainActivity")
                            weeklySleep = result
                        } catch (ce: CancellationException) {
                            // Cancelado por mudança rápida ou ciclo de vida
                        } catch (e: Exception) {
                            HealthConnectLogger.logQueryError("SLEEP_WEEK", "NightSleepSummary", "MainActivity", 0, e)
                        }
                    }
                }

                fun syncAllHealthData(forceRefresh: Boolean = false, reason: String = "MANUAL_SYNC") {
                    if (!AppLifecycleTracker.isForeground) {
                        HealthConnectLogger.logQueryCancelled("SYNC_ALL", "AllMetrics", "MainActivity", 0, "AppInBackground")
                        return
                    }

                    syncJob?.cancel(CancellationException("NewSyncRequested"))
                    syncJob = lifecycleScope.launch {
                        isSyncing = true
                        val qId = HealthConnectLogger.nextQueryId("SYNC")
                        HealthConnectLogger.logQueryStart(qId, "AllMetrics", "MainActivity", reason, coroutineContext)

                        try {
                            syncStatus = repository.checkSyncStatus()
                            val data = repository.fetchAllLiveHealthData(forceRefresh = forceRefresh, component = "MainActivity")
                            liveHealthData = data

                            streakData = repository.calculateStreak(userProfile.goals.weeklyWorkoutDays, "MainActivity")

                            val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            weeklyActivity = repository.queryWeeklyActivity(monday.plusWeeks(currentActivityWeekOffset), "MainActivity")
                            weeklySleep = repository.queryWeeklySleep(monday.plusWeeks(currentSleepWeekOffset), "MainActivity")
                            weeklyDistance = repository.queryWeeklyDistance(monday, "MainActivity")

                            lastSyncTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                            HealthConnectLogger.logQuerySuccess(qId, "AllMetrics", "MainActivity", 0, 1, coroutineContext)
                        } catch (ce: CancellationException) {
                            HealthConnectLogger.logQueryCancelled(qId, "AllMetrics", "MainActivity", 0, "CancelledByLifecycle", coroutineContext)
                        } catch (se: SecurityException) {
                            HealthConnectLogger.logQueryError(qId, "AllMetrics", "MainActivity", 0, se, 1, coroutineContext)
                            syncStatus = HealthSyncStatus.PERMISSIONS_PENDING
                        } catch (e: Exception) {
                            HealthConnectLogger.logQueryError(qId, "AllMetrics", "MainActivity", 0, e, 1, coroutineContext)
                            syncStatus = HealthSyncStatus.ERROR
                        } finally {
                            isSyncing = false
                        }
                    }
                }

                // Ciclo de vida robusto: executa apenas enquanto o app estiver no estado STARTED (em primeiro plano)
                // Ao sair para a tela inicial do Android, repeatOnLifecycle cancela automaticamente as consultas em andamento!
                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            syncAllHealthData(forceRefresh = false, reason = "LIFECYCLE_STARTED")
                        }
                    }
                }

                // Navegação de semana na tela de atividades
                LaunchedEffect(currentActivityWeekOffset) {
                    if (currentActivityWeekOffset != 0L) {
                        loadWeeklyActivityData(currentActivityWeekOffset)
                    }
                }

                // Navegação de semana na tela de sono
                LaunchedEffect(currentSleepWeekOffset) {
                    if (currentSleepWeekOffset != 0L) {
                        loadWeeklySleepData(currentSleepWeekOffset)
                    }
                }

                onPermissionsResultCallback = {
                    syncAllHealthData(forceRefresh = true, reason = "PERMISSION_GRANTED_CALLBACK")
                }

                fun requestAllPermissions() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (repository.isAvailable()) {
                        requestHealthPermissions.launch(repository.requiredPermissions)
                    }
                }

                if (isTutorialVisible) {
                    TutorialScreen(
                        initialName = userProfile.name,
                        onRequestPermissions = { requestAllPermissions() },
                        onCompleteTutorial = { createdProfile ->
                            profileManager.saveProfile(createdProfile)
                            userProfile = createdProfile
                            prefs.edit().putBoolean("has_completed_tutorial", true).apply()
                            isTutorialVisible = false
                            requestAllPermissions()
                            syncAllHealthData(forceRefresh = true, reason = "TUTORIAL_COMPLETED")
                        },
                        onTestNotification = {
                            requestAllPermissions()
                            VitalNotificationHelper.sendSampleNotification(this@MainActivity, "motivation")
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = VitalTheme.colors.background,
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
                                        streakData = streakData,
                                        weeklyDistance = weeklyDistance,
                                        syncStatus = syncStatus,
                                        isSyncing = isSyncing,
                                        onSync = { syncAllHealthData(forceRefresh = true, reason = "USER_CLICKED_SYNC") },
                                        onNavigateToExercise = { currentTab = VitalTab.EXERCISE },
                                        onNavigateToSleep = { currentTab = VitalTab.SLEEP },
                                        onTriggerHealthTip = {
                                            VitalNotificationHelper.sendSampleNotification(
                                                this@MainActivity,
                                                "motivation"
                                            )
                                        }
                                    )
                                    VitalTab.SLEEP -> SleepScreen(
                                        liveData = liveHealthData,
                                        userProfile = userProfile,
                                        weeklySleep = weeklySleep,
                                        currentWeekOffset = currentSleepWeekOffset,
                                        onPreviousWeek = {
                                            currentSleepWeekOffset--
                                            HealthConnectLogger.logWeekNavigation("SleepScreen", currentSleepWeekOffset, "PREV", "OFFSET")
                                        },
                                        onNextWeek = {
                                            if (currentSleepWeekOffset < 0L) {
                                                currentSleepWeekOffset++
                                                HealthConnectLogger.logWeekNavigation("SleepScreen", currentSleepWeekOffset, "NEXT", "OFFSET")
                                            }
                                        }
                                    )
                                    VitalTab.EXERCISE -> ExerciseScreen(
                                        liveData = liveHealthData,
                                        userProfile = userProfile,
                                        weeklyActivity = weeklyActivity,
                                        streakData = streakData,
                                        currentWeekOffset = currentActivityWeekOffset,
                                        onPreviousWeek = {
                                            currentActivityWeekOffset--
                                            HealthConnectLogger.logWeekNavigation("ExerciseScreen", currentActivityWeekOffset, "PREV", "OFFSET")
                                        },
                                        onNextWeek = {
                                            if (currentActivityWeekOffset < 0L) {
                                                currentActivityWeekOffset++
                                                HealthConnectLogger.logWeekNavigation("ExerciseScreen", currentActivityWeekOffset, "NEXT", "OFFSET")
                                            }
                                        }
                                    )
                                    VitalTab.SETTINGS -> SettingsScreen(
                                        userProfile = userProfile,
                                        onUpdateProfile = { updated ->
                                            profileManager.saveProfile(updated)
                                            userProfile = updated
                                        },
                                        currentThemeMode = currentThemeMode,
                                        currentThemePalette = currentThemePalette,
                                        onThemeChanged = { newMode, newPalette ->
                                            currentThemeMode = newMode
                                            currentThemePalette = newPalette
                                            themePrefs.setThemeMode(newMode)
                                            themePrefs.setThemePalette(newPalette)
                                        },
                                        onSyncNow = { syncAllHealthData(forceRefresh = true, reason = "SETTINGS_CLICKED_SYNC") },
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
