package com.ghost.fullscreenclock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.RobotoFont
import com.ghost.fullscreenclock.time.ClockTime
import com.ghost.fullscreenclock.ui.components.FlipCard

@Composable
fun FlipClockScreen(
    clockTime: ClockTime,
    is24Hour: Boolean,
    onScreenTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hourText = if (is24Hour) clockTime.formattedHour24 else clockTime.formattedHour12
    val minuteText = clockTime.formattedMinute
    val secondText = clockTime.formattedSecond
    val amPmText = clockTime.amPmString

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
        val density = LocalDensity.current
        val isLandscape = maxWidth > maxHeight

        // Derive card dimensions dynamically from available screen space
        val mainCardHeight = if (isLandscape) {
            (maxHeight * 0.58f).coerceIn(160.dp, 240.dp)
        } else {
            (maxWidth * 0.35f).coerceIn(120.dp, 190.dp)
        }
        val mainCardWidth = (mainCardHeight * 1.18f).coerceIn(180.dp, 280.dp)

        val secCardHeight = (mainCardHeight * 0.44f).coerceIn(70.dp, 105.dp)
        val secCardWidth = (secCardHeight * 1.18f).coerceIn(80.dp, 125.dp)

        val cardSpacing = (maxWidth * 0.015f).coerceIn(10.dp, 22.dp)

        val amPmFontSize = with(density) {
            ((secCardHeight * 0.32f).toPx() / density.fontScale).toSp()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Hour Split Flap Card
            FlipCard(
                value = hourText,
                cardWidth = mainCardWidth,
                cardHeight = mainCardHeight
            )

            Spacer(modifier = Modifier.width(cardSpacing))

            // Minute Split Flap Card
            FlipCard(
                value = minuteText,
                cardWidth = mainCardWidth,
                cardHeight = mainCardHeight
            )

            Spacer(modifier = Modifier.width(cardSpacing))

            // Seconds & AM/PM Column
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                if (!is24Hour) {
                    Text(
                        text = amPmText,
                        color = PureWhite.copy(alpha = 0.85f),
                        fontSize = amPmFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RobotoFont,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Small Split Flap Card for Seconds
                FlipCard(
                    value = secondText,
                    cardWidth = secCardWidth,
                    cardHeight = secCardHeight
                )
            }
        }
    }
}
