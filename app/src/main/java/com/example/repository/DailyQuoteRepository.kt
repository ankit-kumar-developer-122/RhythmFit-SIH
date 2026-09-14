package com.example.repository

import android.content.Context
import com.example.data.local.RhythmDatabase
import com.example.data.local.dao.DailyQuoteDao
import com.example.data.local.entity.DailyQuoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Daily Quote Repository backed by Room.
 * Ensures the randomized well-wishing quote of the day is served ONLY ONCE per calendar day.
 * If dismissed or claimed on that date, it stays dismissed for the rest of the day across restarts.
 * On a new calendar day, a fresh randomized quote from the Room table is selected and served.
 */
class DailyQuoteRepository(
    private val quoteDao: DailyQuoteDao
) {
    constructor(context: Context) : this(
        RhythmDatabase.getInstance(context).dailyQuoteDao()
    )

    private val _todayQuote = MutableStateFlow<DailyQuoteEntity?>(null)
    val todayQuote: StateFlow<DailyQuoteEntity?> = _todayQuote.asStateFlow()

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Loads or serves the quote for today.
     * Rules:
     * 1. If a quote was already assigned to today:
     *    - If isDismissed == true, return null (never re-annoy user today)
     *    - If not dismissed, return that assigned quote
     * 2. If no quote was served yet today:
     *    - Pick a randomized unserved quote from Room
     *    - Assign dateServed = today, update in Room
     *    - Return it
     */
    suspend fun refreshTodayQuote(): DailyQuoteEntity? = withContext(Dispatchers.IO) {
        val today = getTodayDateString()

        // Check if a quote is already registered for today
        val servedToday = quoteDao.getQuoteServedOn(today)

        if (servedToday != null) {
            // Already served today
            if (servedToday.isDismissed) {
                _todayQuote.value = null
                return@withContext null
            } else {
                _todayQuote.value = servedToday
                return@withContext servedToday
            }
        }

        // Pick a randomized quote that hasn't been served today
        val candidate = quoteDao.getRandomUnservedQuote(today) ?: quoteDao.getRandomQuote()
        if (candidate != null) {
            val assigned = candidate.copy(
                dateServed = today,
                isDismissed = false,
                isClaimed = false
            )
            quoteDao.updateQuote(assigned)
            _todayQuote.value = assigned
            return@withContext assigned
        }

        _todayQuote.value = null
        null
    }

    suspend fun dismissTodayQuote(quoteId: Int) = withContext(Dispatchers.IO) {
        quoteDao.markDismissed(quoteId)
        _todayQuote.value = null
    }

    suspend fun claimTodayQuote(quoteId: Int) = withContext(Dispatchers.IO) {
        quoteDao.markClaimed(quoteId)
        val current = _todayQuote.value
        if (current != null && current.id == quoteId) {
            _todayQuote.value = current.copy(isClaimed = true)
        }
    }
}
