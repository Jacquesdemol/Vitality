package com.vital.health.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val metricType: String, // e.g. "steps", "heart_rate", "active_calories", "sleep_minutes"
    val value: Double,
    val unit: String,
    val source: String,
    val timestamp: Long
)
