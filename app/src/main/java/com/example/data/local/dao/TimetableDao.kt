package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TimetableSlot
import kotlinx.coroutines.flow.Flow

/**
 * MODULE 1: Room DAO for TimetableSlot operations.
 */
@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_slots ORDER BY startHour ASC, startMinute ASC")
    fun getAllSlots(): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots ORDER BY startHour ASC, startMinute ASC")
    suspend fun getAllSlotsSync(): List<TimetableSlot>

    @Query("SELECT * FROM timetable_slots WHERE isNudgeEnabled = 1 ORDER BY startHour ASC, startMinute ASC")
    fun getActiveNudgeSlots(): Flow<List<TimetableSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<TimetableSlot>)

    @Update
    suspend fun updateSlot(slot: TimetableSlot)

    @Delete
    suspend fun deleteSlot(slot: TimetableSlot)

    @Query("DELETE FROM timetable_slots WHERE id = :id")
    suspend fun deleteSlotById(id: Long)
}
