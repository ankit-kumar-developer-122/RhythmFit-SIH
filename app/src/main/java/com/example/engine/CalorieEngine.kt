package com.example.engine

import com.example.data.local.entity.UserProfile
import kotlin.math.roundToInt

/**
 * MODULE 2: Calorie Expenditure Engine.
 * Implements Metabolic Equivalent of Task (MET) calculations
 * personalized against user profile parameters (weight, height, age, gender).
 */
object CalorieEngine {

    // Common MET lookup values from the Compendium of Physical Activities
    private val MET_TABLE = mapOf(
        "desk_stretch" to 2.8,
        "desk_neck" to 2.5,
        "breathing" to 1.5,
        "box_breathing" to 1.5,
        "light_yoga" to 3.0,
        "walking" to 3.8,
        "college_walk" to 3.8,
        "power_walk" to 4.5,
        "jogging" to 7.0,
        "running" to 8.0,
        "hiit" to 7.5,
        "cycling" to 5.5,
        "posture_reset" to 2.6,
        "micro_break" to 3.0,
        "stretching" to 2.8,
        "stairs" to 6.0
    )

    /**
     * Resolves the MET value based on activity name and associated tags.
     */
    fun resolveMet(activityName: String, tags: List<String> = emptyList()): Double {
        val normalized = activityName.lowercase()
        val allTokens = (normalized.split(" ", "_", "-") + tags.map { it.lowercase() }).toSet()

        for (token in allTokens) {
            MET_TABLE[token]?.let { return it }
        }

        return when {
            normalized.contains("breath") || normalized.contains("meditat") -> 1.5
            normalized.contains("stretch") || normalized.contains("posture") || normalized.contains("neck") -> 2.8
            normalized.contains("yoga") -> 3.0
            normalized.contains("walk") -> 3.8
            normalized.contains("run") || normalized.contains("jog") -> 7.0
            normalized.contains("hiit") || normalized.contains("jump") -> 7.5
            normalized.contains("cycle") || normalized.contains("bike") -> 5.5
            else -> 3.2 // Baseline moderate micro-movement
        }
    }

    /**
     * Calculates energy expenditure in kilocalories (kcal).
     * Standard Exercise Physiology Formula:
     * Kcal = MET * BodyWeight(kg) * (Duration(mins) / 60)
     *
     * In addition, adjusts slightly for personal Basal Metabolic Rate (BMR)
     * accounting for age and biological sex differences.
     */
    fun calculateExpenditure(
        met: Double,
        durationMins: Int,
        userProfile: UserProfile
    ): Int {
        if (durationMins <= 0) return 0

        // Standard metabolic formula
        val hours = durationMins / 60.0
        val baseKcal = met * userProfile.weightKg * hours

        // BMR adjustment coefficient (Mifflin-St Jeor ratio against 70kg standard adult)
        val bmr = if (userProfile.gender.equals("Male", ignoreCase = true)) {
            (10.0 * userProfile.weightKg) + (6.25 * userProfile.heightCm) - (5.0 * userProfile.age) + 5.0
        } else {
            (10.0 * userProfile.weightKg) + (6.25 * userProfile.heightCm) - (5.0 * userProfile.age) - 161.0
        }
        val standardBmr = 1600.0 // Standard adult reference
        val ratio = (bmr / standardBmr).coerceIn(0.85, 1.25)

        val adjustedKcal = baseKcal * (0.85 + (0.15 * ratio))
        return adjustedKcal.roundToInt().coerceAtLeast(1)
    }

    /**
     * Helper for quick routine calorie range strings (e.g. "~15-20 kcal").
     */
    fun formatCalorieRange(durationMins: Int, met: Double, weightKg: Double = 68.0): String {
        val expected = (met * weightKg * (durationMins / 60.0)).roundToInt()
        val min = (expected * 0.9).roundToInt().coerceAtLeast(1)
        val max = (expected * 1.15).roundToInt().coerceAtLeast(min + 3)
        return "~$min–$max kcal"
    }
}
