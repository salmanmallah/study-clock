package com.ghost.fullscreenclock.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ghost.fullscreenclock.model.ClockMode

class ClockPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("timescreen_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TIME_FORMAT = "clock_time_format"
        private const val KEY_SHOW_SECONDS = "show_seconds"
        private const val KEY_LAST_MODE = "last_clock_mode"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    }

    var is24HourFormat: Boolean
        get() = prefs.getBoolean(KEY_TIME_FORMAT, true)
        set(value) = prefs.edit().putBoolean(KEY_TIME_FORMAT, value).apply()

    var showSeconds: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SECONDS, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_SECONDS, value).apply()

    var lastMode: String
        get() = prefs.getString(KEY_LAST_MODE, ClockMode.DIGITAL_24.name) ?: ClockMode.DIGITAL_24.name
        set(value) = prefs.edit().putString(KEY_LAST_MODE, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()
}
