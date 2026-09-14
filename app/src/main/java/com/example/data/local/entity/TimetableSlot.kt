package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MODULE 1: Room Database Entity for Timetable Slots.
 * Stores user schedule blocks and gap windows (2-5 mins) for micro-break alerts.
 */
@Entity(tableName = "timetable_slots")
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val dayOfWeek: Int = 0, // 0 = Everyday, 1 = Mon .. 7 = Sun
    val gapDurationMins: Int = 5,
    val suggestedRoutine: String = "Desk Neck & Shoulder Relief",
    val isNudgeEnabled: Boolean = true
) {
    fun formatTimeRange(): String {
        val startFormatted = String.format("%02d:%02d", startHour, startMinute)
        val endFormatted = String.format("%02d:%02d", endHour, endMinute)
        return "$startFormatted - $endFormatted"
    }
}
