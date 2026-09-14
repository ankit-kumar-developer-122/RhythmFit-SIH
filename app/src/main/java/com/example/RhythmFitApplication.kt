package com.example

import android.app.Application
import com.example.data.local.RhythmDatabase
import com.example.notification.NotificationHelper
import com.example.notification.TimetableAlarmScheduler
import com.example.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RhythmFitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        NotificationHelper.createNotificationChannel(this)

        // Schedule periodic sync via WorkManager safely
        try {
            SyncWorker.schedulePeriodicSync(this)
        } catch (e: Exception) {
            // In unit testing environments WorkManager might not be initialized
        }

        // Ensure timetable alarms are scheduled
        CoroutineScope(Dispatchers.IO).launch {
            val db = RhythmDatabase.getInstance(this@RhythmFitApplication)
            val slots = db.timetableDao().getAllSlotsSync()
            val scheduler = TimetableAlarmScheduler(this@RhythmFitApplication)
            for (slot in slots) {
                if (slot.isNudgeEnabled) {
                    scheduler.scheduleNudgeForSlot(slot)
                }
            }
        }
    }
}
