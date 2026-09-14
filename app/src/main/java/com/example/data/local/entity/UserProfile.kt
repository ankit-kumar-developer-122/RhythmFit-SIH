package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MODULE 1: Room Database Entity for User Baseline Profile.
 * Used by Calorie Expenditure Engine (MET) & Rhythm Engine.
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Alex",
    val weightKg: Double = 68.0,
    val heightCm: Double = 172.0,
    val age: Int = 24,
    val gender: String = "Female", // "Female", "Male", "Other"
    val baseConsistencyScore: Double = 72.0,
    val dailyStepGoal: Int = 8000,
    val dailyKcalGoal: Int = 450
)
