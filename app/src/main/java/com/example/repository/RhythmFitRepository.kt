package com.example.repository

import android.content.Context
import com.example.ai.GeminiService
import com.example.ai.MicroRoutine
import com.example.ai.ParsedLogResult
import com.example.data.local.RhythmDatabase
import com.example.data.local.entity.ActivityLog
import com.example.data.local.entity.ConsistencyScore
import com.example.data.local.entity.MoodEntry
import com.example.data.local.entity.TimetableSlot
import com.example.data.local.entity.UserProfile
import com.example.data.model.LeaderboardUser
import com.example.data.remote.mongo.MongoOnlineRepository
import com.example.engine.CalorieEngine
import com.example.engine.RhythmEngine
import com.example.notification.TimetableAlarmScheduler
import com.example.sync.SyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Repository layer acting as the single source of truth for RhythmFit.
 */
class RhythmFitRepository(
    private val context: Context,
    private val database: RhythmDatabase = RhythmDatabase.getInstance(context),
    private val geminiService: GeminiService = GeminiService(),
    private val alarmScheduler: TimetableAlarmScheduler = TimetableAlarmScheduler(context),
    val mongoRepository: MongoOnlineRepository = MongoOnlineRepository(context)
) {
    private val activityDao = database.activityLogDao()
    private val timetableDao = database.timetableDao()
    private val moodDao = database.moodDao()
    private val userProfileDao = database.userProfileDao()
    private val scoreDao = database.consistencyScoreDao()

    // Online MongoDB sync state flows
    val isOnline = mongoRepository.isOnline
    val lastSyncTimestamp = mongoRepository.lastSyncTimestamp
    val syncStatusMessage = mongoRepository.syncStatusMessage
    val isSyncing = mongoRepository.isSyncing

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Reactive Flows for UI
    val allLogs: Flow<List<ActivityLog>> = activityDao.getAllLogs()
    val todayLogs: Flow<List<ActivityLog>> = activityDao.getTodayLogs(getTodayStartTimestamp())
    val timetableSlots: Flow<List<TimetableSlot>> = timetableDao.getAllSlots()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val recentMoods: Flow<List<MoodEntry>> = moodDao.getRecentMoods(10)
    val allMoods: Flow<List<MoodEntry>> = moodDao.getAllMoods()
    val todayScore: Flow<ConsistencyScore?> = scoreDao.getScoreForDate(getTodayDateString())

    fun get7DayMoods(): Flow<List<MoodEntry>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 86_400_000L
        return moodDao.getMoodsSince(sevenDaysAgo)
    }

    fun get7DayLogs(): Flow<List<ActivityLog>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 86_400_000L
        return activityDao.getLogsSince(sevenDaysAgo)
    }

    suspend fun getEffectiveProfile(): UserProfile {
        return userProfileDao.getUserProfileSync() ?: UserProfile()
    }

    /**
     * Parses free-text user logging input via Gemini AI SDK (or offline heuristic).
     */
    suspend fun parseFreeText(text: String): ParsedLogResult {
        val profile = getEffectiveProfile()
        return geminiService.parseFreeTextLog(text, profile)
    }

    /**
     * Recommends a context-aware micro routine for a given mood.
     */
    suspend fun getRoutineForMood(mood: String, durationMins: Int = 5): MicroRoutine {
        val profile = getEffectiveProfile()
        return geminiService.generateContextualRoutine(mood, durationMins, profile)
    }

    /**
     * Logs an activity and updates daily rhythm consistency.
     */
    suspend fun logActivity(
        name: String,
        durationMins: Int,
        steps: Int = 0,
        tags: List<String> = emptyList(),
        isMicroBreak: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        val profile = getEffectiveProfile()
        val met = CalorieEngine.resolveMet(name, tags)
        val kcal = CalorieEngine.calculateExpenditure(met, durationMins, profile)

        val log = ActivityLog(
            activityName = name,
            durationMins = durationMins,
            estimatedKcal = kcal,
            metValue = met,
            tags = tags,
            steps = steps,
            timestamp = System.currentTimeMillis(),
            isSynced = false,
            isMicroBreak = isMicroBreak
        )
        val id = activityDao.insertLog(log)

        // Check if tags include context dampeners (exam, travel, illness)
        val (hasDampener, dampenerReason) = RhythmEngine.hasContextDampener(tags)
        updateConsistencyScoreAfterActivity(isMicroBreak, hasDampener, dampenerReason)

        SyncWorker.triggerImmediateSync(context)
        return@withContext id
    }

    /**
     * Completes a suggested micro-break routine.
     */
    suspend fun completeMicroRoutine(routine: MicroRoutine) = withContext(Dispatchers.IO) {
        val profile = getEffectiveProfile()
        val kcal = CalorieEngine.calculateExpenditure(routine.metValue, routine.durationMins, profile)

        val log = ActivityLog(
            activityName = routine.title,
            durationMins = routine.durationMins,
            estimatedKcal = kcal,
            metValue = routine.metValue,
            tags = listOf("micro_break", routine.category.lowercase(), routine.targetMood.lowercase()),
            steps = 80,
            timestamp = System.currentTimeMillis(),
            isSynced = false,
            isMicroBreak = true
        )
        activityDao.insertLog(log)
        updateConsistencyScoreAfterActivity(isMicroBreak = true, hasContextDampener = false, dampenerReason = null)
        SyncWorker.triggerImmediateSync(context)
    }

    /**
     * Logs 1-Tap Mood entry.
     */
    suspend fun logMood(moodLevel: Int, moodName: String, note: String? = null) = withContext(Dispatchers.IO) {
        val entry = MoodEntry(
            moodLevel = moodLevel,
            moodName = moodName,
            note = note,
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )
        moodDao.insertMood(entry)
        SyncWorker.triggerImmediateSync(context)
    }

    /**
     * Evaluates whether the user has experienced sustained low/stressed mood for 3+ consecutive days.
     */
    suspend fun checkSustainedLowMoodGuardrail(): Boolean = withContext(Dispatchers.IO) {
        val recentMoods = moodDao.getRecentMoodsSync(20)
        if (recentMoods.size < 3) return@withContext false

        // Group by calendar day string
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val moodsByDay = recentMoods.groupBy { dayFormat.format(Date(it.timestamp)) }
        val sortedDays = moodsByDay.keys.sortedDescending()

        if (sortedDays.size < 3) return@withContext false

        // Check if the most recent 3 distinct days each have a low mood entry (level <= 2: Stressed or Overwhelmed)
        var lowStreak = 0
        for (day in sortedDays.take(3)) {
            val entriesForDay = moodsByDay[day] ?: emptyList()
            if (entriesForDay.any { it.isLowMood }) {
                lowStreak++
            }
        }

        return@withContext lowStreak >= 3
    }

    /**
     * Recalculates today's consistency score based on completed routines and dampeners.
     */
    private suspend fun updateConsistencyScoreAfterActivity(
        isMicroBreak: Boolean,
        hasContextDampener: Boolean,
        dampenerReason: String?
    ) {
        val todayStr = getTodayDateString()
        val existingScore = scoreDao.getScoreForDateSync(todayStr)
        val profile = getEffectiveProfile()

        val prevScoreValue = existingScore?.score ?: profile.baseConsistencyScore
        val prevCompletedCount = existingScore?.completedRoutinesCount ?: 0
        val newCompletedCount = if (isMicroBreak) prevCompletedCount + 1 else prevCompletedCount

        val calculatedScore = RhythmEngine.computeConsistencyScore(
            previousScore = prevScoreValue,
            completedRoutinesToday = if (isMicroBreak) 1 else 0,
            missedDaysCount = 0,
            hasFlaggedContextTag = hasContextDampener
        )

        val updated = ConsistencyScore(
            id = existingScore?.id ?: 0,
            dateStr = todayStr,
            score = calculatedScore,
            completedRoutinesCount = newCompletedCount,
            hasFlaggedDampener = hasContextDampener || (existingScore?.hasFlaggedDampener ?: false),
            dampenerReason = dampenerReason ?: existingScore?.dampenerReason,
            timestamp = System.currentTimeMillis()
        )
        scoreDao.insertOrUpdate(updated)
    }

    // Timetable management
    suspend fun saveTimetableSlot(slot: TimetableSlot): Long = withContext(Dispatchers.IO) {
        val id = timetableDao.insertSlot(slot)
        val updatedSlot = slot.copy(id = id)
        alarmScheduler.scheduleNudgeForSlot(updatedSlot)
        return@withContext id
    }

    suspend fun deleteTimetableSlot(slot: TimetableSlot) = withContext(Dispatchers.IO) {
        alarmScheduler.cancelNudgeForSlot(slot.id)
        timetableDao.deleteSlot(slot)
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.updateProfile(profile)
    }

    suspend fun updateDailyKcalGoal(goal: Int) = withContext(Dispatchers.IO) {
        val current = userProfileDao.getUserProfileSync() ?: UserProfile()
        userProfileDao.insertOrUpdateProfile(current.copy(dailyKcalGoal = goal))
    }

    /**
     * Synchronizes pending activity logs and the latest consistency score to MongoDB online.
     */
    suspend fun syncOnlineMongo(): Boolean = withContext(Dispatchers.IO) {
        val todayStr = getTodayDateString()
        val score = scoreDao.getScoreForDateSync(todayStr)?.score ?: 78.4
        val unsyncedLogs = activityDao.getUnsyncedLogs()
        val microBreaks = activityDao.getTodayLogsSync(getTodayStartTimestamp()).count { it.isMicroBreak }
        val streak = 8

        val success = mongoRepository.syncUserDataOnline(
            currentUserScore = score,
            microBreaksCount = microBreaks,
            streakDays = streak,
            unsyncedLogs = unsyncedLogs
        )

        if (success && unsyncedLogs.isNotEmpty()) {
            activityDao.markAsSynced(unsyncedLogs.map { it.id })
        }
        success
    }

    /**
     * Fetches live community rankings from MongoDB online database.
     */
    suspend fun fetchOnlineLeaderboard(): List<LeaderboardUser> = withContext(Dispatchers.IO) {
        val todayStr = getTodayDateString()
        val score = scoreDao.getScoreForDateSync(todayStr)?.score ?: 78.4
        val microBreaks = activityDao.getTodayLogsSync(getTodayStartTimestamp()).count { it.isMicroBreak }
        val streak = 8

        mongoRepository.fetchOnlineLeaderboard(
            currentUserScore = score,
            currentUserMicroBreaks = microBreaks,
            currentUserStreak = streak
        )
    }

    suspend fun cheerUserOnline(targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        mongoRepository.cheerUserOnline(targetUserId)
    }
}
