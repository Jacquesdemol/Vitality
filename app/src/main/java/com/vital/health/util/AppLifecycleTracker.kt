package com.vital.health.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicInteger

/**
 * Monitor do estado do ciclo de vida global do VITAL (Foreground vs Background).
 * Permite que a camada de dados e de logging saiba com precisão se o app está visível.
 */
object AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    private val activeActivityCount = AtomicInteger(0)
    @Volatile
    private var isAppForeground: Boolean = false

    val isForeground: Boolean
        get() = isAppForeground

    val stateLabel: String
        get() = if (isAppForeground) "FOREGROUND" else "BACKGROUND"

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activeActivityCount.incrementAndGet() > 0) {
            isAppForeground = true
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activeActivityCount.decrementAndGet() <= 0) {
            isAppForeground = false
        }
    }

    override fun onActivityResumed(activity: Activity) {
        isAppForeground = true
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
