package com.ghost.fullscreenclock.time

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalTime

data class ClockTime(
    val hour24: Int,
    val hour12: Int,
    val minute: Int,
    val second: Int,
    val isPm: Boolean
) {
    val formattedHour24: String get() = String.format("%02d", hour24)
    val formattedHour12: String get() = String.format("%02d", if (hour12 == 0) 12 else hour12)
    val formattedMinute: String get() = String.format("%02d", minute)
    val formattedSecond: String get() = String.format("%02d", second)
    val amPmString: String get() = if (isPm) "PM" else "AM"

    companion object {
        fun now(): ClockTime {
            val now = LocalTime.now()
            val hour24 = now.hour
            val hour12 = hour24 % 12
            return ClockTime(
                hour24 = hour24,
                hour12 = hour12,
                minute = now.minute,
                second = now.second,
                isPm = hour24 >= 12
            )
        }
    }
}

fun clockTickerFlow(): Flow<ClockTime> = flow {
    while (true) {
        emit(ClockTime.now())
        val delayMillis = 1000L - (System.currentTimeMillis() % 1000L)
        delay(if (delayMillis <= 0) 1000L else delayMillis)
    }
}.flowOn(Dispatchers.Default)
