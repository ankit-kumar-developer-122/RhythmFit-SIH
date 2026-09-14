package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailyQuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyQuoteDao {
    @Query("SELECT * FROM daily_quotes")
    fun getAllQuotesFlow(): Flow<List<DailyQuoteEntity>>

    @Query("SELECT * FROM daily_quotes WHERE dateServed = :todayDate LIMIT 1")
    suspend fun getQuoteServedOn(todayDate: String): DailyQuoteEntity?

    @Query("SELECT * FROM daily_quotes WHERE dateServed = :todayDate LIMIT 1")
    fun getQuoteServedOnFlow(todayDate: String): Flow<DailyQuoteEntity?>

    @Query("SELECT * FROM daily_quotes WHERE dateServed IS NULL OR dateServed != :todayDate ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnservedQuote(todayDate: String): DailyQuoteEntity?

    @Query("SELECT * FROM daily_quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): DailyQuoteEntity?

    @Query("SELECT COUNT(*) FROM daily_quotes")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<DailyQuoteEntity>)

    @Update
    suspend fun updateQuote(quote: DailyQuoteEntity)

    @Query("UPDATE daily_quotes SET isDismissed = 1 WHERE id = :id")
    suspend fun markDismissed(id: Int)

    @Query("UPDATE daily_quotes SET isClaimed = 1 WHERE id = :id")
    suspend fun markClaimed(id: Int)
}
