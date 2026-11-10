package com.vital.health.model

import java.time.Instant
import java.time.LocalDate

enum class SleepStageType {
    DEEP,
    LIGHT,
    REM,
    AWAKE,
    OUT_OF_BED,
    UNKNOWN
}

data class SleepStageInterval(
    val startTime: Instant,
    val endTime: Instant,
    val stageType: SleepStageType,
    val durationMinutes: Long
)

data class SleepSessionData(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val totalDurationMinutes: Long,
    val timeAsleepMinutes: Long,
    val timeInBedMinutes: Long,
    val stages: List<SleepStageInterval>,
    val deepMinutes: Long,
    val lightMinutes: Long,
    val remMinutes: Long,
    val awakeMinutes: Long,
    val hasStages: Boolean,
    val sourceApp: String
)

data class NightSleepSummary(
    val date: LocalDate,
    val session: SleepSessionData?,
    val totalMinutes: Long,
    val formattedDuration: String,
    val formattedBedtime: String,
    val formattedWakeTime: String,
    val qualityAssessment: String,
    val comparisonWithWeeklyAvgMinutes: Long
)

data class ExerciseSessionData(
    val id: String,
    val title: String,
    val exerciseType: Int,
    val typeName: String,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Long,
    val caloriesKcal: Double?,
    val distanceKm: Double?,
    val avgHeartRateBpm: Int?,
    val maxHeartRateBpm: Int?,
    val intensityZone: String?,
    val sourceApp: String
)

data class DayActivitySummary(
    val date: LocalDate,
    val dayOfWeekLabel: String,
    val totalActiveMinutes: Long,
    val totalCaloriesKcal: Double,
    val totalDistanceKm: Double,
    val exerciseSessions: List<ExerciseSessionData>,
    val hasActivity: Boolean,
    val intensityScore: Float // 0f..1f for visual bar height
)

data class DayDistanceSummary(
    val date: LocalDate,
    val dayOfWeekLabel: String,
    val distanceKm: Double,
    val source: String
)

data class WeeklyDistanceSummary(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<DayDistanceSummary>,
    val totalWeeklyKm: Double,
    val dailyAverageKm: Double
)

data class StreakData(
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    val activeDaysThisWeek: Int,
    val weeklyGoalDays: Int,
    val streakMessage: String,
    val isGoalMet: Boolean
)

enum class HealthSyncStatus {
    CONNECTED,
    SYNCING,
    UNAVAILABLE,
    UPDATE_REQUIRED,
    PERMISSIONS_PENDING,
    NO_DATA,
    ERROR
}
