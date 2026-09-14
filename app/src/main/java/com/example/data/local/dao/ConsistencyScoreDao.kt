package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ConsistencyScore
import kotlinx.coroutines.flow.Flow

/**
 * MODULE 1: Room DAO for ConsistencyScore operations.
 */
@Dao
interface ConsistencyScoreDao {
    @Query("SELECT * FROM consistency_scores ORDER BY timestamp DESC LIMIT 7")
    fun getRecentScores(): Flow<List<ConsistencyScore>>

    @Query("SELECT * FROM consistency_scores WHERE dateStr = :dateStr LIMIT 1")
    fun getScoreForDate(dateStr: String): Flow<ConsistencyScore?>

    @Query("SELECT * FROM consistency_scores WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getScoreForDateSync(dateStr: String): ConsistencyScore?

    @Query("SELECT * FROM consistency_scores ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestScoreSync(): ConsistencyScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(score: ConsistencyScore): Long

    @Query("DELETE FROM consistency_scores")
    suspend fun clearAll()
}
