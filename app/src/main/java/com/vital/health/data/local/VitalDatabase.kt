package com.vital.health.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HealthMetricEntity::class], version = 1, exportSchema = false)
abstract class VitalDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao
}
