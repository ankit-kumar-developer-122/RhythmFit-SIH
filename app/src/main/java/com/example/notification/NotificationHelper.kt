package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * Handles notification channels and builds local timetable micro-break nudges.
 */
object NotificationHelper {

    const val CHANNEL_ID = "rhythmfit_micro_nudges"
    const val CHANNEL_NAME = "Micro-Break Nudges"
    const val EXTRA_ROUTINE_NAME = "extra_routine_name"
    const val EXTRA_GAP_MINS = "extra_gap_mins"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle micro-break reminders scheduled between timetable slots"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showMicroBreakNudge(
        context: Context,
        notificationId: Int,
        title: String,
        routineName: String,
        gapMins: Int
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROUTINE_NAME, routineName)
            putExtra(EXTRA_GAP_MINS, gapMins)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_rhythmfit_logo)
            .setContentTitle("Gap Window Available ($gapMins mins)")
            .setContentText("Finished $title? Try a quick '$routineName' to reset posture & energy.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Finished $title?\nYour schedule has a $gapMins-minute gap. A quick '$routineName' will protect your consistency score and refresh your focus!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
