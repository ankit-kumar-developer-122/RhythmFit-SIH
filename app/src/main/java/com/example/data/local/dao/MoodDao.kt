package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * MODULE 1: Room DAO for MoodEntry operations.
 */
@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getMoodsSince(sinceTimestamp: Long): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMoods(limit: Int = 10): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMoodsSync(limit: Int = 10): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE isSynced = 0")
    suspend fun getUnsyncedMoods(): List<MoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(moods: List<MoodEntry>)

    @Query("UPDATE mood_entries SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodById(id: Long)
}
