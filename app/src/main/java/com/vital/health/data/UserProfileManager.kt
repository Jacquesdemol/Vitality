package com.vital.health.data

import android.content.Context
import android.content.SharedPreferences

enum class Gender(val displayName: String) {
    MALE("Masculino"),
    FEMALE("Feminino"),
    NOT_SPECIFIED("Não especificado")
}

enum class CalorieGoalMode(val displayName: String, val shortLabel: String) {
    ACTIVE_CALORIES("Calorias Ativas de Treino", "Ativas"),
    TOTAL_ENERGY("Gasto Energético Total Diário", "Total (Basal + Ativas)")
}

data class UserGoals(
    val dailySteps: Long = 10000L,
    val dailyCalories: Int = 600,
    val calorieGoalMode: CalorieGoalMode = CalorieGoalMode.ACTIVE_CALORIES,
    val sleepHours: Float = 8.0f,
    val weeklyWorkoutDays: Int = 5
)

data class UserProfile(
    val name: String = "Iury Henrique",
    val age: Int = 24,
    val heightCm: Double = 173.0,
    val weightKg: Double = 73.8,
    val gender: Gender = Gender.MALE,
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

    /**
     * Cálculo de Gasto Calórico Basal (BMR) pela fórmula científica Mifflin-St Jeor:
     * Homens: (10 × peso_kg) + (6.25 × altura_cm) - (5 × idade) + 5
     * Mulheres: (10 × peso_kg) + (6.25 × altura_cm) - (5 × idade) - 161
     */
    val estimatedBmrMifflinStJeor: Double?
        get() {
            if (weightKg <= 0.0 || heightCm <= 0.0 || age <= 0) return null
            return when (gender) {
                Gender.MALE -> (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
                Gender.FEMALE -> (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
                Gender.NOT_SPECIFIED -> (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 78.0
            }
        }
}

class UserProfileManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vital_user_profile", Context.MODE_PRIVATE)

    fun getProfile(): UserProfile {
        val name = prefs.getString("user_name", "Iury Henrique") ?: "Iury Henrique"
        val age = prefs.getInt("user_age", 24)
        val height = prefs.getFloat("user_height", 173f).toDouble()
        val weight = prefs.getFloat("user_weight", 73.8f).toDouble()
        val genderStr = prefs.getString("user_gender", Gender.MALE.name) ?: Gender.MALE.name
        val gender = try { Gender.valueOf(genderStr) } catch (_: Exception) { Gender.MALE }

        val steps = prefs.getLong("goal_steps", 10000L)
        val calories = prefs.getInt("goal_calories", 600)
        val goalModeStr = prefs.getString("goal_calorie_mode", CalorieGoalMode.ACTIVE_CALORIES.name) ?: CalorieGoalMode.ACTIVE_CALORIES.name
        val calorieGoalMode = try { CalorieGoalMode.valueOf(goalModeStr) } catch (_: Exception) { CalorieGoalMode.ACTIVE_CALORIES }
        val sleep = prefs.getFloat("goal_sleep", 8.0f)
        val workouts = prefs.getInt("goal_workouts", 5)

        return UserProfile(
            name = name,
            age = age,
            heightCm = height,
            weightKg = weight,
            gender = gender,
            goals = UserGoals(
                dailySteps = steps,
                dailyCalories = calories,
                calorieGoalMode = calorieGoalMode,
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
            .putString("user_gender", profile.gender.name)
            .putLong("goal_steps", profile.goals.dailySteps)
            .putInt("goal_calories", profile.goals.dailyCalories)
            .putString("goal_calorie_mode", profile.goals.calorieGoalMode.name)
            .putFloat("goal_sleep", profile.goals.sleepHours)
            .putInt("goal_workouts", profile.goals.weeklyWorkoutDays)
            .apply()
    }

    fun updateGoals(goals: UserGoals) {
        prefs.edit()
            .putLong("goal_steps", goals.dailySteps)
            .putInt("goal_calories", goals.dailyCalories)
            .putString("goal_calorie_mode", goals.calorieGoalMode.name)
            .putFloat("goal_sleep", goals.sleepHours)
            .putInt("goal_workouts", goals.weeklyWorkoutDays)
            .apply()
    }
}
