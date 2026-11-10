package com.vital.health

import android.app.Application
import androidx.room.Room
import com.vital.health.data.local.VitalDatabase
import com.vital.health.util.AppLifecycleTracker

class VitalApplication : Application() {

    lateinit var database: VitalDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        AppLifecycleTracker.init(this)
        database = Room.databaseBuilder(
            applicationContext,
            VitalDatabase::class.java,
            "vital_health.db"
        ).fallbackToDestructiveMigration().build()
    }
}
