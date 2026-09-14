package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.entity.TimetableSlot
import com.example.receiver.AlarmReceiver
import java.util.Calendar

/**
 * Schedules exact timetable gap nudges using Android's AlarmManager.
 */
class TimetableAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNudgeForSlot(slot: TimetableSlot) {
        if (!slot.isNudgeEnabled) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, slot.endHour)
            set(Calendar.MINUTE, slot.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TIMETABLE_NUDGE
            putExtra(AlarmReceiver.EXTRA_SLOT_TITLE, slot.title)
            putExtra(AlarmReceiver.EXTRA_ROUTINE, slot.suggestedRoutine)
            putExtra(AlarmReceiver.EXTRA_GAP_MINUTES, slot.gapDurationMins)
            putExtra(AlarmReceiver.EXTRA_SLOT_ID, slot.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slot.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled gap nudge for ${slot.title} at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.w("AlarmScheduler", "Exact alarm permission not granted, using fallback: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelNudgeForSlot(slotId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TIMETABLE_NUDGE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slotId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
