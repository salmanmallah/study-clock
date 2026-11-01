package com.ghost.fullscreenclock.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.RobotoFont
import com.ghost.fullscreenclock.time.ClockTime

private data class ClockSizeSpec(
    val digitHeightDp: Dp,
    val colonHeightDp: Dp,
    val secondsHeightDp: Dp,
    val amPmHeightDp: Dp
)

/**
 * Centered Digital Clock display with font sizing empirically derived from
 * the original reference app (reverse-engineered from TECNO CN5 2364x1080 440dpi captures).
 *
 * Sizing Specifications:
 * 1. 12-Hour Mode WITH Seconds:
 *    Original capture (03_clock12_sec_landscape.png):
 *    All 6 digits (HH:MM:SS) have uniform height = ~280-292 px (~102-106 dp, maxHeight * 0.260f).
 *    Total span width = ~1600 px, centered with ~380 px safe margins on left and right.
 *
 * 2. 24-Hour Mode WITH Seconds:
 *    Original capture (04_clock24_sec_landscape.png):
 *    Main digits (HH:MM) have height = ~432 px (~157 dp, maxHeight * 0.400f).
 *    Seconds digits (SS) have height = ~216 px (~78 dp, maxHeight * 0.200f = 0.50 * mainDigitHeight).
 *    Total span width = ~1500 px, centered with ~430 px left margin and ~430 px right margin.
 *
 * 3. 12-Hour Mode WITHOUT Seconds:
 *    Original capture (01_clock12_landscape.png):
 *    Main digits (HH:MM) height = ~475 px (~173 dp, maxHeight * 0.440f).
 *    AM/PM height = ~108 px (~39 dp, maxHeight * 0.100f).
 *
 * 4. 24-Hour Mode WITHOUT Seconds:
 *    Original capture (02_clock24_landscape.png):
 *    Main digits (HH:MM) height = ~496 px (~180 dp, maxHeight * 0.460f).
 */
@Composable
fun DigitalClock(
    clockTime: ClockTime,
    is24Hour: Boolean,
    showSeconds: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hourText = if (is24Hour) clockTime.formattedHour24 else clockTime.formattedHour12
    val minuteText = clockTime.formattedMinute
    val secondText = clockTime.formattedSecond
    val amPmText = clockTime.amPmString

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current

        val sizeSpec = when {
            // 1. 12-Hour WITH Seconds (from original 03_clock12_sec_landscape.png)
            !is24Hour && showSeconds -> {
                val digitH = maxHeight * 0.260f
                val colonH = maxHeight * 0.180f
                val secH = digitH
                val amPmH = maxHeight * 0.075f
                ClockSizeSpec(digitH, colonH, secH, amPmH)
            }
            // 2. 24-Hour WITH Seconds (from original 04_clock24_sec_landscape.png)
            is24Hour && showSeconds -> {
                val digitH = maxHeight * 0.400f
                val colonH = maxHeight * 0.310f
                val secH = maxHeight * 0.200f
                ClockSizeSpec(digitH, colonH, secH, 0.dp)
            }
            // 3. 12-Hour WITHOUT Seconds (from original 01_clock12_landscape.png)
            !is24Hour && !showSeconds -> {
                val digitH = maxHeight * 0.440f
                val colonH = maxHeight * 0.350f
                val amPmH = maxHeight * 0.100f
                ClockSizeSpec(digitH, colonH, 0.dp, amPmH)
            }
            // 4. 24-Hour WITHOUT Seconds (from original 02_clock24_landscape.png)
            else -> {
                val digitH = maxHeight * 0.460f
                val colonH = maxHeight * 0.350f
                ClockSizeSpec(digitH, colonH, 0.dp, 0.dp)
            }
        }

        // Cap-height of Roboto font is ~0.71 of em font-size
        val baseFontDp = sizeSpec.digitHeightDp / 0.71f
        val colonFontDp = sizeSpec.colonHeightDp / 0.71f
        val secondsFontDp = sizeSpec.secondsHeightDp / 0.71f
        val amPmFontDp = sizeSpec.amPmHeightDp / 0.71f

        // System fontScale cancellation ensures font does not blow up if user has "Large text"
        val clockFontSize = with(density) { (baseFontDp.toPx() / density.fontScale).toSp() }
        val colonFontSize = with(density) { (colonFontDp.toPx() / density.fontScale).toSp() }
        val secondsFontSize = with(density) { (secondsFontDp.toPx() / density.fontScale).toSp() }
        val amPmFontSize = with(density) { (amPmFontDp.toPx() / density.fontScale).toSp() }

        val clockTextStyle = TextStyle(
            fontFamily = RobotoFont,
            fontWeight = FontWeight.Medium,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            textAlign = TextAlign.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Hours (HH)
            Text(
                text = hourText,
                color = PureWhite,
                fontSize = clockFontSize,
                style = clockTextStyle,
                maxLines = 1,
                softWrap = false
            )

            // Colon (:) between Hours and Minutes
            Text(
                text = ":",
                color = PureWhite,
                fontSize = colonFontSize,
                style = clockTextStyle,
                modifier = Modifier.padding(horizontal = if (showSeconds && !is24Hour) 4.dp else 6.dp),
                maxLines = 1,
                softWrap = false
            )

            // Minutes (MM)
            Text(
                text = minuteText,
                color = PureWhite,
                fontSize = clockFontSize,
                style = clockTextStyle,
                maxLines = 1,
                softWrap = false
            )

            // Seconds (:SS)
            if (showSeconds) {
                if (is24Hour) {
                    // 24H Mode with Seconds: Small subordinate seconds to the right (Original 04_clock24_sec_landscape.png)
                    Row(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .align(Alignment.CenterVertically),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ":",
                            color = PureWhite,
                            fontSize = secondsFontSize * 0.94f,
                            style = clockTextStyle,
                            modifier = Modifier.padding(end = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )

                        Text(
                            text = secondText,
                            color = PureWhite,
                            fontSize = secondsFontSize,
                            style = clockTextStyle,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    // 12H Mode with Seconds: Uniform 6-digit layout (Original 03_clock12_sec_landscape.png)
                    Text(
                        text = ":",
                        color = PureWhite,
                        fontSize = colonFontSize,
                        style = clockTextStyle,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = secondText,
                        color = PureWhite,
                        fontSize = clockFontSize,
                        style = clockTextStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // AM / PM indicator in 12-hour mode
            if (!is24Hour) {
                Column(
                    modifier = Modifier
                        .padding(start = if (showSeconds) 10.dp else 14.dp)
                        .align(Alignment.Bottom),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = amPmText,
                        color = PureWhite.copy(alpha = 0.9f),
                        fontSize = amPmFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RobotoFont,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier.padding(bottom = if (showSeconds) 8.dp else 20.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
