package com.example.ai

/**
 * Structured output model for parsed natural language fitness logs.
 */
data class ParsedLogResult(
    val activity: String,
    val durationMins: Int,
    val estimatedKcal: Int,
    val tags: List<String>,
    val steps: Int = 0
)

/**
 * Model representing an adaptive micro-routine suggested by AI or curated catalog.
 */
data class MicroRoutine(
    val id: String,
    val title: String,
    val category: String, // e.g. "Mobility", "Calm", "Energy", "Posture"
    val durationMins: Int,
    val metValue: Double,
    val calorieEstimateStr: String,
    val targetMood: String, // "Energized", "Neutral", "Tired", "Stressed", "Overwhelmed"
    val steps: List<RoutineStep>,
    val breathingCadence: String? = null
)

data class RoutineStep(
    val title: String,
    val instruction: String,
    val durationSeconds: Int,
    val audioCue: String
)
