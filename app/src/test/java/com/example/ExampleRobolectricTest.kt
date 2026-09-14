package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.RhythmDatabase
import com.example.data.local.entity.ActivityLog
import com.example.data.local.entity.UserProfile
import com.example.engine.CalorieEngine
import com.example.engine.RhythmEngine
import com.example.repository.DailyQuoteRepository
import com.example.repository.RhythmFitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RhythmFit", appName)
    }

    @Test
    fun `test daily kcal goal completion updates reactively from Room as activity logs are added`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = RhythmDatabase.getInstance(context)
        val activityDao = db.activityLogDao()
        val userProfileDao = db.userProfileDao()

        // 1. Seed user profile with 400 kcal target
        userProfileDao.insertOrUpdateProfile(
            UserProfile(id = 1, dailyKcalGoal = 400)
        )
        val profile = userProfileDao.getUserProfileSync()
        assertNotNull(profile)
        assertEquals(400, profile!!.dailyKcalGoal)

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val baselineKcal = activityDao.getTodayLogs(calendar.timeInMillis).first().sumOf { it.estimatedKcal }

        // 2. Insert first activity log into Room
        val log1 = ActivityLog(
            activityName = "Brisk Morning Walk",
            durationMins = 15,
            estimatedKcal = 55,
            timestamp = System.currentTimeMillis()
        )
        activityDao.insertLog(log1)

        val logs1: List<ActivityLog> = activityDao.getTodayLogs(calendar.timeInMillis).first()
        val burned1 = logs1.sumOf { it.estimatedKcal }
        assertEquals(baselineKcal + 55, burned1)

        val progressRatio1 = (burned1.toFloat() / profile.dailyKcalGoal.toFloat()).coerceIn(0f, 1f)
        assertTrue("Progress ratio should be > 0", progressRatio1 > 0f)

        // 3. Insert second activity log into Room
        val log2 = ActivityLog(
            activityName = "HIIT Circuit",
            durationMins = 20,
            estimatedKcal = 145,
            timestamp = System.currentTimeMillis()
        )
        activityDao.insertLog(log2)

        val logs2: List<ActivityLog> = activityDao.getTodayLogs(calendar.timeInMillis).first()
        val burned2 = logs2.sumOf { it.estimatedKcal }
        assertEquals(burned1 + 145, burned2)

        val progressRatio2 = (burned2.toFloat() / profile.dailyKcalGoal.toFloat()).coerceIn(0f, 1f)
        assertTrue("Progress ratio should increase with more logs", progressRatio2 > progressRatio1)
    }

    @Test
    fun `test daily quote repository serves quote once per day and respects dismissal`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = RhythmDatabase.getInstance(context)
        RhythmDatabase.ensureQuotesSeeded(db)

        val quoteRepo = DailyQuoteRepository(context)

        // 1. First fetch for today returns a quote
        val quote1 = quoteRepo.refreshTodayQuote()
        assertNotNull("First quote fetch of the day should return a quote", quote1)

        // 2. Fetching again on the same day returns the identical quote (consistent for the day)
        val quote2 = quoteRepo.refreshTodayQuote()
        assertNotNull(quote2)
        assertEquals("Should serve the same quote throughout the day", quote1?.id, quote2?.id)

        // 3. Dismissing the quote marks it dismissed
        quoteRepo.dismissTodayQuote(quote1!!.id)

        // 4. Fetching again on the same day returns null (never re-annoy the user today)
        val quoteAfterDismiss = quoteRepo.refreshTodayQuote()
        assertNull("Dismissed quote must not be served again for the entire day", quoteAfterDismiss)
    }

    @Test
    fun `test rhythm engine routine bonus and soft decay dampening`() {
        // Routine completion bonus (+4.5)
        val scoreWithBonus = RhythmEngine.computeConsistencyScore(
            previousScore = 70.0,
            completedRoutinesToday = 1,
            missedDaysCount = 0,
            hasFlaggedContextTag = false
        )
        assertEquals(74.5, scoreWithBonus, 0.1)

        // Standard decay: 7.0 penalty
        val standardDecayed = RhythmEngine.computeConsistencyScore(
            previousScore = 70.0,
            completedRoutinesToday = 0,
            missedDaysCount = 1,
            hasFlaggedContextTag = false
        )
        assertEquals(63.0, standardDecayed, 0.1)

        // Dampened decay (e.g. exam or travel): 80% reduction -> penalty is only 1.4
        val dampenedDecayed = RhythmEngine.computeConsistencyScore(
            previousScore = 70.0,
            completedRoutinesToday = 0,
            missedDaysCount = 1,
            hasFlaggedContextTag = true
        )
        assertEquals(68.6, dampenedDecayed, 0.1)
    }

    @Test
    fun `test calorie engine met calculation`() {
        val profile = UserProfile(weightKg = 70.0)
        // 30 min brisk walk (3.8 MET)
        val kcal = CalorieEngine.calculateExpenditure(3.8, 30, profile)
        assertTrue("Kcal burned should be approximately 120-140 kcal", kcal in 110..150)
    }
}

