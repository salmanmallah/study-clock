package com.ghost.fullscreenclock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.RobotoFont
import com.ghost.fullscreenclock.time.ClockTime
import com.ghost.fullscreenclock.ui.components.DigitalClock
import kotlinx.coroutines.delay

/**
 * Minimalist, distraction-free Study Clock screen.
 *
 * Requirements satisfied:
 * - Single clock screen at all times with no screen navigation.
 * - Tap screen reveals the top control bar with 12H/24H format and Show Seconds toggles.
 * - Automatic hiding of controls after 3 seconds of inactivity.
 * - Font size, style, and alignment remain 100% constant across format switches.
 */
@Composable
fun DigitalClockScreen(
    clockTime: ClockTime,
    is24Hour: Boolean,
    showSeconds: Boolean,
    controlsVisible: Boolean,
    onScreenTap: () -> Unit,
    onTimeFormatChanged: (Boolean) -> Unit,
    onShowSecondsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {}
) {
    val screenInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .clickable(
                interactionSource = screenInteractionSource,
                indication = null,
                onClick = onScreenTap
            )
    ) {
        // Centered Main Clock Display (Consistent font size, baseline, and positioning)
        DigitalClock(
            clockTime = clockTime,
            is24Hour = is24Hour,
            showSeconds = showSeconds
        )

        // Top Control Bar with 3-second auto-hide and smooth vertical slide animation
        AnimatedVisibility(
            visible = controlsVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 180)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            StudyClockTopControlBar(
                is24Hour = is24Hour,
                showSeconds = showSeconds,
                onTimeFormatChanged = { new24 ->
                    onTimeFormatChanged(new24)
                    onInteraction()
                },
                onShowSecondsChanged = { newSec ->
                    onShowSecondsChanged(newSec)
                    onInteraction()
                },
                onInteraction = onInteraction
            )
        }
    }
}

/**
 * Unified Top Control Bar containing:
 * 1. 12-Hour / 24-Hour segmented toggle.
 * 2. Show Seconds (Off / On) segmented toggle.
 */
@Composable
fun StudyClockTopControlBar(
    is24Hour: Boolean,
    showSeconds: Boolean,
    onTimeFormatChanged: (Boolean) -> Unit,
    onShowSecondsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {}
) {
    val barInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1B1B1F).copy(alpha = 0.94f))
            .border(
                width = 1.dp,
                color = Color(0xFF383842),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                indication = null,
                interactionSource = barInteractionSource,
                onClick = onInteraction
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 1. 12-Hour / 24-Hour Segmented Control
            FormatToggleSegment(
                is24Hour = is24Hour,
                onFormatSelected = onTimeFormatChanged
            )

            // Vertical divider between control groups
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(Color(0xFF383842))
            )
            Spacer(modifier = Modifier.width(8.dp))

            // 2. Show Seconds (Off / On) Segmented Control
            SecondsToggleSegment(
                showSeconds = showSeconds,
                onShowSecondsSelected = onShowSecondsChanged
            )
        }
    }
}

/**
 * Top Control Bar for Flip Clock containing the 12-Hour / 24-Hour segmented toggle.
 */
@Composable
fun FlipClockTopControlBar(
    is24Hour: Boolean,
    onTimeFormatChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {}
) {
    val barInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1B1B1F).copy(alpha = 0.94f))
            .border(
                width = 1.dp,
                color = Color(0xFF383842),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                indication = null,
                interactionSource = barInteractionSource,
                onClick = onInteraction
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        FormatToggleSegment(
            is24Hour = is24Hour,
            onFormatSelected = { new24 ->
                onTimeFormatChanged(new24)
                onInteraction()
            }
        )
    }
}

/**
 * Segmented toggle for 12-Hour vs 24-Hour format
 */
@Composable
private fun FormatToggleSegment(
    is24Hour: Boolean,
    onFormatSelected: (Boolean) -> Unit
) {
    val segmentWidth = 98.dp
    val segmentHeight = 32.dp
    val targetOffset = if (is24Hour) 101.dp else 2.dp

    val indicatorOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 200),
        label = "FormatIndicatorOffset"
    )

    Box(
        modifier = Modifier
            .width(202.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131316))
            .padding(2.dp)
    ) {
        // Sliding active pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset, y = 0.dp)
                .width(segmentWidth)
                .height(segmentHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(PureWhite)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 12-Hour
            val src12 = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = src12,
                        indication = null
                    ) {
                        if (is24Hour) onFormatSelected(false)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "12-Hour",
                    color = if (!is24Hour) PureBlack else PureWhite.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    fontWeight = if (!is24Hour) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = RobotoFont,
                    textAlign = TextAlign.Center
                )
            }

            // 24-Hour
            val src24 = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = src24,
                        indication = null
                    ) {
                        if (!is24Hour) onFormatSelected(true)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "24-Hour",
                    color = if (is24Hour) PureBlack else PureWhite.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    fontWeight = if (is24Hour) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = RobotoFont,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Segmented toggle for Show Seconds (Off / On)
 */
@Composable
private fun SecondsToggleSegment(
    showSeconds: Boolean,
    onShowSecondsSelected: (Boolean) -> Unit
) {
    val segmentWidth = 84.dp
    val segmentHeight = 32.dp
    val targetOffset = if (showSeconds) 87.dp else 2.dp

    val indicatorOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 200),
        label = "SecondsIndicatorOffset"
    )

    Box(
        modifier = Modifier
            .width(174.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131316))
            .padding(2.dp)
    ) {
        // Sliding active pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset, y = 0.dp)
                .width(segmentWidth)
                .height(segmentHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(PureWhite)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seconds Off
            val srcOff = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = srcOff,
                        indication = null
                    ) {
                        if (showSeconds) onShowSecondsSelected(false)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sec Off",
                    color = if (!showSeconds) PureBlack else PureWhite.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    fontWeight = if (!showSeconds) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = RobotoFont,
                    textAlign = TextAlign.Center
                )
            }

            // Seconds On
            val srcOn = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = srcOn,
                        indication = null
                    ) {
                        if (!showSeconds) onShowSecondsSelected(true)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sec On",
                    color = if (showSeconds) PureBlack else PureWhite.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    fontWeight = if (showSeconds) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = RobotoFont,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
