package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MODULE 1: Room Database Entity for Daily Soft Consistency Score (0–100).
 * Preserves historical rhythm dynamics without punishing binary breaks.
 */
@Entity(tableName = "consistency_scores")
data class ConsistencyScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateStr: String, // e.g. "2026-09-13"
    val score: Double, // 0.0 to 100.0
    val completedRoutinesCount: Int = 0,
    val hasFlaggedDampener: Boolean = false,
    val dampenerReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
