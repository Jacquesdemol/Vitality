package com.vital.health.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.vital.health.data.local.VitalDatabase
import com.vital.health.data.local.HealthMetricEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * HealthConnectRepository - Camada de integração com o Android Health Connect SDK
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

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun getGrantedPermissions(): Set<String> {
        val client = healthConnectClient ?: return emptySet()
        return client.permissionController.getGrantedPermissions()
    }

    suspend fun readTodaySteps(): Long {
        val client = healthConnectClient ?: return 0L
        val startTime = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val endTime = Instant.now()

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val totalSteps = response[StepsRecord.COUNT_TOTAL] ?: 0L
            database.healthDao().insertMetric(
                HealthMetricEntity(
                    metricType = "steps",
                    value = totalSteps.toDouble(),
                    unit = "steps",
                    source = "Health Connect (StepsRecord)",
                    timestamp = Instant.now().toEpochMilli()
                )
            )
            totalSteps
        } catch (e: Exception) {
            database.healthDao().getLatestMetric("steps")?.value?.toLong() ?: 0L
        }
    }

    suspend fun readLatestHeartRate(): Int? {
        val client = healthConnectClient ?: return null
        val startTime = Instant.now().minus(24, ChronoUnit.HOURS)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            )
            val latestSample = response.records.lastOrNull()?.samples?.lastOrNull()
            val bpm = latestSample?.beatsPerMinute?.toInt()

            if (bpm != null) {
                database.healthDao().insertMetric(
                    HealthMetricEntity(
                        metricType = "heart_rate",
                        value = bpm.toDouble(),
                        unit = "bpm",
                        source = response.records.lastOrNull()?.metadata?.dataOrigin?.packageName ?: "Health Connect",
                        timestamp = latestSample.time.toEpochMilli()
                    )
                )
            }
            bpm
        } catch (e: Exception) {
            database.healthDao().getLatestMetric("heart_rate")?.value?.toInt()
        }
    }

    suspend fun readLatestSleepSession(): SleepSessionRecord? {
        val client = healthConnectClient ?: return null
        val startTime = Instant.now().minus(36, ChronoUnit.HOURS)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            )
            response.records.lastOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readTodayActiveCalories(): Double {
        val client = healthConnectClient ?: return 0.0
        val startTime = Instant.now().truncatedTo(ChronoUnit.DAYS)

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            )
            val calories = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
            database.healthDao().insertMetric(
                HealthMetricEntity(
                    metricType = "active_calories",
                    value = calories,
                    unit = "kcal",
                    source = "Health Connect (ActiveCaloriesBurnedRecord)",
                    timestamp = Instant.now().toEpochMilli()
                )
            )
            calories
        } catch (e: Exception) {
            database.healthDao().getLatestMetric("active_calories")?.value ?: 0.0
        }
    }

    suspend fun readRecentExerciseSessions(): List<ExerciseSessionRecord> {
        val client = healthConnectClient ?: return emptyList()
        val startTime = Instant.now().minus(7, ChronoUnit.DAYS)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            )
            response.records
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readLatestWeightAndBodyFat(): Pair<Double?, Double?> {
        val client = healthConnectClient ?: return Pair(null, null)
        val startTime = Instant.now().minus(30, ChronoUnit.DAYS)

        val weightResponse = try {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            ).records.lastOrNull()?.weight?.inKilograms
        } catch (e: Exception) { null }

        val fatResponse = try {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            ).records.lastOrNull()?.percentage?.value
        } catch (e: Exception) { null }

        return Pair(weightResponse, fatResponse)
    }
}
