package com.ghost.fullscreenclock.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghost.fullscreenclock.model.ClockMode
import com.ghost.fullscreenclock.preferences.ClockPreferences
import com.ghost.fullscreenclock.time.ClockTime
import com.ghost.fullscreenclock.time.clockTickerFlow
import com.ghost.fullscreenclock.ui.components.BottomModeDrawer
import com.ghost.fullscreenclock.ui.components.SideDrawerHandle
import com.ghost.fullscreenclock.ui.screens.DigitalClockScreen
import com.ghost.fullscreenclock.ui.screens.FlipClockScreen
import com.ghost.fullscreenclock.ui.screens.PomodoroScreen
import com.ghost.fullscreenclock.ui.screens.SettingsScreen
import com.ghost.fullscreenclock.ui.screens.StopwatchScreen
import com.ghost.fullscreenclock.ui.screens.TimerScreen
import com.ghost.fullscreenclock.ui.viewmodel.TimersViewModel
import kotlinx.coroutines.delay

@Composable
fun ClockApp(
    prefs: ClockPreferences,
    onKeepScreenOnChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    timersViewModel: TimersViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var is24Hour by remember { mutableStateOf(prefs.is24HourFormat) }
    var showSeconds by remember { mutableStateOf(prefs.showSeconds) }
    var keepScreenOn by remember { mutableStateOf(prefs.keepScreenOn) }

    // Resolve initial mode from preferences
    val initialMode = remember {
        try {
            val saved = ClockMode.valueOf(prefs.lastMode)
            if (saved == ClockMode.SETTINGS) {
                if (prefs.showSeconds) {
                    if (prefs.is24HourFormat) ClockMode.SECONDS_24 else ClockMode.SECONDS_12
                } else {
                    if (prefs.is24HourFormat) ClockMode.DIGITAL_24 else ClockMode.DIGITAL_12
                }
            } else saved
        } catch (_: Exception) {
            ClockMode.DIGITAL_24
        }
    }

    var currentMode by remember { mutableStateOf(initialMode) }
    var previousClockMode by remember { mutableStateOf(initialMode) }

    // Synchronize initial is24Hour and showSeconds if digital mode was saved
    LaunchedEffect(Unit) {
        when (initialMode) {
            ClockMode.DIGITAL_12 -> {
                is24Hour = false
                showSeconds = false
            }
            ClockMode.DIGITAL_24 -> {
                is24Hour = true
                showSeconds = false
            }
            ClockMode.SECONDS_12 -> {
                is24Hour = false
                showSeconds = true
            }
            ClockMode.SECONDS_24 -> {
                is24Hour = true
                showSeconds = true
            }
            else -> {}
        }
    }

    // Unified controls visibility (both top bar and bottom mode drawer appear together)
    var showControls by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }

    fun registerInteraction() {
        lastInteractionTime = System.currentTimeMillis()
    }

    // Auto-hide controls after 3 seconds of inactivity
    LaunchedEffect(showControls, lastInteractionTime) {
        if (showControls) {
            delay(3000L)
            showControls = false
        }
    }

    // Collect clock ticker aligned with wall-clock seconds
    val clockTime by clockTickerFlow().collectAsStateWithLifecycle(initialValue = ClockTime.now())

    // Handle screen orientation based on current mode cleanly
    LaunchedEffect(currentMode) {
        activity?.let { act ->
            val targetOrientation = if (currentMode.isLandscape) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            if (act.requestedOrientation != targetOrientation) {
                act.requestedOrientation = targetOrientation
            }
        }
    }

    // Handle Android hardware/gesture Back button
    BackHandler(enabled = showControls || currentMode == ClockMode.SETTINGS) {
        if (showControls) {
            showControls = false
        } else if (currentMode == ClockMode.SETTINGS) {
            currentMode = previousClockMode
        }
    }

    // Helper to map 24h & seconds flags to appropriate digital ClockMode
    fun updateDigitalMode(new24: Boolean, newSec: Boolean) {
        val targetMode = when {
            !new24 && !newSec -> ClockMode.DIGITAL_12
            new24 && !newSec -> ClockMode.DIGITAL_24
            !new24 && newSec -> ClockMode.SECONDS_12
            else -> ClockMode.SECONDS_24
        }
        currentMode = targetMode
        previousClockMode = targetMode
        prefs.lastMode = targetMode.name
    }

    val isDigitalScreen = currentMode in listOf(
        ClockMode.DIGITAL_12,
        ClockMode.DIGITAL_24,
        ClockMode.SECONDS_12,
        ClockMode.SECONDS_24
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Active Screen: If in digital modes, render DigitalClockScreen directly
        // to guarantee zero flicker and zero screen rebuilds during format/seconds toggles.
        if (isDigitalScreen) {
            DigitalClockScreen(
                clockTime = clockTime,
                is24Hour = is24Hour,
                showSeconds = showSeconds,
                controlsVisible = showControls,
                onScreenTap = {
                    showControls = !showControls
                    if (showControls) registerInteraction()
                },
                onTimeFormatChanged = { new24 ->
                    is24Hour = new24
                    prefs.is24HourFormat = new24
                    updateDigitalMode(new24, showSeconds)
                    registerInteraction()
                },
                onShowSecondsChanged = { newSec ->
                    showSeconds = newSec
                    prefs.showSeconds = newSec
                    updateDigitalMode(is24Hour, newSec)
                    registerInteraction()
                },
                onInteraction = { registerInteraction() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AnimatedContent(
                targetState = currentMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 200)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 160))
                },
                label = "ScreenModeTransition"
            ) { targetMode ->
                when (targetMode) {
                    ClockMode.FLIP_CLOCK -> {
                        FlipClockScreen(
                            clockTime = clockTime,
                            is24Hour = is24Hour,
                            showSeconds = showSeconds,
                            controlsVisible = showControls,
                            onScreenTap = {
                                showControls = !showControls
                                if (showControls) registerInteraction()
                            },
                            onTimeFormatChanged = { new24 ->
                                is24Hour = new24
                                prefs.is24HourFormat = new24
                                registerInteraction()
                            },
                            onShowSecondsChanged = { newSec ->
                                showSeconds = newSec
                                prefs.showSeconds = newSec
                                registerInteraction()
                            },
                            onInteraction = { registerInteraction() }
                        )
                    }
                    ClockMode.POMODORO -> {
                        PomodoroScreen(
                            viewModel = timersViewModel,
                            onScreenTap = {
                                showControls = !showControls
                                if (showControls) registerInteraction()
                            }
                        )
                    }
                    ClockMode.STOPWATCH -> {
                        StopwatchScreen(
                            viewModel = timersViewModel,
                            onScreenTap = {
                                showControls = !showControls
                                if (showControls) registerInteraction()
                            }
                        )
                    }
                    ClockMode.TIMER -> {
                        TimerScreen(
                            viewModel = timersViewModel,
                            onScreenTap = {
                                showControls = !showControls
                                if (showControls) registerInteraction()
                            }
                        )
                    }
                    ClockMode.SETTINGS -> {
                        SettingsScreen(
                            prefs = prefs,
                            is24Hour = is24Hour,
                            onTimeFormatChanged = { new24 ->
                                is24Hour = new24
                                prefs.is24HourFormat = new24
                                updateDigitalMode(new24, showSeconds)
                            },
                            keepScreenOn = keepScreenOn,
                            onKeepScreenOnChanged = { newKeep ->
                                keepScreenOn = newKeep
                                prefs.keepScreenOn = newKeep
                                onKeepScreenOnChanged(newKeep)
                            },
                            onBack = {
                                currentMode = previousClockMode
                            }
                        )
                    }
                    else -> {}
                }
            }
        }

        // Side Drawer Pull Handle on Right Edge (when drawer is closed and not in settings)
        if (!showControls && currentMode != ClockMode.SETTINGS) {
            SideDrawerHandle(
                onToggleDrawer = {
                    showControls = true
                    registerInteraction()
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // Bottom Mode Drawer
        if (currentMode != ClockMode.SETTINGS) {
            BottomModeDrawer(
                visible = showControls,
                currentMode = currentMode,
                onModeSelected = { selectedMode ->
                    registerInteraction()
                    when (selectedMode) {
                        ClockMode.SETTINGS -> {
                            if (currentMode != ClockMode.SETTINGS) {
                                previousClockMode = currentMode
                            }
                            currentMode = ClockMode.SETTINGS
                            showControls = false
                        }
                        ClockMode.DIGITAL_12 -> {
                            is24Hour = false
                            showSeconds = false
                            prefs.is24HourFormat = false
                            prefs.showSeconds = false
                            updateDigitalMode(false, false)
                        }
                        ClockMode.DIGITAL_24 -> {
                            is24Hour = true
                            showSeconds = false
                            prefs.is24HourFormat = true
                            prefs.showSeconds = false
                            updateDigitalMode(true, false)
                        }
                        ClockMode.SECONDS_12 -> {
                            is24Hour = false
                            showSeconds = true
                            prefs.is24HourFormat = false
                            prefs.showSeconds = true
                            updateDigitalMode(false, true)
                        }
                        ClockMode.SECONDS_24 -> {
                            is24Hour = true
                            showSeconds = true
                            prefs.is24HourFormat = true
                            prefs.showSeconds = true
                            updateDigitalMode(true, true)
                        }
                        else -> {
                            // Non-digital modes (FLIP_CLOCK, POMODORO, STOPWATCH, TIMER)
                            currentMode = selectedMode
                            previousClockMode = selectedMode
                            prefs.lastMode = selectedMode.name
                            showControls = false
                        }
                    }
                },
                onInteraction = { registerInteraction() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
