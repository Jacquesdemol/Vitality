package com.vital.health.data

import android.content.Context
import android.content.SharedPreferences

data class UserGoals(
    val dailySteps: Long = 10000L,
    val dailyCalories: Int = 600,
    val sleepHours: Float = 8.0f,
    val weeklyWorkoutDays: Int = 5
)

data class UserProfile(
    val name: String = "Iury Henrique",
    val age: Int = 24,
    val heightCm: Double = 173.0,
    val weightKg: Double = 73.8,
    val goals: UserGoals = UserGoals()
) {
    val bmi: Double
        get() {
            val heightM = heightCm / 100.0
            return if (heightM > 0) {
                val raw = weightKg / (heightM * heightM)
                kotlin.math.round(raw * 10.0) / 10.0
            } else 24.1
        }
}

class UserProfileManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vital_user_profile", Context.MODE_PRIVATE)

    fun getProfile(): UserProfile {
        val name = prefs.getString("user_name", "Iury Henrique") ?: "Iury Henrique"
        val age = prefs.getInt("user_age", 24)
        val height = prefs.getFloat("user_height", 173f).toDouble()
        val weight = prefs.getFloat("user_weight", 73.8f).toDouble()

        val steps = prefs.getLong("goal_steps", 10000L)
        val calories = prefs.getInt("goal_calories", 600)
        val sleep = prefs.getFloat("goal_sleep", 8.0f)
        val workouts = prefs.getInt("goal_workouts", 5)

        return UserProfile(
            name = name,
            age = age,
            heightCm = height,
            weightKg = weight,
            goals = UserGoals(
                dailySteps = steps,
                dailyCalories = calories,
                sleepHours = sleep,
                weeklyWorkoutDays = workouts
            )
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString("user_name", profile.name)
            .putInt("user_age", profile.age)
            .putFloat("user_height", profile.heightCm.toFloat())
            .putFloat("user_weight", profile.weightKg.toFloat())
            .putLong("goal_steps", profile.goals.dailySteps)
            .putInt("goal_calories", profile.goals.dailyCalories)
            .putFloat("goal_sleep", profile.goals.sleepHours)
            .putInt("goal_workouts", profile.goals.weeklyWorkoutDays)
            .apply()
    }

    fun updateGoals(goals: UserGoals) {
        prefs.edit()
            .putLong("goal_steps", goals.dailySteps)
            .putInt("goal_calories", goals.dailyCalories)
            .putFloat("goal_sleep", goals.sleepHours)
            .putInt("goal_workouts", goals.weeklyWorkoutDays)
            .apply()
    }
}
