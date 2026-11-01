package com.ghost.fullscreenclock.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.ghost.fullscreenclock.MainActivity
import com.ghost.fullscreenclock.R

class TimerAlertReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "fullscreen_clock_alerts"
        const val CHANNEL_NAME = "Clock Timer Alerts"
        const val EXTRA_ALERT_TYPE = "alert_type"
        const val TYPE_TIMER = "TIMER"
        const val TYPE_POMODORO = "POMODORO"
        const val NOTIFICATION_ID_TIMER = 1001
        const val NOTIFICATION_ID_POMODORO = 1002

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for Timer and Pomodoro completion alerts"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_ALERT_TYPE) ?: TYPE_TIMER
        createNotificationChannel(context)

        val title = if (type == TYPE_POMODORO) "Pomodoro Completed" else "Timer Finished"
        val message = if (type == TYPE_POMODORO) {
            "Focus session complete! Time for a break."
        } else {
            "Your countdown timer has reached zero."
        }
        val notificationId = if (type == TYPE_POMODORO) NOTIFICATION_ID_POMODORO else NOTIFICATION_ID_TIMER

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        // Trigger vibration
        triggerVibration(context)
    }

    private fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibration = CombinedVibration.createParallel(
                    VibrationEffect.createWaveform(longArrayOf(0, 500, 250, 500, 250, 500), -1)
                )
                vibratorManager?.vibrate(vibration)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 250, 500, 250, 500), -1)
            }
        } catch (_: Exception) {
            // Ignore if vibration fails
        }
    }
}
