package com.vital.health.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.vital.health.data.local.HealthMetricEntity
import com.vital.health.data.local.VitalDatabase
import com.vital.health.model.*
import com.vital.health.util.AppLifecycleTracker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * HealthConnectRepository - Camada de integração com o Android Health Connect SDK
 * Cumpre os requisitos estritos da auditoria e correção do ciclo de vida:
 * 1. Não executa consultas enquanto o aplicativo estiver em segundo plano (evita erro READ_HEALTH_DATA_IN_BACKGROUND).
 * 2. Em caso de SecurityException (permissão ausente ou negada), aborta imediatamente o loop sem retries infinitos.
 * 3. Otimiza o cálculo de ofensiva (calculateStreak), eliminando até centenas de agregações desnecessárias.
 * 4. Instrumentação completa com a tag VITAL_HC_DEBUG contendo ID único, horários, estado do app, registros e exceções.
 * 5. Reutiliza cache em memória e serializa consultas simultâneas via Mutex.
 */
class HealthConnectRepository(
    private val context: Context,
    private val database: VitalDatabase
) {

    val healthConnectClient: HealthConnectClient? by lazy {
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

    private val syncMutex = Mutex()

    @Volatile
    private var cachedLiveData: LiveHealthData? = null
    @Volatile
    private var lastLiveDataFetchTime: Long = 0L

    fun getSdkStatus(): HealthSyncStatus {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthSyncStatus.CONNECTED
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthSyncStatus.UPDATE_REQUIRED
            else -> HealthSyncStatus.UNAVAILABLE
        }
    }

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun getGrantedPermissions(): Set<String> {
        val client = healthConnectClient ?: return emptySet()
        val qId = HealthConnectLogger.nextQueryId("PERM")
        val coroutineCtx = currentCoroutineContext()
        val startNs = System.currentTimeMillis()

        HealthConnectLogger.logQueryStart(qId, "PermissionCheck", "Repository", "CHECK_PERMISSIONS", coroutineCtx)
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            val duration = System.currentTimeMillis() - startNs
            HealthConnectLogger.logQuerySuccess(qId, "PermissionCheck", "Repository", duration, granted.size, coroutineCtx)
            granted
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "PermissionCheck", "Repository", System.currentTimeMillis() - startNs, "CoroutineCancelled", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            HealthConnectLogger.logQueryError(qId, "PermissionCheck", "Repository", System.currentTimeMillis() - startNs, e, 1, coroutineCtx)
            emptySet()
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        val granted = getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    suspend fun checkSyncStatus(): HealthSyncStatus {
        if (!isAvailable()) return getSdkStatus()
        val granted = getGrantedPermissions()
        if (granted.isEmpty()) return HealthSyncStatus.PERMISSIONS_PENDING
        return HealthSyncStatus.CONNECTED
    }

    // ------------------- PASSOS REAIS -------------------

    suspend fun readTodaySteps(component: String = "HomeScreen"): Long {
        return readStepsForDate(LocalDate.now(), component, "READ_TODAY_STEPS")
    }

    suspend fun readStepsForDate(date: LocalDate, component: String = "Repository", reason: String = "DATE_QUERY"): Long {
        val client = healthConnectClient ?: return 0L
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "StepsRecord", component, 0, "AppInBackground")
            return 0L
        }

        val qId = HealthConnectLogger.nextQueryId("STEPS")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zone).toInstant()

        HealthConnectLogger.logQueryStart(qId, "StepsRecord", component, reason, coroutineCtx)

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val totalSteps = response[StepsRecord.COUNT_TOTAL] ?: 0L
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "StepsRecord", component, duration, if (totalSteps > 0) 1 else 0, coroutineCtx)

            if (date == LocalDate.now()) {
                database.healthDao().insertMetric(
                    HealthMetricEntity(
                        metricType = "steps",
                        value = totalSteps.toDouble(),
                        unit = "steps",
                        source = "Health Connect (StepsRecord)",
                        timestamp = Instant.now().toEpochMilli()
                    )
                )
            }
            totalSteps
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "StepsRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "StepsRecord", component, duration, e, 1, coroutineCtx)
            if (date == LocalDate.now()) {
                database.healthDao().getLatestMetric("steps")?.value?.toLong() ?: 0L
            } else 0L
        }
    }

    // ------------------- DISTÂNCIA REAL -------------------

    suspend fun readDailyDistanceKm(date: LocalDate, component: String = "Repository"): Double {
        val client = healthConnectClient ?: return 0.0
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "DistanceRecord", component, 0, "AppInBackground")
            return 0.0
        }

        val qId = HealthConnectLogger.nextQueryId("DIST")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zone).toInstant()

        HealthConnectLogger.logQueryStart(qId, "DistanceRecord", component, "READ_DAILY_DISTANCE", coroutineCtx)

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val distance = response[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "DistanceRecord", component, duration, if (distance > 0.0) 1 else 0, coroutineCtx)
            distance
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "DistanceRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "DistanceRecord", component, duration, e, 1, coroutineCtx)
            0.0
        }
    }

    suspend fun queryWeeklyDistance(weekStartDate: LocalDate, component: String = "HomeScreen"): WeeklyDistanceSummary {
        val daySummaries = mutableListOf<DayDistanceSummary>()
        var permissionErrorEncountered = false

        for (i in 0..6) {
            val date = weekStartDate.plusDays(i.toLong())
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                .replaceFirstChar { it.uppercase() }
                .take(3)

            val distance = if (!permissionErrorEncountered && AppLifecycleTracker.isForeground) {
                try {
                    readDailyDistanceKm(date, component)
                } catch (e: SecurityException) {
                    permissionErrorEncountered = true
                    0.0
                }
            } else 0.0

            daySummaries.add(
                DayDistanceSummary(
                    date = date,
                    dayOfWeekLabel = dayLabel,
                    distanceKm = distance,
                    source = if (distance > 0) "Health Connect" else "Sem registro"
                )
            )
        }

        val total = daySummaries.sumOf { it.distanceKm }
        val avg = total / 7.0

        return WeeklyDistanceSummary(
            startDate = weekStartDate,
            endDate = weekStartDate.plusDays(6),
            days = daySummaries,
            totalWeeklyKm = total,
            dailyAverageKm = avg
        )
    }

    // ------------------- CALORIAS ATIVAS REAIS -------------------

    suspend fun readTodayActiveCalories(component: String = "HomeScreen"): Double {
        return readActiveCaloriesForDate(LocalDate.now(), component)
    }

    suspend fun readActiveCaloriesForDate(date: LocalDate, component: String = "Repository"): Double {
        val client = healthConnectClient ?: return 0.0
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "ActiveCaloriesBurnedRecord", component, 0, "AppInBackground")
            return 0.0
        }

        val qId = HealthConnectLogger.nextQueryId("CAL")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zone).toInstant()

        HealthConnectLogger.logQueryStart(qId, "ActiveCaloriesBurnedRecord", component, "READ_ACTIVE_CALORIES", coroutineCtx)

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val calories = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "ActiveCaloriesBurnedRecord", component, duration, if (calories > 0.0) 1 else 0, coroutineCtx)

            if (date == LocalDate.now() && calories > 0) {
                database.healthDao().insertMetric(
                    HealthMetricEntity(
                        metricType = "active_calories",
                        value = calories,
                        unit = "kcal",
                        source = "Health Connect",
                        timestamp = Instant.now().toEpochMilli()
                    )
                )
            }
            calories
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "ActiveCaloriesBurnedRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "ActiveCaloriesBurnedRecord", component, duration, e, 1, coroutineCtx)
            0.0
        }
    }

    // ------------------- FREQUÊNCIA CARDÍACA ATUAL VS REPOUSO -------------------

    suspend fun readLatestHeartRate(component: String = "HomeScreen"): Int? {
        val client = healthConnectClient ?: return null
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "HeartRateRecord", component, 0, "AppInBackground")
            return null
        }

        val qId = HealthConnectLogger.nextQueryId("HR")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val startTime = Instant.now().minus(24, ChronoUnit.HOURS)

        HealthConnectLogger.logQueryStart(qId, "HeartRateRecord", component, "READ_LATEST_HR", coroutineCtx)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            )
            val latestSample = response.records.lastOrNull()?.samples?.lastOrNull()
            val bpm = latestSample?.beatsPerMinute?.toInt()
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "HeartRateRecord", component, duration, response.records.size, coroutineCtx)
            bpm
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "HeartRateRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "HeartRateRecord", component, duration, e, 1, coroutineCtx)
            database.healthDao().getLatestMetric("heart_rate")?.value?.toInt()
        }
    }

    suspend fun readLatestRestingHeartRate(component: String = "HomeScreen"): Int? {
        val client = healthConnectClient ?: return null
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "RestingHeartRateRecord", component, 0, "AppInBackground")
            return null
        }

        val qId = HealthConnectLogger.nextQueryId("RHR")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val startTime = Instant.now().minus(48, ChronoUnit.HOURS)

        HealthConnectLogger.logQueryStart(qId, "RestingHeartRateRecord", component, "READ_RESTING_HR", coroutineCtx)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            )
            val record = response.records.lastOrNull()
            val bpm = record?.beatsPerMinute?.toInt()
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "RestingHeartRateRecord", component, duration, response.records.size, coroutineCtx)
            bpm
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "RestingHeartRateRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "RestingHeartRateRecord", component, duration, e, 1, coroutineCtx)
            null
        }
    }

    // ------------------- SONO REAL & ESTÁGIOS REAIS -------------------

    suspend fun readSleepSessionForNight(nightDate: LocalDate, component: String = "SleepScreen"): SleepSessionData? {
        val client = healthConnectClient ?: return null
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "SleepSessionRecord", component, 0, "AppInBackground")
            return null
        }

        val qId = HealthConnectLogger.nextQueryId("SLEEP")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val searchStart = nightDate.minusDays(1).atTime(18, 0).atZone(zone).toInstant()
        val searchEnd = nightDate.atTime(16, 0).atZone(zone).toInstant()

        HealthConnectLogger.logQueryStart(qId, "SleepSessionRecord", component, "READ_NIGHT_SLEEP for $nightDate", coroutineCtx)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(searchStart, searchEnd)
                )
            )

            val record = response.records.maxByOrNull {
                Duration.between(it.startTime, it.endTime).toMinutes()
            }

            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "SleepSessionRecord", component, duration, response.records.size, coroutineCtx)

            record?.let { mapSleepSessionRecord(it) }
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "SleepSessionRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "SleepSessionRecord", component, duration, e, 1, coroutineCtx)
            null
        }
    }

    suspend fun queryWeeklySleep(weekStartDate: LocalDate, component: String = "SleepScreen"): List<NightSleepSummary> {
        val result = mutableListOf<NightSleepSummary>()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()

        val sessions = mutableListOf<SleepSessionData?>()
        var permissionDenied = false

        for (i in 0..6) {
            val date = weekStartDate.plusDays(i.toLong())
            val session = if (!permissionDenied && AppLifecycleTracker.isForeground) {
                try {
                    readSleepSessionForNight(date, component)
                } catch (e: SecurityException) {
                    permissionDenied = true
                    null
                }
            } else null
            sessions.add(session)
        }

        val validSessions = sessions.filterNotNull()
        val avgDuration = if (validSessions.isNotEmpty()) {
            validSessions.map { it.totalDurationMinutes }.average().toLong()
        } else 0L

        for (i in 0..6) {
            val date = weekStartDate.plusDays(i.toLong())
            val session = sessions[i]

            if (session != null && session.totalDurationMinutes > 0L) {
                val duration = session.totalDurationMinutes
                val hours = duration / 60
                val mins = duration % 60
                val bedtimeStr = session.startTime.atZone(zone).format(formatter)
                val wakeStr = session.endTime.atZone(zone).format(formatter)
                val diffAvg = if (avgDuration > 0) duration - avgDuration else 0L

                val assessment = when {
                    duration >= 420 -> "Descanso adequado"
                    duration in 360..419 -> "Descanso moderado"
                    else -> "Duração abaixo do ideal"
                }

                result.add(
                    NightSleepSummary(
                        date = date,
                        session = session,
                        totalMinutes = duration,
                        formattedDuration = "${hours}h ${mins}m",
                        formattedBedtime = bedtimeStr,
                        formattedWakeTime = wakeStr,
                        qualityAssessment = assessment,
                        comparisonWithWeeklyAvgMinutes = diffAvg
                    )
                )
            } else {
                result.add(
                    NightSleepSummary(
                        date = date,
                        session = null,
                        totalMinutes = 0L,
                        formattedDuration = "--",
                        formattedBedtime = "--:--",
                        formattedWakeTime = "--:--",
                        qualityAssessment = "Sem registro de sono",
                        comparisonWithWeeklyAvgMinutes = 0L
                    )
                )
            }
        }

        return result
    }

    private fun mapSleepSessionRecord(record: SleepSessionRecord): SleepSessionData {
        val totalMins = Duration.between(record.startTime, record.endTime).toMinutes().coerceAtLeast(0L)
        val stageIntervals = mutableListOf<SleepStageInterval>()

        var deepMins = 0L
        var lightMins = 0L
        var remMins = 0L
        var awakeMins = 0L

        if (record.stages.isNotEmpty()) {
            record.stages.forEach { stage ->
                val stageDuration = Duration.between(stage.startTime, stage.endTime).toMinutes().coerceAtLeast(0L)
                val stageType = when (stage.stage) {
                    SleepSessionRecord.STAGE_TYPE_DEEP -> {
                        deepMins += stageDuration
                        SleepStageType.DEEP
                    }
                    SleepSessionRecord.STAGE_TYPE_LIGHT, SleepSessionRecord.STAGE_TYPE_SLEEPING -> {
                        lightMins += stageDuration
                        SleepStageType.LIGHT
                    }
                    SleepSessionRecord.STAGE_TYPE_REM -> {
                        remMins += stageDuration
                        SleepStageType.REM
                    }
                    SleepSessionRecord.STAGE_TYPE_AWAKE -> {
                        awakeMins += stageDuration
                        SleepStageType.AWAKE
                    }
                    SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> {
                        awakeMins += stageDuration
                        SleepStageType.OUT_OF_BED
                    }
                    else -> SleepStageType.UNKNOWN
                }

                stageIntervals.add(
                    SleepStageInterval(
                        startTime = stage.startTime,
                        endTime = stage.endTime,
                        stageType = stageType,
                        durationMinutes = stageDuration
                    )
                )
            }
        }

        val hasStages = stageIntervals.isNotEmpty()
        val timeAsleep = if (hasStages) {
            (deepMins + lightMins + remMins).coerceAtLeast(0L)
        } else {
            totalMins
        }

        return SleepSessionData(
            id = record.metadata.id,
            startTime = record.startTime,
            endTime = record.endTime,
            totalDurationMinutes = totalMins,
            timeAsleepMinutes = timeAsleep,
            timeInBedMinutes = totalMins,
            stages = stageIntervals,
            deepMinutes = deepMins,
            lightMinutes = lightMins,
            remMinutes = remMins,
            awakeMinutes = awakeMins,
            hasStages = hasStages,
            sourceApp = record.metadata.dataOrigin.packageName
        )
    }

    // ------------------- EXERCÍCIOS REAIS & SESSÕES -------------------

    /**
     * Consulta leve para cálculo de ofensiva/streak: lê EXCLUSIVAMENTE os registros
     * de ExerciseSessionRecord em 1 única chamada, sem disparar centenas de agregações.
     */
    suspend fun queryExerciseSessionTimes(startTime: Instant, endTime: Instant, component: String = "StreakEngine"): List<Instant> {
        val client = healthConnectClient ?: return emptyList()
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "ExerciseSessionTimes", component, 0, "AppInBackground")
            return emptyList()
        }

        val qId = HealthConnectLogger.nextQueryId("EX_TIMES")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val startTimes = mutableListOf<Instant>()
        var pageToken: String? = null

        HealthConnectLogger.logQueryStart(qId, "ExerciseSessionRecord(Times)", component, "STREAK_QUERY", coroutineCtx)

        do {
            try {
                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = ExerciseSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        pageSize = 500,
                        pageToken = pageToken
                    )
                )
                response.records.forEach { startTimes.add(it.startTime) }
                pageToken = response.pageToken
            } catch (ce: CancellationException) {
                HealthConnectLogger.logQueryCancelled(qId, "ExerciseSessionRecord(Times)", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
                throw ce
            } catch (e: Exception) {
                HealthConnectLogger.logQueryError(qId, "ExerciseSessionRecord(Times)", component, System.currentTimeMillis() - startMs, e, 1, coroutineCtx)
                break
            }
        } while (pageToken != null)

        val duration = System.currentTimeMillis() - startMs
        HealthConnectLogger.logQuerySuccess(qId, "ExerciseSessionRecord(Times)", component, duration, startTimes.size, coroutineCtx)
        return startTimes
    }

    /**
     * Consulta detalhada de exercícios para uma janela limitada (ex: 1 semana).
     * Se uma SecurityException ocorrer (ex: app em background ou permissão negada),
     * interrompe o laço IMEDIATAMENTE, evitando a repetição rápida de centenas de chamadas.
     */
    suspend fun queryExerciseSessions(
        startTime: Instant,
        endTime: Instant,
        component: String = "ExerciseScreen",
        reason: String = "LOAD_EXERCISES"
    ): List<ExerciseSessionData> {
        val client = healthConnectClient ?: return emptyList()
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "ExerciseSessionRecord", component, 0, "AppInBackground")
            return emptyList()
        }

        val qId = HealthConnectLogger.nextQueryId("EX_FULL")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val allRecords = mutableListOf<ExerciseSessionRecord>()
        var pageToken: String? = null

        HealthConnectLogger.logQueryStart(qId, "ExerciseSessionRecord", component, reason, coroutineCtx)

        do {
            try {
                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = ExerciseSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        pageSize = 200,
                        pageToken = pageToken
                    )
                )
                allRecords.addAll(response.records)
                pageToken = response.pageToken
            } catch (ce: CancellationException) {
                HealthConnectLogger.logQueryCancelled(qId, "ExerciseSessionRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
                throw ce
            } catch (e: Exception) {
                HealthConnectLogger.logQueryError(qId, "ExerciseSessionRecord", component, System.currentTimeMillis() - startMs, e, 1, coroutineCtx)
                return emptyList()
            }
        } while (pageToken != null)

        val resultList = mutableListOf<ExerciseSessionData>()
        var securityErrorEncountered = false

        for (session in allRecords) {
            val durationMins = Duration.between(session.startTime, session.endTime).toMinutes().coerceAtLeast(1L)
            var calories: Double? = null
            var distanceKm: Double? = null
            var avgHr: Int? = null
            var maxHr: Int? = null

            // Só consulta métricas adicionais se o app continuar no foreground e não tiver encontrado SecurityException
            if (!securityErrorEncountered && AppLifecycleTracker.isForeground) {
                try {
                    val cResponse = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(session.startTime, session.endTime)
                        )
                    )
                    calories = cResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories
                } catch (se: SecurityException) {
                    securityErrorEncountered = true
                    HealthConnectLogger.logQueryError(qId, "ActiveCalories(Session)", component, 0, se, 1, coroutineCtx)
                } catch (_: Exception) {}

                if (!securityErrorEncountered) {
                    try {
                        val dResponse = client.aggregate(
                            AggregateRequest(
                                metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                                timeRangeFilter = TimeRangeFilter.between(session.startTime, session.endTime)
                            )
                        )
                        distanceKm = dResponse[DistanceRecord.DISTANCE_TOTAL]?.inKilometers
                    } catch (se: SecurityException) {
                        securityErrorEncountered = true
                        HealthConnectLogger.logQueryError(qId, "Distance(Session)", component, 0, se, 1, coroutineCtx)
                    } catch (_: Exception) {}
                }

                if (!securityErrorEncountered) {
                    try {
                        val hrResponse = client.readRecords(
                            ReadRecordsRequest(
                                recordType = HeartRateRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(session.startTime, session.endTime)
                            )
                        )
                        val samples = hrResponse.records.flatMap { it.samples }
                        if (samples.isNotEmpty()) {
                            avgHr = samples.map { it.beatsPerMinute }.average().toInt()
                            maxHr = samples.maxOf { it.beatsPerMinute }.toInt()
                        }
                    } catch (se: SecurityException) {
                        securityErrorEncountered = true
                        HealthConnectLogger.logQueryError(qId, "HeartRate(Session)", component, 0, se, 1, coroutineCtx)
                    } catch (_: Exception) {}
                }
            }

            val zone = when {
                avgHr == null -> null
                avgHr >= 155 -> "Intensa (Zona 4/5)"
                avgHr >= 130 -> "Moderada (Zona 3)"
                else -> "Leve (Zona 1/2)"
            }

            resultList.add(
                ExerciseSessionData(
                    id = session.metadata.id,
                    title = session.title ?: mapExerciseTypeName(session.exerciseType),
                    exerciseType = session.exerciseType,
                    typeName = mapExerciseTypeName(session.exerciseType),
                    startTime = session.startTime,
                    endTime = session.endTime,
                    durationMinutes = durationMins,
                    caloriesKcal = calories,
                    distanceKm = distanceKm,
                    avgHeartRateBpm = avgHr,
                    maxHeartRateBpm = maxHr,
                    intensityZone = zone,
                    sourceApp = session.metadata.dataOrigin.packageName
                )
            )
        }

        val totalDuration = System.currentTimeMillis() - startMs
        HealthConnectLogger.logQuerySuccess(qId, "ExerciseSessionRecord(Total)", component, totalDuration, resultList.size, coroutineCtx)
        return resultList
    }

    suspend fun queryWeeklyActivity(weekStartDate: LocalDate, component: String = "ExerciseScreen"): List<DayActivitySummary> {
        val zone = ZoneId.systemDefault()
        val startInstant = weekStartDate.atStartOfDay(zone).toInstant()
        val endInstant = weekStartDate.plusDays(7).atStartOfDay(zone).toInstant()

        val allSessions = queryExerciseSessions(startInstant, endInstant, component, "WEEKLY_ACTIVITY_QUERY")
        val daySummaries = mutableListOf<DayActivitySummary>()

        for (i in 0..6) {
            val date = weekStartDate.plusDays(i.toLong())
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                .replaceFirstChar { it.uppercase() }
                .take(3)

            val daySessions = allSessions.filter {
                val sDate = it.startTime.atZone(zone).toLocalDate()
                sDate == date
            }

            val totalMins = daySessions.sumOf { it.durationMinutes }
            val totalCalories = daySessions.mapNotNull { it.caloriesKcal }.sum()
            val totalDist = daySessions.mapNotNull { it.distanceKm }.sum()
            val hasActivity = daySessions.isNotEmpty()

            val intensityScore = when {
                totalMins >= 60 -> 1.0f
                totalMins >= 45 -> 0.8f
                totalMins >= 30 -> 0.6f
                totalMins > 0 -> 0.4f
                else -> 0.0f
            }

            daySummaries.add(
                DayActivitySummary(
                    date = date,
                    dayOfWeekLabel = dayLabel,
                    totalActiveMinutes = totalMins,
                    totalCaloriesKcal = totalCalories,
                    totalDistanceKm = totalDist,
                    exerciseSessions = daySessions,
                    hasActivity = hasActivity,
                    intensityScore = intensityScore
                )
            )
        }

        return daySummaries
    }

    // ------------------- OFENSIVA DE DIAS CONSECUTIVOS (ESTILO DUOLINGO) -------------------

    suspend fun calculateStreak(weeklyGoalDays: Int, component: String = "HomeScreen"): StreakData {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30).atStartOfDay(zone).toInstant()
        val nowInstant = Instant.now()

        // Otimização crucial: usa queryExerciseSessionTimes em vez de queryExerciseSessions com agregados!
        val sessionStartTimes = queryExerciseSessionTimes(thirtyDaysAgo, nowInstant, component)
        val workoutDates = sessionStartTimes.map { it.atZone(zone).toLocalDate() }.toSet()

        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        var activeDaysThisWeek = 0
        for (i in 0..6) {
            val d = startOfWeek.plusDays(i.toLong())
            if (workoutDates.contains(d)) activeDaysThisWeek++
        }

        var currentStreak = 0
        var checkDate = if (workoutDates.contains(today)) today else today.minusDays(1)

        while (workoutDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        var maxStreak = 0
        var tempStreak = 0
        for (i in 30 downTo 0) {
            val d = today.minusDays(i.toLong())
            if (workoutDates.contains(d)) {
                tempStreak++
                if (tempStreak > maxStreak) maxStreak = tempStreak
            } else {
                tempStreak = 0
            }
        }
        if (currentStreak > maxStreak) maxStreak = currentStreak

        val message = when {
            currentStreak >= 5 -> "Você treinou $currentStreak dias consecutivos. Imparável!"
            currentStreak >= 3 -> "$currentStreak dias consecutivos de atividade. Fighting!"
            activeDaysThisWeek >= weeklyGoalDays -> "Você completou $activeDaysThisWeek dias nesta semana. Meta superada!"
            activeDaysThisWeek > 0 -> "Sua consistência está aumentando. Continue assim!"
            else -> "Inicie sua sequência com uma atividade hoje."
        }

        return StreakData(
            currentStreakDays = currentStreak,
            bestStreakDays = maxStreak,
            activeDaysThisWeek = activeDaysThisWeek,
            weeklyGoalDays = weeklyGoalDays,
            streakMessage = message,
            isGoalMet = activeDaysThisWeek >= weeklyGoalDays
        )
    }

    // ------------------- COMPOSIÇÃO CORPORAL -------------------

    suspend fun readLatestWeightAndBodyFat(component: String = "HomeScreen"): Pair<Double?, Double?> {
        val client = healthConnectClient ?: return Pair(null, null)
        if (!AppLifecycleTracker.isForeground) return Pair(null, null)

        val qId = HealthConnectLogger.nextQueryId("BODY")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val startTime = Instant.now().minus(30, ChronoUnit.DAYS)

        HealthConnectLogger.logQueryStart(qId, "Weight/BodyFatRecord", component, "READ_BODY_COMPOSITION", coroutineCtx)

        val weight = try {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            ).records.lastOrNull()?.weight?.inKilograms
        } catch (e: Exception) {
            HealthConnectLogger.logQueryError(qId, "WeightRecord", component, 0, e, 1, coroutineCtx)
            null
        }

        val fat = try {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            ).records.lastOrNull()?.percentage?.value
        } catch (e: Exception) {
            HealthConnectLogger.logQueryError(qId, "BodyFatRecord", component, 0, e, 1, coroutineCtx)
            null
        }

        val duration = System.currentTimeMillis() - startMs
        HealthConnectLogger.logQuerySuccess(qId, "Weight/BodyFatRecord", component, duration, if (weight != null || fat != null) 1 else 0, coroutineCtx)
        return Pair(weight, fat)
    }

    // ------------------- SINCRONIZAÇÃO COMPLETA COM CACHE E MUTEX -------------------

    suspend fun fetchAllLiveHealthData(forceRefresh: Boolean = false, component: String = "MainActivity"): LiveHealthData {
        val now = System.currentTimeMillis()
        val cached = cachedLiveData

        // Retorna cache em memória se a consulta anterior ocorreu há menos de 3 minutos
        if (!forceRefresh && cached != null && (now - lastLiveDataFetchTime) < 180_000L) {
            HealthConnectLogger.logQuerySuccess("CACHE", "LiveHealthData", component, 0, 1)
            return cached
        }

        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "LiveHealthData", component, 0, "AppInBackground")
            return cached ?: LiveHealthData()
        }

        return syncMutex.withLock {
            // Re-verifica cache sob o lock
            if (!forceRefresh && cachedLiveData != null && (System.currentTimeMillis() - lastLiveDataFetchTime) < 180_000L) {
                return@withLock cachedLiveData!!
            }

            val steps = readTodaySteps(component)
            val activeCalories = readTodayActiveCalories(component).toInt()
            val latestHeartRate = readLatestHeartRate(component)
            val restingHeartRate = readLatestRestingHeartRate(component)
            val today = LocalDate.now()
            val sleepSession = readSleepSessionForNight(today, component)
            val zone = ZoneId.systemDefault()
            val weekStart = today.minusDays(7).atStartOfDay(zone).toInstant()
            val exercises = queryExerciseSessions(weekStart, Instant.now(), component, "LIVE_FETCH_RECENT_EXERCISES")
            val (weight, bodyFat) = readLatestWeightAndBodyFat(component)
            val distanceKm = readDailyDistanceKm(today, component)

            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val sleepDuration = sleepSession?.totalDurationMinutes ?: 0L
            val sleepStartStr = sleepSession?.let { it.startTime.atZone(zone).format(formatter) } ?: "--:--"
            val sleepEndStr = sleepSession?.let { it.endTime.atZone(zone).format(formatter) } ?: "--:--"

            val hasRealData = steps > 0 || latestHeartRate != null || restingHeartRate != null || sleepDuration > 0 || exercises.isNotEmpty() || distanceKm > 0.0

            val result = LiveHealthData(
                steps = steps,
                activeCaloriesKcal = activeCalories,
                heartRateBpm = latestHeartRate,
                restingHeartRateBpm = restingHeartRate,
                sleepDurationMinutes = sleepDuration,
                sleepStartTime = sleepStartStr,
                sleepEndTime = sleepEndStr,
                sleepStageDeep = sleepSession?.deepMinutes ?: 0L,
                sleepStageLight = sleepSession?.lightMinutes ?: 0L,
                sleepStageRem = sleepSession?.remMinutes ?: 0L,
                sleepStageAwake = sleepSession?.awakeMinutes ?: 0L,
                weeklyExerciseDays = exercises.map { it.startTime.atZone(zone).toLocalDate() }.toSet().size,
                weeklyExerciseMinutes = exercises.sumOf { it.durationMinutes },
                weightKg = weight,
                bodyFatPct = bodyFat,
                distanceKm = distanceKm,
                lastSyncTimestamp = System.currentTimeMillis(),
                isRealHealthConnectData = hasRealData,
                sourcePackage = if (hasRealData) "Health Connect Live" else "Aguardando Leitura"
            )

            cachedLiveData = result
            lastLiveDataFetchTime = System.currentTimeMillis()
            result
        }
    }

    private fun mapExerciseTypeName(type: Int): String {
        return when (type) {
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Corrida"
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Caminhada"
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Ciclismo"
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "Natação"
            ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Musculação"
            ExerciseSessionRecord.EXERCISE_TYPE_PILATES -> "Pilates"
            ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
            ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
            ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> "Calistenia"
            ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE -> "Remo"
            else -> "Exercício Físico"
        }
    }
}

data class LiveHealthData(
    val steps: Long = 0L,
    val activeCaloriesKcal: Int = 0,
    val heartRateBpm: Int? = null,
    val restingHeartRateBpm: Int? = null,
    val sleepDurationMinutes: Long = 0L,
    val sleepStartTime: String = "--:--",
    val sleepEndTime: String = "--:--",
    val sleepStageDeep: Long = 0L,
    val sleepStageLight: Long = 0L,
    val sleepStageRem: Long = 0L,
    val sleepStageAwake: Long = 0L,
    val weeklyExerciseDays: Int = 0,
    val weeklyExerciseMinutes: Long = 0L,
    val weightKg: Double? = null,
    val bodyFatPct: Double? = null,
    val distanceKm: Double = 0.0,
    val lastSyncTimestamp: Long = 0L,
    val isRealHealthConnectData: Boolean = false,
    val sourcePackage: String = "Health Connect"
)
