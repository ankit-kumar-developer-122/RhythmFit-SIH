package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MODULE 1: Room Database Entity for Activity Logs.
 * Stores heterogeneous fitness records (free-text AI parsed logs, micro-breaks, and manual entries).
 */
@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityName: String,
    val durationMins: Int,
    val estimatedKcal: Int,
    val metValue: Double = 3.0,
    val tags: List<String> = emptyList(),
    val steps: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isMicroBreak: Boolean = false
)
