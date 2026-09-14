package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for Well-Wishing Daily Quotes.
 * Stores quote content, context, and tracks dateServed, dismissal status, and claim status
 * so quotes are only served once per calendar day without repetitive annoyance.
 */
@Entity(tableName = "daily_quotes")
data class DailyQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val quote: String,
    val author: String,
    val greetingContext: String,
    val theme: String,
    val affirmationBenefit: String = "+15 Consistency XP",
    val dateServed: String? = null, // e.g. "2026-09-14"
    val isDismissed: Boolean = false,
    val isClaimed: Boolean = false
)
