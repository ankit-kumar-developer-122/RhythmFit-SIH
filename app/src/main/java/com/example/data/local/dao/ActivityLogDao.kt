package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActivityLog
import kotlinx.coroutines.flow.Flow

/**
 * MODULE 1: Room DAO for ActivityLog operations.
 */
@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getLogsSince(sinceTimestamp: Long): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
    fun getTodayLogs(startOfDayTimestamp: Long): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
    suspend fun getTodayLogsSync(startOfDayTimestamp: Long): List<ActivityLog>

    @Query("SELECT * FROM activity_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<ActivityLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ActivityLog>)

    @Update
    suspend fun updateLog(log: ActivityLog)

    @Query("UPDATE activity_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("SELECT COUNT(*) FROM activity_logs WHERE timestamp >= :startOfDayTimestamp AND isMicroBreak = 1")
    fun getTodayMicroBreaksCount(startOfDayTimestamp: Long): Flow<Int>
}
