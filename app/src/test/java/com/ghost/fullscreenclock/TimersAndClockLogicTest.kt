package com.ghost.fullscreenclock

import com.ghost.fullscreenclock.model.ClockMode
import com.ghost.fullscreenclock.time.ClockTime
import com.ghost.fullscreenclock.ui.viewmodel.LapRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimersAndClockLogicTest {

    @Test
    fun `ClockTime formats 12-hour and 24-hour components accurately`() {
        val midnight = ClockTime(hour24 = 0, hour12 = 0, minute = 5, second = 9, isPm = false)
        assertEquals("00", midnight.formattedHour24)
        assertEquals("12", midnight.formattedHour12)
        assertEquals("05", midnight.formattedMinute)
        assertEquals("09", midnight.formattedSecond)
        assertEquals("AM", midnight.amPmString)

        val afternoon = ClockTime(hour24 = 15, hour12 = 3, minute = 42, second = 30, isPm = true)
        assertEquals("15", afternoon.formattedHour24)
        assertEquals("03", afternoon.formattedHour12)
        assertEquals("42", afternoon.formattedMinute)
        assertEquals("30", afternoon.formattedSecond)
        assertEquals("PM", afternoon.amPmString)
    }

    @Test
    fun `ClockMode landscape orientations match expected desk modes`() {
        assertTrue(ClockMode.DIGITAL_12.isLandscape)
        assertTrue(ClockMode.DIGITAL_24.isLandscape)
        assertTrue(ClockMode.SECONDS_12.isLandscape)
        assertTrue(ClockMode.SECONDS_24.isLandscape)
        assertTrue(ClockMode.FLIP_CLOCK.isLandscape)
        assertTrue(ClockMode.POMODORO.isLandscape)
        assertTrue(ClockMode.STOPWATCH.isLandscape)
        assertTrue(ClockMode.TIMER.isLandscape)
        assertFalse(ClockMode.SETTINGS.isLandscape)
    }

    @Test
    fun `LapRecord stores split and cumulative total milliseconds`() {
        val lap1 = LapRecord(lapNumber = 1, splitMillis = 1250L, totalMillis = 1250L)
        val lap2 = LapRecord(lapNumber = 2, splitMillis = 980L, totalMillis = 2230L)
        assertEquals(1, lap1.lapNumber)
        assertEquals(2, lap2.lapNumber)
        assertEquals(lap1.totalMillis + lap2.splitMillis, lap2.totalMillis)
    }
}
