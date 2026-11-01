package com.ghost.fullscreenclock.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CombinedVibration
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ghost.fullscreenclock.BuildConfig
import com.ghost.fullscreenclock.MainActivity
import com.ghost.fullscreenclock.R
import com.ghost.fullscreenclock.receiver.TimerAlertReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

data class LapRecord(
    val lapNumber: Int,
    val splitMillis: Long,
    val totalMillis: Long
)

class TimersViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context get() = getApplication<Application>().applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val prefs = appContext.getSharedPreferences("timers_state_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_TIMER_DEADLINE = "pref_timer_deadline"
        private const val PREF_TIMER_REMAINING = "pref_timer_remaining"
        private const val PREF_TIMER_RUNNING = "pref_timer_running"
        private const val PREF_POMO_DEADLINE = "pref_pomo_deadline"
        private const val PREF_POMO_REMAINING = "pref_pomo_remaining"
        private const val PREF_POMO_RUNNING = "pref_pomo_running"

        const val POMODORO_STANDARD_DURATION = 25 * 60 * 1000L
        const val POMODORO_DEBUG_DURATION = 15 * 1000L // 15 seconds for debug testing
    }

    // ==========================================
    // STOPWATCH STATE & LOGIC
    // ==========================================
    private val _stopwatchRunning = MutableStateFlow(false)
    val stopwatchRunning: StateFlow<Boolean> = _stopwatchRunning.asStateFlow()

    private val _stopwatchElapsedMillis = MutableStateFlow(0L)
    val stopwatchElapsedMillis: StateFlow<Long> = _stopwatchElapsedMillis.asStateFlow()

    private val _stopwatchLaps = MutableStateFlow<List<LapRecord>>(emptyList())
    val stopwatchLaps: StateFlow<List<LapRecord>> = _stopwatchLaps.asStateFlow()

    private var stopwatchStartTime = 0L
    private var stopwatchAccumulated = 0L
    private var stopwatchJob: Job? = null

    fun toggleStopwatch() {
        if (_stopwatchRunning.value) {
            pauseStopwatch()
        } else {
            startStopwatch()
        }
    }

    fun startStopwatch() {
        if (_stopwatchRunning.value) return
        _stopwatchRunning.value = true
        stopwatchStartTime = SystemClock.elapsedRealtime()

        stopwatchJob = viewModelScope.launch {
            while (isActive && _stopwatchRunning.value) {
                val now = SystemClock.elapsedRealtime()
                _stopwatchElapsedMillis.value = stopwatchAccumulated + (now - stopwatchStartTime)
                delay(16L) // ~60fps smooth render
            }
        }
    }

    fun pauseStopwatch() {
        if (!_stopwatchRunning.value) return
        _stopwatchRunning.value = false
        stopwatchJob?.cancel()
        stopwatchJob = null
        stopwatchAccumulated += (SystemClock.elapsedRealtime() - stopwatchStartTime)
        _stopwatchElapsedMillis.value = stopwatchAccumulated
    }

    fun recordStopwatchLap() {
        // Priority 1 requirement: Compute accumulated + (elapsedRealtime() - start) at the exact moment of tap
        val currentTotal = if (_stopwatchRunning.value) {
            stopwatchAccumulated + (SystemClock.elapsedRealtime() - stopwatchStartTime)
        } else {
            stopwatchAccumulated
        }
        val previousTotal = _stopwatchLaps.value.firstOrNull()?.totalMillis ?: 0L
        val split = (currentTotal - previousTotal).coerceAtLeast(0L)
        val newLap = LapRecord(
            lapNumber = _stopwatchLaps.value.size + 1,
            splitMillis = split,
            totalMillis = currentTotal
        )
        _stopwatchLaps.value = listOf(newLap) + _stopwatchLaps.value
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = null
        _stopwatchRunning.value = false
        stopwatchStartTime = 0L
        stopwatchAccumulated = 0L
        _stopwatchElapsedMillis.value = 0L
        _stopwatchLaps.value = emptyList()
    }


    // ==========================================
    // COUNTDOWN TIMER STATE & LOGIC
    // ==========================================
    private val _timerSetSeconds = MutableStateFlow(5 * 60)
    val timerSetSeconds: StateFlow<Int> = _timerSetSeconds.asStateFlow()

    private val _timerRemainingMillis = MutableStateFlow(5 * 60 * 1000L)
    val timerRemainingMillis: StateFlow<Long> = _timerRemainingMillis.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _canAdjustTimer = MutableStateFlow(true)
    val canAdjustTimer: StateFlow<Boolean> = _canAdjustTimer.asStateFlow()

    private var timerDeadline = 0L
    private var timerJob: Job? = null

    private fun updateCanAdjust() {
        val setTotal = _timerSetSeconds.value * 1000L
        _canAdjustTimer.value = !_timerRunning.value && (_timerRemainingMillis.value == setTotal)
    }

    /** Adds (or subtracts) seconds to the preset duration. Range: 5s .. 99h59m59s. */
    fun adjustTimerSeconds(deltaSeconds: Int) {
        if (!_canAdjustTimer.value) return
        setTimerDurationSeconds(_timerSetSeconds.value + deltaSeconds)
    }

    /** Sets the preset duration directly (e.g. from quick preset chips). */
    fun setTimerDurationSeconds(seconds: Int) {
        if (!_canAdjustTimer.value) return
        val updated = seconds.coerceIn(5, 99 * 3600 + 59 * 60 + 59)
        _timerSetSeconds.value = updated
        _timerRemainingMillis.value = updated * 1000L
        updateCanAdjust()
    }

    fun toggleTimer() {
        if (_timerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        if (_timerRunning.value) return
        if (_timerRemainingMillis.value <= 0L) {
            _timerRemainingMillis.value = _timerSetSeconds.value * 1000L
        }
        _timerRunning.value = true
        updateCanAdjust()

        timerDeadline = SystemClock.elapsedRealtime() + _timerRemainingMillis.value
        val triggerWallMillis = System.currentTimeMillis() + _timerRemainingMillis.value
        scheduleAlarm(TimerAlertReceiver.TYPE_TIMER, triggerWallMillis)

        persistTimerState(running = true, deadline = triggerWallMillis, remaining = _timerRemainingMillis.value)

        timerJob = viewModelScope.launch {
            while (isActive && _timerRunning.value) {
                val now = SystemClock.elapsedRealtime()
                val rem = timerDeadline - now
                if (rem <= 0L) {
                    _timerRemainingMillis.value = 0L
                    _timerRunning.value = false
                    updateCanAdjust()
                    cancelAlarm(TimerAlertReceiver.TYPE_TIMER)
                    clearTimerPersistence()
                    triggerInAppAlert(TimerAlertReceiver.TYPE_TIMER)
                    break
                } else {
                    _timerRemainingMillis.value = rem
                }
                delay(50L)
            }
        }
    }

    fun pauseTimer() {
        if (!_timerRunning.value) return
        _timerRunning.value = false
        timerJob?.cancel()
        timerJob = null
        val now = SystemClock.elapsedRealtime()
        _timerRemainingMillis.value = max(0L, timerDeadline - now)
        cancelAlarm(TimerAlertReceiver.TYPE_TIMER)
        persistTimerState(running = false, deadline = 0L, remaining = _timerRemainingMillis.value)
        updateCanAdjust()
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerRunning.value = false
        cancelAlarm(TimerAlertReceiver.TYPE_TIMER)
        clearTimerPersistence()
        _timerRemainingMillis.value = _timerSetSeconds.value * 1000L
        updateCanAdjust()
    }


    // ==========================================
    // POMODORO STATE & LOGIC
    // ==========================================
    var useDebugPomodoroDuration: Boolean = false // Debug-only flag for short duration verification

    val pomodoroTotalMillis: Long get() = pomodoroTargetDuration

    private val pomodoroTargetDuration: Long
        get() = if (BuildConfig.DEBUG && useDebugPomodoroDuration) POMODORO_DEBUG_DURATION else POMODORO_STANDARD_DURATION

    private val _pomodoroRemainingMillis = MutableStateFlow(POMODORO_STANDARD_DURATION)
    val pomodoroRemainingMillis: StateFlow<Long> = _pomodoroRemainingMillis.asStateFlow()

    private val _pomodoroRunning = MutableStateFlow(false)
    val pomodoroRunning: StateFlow<Boolean> = _pomodoroRunning.asStateFlow()

    private val _pomodoroCompletedSessions = MutableStateFlow(0)
    val pomodoroCompletedSessions: StateFlow<Int> = _pomodoroCompletedSessions.asStateFlow()

    private var pomodoroDeadline = 0L
    private var pomodoroJob: Job? = null

    fun setDebugPomodoroMode(enabled: Boolean) {
        if (!BuildConfig.DEBUG) return
        useDebugPomodoroDuration = enabled
        if (!_pomodoroRunning.value) {
            _pomodoroRemainingMillis.value = pomodoroTargetDuration
        }
    }

    fun togglePomodoro() {
        if (_pomodoroRunning.value) {
            pausePomodoro()
        } else {
            startPomodoro()
        }
    }

    fun startPomodoro() {
        if (_pomodoroRunning.value) return
        if (_pomodoroRemainingMillis.value <= 0L) {
            _pomodoroRemainingMillis.value = pomodoroTargetDuration
        }
        _pomodoroRunning.value = true
        pomodoroDeadline = SystemClock.elapsedRealtime() + _pomodoroRemainingMillis.value
        val triggerWallMillis = System.currentTimeMillis() + _pomodoroRemainingMillis.value
        scheduleAlarm(TimerAlertReceiver.TYPE_POMODORO, triggerWallMillis)

        persistPomoState(running = true, deadline = triggerWallMillis, remaining = _pomodoroRemainingMillis.value)

        pomodoroJob = viewModelScope.launch {
            while (isActive && _pomodoroRunning.value) {
                val now = SystemClock.elapsedRealtime()
                val rem = pomodoroDeadline - now
                if (rem <= 0L) {
                    _pomodoroRemainingMillis.value = 0L
                    _pomodoroRunning.value = false
                    _pomodoroCompletedSessions.value += 1
                    cancelAlarm(TimerAlertReceiver.TYPE_POMODORO)
                    clearPomoPersistence()
                    triggerInAppAlert(TimerAlertReceiver.TYPE_POMODORO)
                    break
                } else {
                    _pomodoroRemainingMillis.value = rem
                }
                delay(100L)
            }
        }
    }

    fun pausePomodoro() {
        if (!_pomodoroRunning.value) return
        _pomodoroRunning.value = false
        pomodoroJob?.cancel()
        pomodoroJob = null
        val now = SystemClock.elapsedRealtime()
        _pomodoroRemainingMillis.value = max(0L, pomodoroDeadline - now)
        cancelAlarm(TimerAlertReceiver.TYPE_POMODORO)
        persistPomoState(running = false, deadline = 0L, remaining = _pomodoroRemainingMillis.value)
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        pomodoroJob = null
        _pomodoroRunning.value = false
        cancelAlarm(TimerAlertReceiver.TYPE_POMODORO)
        clearPomoPersistence()
        _pomodoroRemainingMillis.value = pomodoroTargetDuration
    }

    fun resetPomodoroSessions() {
        _pomodoroCompletedSessions.value = 0
    }


    // ==========================================
    // ALARM & ALERT MANAGEMENT
    // ==========================================
    private fun getPendingIntent(type: String): PendingIntent {
        val intent = Intent(appContext, TimerAlertReceiver::class.java).apply {
            putExtra(TimerAlertReceiver.EXTRA_ALERT_TYPE, type)
        }
        val requestCode = if (type == TimerAlertReceiver.TYPE_POMODORO) 2002 else 2001
        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleAlarm(type: String, triggerWallMillis: Long) {
        val pi = getPendingIntent(type)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerWallMillis, pi)
            } else {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerWallMillis, pi)
            }
        } catch (_: SecurityException) {
            // Fallback for missing exact alarm permission
            alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerWallMillis, pi)
        }
    }

    private fun cancelAlarm(type: String) {
        val pi = getPendingIntent(type)
        try {
            alarmManager?.cancel(pi)
        } catch (_: Exception) {}
    }

    private fun triggerInAppAlert(type: String) {
        // In-app alert triggers notification and vibration exactly once
        val title = if (type == TimerAlertReceiver.TYPE_POMODORO) "Pomodoro Completed" else "Timer Finished"
        val message = if (type == TimerAlertReceiver.TYPE_POMODORO) {
            "Focus session complete! Time for a break."
        } else {
            "Your countdown timer has reached zero."
        }
        val notificationId = if (type == TimerAlertReceiver.TYPE_POMODORO) {
            TimerAlertReceiver.NOTIFICATION_ID_POMODORO
        } else {
            TimerAlertReceiver.NOTIFICATION_ID_TIMER
        }

        val launchIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, TimerAlertReceiver.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        triggerVibration()
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibration = CombinedVibration.createParallel(
                    VibrationEffect.createWaveform(longArrayOf(0, 500, 250, 500, 250, 500), -1)
                )
                vibratorManager?.vibrate(vibration)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 250, 500, 250, 500), -1)
            }
        } catch (_: Exception) {}
    }


    // ==========================================
    // PERSISTENCE (PROCESS DEATH RECOVERY)
    // ==========================================
    private fun persistTimerState(running: Boolean, deadline: Long, remaining: Long) {
        prefs.edit()
            .putBoolean(PREF_TIMER_RUNNING, running)
            .putLong(PREF_TIMER_DEADLINE, deadline)
            .putLong(PREF_TIMER_REMAINING, remaining)
            .apply()
    }

    private fun clearTimerPersistence() {
        prefs.edit()
            .remove(PREF_TIMER_RUNNING)
            .remove(PREF_TIMER_DEADLINE)
            .remove(PREF_TIMER_REMAINING)
            .apply()
    }

    private fun persistPomoState(running: Boolean, deadline: Long, remaining: Long) {
        prefs.edit()
            .putBoolean(PREF_POMO_RUNNING, running)
            .putLong(PREF_POMO_DEADLINE, deadline)
            .putLong(PREF_POMO_REMAINING, remaining)
            .apply()
    }

    private fun clearPomoPersistence() {
        prefs.edit()
            .remove(PREF_POMO_RUNNING)
            .remove(PREF_POMO_DEADLINE)
            .remove(PREF_POMO_REMAINING)
            .apply()
    }

    private fun restorePersistedTimers() {
        val nowWall = System.currentTimeMillis()

        // Restore Timer
        val timerWasRunning = prefs.getBoolean(PREF_TIMER_RUNNING, false)
        val savedTimerDeadline = prefs.getLong(PREF_TIMER_DEADLINE, 0L)
        val savedTimerRemaining = prefs.getLong(PREF_TIMER_REMAINING, 5 * 60 * 1000L)

        if (timerWasRunning && savedTimerDeadline > 0L) {
            val rem = savedTimerDeadline - nowWall
            if (rem > 0L) {
                _timerRemainingMillis.value = rem
                startTimer()
            } else {
                _timerRemainingMillis.value = 0L
                _timerRunning.value = false
            }
        } else {
            _timerRemainingMillis.value = savedTimerRemaining
        }

        // Restore Pomodoro
        val pomoWasRunning = prefs.getBoolean(PREF_POMO_RUNNING, false)
        val savedPomoDeadline = prefs.getLong(PREF_POMO_DEADLINE, 0L)
        val savedPomoRemaining = prefs.getLong(PREF_POMO_REMAINING, pomodoroTargetDuration)

        if (pomoWasRunning && savedPomoDeadline > 0L) {
            val rem = savedPomoDeadline - nowWall
            if (rem > 0L) {
                _pomodoroRemainingMillis.value = rem
                startPomodoro()
            } else {
                _pomodoroRemainingMillis.value = 0L
                _pomodoroRunning.value = false
            }
        } else {
            _pomodoroRemainingMillis.value = savedPomoRemaining
        }

        updateCanAdjust()
    }

    init {
        TimerAlertReceiver.createNotificationChannel(appContext)
        restorePersistedTimers()
    }

    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
        timerJob?.cancel()
        pomodoroJob?.cancel()
    }
}
