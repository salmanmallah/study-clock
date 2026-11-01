package com.ghost.fullscreenclock.model

import androidx.annotation.DrawableRes
import com.ghost.fullscreenclock.R

enum class ClockMode(
    val title: String,
    @DrawableRes val iconRes: Int,
    val isLandscape: Boolean = true
) {
    DIGITAL_12(
        title = "12-Hour",
        iconRes = R.drawable.clockpage12,
        isLandscape = true
    ),
    DIGITAL_24(
        title = "24-Hour",
        iconRes = R.drawable.clockpage24,
        isLandscape = true
    ),
    SECONDS_12(
        title = "12H Sec",
        iconRes = R.drawable.secpage12,
        isLandscape = true
    ),
    SECONDS_24(
        title = "24H Sec",
        iconRes = R.drawable.secpage24,
        isLandscape = true
    ),
    FLIP_CLOCK(
        title = "Flip Clock",
        iconRes = R.drawable.flipclock,
        isLandscape = true
    ),
    POMODORO(
        title = "Pomodoro",
        iconRes = R.drawable.pomodoro,
        isLandscape = true
    ),
    STOPWATCH(
        title = "Stopwatch",
        iconRes = R.drawable.stopwatch,
        isLandscape = true
    ),
    TIMER(
        title = "Timer",
        iconRes = R.drawable.timer,
        isLandscape = true
    ),
    SETTINGS(
        title = "Settings",
        iconRes = R.drawable.settings,
        isLandscape = false
    )
}
