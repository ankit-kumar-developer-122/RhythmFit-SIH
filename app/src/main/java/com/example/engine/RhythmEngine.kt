package com.example.engine

import kotlin.math.roundToInt

/**
 * MODULE 2: Adaptive Rhythm Engine.
 * Calculates the Soft Consistency Score (0–100) that eliminates the anxiety of
 * binary streak breaking by gracefully dampening decay during life events (exams, illness, travel).
 */
object RhythmEngine {

    const val MULTIPLIER_PER_ROUTINE = 4.5
    const val STANDARD_DAILY_DECAY = 7.0
    const val DAMPENED_DECAY_FACTOR = 0.20 // 80% penalty reduction for context tags

    val CONTEXT_DAMPENER_TAGS = setOf(
        "exam",
        "exams",
        "exam stress",
        "study",
        "travel",
        "traveling",
        "flight",
        "illness",
        "sick",
        "fever",
        "wedding",
        "family emergency"
    )

    /**
     * Checks if any of the provided tags match life context dampeners.
     */
    fun hasContextDampener(tags: List<String>): Pair<Boolean, String?> {
        for (tag in tags) {
            val lower = tag.trim().lowercase()
            if (CONTEXT_DAMPENER_TAGS.any { lower.contains(it) }) {
                return Pair(true, tag)
            }
        }
        return Pair(false, null)
    }

    /**
     * Calculates the updated consistency score.
     * Formula:
     * Score = (Base Score) + (Completed Micro-breaks * Multiplier) - (Soft Decay Penalty for Missed Days)
     * Where missed days with flagged context tags reduce the decay penalty by 80%.
     */
    fun computeConsistencyScore(
        previousScore: Double,
        completedRoutinesToday: Int,
        missedDaysCount: Int = 0,
        hasFlaggedContextTag: Boolean = false
    ): Double {
        val routineBonus = (completedRoutinesToday * MULTIPLIER_PER_ROUTINE).coerceAtMost(25.0)

        val decayPerDay = if (hasFlaggedContextTag) {
            STANDARD_DAILY_DECAY * DAMPENED_DECAY_FACTOR // 80% reduction: only 20% penalty
        } else {
            STANDARD_DAILY_DECAY
        }

        val totalDecay = (missedDaysCount * decayPerDay)
        val calculated = previousScore + routineBonus - totalDecay

        return (calculated.coerceIn(0.0, 100.0) * 10.0).roundToInt() / 10.0
    }

    /**
     * Returns a human-centric rhythm category label and supportive description.
     */
    fun getRhythmCategory(score: Double): RhythmCategory {
        return when {
            score >= 85.0 -> RhythmCategory(
                title = "Flourishing Rhythm",
                subtitle = "In deep sync with your natural flow",
                tone = "Great job keeping active pauses in your day!",
                colorHex = 0xFF10B981 // Emerald Green
            )
            score >= 70.0 -> RhythmCategory(
                title = "Harmonious Rhythm",
                subtitle = "Balanced and sustainable momentum",
                tone = "Micro-breaks are supporting your focus well.",
                colorHex = 0xFF06B6D4 // Cyan
            )
            score >= 50.0 -> RhythmCategory(
                title = "Adapting Rhythm",
                subtitle = "Life is busy; your body is adjusting",
                tone = "Even 2-minute pauses protect your energy.",
                colorHex = 0xFFF59E0B // Amber
            )
            score >= 30.0 -> RhythmCategory(
                title = "Resting Window",
                subtitle = "Gentle recovery mode active",
                tone = "No pressure — just gentle desk stretches when ready.",
                colorHex = 0xFF6366F1 // Indigo
            )
            else -> RhythmCategory(
                title = "Soft Reset",
                subtitle = "Fresh rhythm ready to begin",
                tone = "A 2-min breath reset will reignite your score.",
                colorHex = 0xFFEC4899 // Rose
            )
        }
    }

    data class RhythmCategory(
        val title: String,
        val subtitle: String,
        val tone: String,
        val colorHex: Long
    )
}
