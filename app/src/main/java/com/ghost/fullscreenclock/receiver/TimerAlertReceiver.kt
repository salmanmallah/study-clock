package com.ghost.fullscreenclock.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.ghost.fullscreenclock.MainActivity
import com.ghost.fullscreenclock.R
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin

class TimerAlertReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "fullscreen_clock_alerts"
        const val CHANNEL_NAME = "Clock Timer Alerts"
        const val EXTRA_ALERT_TYPE = "alert_type"
        const val TYPE_TIMER = "TIMER"
        const val TYPE_POMODORO = "POMODORO"
        const val NOTIFICATION_ID_TIMER = 1001
        const val NOTIFICATION_ID_POMODORO = 1002

        private const val DEDUP_WINDOW_MS = 3000L
        private val lastAlertElapsedByType = ConcurrentHashMap<String, Long>()

        private val isBeeping = AtomicBoolean(false)
        @Volatile
        private var activeThread: Thread? = null

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

        /**
         * Single coordinated alert dispatcher with a 3-second deduplication guard per timer type.
         * Prevents duplicate notifications, vibration, and beep bursts if both the foreground
         * coroutine and AlarmManager broadcast fire at the same deadline.
         */
        @Synchronized
        fun dispatchAlert(context: Context, type: String): Boolean {
            val nowElapsed = SystemClock.elapsedRealtime()
            val lastElapsed = lastAlertElapsedByType[type] ?: 0L
            if (lastElapsed > 0L && (nowElapsed - lastElapsed) in 0 until DEDUP_WINDOW_MS) {
                return false
            }
            lastAlertElapsedByType[type] = nowElapsed

            createNotificationChannel(context)

            val title = if (type == TYPE_POMODORO) "Pomodoro Completed" else "Timer Finished"
            val message = if (type == TYPE_POMODORO) {
                "Focus session complete! Time for a break."
            } else {
                "Your countdown timer has reached zero."
            }
            val notificationId = if (type == TYPE_POMODORO) {
                NOTIFICATION_ID_POMODORO
            } else {
                NOTIFICATION_ID_TIMER
            }

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

            triggerVibration(context)
            playBeepPattern()
            return true
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

        /**
         * Plays a classic digital clock "beep-beep ... beep-beep ..." alarm pattern
         * over the ALARM audio stream so it is clearly audible when the timer ends.
         */
        fun playBeepPattern() {
            stopBeepPattern()
            isBeeping.set(true)
            val thread = Thread {
                var audioTrack: AudioTrack? = null
                var toneGen: ToneGenerator? = null
                try {
                    val sampleRate = 44100
                    val beepSamples = buildBeepWaveform(
                        sampleRate = sampleRate,
                        freqHz = 1046.5, // C6 digital alarm tone
                        beepMs = 130,
                        gapMs = 90
                    )
                    val bufferBytes = beepSamples.size * 2
                    audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferBytes)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    audioTrack.write(beepSamples, 0, beepSamples.size)
                    toneGen = try {
                        ToneGenerator(AudioManager.STREAM_ALARM, 95)
                    } catch (_: Exception) {
                        null
                    }

                    // Play 4 double-beep bursts ("beep-beep ... beep-beep ...")
                    for (burst in 0 until 4) {
                        if (!isBeeping.get() || Thread.currentThread().isInterrupted) break
                        try {
                            audioTrack.stop()
                            audioTrack.reloadStaticData()
                            audioTrack.play()
                        } catch (_: Exception) {
                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 260)
                        }
                        Thread.sleep(720L)
                    }
                } catch (_: InterruptedException) {
                    // Cancelled cleanly
                } catch (_: Exception) {
                    // Ignore audio hardware exceptions
                } finally {
                    try {
                        audioTrack?.stop()
                        audioTrack?.release()
                    } catch (_: Exception) {}
                    try {
                        toneGen?.release()
                    } catch (_: Exception) {}
                    isBeeping.set(false)
                }
            }.apply {
                isDaemon = true
                name = "TimerBeepThread"
                start()
            }
            activeThread = thread
        }

        fun stopBeepPattern() {
            isBeeping.set(false)
            activeThread?.interrupt()
            activeThread = null
        }

        /**
         * Builds a 16-bit PCM buffer containing two crisp digital beeps ("beep-beep")
         * with a 5ms fade-in/out envelope to prevent speaker clicks.
         */
        private fun buildBeepWaveform(
            sampleRate: Int,
            freqHz: Double,
            beepMs: Int,
            gapMs: Int
        ): ShortArray {
            val beepLen = (sampleRate * beepMs) / 1000
            val gapLen = (sampleRate * gapMs) / 1000
            val totalLen = beepLen * 2 + gapLen
            val fadeLen = (sampleRate * 5) / 1000
            val out = ShortArray(totalLen)

            fun writeBeep(offset: Int) {
                for (i in 0 until beepLen) {
                    val env = when {
                        i < fadeLen -> i.toDouble() / fadeLen
                        i > beepLen - fadeLen -> (beepLen - i).toDouble() / fadeLen
                        else -> 1.0
                    }
                    // Fundamental + light 3rd harmonic for an authentic digital clock timbre
                    val angle = 2.0 * PI * freqHz * i / sampleRate
                    val wave = 0.82 * sin(angle) + 0.18 * sin(3.0 * angle)
                    val sample = (wave * env * Short.MAX_VALUE * 0.85).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    out[offset + i] = sample.toShort()
                }
            }

            writeBeep(0)
            writeBeep(beepLen + gapLen)
            return out
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_ALERT_TYPE) ?: TYPE_TIMER
        dispatchAlert(context, type)
    }
}
