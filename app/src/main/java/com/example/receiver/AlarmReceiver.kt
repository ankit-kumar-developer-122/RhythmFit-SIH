package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.NotificationHelper

/**
 * BroadcastReceiver triggered by AlarmManager at the conclusion of a timetable slot
 * to alert the user of an available micro-break gap window.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TIMETABLE_NUDGE = "com.example.ACTION_TIMETABLE_NUDGE"
        const val EXTRA_SLOT_TITLE = "extra_slot_title"
        const val EXTRA_ROUTINE = "extra_routine"
        const val EXTRA_GAP_MINUTES = "extra_gap_minutes"
        const val EXTRA_SLOT_ID = "extra_slot_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TIMETABLE_NUDGE) {
            val slotTitle = intent.getStringExtra(EXTRA_SLOT_TITLE) ?: "Schedule Block"
            val routine = intent.getStringExtra(EXTRA_ROUTINE) ?: "Desk Neck & Shoulder Relief"
            val gapMins = intent.getIntExtra(EXTRA_GAP_MINUTES, 5)
            val slotId = intent.getLongExtra(EXTRA_SLOT_ID, 1L).toInt()

            NotificationHelper.showMicroBreakNudge(
                context = context,
                notificationId = slotId,
                title = slotTitle,
                routineName = routine,
                gapMins = gapMins
            )
        }
    }
}
