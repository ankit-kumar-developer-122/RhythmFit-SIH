package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MODULE 1: Room Database Entity for Mood Entries.
 * Captures 1-Tap 5-point mood levels:
 * 1: Overwhelmed, 2: Stressed, 3: Tired, 4: Neutral, 5: Energized
 */
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val moodLevel: Int, // 1 to 5
    val moodName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
    val isSynced: Boolean = false
) {
    val isLowMood: Boolean
        get() = moodLevel <= 2 // Stressed or Overwhelmed
}
