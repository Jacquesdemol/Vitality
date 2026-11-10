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
import com.vital.health.model.*
import com.vital.health.notifications.VitalNotificationHelper
import com.vital.health.theme.*
import com.vital.health.ui.components.VitalBottomBar
import com.vital.health.ui.components.VitalTab
import com.vital.health.ui.screens.*
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

    // Health Connect Permission Launcher
    private val requestHealthPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        onPermissionsResultCallback?.invoke()
    }

    // Android 13+ Notification Permission Launcher
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa canal de notificações e agendamento com citações reais
        VitalNotificationHelper.createNotificationChannel(this)
        VitalNotificationHelper.schedulePeriodicHealthReminders(this)

        val prefs = getSharedPreferences("vital_health_prefs", Context.MODE_PRIVATE)
        val initialTutorialCompleted = prefs.getBoolean("has_completed_tutorial", false)

        setContent {
            var currentThemeMode by remember { mutableStateOf(themePrefs.getThemeMode()) }
            var currentThemePalette by remember { mutableStateOf(themePrefs.getThemePalette()) }

            VitalTheme(mode = currentThemeMode, palette = currentThemePalette) {
                val coroutineScope = rememberCoroutineScope()

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
                    coroutineScope.launch {
                        try {
                            val today = LocalDate.now().plusWeeks(offset)
                            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            weeklyActivity = repository.queryWeeklyActivity(monday)
                        } catch (_: Exception) {}
                    }
                }

                fun loadWeeklySleepData(offset: Long) {
                    coroutineScope.launch {
                        try {
                            val today = LocalDate.now().plusWeeks(offset)
                            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            weeklySleep = repository.queryWeeklySleep(monday)
                        } catch (_: Exception) {}
                    }
                }

                fun syncAllHealthData() {
                    coroutineScope.launch {
                        isSyncing = true
                        try {
                            syncStatus = repository.checkSyncStatus()
                            val data = repository.fetchAllLiveHealthData()
                            liveHealthData = data

                            streakData = repository.calculateStreak(userProfile.goals.weeklyWorkoutDays)

                            val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            weeklyActivity = repository.queryWeeklyActivity(monday.plusWeeks(currentActivityWeekOffset))
                            weeklySleep = repository.queryWeeklySleep(monday.plusWeeks(currentSleepWeekOffset))
                            weeklyDistance = repository.queryWeeklyDistance(monday)

                            lastSyncTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (_: Exception) {
                            syncStatus = HealthSyncStatus.ERROR
                        } finally {
                            isSyncing = false
                        }
                    }
                }

                // Sincroniza dados do Health Connect ao iniciar
                LaunchedEffect(Unit) {
                    syncAllHealthData()
                }

                LaunchedEffect(currentActivityWeekOffset) {
                    loadWeeklyActivityData(currentActivityWeekOffset)
                }

                LaunchedEffect(currentSleepWeekOffset) {
                    loadWeeklySleepData(currentSleepWeekOffset)
                }

                onPermissionsResultCallback = {
                    syncAllHealthData()
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
                        onRequestPermissions = { requestAllPermissions() },
                        onCompleteTutorial = { createdProfile ->
                            profileManager.saveProfile(createdProfile)
                            userProfile = createdProfile
                            prefs.edit().putBoolean("has_completed_tutorial", true).apply()
                            isTutorialVisible = false
                            requestAllPermissions()
                            syncAllHealthData()
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
                                        onSync = { syncAllHealthData() },
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
                                        onPreviousWeek = { currentSleepWeekOffset-- },
                                        onNextWeek = {
                                            if (currentSleepWeekOffset < 0L) currentSleepWeekOffset++
                                        }
                                    )
                                    VitalTab.EXERCISE -> ExerciseScreen(
                                        liveData = liveHealthData,
                                        userProfile = userProfile,
                                        weeklyActivity = weeklyActivity,
                                        streakData = streakData,
                                        currentWeekOffset = currentActivityWeekOffset,
                                        onPreviousWeek = { currentActivityWeekOffset-- },
                                        onNextWeek = {
                                            if (currentActivityWeekOffset < 0L) currentActivityWeekOffset++
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
                                        onSyncNow = { syncAllHealthData() },
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
