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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.StopwatchDarkGraphite
import com.ghost.fullscreenclock.theme.StopwatchSecondaryGray
import com.ghost.fullscreenclock.ui.viewmodel.TimersViewModel

@Composable
fun StopwatchScreen(
    viewModel: TimersViewModel,
    onScreenTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.stopwatchRunning.collectAsState()
    val elapsedMillis by viewModel.stopwatchElapsedMillis.collectAsState()
    val laps by viewModel.stopwatchLaps.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    val minutes = elapsedMillis / 60000
    val seconds = (elapsedMillis % 60000) / 1000
    val centis = (elapsedMillis % 1000) / 10
    val timeFormatted = String.format("%02d:%02d.%02d", minutes, seconds, centis)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onScreenTap
            ),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val availableHeight = maxHeight
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isLandscape) 48.dp else 24.dp,
                    vertical = if (isLandscape) 16.dp else 40.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 36.dp))

            Text(
                text = "STOPWATCH",
                color = StopwatchSecondaryGray,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 32.dp))

            // Main Digital Stopwatch Display (fixed dp-derived font size ignoring fontScale)
            val digitsFontSize = with(density) {
                val baseDp = if (isLandscape) 52.dp else 68.dp
                (baseDp.toPx() / density.fontScale).toSp()
            }

            Text(
                text = timeFormatted,
                color = StopwatchDarkGraphite,
                fontSize = digitsFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 40.dp))

            // Control Buttons: Pure centered Box to prevent any Button internal padding misalignments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Button: Reset / Lap
                val leftButtonText = if (isRunning) "Lap" else "Reset"
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8E8E8))
                        .clickable {
                            if (isRunning) {
                                viewModel.recordStopwatchLap()
                            } else {
                                viewModel.resetStopwatch()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = leftButtonText,
                        color = StopwatchDarkGraphite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                // Right Button: Start / Stop
                val rightButtonText = if (isRunning) "Stop" else "Start"
                val rightButtonColor = if (isRunning) Color(0xFFFF3B30) else Color(0xFF34C759)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(rightButtonColor)
                        .clickable { viewModel.toggleStopwatch() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rightButtonText,
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 28.dp))

            // Laps Table
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(laps, key = { it.lapNumber }) { lap ->
                    val splitMin = lap.splitMillis / 60000
                    val splitSec = (lap.splitMillis % 60000) / 1000
                    val splitCent = (lap.splitMillis % 1000) / 10
                    val splitStr = String.format("+%02d:%02d.%02d", splitMin, splitSec, splitCent)

                    val totalMin = lap.totalMillis / 60000
                    val totalSec = (lap.totalMillis % 60000) / 1000
                    val totalCent = (lap.totalMillis % 1000) / 10
                    val totalStr = String.format("%02d:%02d.%02d", totalMin, totalSec, totalCent)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lap ${lap.lapNumber}",
                            color = StopwatchSecondaryGray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = splitStr,
                                color = StopwatchSecondaryGray,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Text(
                                text = totalStr,
                                color = StopwatchDarkGraphite,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF2F2F2))
                }
            }
        }
    }
}
