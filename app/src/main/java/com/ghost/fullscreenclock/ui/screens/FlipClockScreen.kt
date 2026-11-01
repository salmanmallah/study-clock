package com.ghost.fullscreenclock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.time.ClockTime
import com.ghost.fullscreenclock.ui.components.FlipCard

@Composable
fun FlipClockScreen(
    clockTime: ClockTime,
    is24Hour: Boolean,
    controlsVisible: Boolean,
    onScreenTap: () -> Unit,
    onTimeFormatChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    // In 12-hour mode, single-digit hour is formatted without leading zero ("2"),
    // matching original app screenshot. In 24-hour mode, with leading zero ("02").
    val hourText = if (is24Hour) {
        clockTime.formattedHour24
    } else {
        val h = if (clockTime.hour12 == 0) 12 else clockTime.hour12
        h.toString()
    }
    val minuteText = clockTime.formattedMinute
    val amPmText = if (!is24Hour) clockTime.amPmString else null

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onScreenTap
            ),
        contentAlignment = Alignment.Center
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // Authentic original dimensions:
            // Cards take ~81% of screen height, aspect ratio width/height is ~0.89
            val cardHeight = (maxHeight * 0.81f).coerceIn(200.dp, 800.dp)
            val cardWidth = cardHeight * 0.89f
            val cardSpacing = cardWidth * 0.15f
            val seamHeight = (cardHeight * 0.011f).coerceIn(3.dp, 4.dp)
            val cornerRadius = (cardHeight * 0.044f).coerceIn(12.dp, 22.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour Split-Flap Card (includes AM/PM in bottom-left in 12h mode)
                FlipCard(
                    value = hourText,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    bottomCornerLabel = amPmText,
                    seamHeight = seamHeight,
                    cornerRadius = cornerRadius
                )

                Spacer(modifier = Modifier.width(cardSpacing))

                // Minute Split-Flap Card
                FlipCard(
                    value = minuteText,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    bottomCornerLabel = null,
                    seamHeight = seamHeight,
                    cornerRadius = cornerRadius
                )
            }
        } else {
            // Portrait fallback: stacked vertically
            val cardWidth = (maxWidth * 0.86f).coerceIn(160.dp, 450.dp)
            val cardHeight = cardWidth / 0.89f
            val cardSpacing = cardHeight * 0.08f
            val seamHeight = (cardHeight * 0.011f).coerceIn(3.dp, 4.dp)
            val cornerRadius = (cardHeight * 0.044f).coerceIn(12.dp, 22.dp)

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlipCard(
                    value = hourText,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    bottomCornerLabel = amPmText,
                    seamHeight = seamHeight,
                    cornerRadius = cornerRadius
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                FlipCard(
                    value = minuteText,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    bottomCornerLabel = null,
                    seamHeight = seamHeight,
                    cornerRadius = cornerRadius
                )
            }
        }

        // Top 12-Hour / 24-Hour Format Toggle Bar with 3-second auto-hide
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
            FlipClockTopControlBar(
                is24Hour = is24Hour,
                onTimeFormatChanged = onTimeFormatChanged,
                onInteraction = onInteraction
            )
        }
    }
}
