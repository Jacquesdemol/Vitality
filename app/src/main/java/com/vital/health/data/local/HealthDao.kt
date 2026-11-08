package com.vital.health.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(entity: HealthMetricEntity): Long

    @Query("SELECT * FROM health_metrics WHERE metricType = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMetric(type: String): HealthMetricEntity?

    @Query("SELECT * FROM health_metrics WHERE metricType = :type ORDER BY timestamp DESC")
    fun observeMetrics(type: String): Flow<List<HealthMetricEntity>>

    @Query("DELETE FROM health_metrics")
    suspend fun clearAll()
}
