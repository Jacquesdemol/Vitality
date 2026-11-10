package com.vital.health.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.vital.health.data.UserProfile
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
 * Cumpre os requisitos estritos:
 * 1. Priorização de origens: Zepp/Huami ("com.huami.watch.hmwatchmanager") prioritário;
 *    Google Fit ("com.google.android.apps.fitness") secundário como alternativa;
 *    sem somar registros duplicados entre as fontes.
 * 2. Ciclo de vida estrito: não executa consultas em background; aborta rápido em SecurityException.
 * 3. Leitura e cálculo de Gasto Basal (BMR) via BasalMetabolicRateRecord ou fórmula Mifflin-St Jeor.
 * 4. Cálculo de Gasto Energético Total (Basal + Ativas) sem contagem duplicada.
 * 5. Navegação e consultas por data exata (LocalDate) com fusos horários locais.
 * 6. Logs de diagnóstico VITAL_HC_DEBUG registrando origens, contagens e conflitos.
 */
class HealthConnectRepository(
    private val context: Context,
    private val database: VitalDatabase
) {

    companion object {
        const val ORIGIN_HUAMI = "com.huami.watch.hmwatchmanager"
        const val ORIGIN_GOOGLE_FIT = "com.google.android.apps.fitness"

        fun formatOriginDisplayName(packageName: String): String {
            return when {
                packageName.contains("huami", ignoreCase = true) || packageName.contains("hmwatchmanager", ignoreCase = true) -> "Zepp (Huami)"
                packageName.contains("fitness", ignoreCase = true) || packageName.contains("google", ignoreCase = true) -> "Google Fit"
                packageName.contains("samsung", ignoreCase = true) -> "Samsung Health"
                packageName.isNotBlank() -> packageName.substringAfterLast('.')
                else -> "Health Connect"
            }
        }
    }

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
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
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

    // ------------------- PASSOS REAIS COM PRIORIDADE HUAMI > GOOGLE FIT -------------------

    suspend fun readTodaySteps(component: String = "HomeScreen"): Pair<Long, String> {
        return readStepsForDateWithOrigin(LocalDate.now(), component, "READ_TODAY_STEPS")
    }

    suspend fun readStepsForDateWithOrigin(
        date: LocalDate,
        component: String = "Repository",
        reason: String = "DATE_QUERY"
    ): Pair<Long, String> {
        val client = healthConnectClient ?: return Pair(0L, "Health Connect")
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "StepsRecord", component, 0, "AppInBackground")
            return Pair(0L, "Health Connect")
        }

        val qId = HealthConnectLogger.nextQueryId("STEPS")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zone).toInstant()
        val intervalStr = "$startTime a $endTime"

        HealthConnectLogger.logQueryStart(qId, "StepsRecord", component, reason, coroutineCtx)

        return try {
            val huamiResponse = try {
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        dataOriginFilter = setOf(DataOrigin(ORIGIN_HUAMI))
                    )
                )
            } catch (_: Exception) { null }

            val huamiSteps = huamiResponse?.get(StepsRecord.COUNT_TOTAL) ?: 0L

            if (huamiSteps > 0L) {
                HealthConnectLogger.logOriginResolution(
                    metric = "Passos",
                    selectedDate = date,
                    intervalExact = intervalStr,
                    huamiCount = 1,
                    googleFitCount = 0,
                    chosenOrigin = "Zepp (Huami)",
                    isFromCache = false,
                    diagnosticNote = "Huami prioritário utilizado ($huamiSteps passos)"
                )
                val duration = System.currentTimeMillis() - startMs
                HealthConnectLogger.logQuerySuccess(qId, "StepsRecord", component, duration, 1, coroutineCtx)

                if (date == LocalDate.now()) {
                    database.healthDao().insertMetric(
                        HealthMetricEntity(
                            metricType = "steps",
                            value = huamiSteps.toDouble(),
                            unit = "steps",
                            source = "Zepp (Huami)",
                            timestamp = Instant.now().toEpochMilli()
                        )
                    )
                }
                return Pair(huamiSteps, "Zepp (Huami)")
            }

            val fitResponse = try {
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        dataOriginFilter = setOf(DataOrigin(ORIGIN_GOOGLE_FIT))
                    )
                )
            } catch (_: Exception) { null }

            val fitSteps = fitResponse?.get(StepsRecord.COUNT_TOTAL) ?: 0L

            if (fitSteps > 0L) {
                HealthConnectLogger.logOriginResolution(
                    metric = "Passos",
                    selectedDate = date,
                    intervalExact = intervalStr,
                    huamiCount = 0,
                    googleFitCount = 1,
                    chosenOrigin = "Google Fit",
                    isFromCache = false,
                    diagnosticNote = "Fallback para Google Fit ($fitSteps passos)"
                )
                val duration = System.currentTimeMillis() - startMs
                HealthConnectLogger.logQuerySuccess(qId, "StepsRecord", component, duration, 1, coroutineCtx)
                return Pair(fitSteps, "Google Fit")
            }

            val generalResponse = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val generalSteps = generalResponse[StepsRecord.COUNT_TOTAL] ?: 0L
            val originLabel = if (generalSteps > 0L) "Health Connect" else "Sem registro"

            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "StepsRecord", component, duration, if (generalSteps > 0L) 1 else 0, coroutineCtx)
            Pair(generalSteps, originLabel)
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "StepsRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "StepsRecord", component, duration, e, 1, coroutineCtx)
            Pair(0L, "Health Connect")
        }
    }

    // ------------------- DISTÂNCIA REAL COM PRIORIDADE HUAMI > GOOGLE FIT -------------------

    suspend fun readDailyDistanceKmWithOrigin(date: LocalDate, component: String = "Repository"): Pair<Double, String> {
        val client = healthConnectClient ?: return Pair(0.0, "Health Connect")
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "DistanceRecord", component, 0, "AppInBackground")
            return Pair(0.0, "Health Connect")
        }

        val qId = HealthConnectLogger.nextQueryId("DIST")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zone).toInstant()
        val intervalStr = "$startTime a $endTime"

        HealthConnectLogger.logQueryStart(qId, "DistanceRecord", component, "READ_DAILY_DISTANCE", coroutineCtx)

        return try {
            val huamiResponse = try {
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        dataOriginFilter = setOf(DataOrigin(ORIGIN_HUAMI))
                    )
                )
            } catch (_: Exception) { null }

            val huamiDistance = huamiResponse?.get(DistanceRecord.DISTANCE_TOTAL)?.inKilometers ?: 0.0

            if (huamiDistance > 0.0) {
                HealthConnectLogger.logOriginResolution(
                    metric = "Distância",
                    selectedDate = date,
                    intervalExact = intervalStr,
                    huamiCount = 1,
                    googleFitCount = 0,
                    chosenOrigin = "Zepp (Huami)",
                    isFromCache = false,
                    diagnosticNote = "Huami prioritário (${String.format(Locale.US, "%.2f", huamiDistance)} km)"
                )
                val duration = System.currentTimeMillis() - startMs
                HealthConnectLogger.logQuerySuccess(qId, "DistanceRecord", component, duration, 1, coroutineCtx)
                return Pair(huamiDistance, "Zepp (Huami)")
            }

            val fitResponse = try {
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        dataOriginFilter = setOf(DataOrigin(ORIGIN_GOOGLE_FIT))
                    )
                )
            } catch (_: Exception) { null }

            val fitDistance = fitResponse?.get(DistanceRecord.DISTANCE_TOTAL)?.inKilometers ?: 0.0

            if (fitDistance > 0.0) {
                HealthConnectLogger.logOriginResolution(
                    metric = "Distância",
                    selectedDate = date,
                    intervalExact = intervalStr,
                    huamiCount = 0,
                    googleFitCount = 1,
                    chosenOrigin = "Google Fit",
                    isFromCache = false,
                    diagnosticNote = "Google Fit alternativo (${String.format(Locale.US, "%.2f", fitDistance)} km)"
                )
                val duration = System.currentTimeMillis() - startMs
                HealthConnectLogger.logQuerySuccess(qId, "DistanceRecord", component, duration, 1, coroutineCtx)
                return Pair(fitDistance, "Google Fit")
            }

            val generalResponse = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val generalDistance = generalResponse[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0
            val originLabel = if (generalDistance > 0.0) "Health Connect" else "Sem registro"

            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "DistanceRecord", component, duration, if (generalDistance > 0.0) 1 else 0, coroutineCtx)
            Pair(generalDistance, originLabel)
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "DistanceRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "DistanceRecord", component, duration, e, 1, coroutineCtx)
            Pair(0.0, "Health Connect")
        }
    }

    suspend fun queryWeeklyDistance(weekStartDate: LocalDate, component: String = "HomeScreen"): WeeklyDistanceSummary {
        val daySummaries = mutableListOf<DayDistanceSummary>()
        var permissionErrorEncountered = false
        var primaryOrigin = "Health Connect"

        for (i in 0..6) {
            val date = weekStartDate.plusDays(i.toLong())
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                .replaceFirstChar { it.uppercase() }
                .take(3)

            val (distance, origin) = if (!permissionErrorEncountered && AppLifecycleTracker.isForeground) {
                try {
                    readDailyDistanceKmWithOrigin(date, component)
                } catch (e: SecurityException) {
                    permissionErrorEncountered = true
                    Pair(0.0, "Sem permissão")
                }
            } else Pair(0.0, "Sem registro")

            if (distance > 0 && primaryOrigin == "Health Connect") {
                primaryOrigin = origin
            }

            daySummaries.add(
                DayDistanceSummary(
                    date = date,
                    dayOfWeekLabel = dayLabel,
                    distanceKm = distance,
                    source = origin
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
            dailyAverageKm = avg,
            sourceOrigin = primaryOrigin
        )
    }

    // ------------------- CALORIAS ATIVAS REAIS COM PRIORIDADE HUAMI > GOOGLE FIT -------------------

    suspend fun readTodayActiveCalories(component: String = "HomeScreen"): Pair<Double, String> {
        return readActiveCaloriesForDateWithOrigin(LocalDate.now(), component)
    }

    suspend fun readActiveCaloriesForDateWithOrigin(date: LocalDate, component: String = "Repository"): Pair<Double, String> {
        val client = healthConnectClient ?: return Pair(0.0, "Health Connect")
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "ActiveCaloriesBurnedRecord", component, 0, "AppInBackground")
            return Pair(0.0, "Health Connect")
        }

        val qId = HealthConnectLogger.nextQueryId("CAL")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zone).toInstant()
        val intervalStr = "$startTime a $endTime"

        HealthConnectLogger.logQueryStart(qId, "ActiveCaloriesBurnedRecord", component, "READ_ACTIVE_CALORIES", coroutineCtx)

        return try {
            val huamiResponse = try {
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        dataOriginFilter = setOf(DataOrigin(ORIGIN_HUAMI))
                    )
                )
            } catch (_: Exception) { null }

            val huamiCalories = huamiResponse?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)?.inKilocalories ?: 0.0

            if (huamiCalories > 0.0) {
                HealthConnectLogger.logOriginResolution(
                    metric = "Calorias Ativas",
                    selectedDate = date,
                    intervalExact = intervalStr,
                    huamiCount = 1,
                    googleFitCount = 0,
                    chosenOrigin = "Zepp (Huami)",
                    isFromCache = false,
                    diagnosticNote = "Huami prioritário (${huamiCalories.toInt()} kcal)"
                )
                val duration = System.currentTimeMillis() - startMs
                HealthConnectLogger.logQuerySuccess(qId, "ActiveCaloriesBurnedRecord", component, duration, 1, coroutineCtx)

                if (date == LocalDate.now()) {
                    database.healthDao().insertMetric(
                        HealthMetricEntity(
                            metricType = "active_calories",
                            value = huamiCalories,
                            unit = "kcal",
                            source = "Zepp (Huami)",
                            timestamp = Instant.now().toEpochMilli()
                        )
                    )
                }
                return Pair(huamiCalories, "Zepp (Huami)")
            }

            val fitResponse = try {
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        dataOriginFilter = setOf(DataOrigin(ORIGIN_GOOGLE_FIT))
                    )
                )
            } catch (_: Exception) { null }

            val fitCalories = fitResponse?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)?.inKilocalories ?: 0.0

            if (fitCalories > 0.0) {
                HealthConnectLogger.logOriginResolution(
                    metric = "Calorias Ativas",
                    selectedDate = date,
                    intervalExact = intervalStr,
                    huamiCount = 0,
                    googleFitCount = 1,
                    chosenOrigin = "Google Fit",
                    isFromCache = false,
                    diagnosticNote = "Google Fit alternativo (${fitCalories.toInt()} kcal)"
                )
                val duration = System.currentTimeMillis() - startMs
                HealthConnectLogger.logQuerySuccess(qId, "ActiveCaloriesBurnedRecord", component, duration, 1, coroutineCtx)
                return Pair(fitCalories, "Google Fit")
            }

            val generalResponse = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val generalCalories = generalResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
            val originLabel = if (generalCalories > 0.0) "Health Connect" else "Sem registro"

            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "ActiveCaloriesBurnedRecord", component, duration, if (generalCalories > 0.0) 1 else 0, coroutineCtx)
            Pair(generalCalories, originLabel)
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "ActiveCaloriesBurnedRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "ActiveCaloriesBurnedRecord", component, duration, e, 1, coroutineCtx)
            Pair(0.0, "Health Connect")
        }
    }

    // ------------------- FREQUÊNCIA CARDÍACA COM PRIORIDADE HUAMI > GOOGLE FIT -------------------

    suspend fun readLatestHeartRateWithOrigin(component: String = "HomeScreen"): Pair<Int?, String> {
        val client = healthConnectClient ?: return Pair(null, "Health Connect")
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "HeartRateRecord", component, 0, "AppInBackground")
            return Pair(null, "Health Connect")
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

            val huamiRecords = response.records.filter { it.metadata.dataOrigin.packageName == ORIGIN_HUAMI }
            val fitRecords = response.records.filter { it.metadata.dataOrigin.packageName == ORIGIN_GOOGLE_FIT }

            val chosenRecord = when {
                huamiRecords.isNotEmpty() -> huamiRecords.last()
                fitRecords.isNotEmpty() -> fitRecords.last()
                else -> response.records.lastOrNull()
            }

            val originLabel = chosenRecord?.let { formatOriginDisplayName(it.metadata.dataOrigin.packageName) } ?: "Health Connect"
            val latestSample = chosenRecord?.samples?.lastOrNull()
            val bpm = latestSample?.beatsPerMinute?.toInt()

            HealthConnectLogger.logOriginResolution(
                metric = "Frequência Cardíaca",
                selectedDate = LocalDate.now(),
                intervalExact = "$startTime a ${Instant.now()}",
                huamiCount = huamiRecords.size,
                googleFitCount = fitRecords.size,
                chosenOrigin = originLabel,
                isFromCache = false,
                diagnosticNote = "Registros totais: ${response.records.size}, BPM lido: $bpm"
            )

            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "HeartRateRecord", component, duration, response.records.size, coroutineCtx)
            Pair(bpm, originLabel)
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "HeartRateRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "HeartRateRecord", component, duration, e, 1, coroutineCtx)
            val cachedBpm = database.healthDao().getLatestMetric("heart_rate")?.value?.toInt()
            Pair(cachedBpm, "Banco Local")
        }
    }

    suspend fun readLatestRestingHeartRateWithOrigin(component: String = "HomeScreen"): Pair<Int?, String> {
        val client = healthConnectClient ?: return Pair(null, "Health Connect")
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "RestingHeartRateRecord", component, 0, "AppInBackground")
            return Pair(null, "Health Connect")
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

            val huamiRecords = response.records.filter { it.metadata.dataOrigin.packageName == ORIGIN_HUAMI }
            val fitRecords = response.records.filter { it.metadata.dataOrigin.packageName == ORIGIN_GOOGLE_FIT }

            val chosenRecord = when {
                huamiRecords.isNotEmpty() -> huamiRecords.last()
                fitRecords.isNotEmpty() -> fitRecords.last()
                else -> response.records.lastOrNull()
            }

            val originLabel = chosenRecord?.let { formatOriginDisplayName(it.metadata.dataOrigin.packageName) } ?: "Health Connect"
            val bpm = chosenRecord?.beatsPerMinute?.toInt()

            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "RestingHeartRateRecord", component, duration, response.records.size, coroutineCtx)
            Pair(bpm, originLabel)
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "RestingHeartRateRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "RestingHeartRateRecord", component, duration, e, 1, coroutineCtx)
            Pair(null, "Health Connect")
        }
    }

    // ------------------- SONO REAL & ESTÁGIOS COM PRIORIDADE HUAMI > GOOGLE FIT -------------------

    suspend fun readSleepSessionForNightWithOrigin(nightDate: LocalDate, component: String = "SleepScreen"): Pair<SleepSessionData?, String> {
        val client = healthConnectClient ?: return Pair(null, "Health Connect")
        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "SleepSessionRecord", component, 0, "AppInBackground")
            return Pair(null, "Health Connect")
        }

        val qId = HealthConnectLogger.nextQueryId("SLEEP")
        val coroutineCtx = currentCoroutineContext()
        val startMs = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val searchStart = nightDate.minusDays(1).atTime(18, 0).atZone(zone).toInstant()
        val searchEnd = nightDate.atTime(16, 0).atZone(zone).toInstant()
        val intervalStr = "$searchStart a $searchEnd"

        HealthConnectLogger.logQueryStart(qId, "SleepSessionRecord", component, "READ_NIGHT_SLEEP for $nightDate", coroutineCtx)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(searchStart, searchEnd)
                )
            )

            val huamiSessions = response.records.filter { it.metadata.dataOrigin.packageName == ORIGIN_HUAMI }
            val fitSessions = response.records.filter { it.metadata.dataOrigin.packageName == ORIGIN_GOOGLE_FIT }

            val chosenRecord = when {
                huamiSessions.isNotEmpty() -> huamiSessions.maxByOrNull { Duration.between(it.startTime, it.endTime).toMinutes() }
                fitSessions.isNotEmpty() -> fitSessions.maxByOrNull { Duration.between(it.startTime, it.endTime).toMinutes() }
                else -> response.records.maxByOrNull { Duration.between(it.startTime, it.endTime).toMinutes() }
            }

            val originLabel = chosenRecord?.let { formatOriginDisplayName(it.metadata.dataOrigin.packageName) } ?: "Sem registro"

            HealthConnectLogger.logOriginResolution(
                metric = "Sessão de Sono",
                selectedDate = nightDate,
                intervalExact = intervalStr,
                huamiCount = huamiSessions.size,
                googleFitCount = fitSessions.size,
                chosenOrigin = originLabel,
                isFromCache = false,
                diagnosticNote = "Sessão mais relevante selecionada sem somar durações"
            )

            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQuerySuccess(qId, "SleepSessionRecord", component, duration, response.records.size, coroutineCtx)

            val mapped = chosenRecord?.let { mapSleepSessionRecord(it) }
            Pair(mapped, originLabel)
        } catch (ce: CancellationException) {
            HealthConnectLogger.logQueryCancelled(qId, "SleepSessionRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
            throw ce
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startMs
            HealthConnectLogger.logQueryError(qId, "SleepSessionRecord", component, duration, e, 1, coroutineCtx)
            Pair(null, "Health Connect")
        }
    }

    suspend fun queryWeeklySleep(weekStartDate: LocalDate, component: String = "SleepScreen"): List<NightSleepSummary> {
        val result = mutableListOf<NightSleepSummary>()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()

        val sessions = mutableListOf<Pair<SleepSessionData?, String>>()
        var permissionDenied = false

        for (i in 0..6) {
            val date = weekStartDate.plusDays(i.toLong())
            val sessionPair = if (!permissionDenied && AppLifecycleTracker.isForeground) {
                try {
                    readSleepSessionForNightWithOrigin(date, component)
                } catch (e: SecurityException) {
                    permissionDenied = true
                    Pair(null, "Sem permissão")
                }
            } else Pair(null, "Sem registro")
            sessions.add(sessionPair)
        }

        val validSessions = sessions.mapNotNull { it.first }
        val avgDuration = if (validSessions.isNotEmpty()) {
            validSessions.map { it.totalDurationMinutes }.average().toLong()
        } else 0L

        for (i in 0..6) {
            val date = weekStartDate.plusDays(i.toLong())
            val (session, origin) = sessions[i]

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
                        comparisonWithWeeklyAvgMinutes = diffAvg,
                        sourceOrigin = origin
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
                        comparisonWithWeeklyAvgMinutes = 0L,
                        sourceOrigin = origin
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
            sourceApp = formatOriginDisplayName(record.metadata.dataOrigin.packageName)
        )
    }

    // ------------------- EXERCÍCIOS REAIS COM DEDUPLICAÇÃO HUAMI VS FIT -------------------

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
        val allRawRecords = mutableListOf<ExerciseSessionRecord>()
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
                allRawRecords.addAll(response.records)
                pageToken = response.pageToken
            } catch (ce: CancellationException) {
                HealthConnectLogger.logQueryCancelled(qId, "ExerciseSessionRecord", component, System.currentTimeMillis() - startMs, "CancelledByLifecycle", coroutineCtx)
                throw ce
            } catch (e: Exception) {
                HealthConnectLogger.logQueryError(qId, "ExerciseSessionRecord", component, System.currentTimeMillis() - startMs, e, 1, coroutineCtx)
                return emptyList()
            }
        } while (pageToken != null)

        val huamiSessions = allRawRecords.filter { it.metadata.dataOrigin.packageName == ORIGIN_HUAMI }
        val fitSessions = allRawRecords.filter { it.metadata.dataOrigin.packageName == ORIGIN_GOOGLE_FIT }
        val otherSessions = allRawRecords.filter { it.metadata.dataOrigin.packageName != ORIGIN_HUAMI && it.metadata.dataOrigin.packageName != ORIGIN_GOOGLE_FIT }

        val deduplicatedFitSessions = fitSessions.filter { fitSession ->
            val hasOverlap = huamiSessions.any { huamiSession ->
                val startDiff = Math.abs(Duration.between(fitSession.startTime, huamiSession.startTime).toMinutes())
                val endDiff = Math.abs(Duration.between(fitSession.endTime, huamiSession.endTime).toMinutes())
                startDiff <= 15 || endDiff <= 15
            }
            if (hasOverlap) {
                HealthConnectLogger.logOriginResolution(
                    metric = "Exercício (Deduplicação)",
                    selectedDate = fitSession.startTime.atZone(ZoneId.systemDefault()).toLocalDate(),
                    intervalExact = "${fitSession.startTime} a ${fitSession.endTime}",
                    huamiCount = 1,
                    googleFitCount = 1,
                    chosenOrigin = "Zepp (Huami)",
                    isFromCache = false,
                    diagnosticNote = "Descartada duplicata do Google Fit sincronizada do relógio"
                )
            }
            !hasOverlap
        }

        val allRecords = huamiSessions + deduplicatedFitSessions + otherSessions
        val resultList = mutableListOf<ExerciseSessionData>()
        var securityErrorEncountered = false

        for (session in allRecords) {
            val durationMins = Duration.between(session.startTime, session.endTime).toMinutes().coerceAtLeast(1L)
            var calories: Double? = null
            var distanceKm: Double? = null
            var avgHr: Int? = null
            var maxHr: Int? = null
            val sessionOrigin = session.metadata.dataOrigin

            if (!securityErrorEncountered && AppLifecycleTracker.isForeground) {
                try {
                    val cResponse = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(session.startTime, session.endTime),
                            dataOriginFilter = setOf(sessionOrigin)
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
                                timeRangeFilter = TimeRangeFilter.between(session.startTime, session.endTime),
                                dataOriginFilter = setOf(sessionOrigin)
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
                        val hrMatchingRecords = hrResponse.records.filter { it.metadata.dataOrigin == sessionOrigin }
                            .ifEmpty { hrResponse.records }
                        val samples = hrMatchingRecords.flatMap { it.samples }
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
                    sourceApp = formatOriginDisplayName(session.metadata.dataOrigin.packageName)
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
            val primarySource = daySessions.firstOrNull()?.sourceApp ?: "Health Connect"

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
                    intensityScore = intensityScore,
                    sourceOrigin = primarySource
                )
            )
        }

        return daySummaries
    }

    suspend fun calculateStreak(weeklyGoalDays: Int, component: String = "HomeScreen"): StreakData {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30).atStartOfDay(zone).toInstant()
        val nowInstant = Instant.now()

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
            currentStreak >= 5 -> "Você treinou $currentStreak dias consecutivos. Consistência exemplar!"
            currentStreak >= 3 -> "$currentStreak dias consecutivos em movimento. Mantenha o ritmo!"
            activeDaysThisWeek >= weeklyGoalDays -> "Meta semanal de $activeDaysThisWeek dias superada!"
            activeDaysThisWeek > 0 -> "Ótimo início nesta semana. Continue firme!"
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

    // ------------------- GASTO CALÓRICO BASAL (BMR) & BALANÇO ENERGÉTICO -------------------

    suspend fun readBasalMetabolicRate(userProfile: UserProfile?, component: String = "Repository"): BasalEnergyData {
        val client = healthConnectClient
        if (client != null && AppLifecycleTracker.isForeground) {
            try {
                val qId = HealthConnectLogger.nextQueryId("BMR")
                val startTime = Instant.now().minus(30, ChronoUnit.DAYS)
                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = BasalMetabolicRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                    )
                )

                val huamiRecord = response.records.filter { it.metadata.dataOrigin.packageName == ORIGIN_HUAMI }.lastOrNull()
                val chosenRecord = huamiRecord ?: response.records.lastOrNull()

                if (chosenRecord != null) {
                    val kcalPerDay = chosenRecord.basalMetabolicRate.inKilocaloriesPerDay
                    val origin = formatOriginDisplayName(chosenRecord.metadata.dataOrigin.packageName)

                    HealthConnectLogger.logOriginResolution(
                        metric = "Gasto Basal (BMR)",
                        selectedDate = LocalDate.now(),
                        intervalExact = "${chosenRecord.time}",
                        huamiCount = if (huamiRecord != null) 1 else 0,
                        googleFitCount = if (chosenRecord.metadata.dataOrigin.packageName == ORIGIN_GOOGLE_FIT) 1 else 0,
                        chosenOrigin = origin,
                        isFromCache = false,
                        diagnosticNote = "Registro real de BMR lido: ${kcalPerDay.toInt()} kcal/dia"
                    )

                    return BasalEnergyData(
                        bmrKcal = kcalPerDay,
                        sourceOrigin = origin,
                        isEstimated = false,
                        measurementDate = chosenRecord.time
                    )
                }
            } catch (_: Exception) {}
        }

        val estimated = userProfile?.estimatedBmrMifflinStJeor ?: 1650.0
        return BasalEnergyData(
            bmrKcal = estimated,
            sourceOrigin = "Estimativa (Mifflin-St Jeor)",
            isEstimated = true,
            measurementDate = null
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

    // ------------------- SINCRONIZAÇÃO COMPLETA COM CACHE E PRIORIDADES -------------------

    suspend fun fetchAllLiveHealthData(
        forceRefresh: Boolean = false,
        userProfile: UserProfile? = null,
        component: String = "MainActivity"
    ): LiveHealthData {
        val now = System.currentTimeMillis()
        val cached = cachedLiveData

        if (!forceRefresh && cached != null && (now - lastLiveDataFetchTime) < 180_000L) {
            HealthConnectLogger.logQuerySuccess("CACHE", "LiveHealthData", component, 0, 1)
            return cached
        }

        if (!AppLifecycleTracker.isForeground) {
            HealthConnectLogger.logQueryCancelled("SKIP", "LiveHealthData", component, 0, "AppInBackground")
            return cached ?: LiveHealthData()
        }

        return syncMutex.withLock {
            if (!forceRefresh && cachedLiveData != null && (System.currentTimeMillis() - lastLiveDataFetchTime) < 180_000L) {
                return@withLock cachedLiveData!!
            }

            val today = LocalDate.now()
            val (steps, stepsOrigin) = readTodaySteps(component)
            val (activeCaloriesDouble, caloriesOrigin) = readTodayActiveCalories(component)
            val activeCalories = activeCaloriesDouble.toInt()
            val (latestHeartRate, hrOrigin) = readLatestHeartRateWithOrigin(component)
            val (restingHeartRate, rhrOrigin) = readLatestRestingHeartRateWithOrigin(component)
            val (sleepSession, sleepOrigin) = readSleepSessionForNightWithOrigin(today, component)
            val (distanceKm, distanceOrigin) = readDailyDistanceKmWithOrigin(today, component)

            val bmrData = readBasalMetabolicRate(userProfile, component)
            val totalEnergy = bmrData.bmrKcal + activeCaloriesDouble

            val zone = ZoneId.systemDefault()
            val weekStart = today.minusDays(7).atStartOfDay(zone).toInstant()
            val exercises = queryExerciseSessions(weekStart, Instant.now(), component, "LIVE_FETCH_RECENT_EXERCISES")
            val (weight, bodyFat) = readLatestWeightAndBodyFat(component)

            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val sleepDuration = sleepSession?.totalDurationMinutes ?: 0L
            val sleepStartStr = sleepSession?.let { it.startTime.atZone(zone).format(formatter) } ?: "--:--"
            val sleepEndStr = sleepSession?.let { it.endTime.atZone(zone).format(formatter) } ?: "--:--"

            val hasRealData = steps > 0 || latestHeartRate != null || restingHeartRate != null || sleepDuration > 0 || exercises.isNotEmpty() || distanceKm > 0.0

            val result = LiveHealthData(
                steps = steps,
                stepsOrigin = stepsOrigin,
                activeCaloriesKcal = activeCalories,
                activeCaloriesOrigin = caloriesOrigin,
                heartRateBpm = latestHeartRate,
                heartRateOrigin = hrOrigin,
                restingHeartRateBpm = restingHeartRate,
                restingHeartRateOrigin = rhrOrigin,
                sleepDurationMinutes = sleepDuration,
                sleepStartTime = sleepStartStr,
                sleepEndTime = sleepEndStr,
                sleepStageDeep = sleepSession?.deepMinutes ?: 0L,
                sleepStageLight = sleepSession?.lightMinutes ?: 0L,
                sleepStageRem = sleepSession?.remMinutes ?: 0L,
                sleepStageAwake = sleepSession?.awakeMinutes ?: 0L,
                sleepOrigin = sleepOrigin,
                weeklyExerciseDays = exercises.map { it.startTime.atZone(zone).toLocalDate() }.toSet().size,
                weeklyExerciseMinutes = exercises.sumOf { it.durationMinutes },
                weightKg = weight,
                bodyFatPct = bodyFat,
                distanceKm = distanceKm,
                distanceOrigin = distanceOrigin,
                bmrKcal = bmrData.bmrKcal,
                bmrOrigin = bmrData.sourceOrigin,
                isBmrEstimated = bmrData.isEstimated,
                bmrMeasurementDate = bmrData.measurementDate,
                totalEnergyKcal = totalEnergy,
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
