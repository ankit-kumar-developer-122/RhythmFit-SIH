package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ActivityLogDao
import com.example.data.local.dao.ConsistencyScoreDao
import com.example.data.local.dao.DailyQuoteDao
import com.example.data.local.dao.MoodDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.ActivityLog
import com.example.data.local.entity.ConsistencyScore
import com.example.data.local.entity.DailyQuoteEntity
import com.example.data.local.entity.MoodEntry
import com.example.data.local.entity.TimetableSlot
import com.example.data.local.entity.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MODULE 1: Room Database single source of truth for RhythmFit.
 */
@Database(
    entities = [
        ActivityLog::class,
        TimetableSlot::class,
        MoodEntry::class,
        UserProfile::class,
        ConsistencyScore::class,
        DailyQuoteEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RhythmDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun timetableDao(): TimetableDao
    abstract fun moodDao(): MoodDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun consistencyScoreDao(): ConsistencyScoreDao
    abstract fun dailyQuoteDao(): DailyQuoteDao

    companion object {
        @Volatile
        private var INSTANCE: RhythmDatabase? = null

        fun getInstance(context: Context): RhythmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RhythmDatabase::class.java,
                    "rhythm_fit.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed baseline data on separate coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                seedInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                // Also ensure quotes are seeded if empty
                CoroutineScope(Dispatchers.IO).launch {
                    ensureQuotesSeeded(instance)
                }
                instance
            }
        }

        suspend fun ensureQuotesSeeded(database: RhythmDatabase) {
            val quoteDao = database.dailyQuoteDao()
            if (quoteDao.getCount() == 0) {
                quoteDao.insertQuotes(defaultQuotesList())
            }
        }

        private fun defaultQuotesList(): List<DailyQuoteEntity> = listOf(
            DailyQuoteEntity(
                quote = "May your day be filled with steady breath, gentle strength, and peaceful clarity. You don't have to be extreme, just consistently kind to your body.",
                author = "Dr. Elena Rostova",
                greetingContext = "Daily Harmony Wish",
                theme = "Kind Consistency",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "Small 2-minute pauses are not a delay in your day; they are the calm anchor that protects your focus from burnout.",
                author = "RhythmFit Mind Philosophy",
                greetingContext = "Mindful Focus Wish",
                theme = "Micro-Rhythms",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "Wishing you steady energy that outlasts the morning rush and a calm heart that welcomes every opportunity to stretch and renew.",
                author = "Marcus Aurelius inspired",
                greetingContext = "Vitality & Strength",
                theme = "Enduring Flow",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "May you find joy in simply showing up. Consistency is quiet momentum building mountains one small pebble at a time.",
                author = "James Clear inspired",
                greetingContext = "Momentum Wish",
                theme = "Quiet Power",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "Health is about honoring seasons of rest as deeply as seasons of movement. Breathe deeply, move gently, and trust your progress.",
                author = "Somatic Wellness Guide",
                greetingContext = "Gentle Healing Wish",
                theme = "Grace & Renewal",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "May your thoughts be light and your footsteps purposeful today. One mindful stretch can transform your entire afternoon.",
                author = "Mind-Body Alliance",
                greetingContext = "Midday Rejuvenation",
                theme = "Clarity & Lift",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "Listen to the quiet signals your body whispers before they become shouts. May today bring you balance, ease, and sustained vitality.",
                author = "Dr. Maya Lin",
                greetingContext = "Intuitive Wellness Wish",
                theme = "Body Listening",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "You are doing better than you realize. Take this moment to unclench your jaw, soften your shoulders, and celebrate yourself.",
                author = "Gentle Health Collective",
                greetingContext = "Compassion & Peace",
                theme = "Self-Compassion",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "Energy flows where attention goes. May you direct your breath into spaces of tension and discover renewed freedom of movement.",
                author = "Yoga & Posture Arts",
                greetingContext = "Energetic Flow Wish",
                theme = "Breath & Release",
                affirmationBenefit = "+15 Consistency XP"
            ),
            DailyQuoteEntity(
                quote = "True strength is not rigid tension, but fluid adaptability. May your muscles stay supple, your spine tall, and your spirits high.",
                author = "Movement Biomechanics Hub",
                greetingContext = "Resilience & Posture",
                theme = "Supple Strength",
                affirmationBenefit = "+15 Consistency XP"
            )
        )

        private suspend fun seedInitialData(database: RhythmDatabase) {
            val userProfileDao = database.userProfileDao()
            val timetableDao = database.timetableDao()
            val scoreDao = database.consistencyScoreDao()
            val activityDao = database.activityLogDao()
            val quoteDao = database.dailyQuoteDao()

            // Seed quotes
            if (quoteDao.getCount() == 0) {
                quoteDao.insertQuotes(defaultQuotesList())
            }

            // Seed user profile
            userProfileDao.insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    name = "Alex",
                    weightKg = 68.0,
                    heightCm = 172.0,
                    age = 24,
                    gender = "Female",
                    baseConsistencyScore = 72.0,
                    dailyStepGoal = 8000,
                    dailyKcalGoal = 450
                )
            )

            // Seed default daily slots
            val defaultSlots = listOf(
                TimetableSlot(
                    title = "Deep Focus Block 1",
                    startHour = 9,
                    startMinute = 0,
                    endHour = 10,
                    endMinute = 30,
                    gapDurationMins = 5,
                    suggestedRoutine = "Desk Neck & Shoulder Relief",
                    isNudgeEnabled = true
                ),
                TimetableSlot(
                    title = "Class / Team Sprint",
                    startHour = 11,
                    startMinute = 0,
                    endHour = 12,
                    endMinute = 30,
                    gapDurationMins = 4,
                    suggestedRoutine = "4-7-8 Breathing Reset",
                    isNudgeEnabled = true
                ),
                TimetableSlot(
                    title = "Study & Project Work",
                    startHour = 14,
                    startMinute = 0,
                    endHour = 15,
                    endMinute = 30,
                    gapDurationMins = 5,
                    suggestedRoutine = "Lower Back & Hamstring Loosener",
                    isNudgeEnabled = true
                ),
                TimetableSlot(
                    title = "Afternoon Winddown",
                    startHour = 17,
                    startMinute = 0,
                    endHour = 18,
                    endMinute = 0,
                    gapDurationMins = 3,
                    suggestedRoutine = "Wrist & Posture Reset",
                    isNudgeEnabled = true
                )
            )
            timetableDao.insertAll(defaultSlots)

            // Seed baseline consistency score
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            scoreDao.insertOrUpdate(
                ConsistencyScore(
                    dateStr = todayStr,
                    score = 72.0,
                    completedRoutinesCount = 1,
                    hasFlaggedDampener = false,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Seed an initial starter micro-break
            activityDao.insertLog(
                ActivityLog(
                    activityName = "Morning Posture Realignment",
                    durationMins = 4,
                    estimatedKcal = 16,
                    metValue = 3.2,
                    tags = listOf("routine", "morning_start"),
                    steps = 120,
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    isSynced = true,
                    isMicroBreak = true
                )
            )

            // Seed historical 7-day mood entries and activity logs for pattern analysis
            val moodDao = database.moodDao()
            val now = System.currentTimeMillis()
            val oneDayMs = 86_400_000L

            if (moodDao.getRecentMoodsSync(1).isEmpty()) {
                val historicalMoods = listOf(
                    MoodEntry(moodLevel = 5, moodName = "Energized", timestamp = now - 6 * oneDayMs, note = "High energy after morning run"),
                    MoodEntry(moodLevel = 4, moodName = "Neutral", timestamp = now - 5 * oneDayMs, note = "Steady focus and productivity"),
                    MoodEntry(moodLevel = 2, moodName = "Stressed", timestamp = now - 4 * oneDayMs, note = "Heavy workload & meetings"),
                    MoodEntry(moodLevel = 3, moodName = "Tired", timestamp = now - 3 * oneDayMs, note = "Low sleep, took micro-stretch"),
                    MoodEntry(moodLevel = 4, moodName = "Neutral", timestamp = now - 2 * oneDayMs, note = "Rebounded after evening walk"),
                    MoodEntry(moodLevel = 5, moodName = "Energized", timestamp = now - 1 * oneDayMs, note = "Super active day, felt great"),
                    MoodEntry(moodLevel = 4, moodName = "Neutral", timestamp = now - 3600_000L * 3, note = "Balanced calm rhythm")
                )
                moodDao.insertAll(historicalMoods)

                val historicalLogs = listOf(
                    ActivityLog(activityName = "Morning 5K Jog", durationMins = 28, estimatedKcal = 220, metValue = 7.0, timestamp = now - 6 * oneDayMs + 3600_000L * 8, steps = 3400),
                    ActivityLog(activityName = "Core & Mobility Flow", durationMins = 18, estimatedKcal = 95, metValue = 4.2, timestamp = now - 5 * oneDayMs + 3600_000L * 12, steps = 600),
                    ActivityLog(activityName = "Desk Posture Stretch", durationMins = 5, estimatedKcal = 18, metValue = 2.5, timestamp = now - 4 * oneDayMs + 3600_000L * 15, steps = 90, isMicroBreak = true),
                    ActivityLog(activityName = "4-7-8 Breathing Reset", durationMins = 4, estimatedKcal = 12, metValue = 2.0, timestamp = now - 3 * oneDayMs + 3600_000L * 14, steps = 40, isMicroBreak = true),
                    ActivityLog(activityName = "Brisk Park Walk", durationMins = 22, estimatedKcal = 115, metValue = 3.8, timestamp = now - 2 * oneDayMs + 3600_000L * 17, steps = 2400),
                    ActivityLog(activityName = "HIIT Circuit & Cooldown", durationMins = 32, estimatedKcal = 275, metValue = 8.0, timestamp = now - 1 * oneDayMs + 3600_000L * 10, steps = 3600)
                )
                activityDao.insertAll(historicalLogs)
            }
        }
    }
}
