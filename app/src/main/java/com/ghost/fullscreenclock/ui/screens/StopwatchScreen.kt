package com.ghost.fullscreenclock.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.ui.viewmodel.LapRecord
import com.ghost.fullscreenclock.ui.viewmodel.TimersViewModel

private val StartGreen = Color(0xFF34C759)
private val StopRed = Color(0xFFFF3B30)
private val WarningAmber = Color(0xFFFFCC00)
private val DarkGrayText = Color(0xFF8E8E93)
private val LightGrayText = Color(0xFFAEAEB2)
private val LapPanelSurface = Color(0xFF121212)

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
    val mainTimeStr = String.format("%02d:%02d", minutes, seconds)
    val centisStr = String.format(".%02d", centis)

    // 60-second sweeping ring progress
    val minuteProgress = ((elapsedMillis % 60000L).toFloat() / 60000f).coerceIn(0f, 1f)
    val statusText = when {
        isRunning -> "RUNNING"
        elapsedMillis > 0L -> "PAUSED"
        else -> "READY"
    }

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

        val dialSize = if (isLandscape) {
            (maxHeight * 0.72f).coerceIn(180.dp, 300.dp)
        } else {
            (maxWidth * 0.68f).coerceIn(200.dp, 280.dp)
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 56.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StopwatchDial(
                    dialSize = dialSize,
                    mainTimeStr = mainTimeStr,
                    centisStr = centisStr,
                    progress = minuteProgress,
                    isRunning = isRunning,
                    hasElapsed = elapsedMillis > 0L,
                    statusText = statusText
                )

                StopwatchControlsAndLaps(
                    isLandscape = true,
                    isRunning = isRunning,
                    hasElapsed = elapsedMillis > 0L,
                    laps = laps,
                    onToggle = { viewModel.toggleStopwatch() },
                    onLap = { viewModel.recordStopwatchLap() },
                    onReset = { viewModel.resetStopwatch() }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                StopwatchDial(
                    dialSize = dialSize,
                    mainTimeStr = mainTimeStr,
                    centisStr = centisStr,
                    progress = minuteProgress,
                    isRunning = isRunning,
                    hasElapsed = elapsedMillis > 0L,
                    statusText = statusText
                )

                Spacer(modifier = Modifier.height(24.dp))

                StopwatchControlsAndLaps(
                    isLandscape = false,
                    isRunning = isRunning,
                    hasElapsed = elapsedMillis > 0L,
                    laps = laps,
                    onToggle = { viewModel.toggleStopwatch() },
                    onLap = { viewModel.recordStopwatchLap() },
                    onReset = { viewModel.resetStopwatch() }
                )
            }
        }
    }
}

@Composable
private fun StopwatchDial(
    dialSize: Dp,
    mainTimeStr: String,
    centisStr: String,
    progress: Float,
    isRunning: Boolean,
    hasElapsed: Boolean,
    statusText: String
) {
    val density = LocalDensity.current
    val mainFontSize = with(density) {
        ((dialSize * 0.19f).toPx() / density.fontScale).toSp()
    }
    val centisFontSize = with(density) {
        ((dialSize * 0.12f).toPx() / density.fontScale).toSp()
    }

    val ringColor = when {
        isRunning -> StartGreen
        hasElapsed -> WarningAmber
        else -> PureWhite
    }

    Box(
        modifier = Modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 8.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(inset, inset)

            // Background circle track
            drawArc(
                color = CharcoalCard,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active 60s sweeping progress arc (clockwise from 12 o'clock)
            val sweep = if (!hasElapsed && !isRunning) 0f else (progress * 360f).coerceAtLeast(2f)
            if (sweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = PureWhite,
                            fontSize = mainFontSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    ) {
                        append(mainTimeStr)
                    }
                    withStyle(
                        SpanStyle(
                            color = if (isRunning) StartGreen else LightGrayText,
                            fontSize = centisFontSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    ) {
                        append(centisStr)
                    }
                },
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statusText,
                color = if (isRunning) StartGreen else DarkGrayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun StopwatchControlsAndLaps(
    isLandscape: Boolean,
    isRunning: Boolean,
    hasElapsed: Boolean,
    laps: List<LapRecord>,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit
) {
    val fastestLapNumber = remember(laps) {
        if (laps.size >= 2) laps.minByOrNull { it.splitMillis }?.lapNumber else null
    }
    val slowestLapNumber = remember(laps) {
        if (laps.size >= 2) laps.maxByOrNull { it.splitMillis }?.lapNumber else null
    }

    Column(
        modifier = if (isLandscape) {
            Modifier
                .width(460.dp)
                .fillMaxHeight()
        } else {
            Modifier.fillMaxWidth()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "STOPWATCH",
            color = DarkGrayText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row (matches Timer & Pomodoro)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) StopRed else StartGreen
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .height(54.dp)
                    .width(155.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) "STOP" else if (hasElapsed) "RESUME" else "START",
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isRunning) {
                Button(
                    onClick = onLap,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalCard
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .width(120.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Lap",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LAP",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                IconButton(
                    onClick = onReset,
                    enabled = hasElapsed,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (hasElapsed) CharcoalCard else CharcoalCard.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = if (hasElapsed) PureWhite else DarkGrayText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Laps Panel
        if (laps.isEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalCard.copy(alpha = 0.55f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) "Tap LAP to record split times" else "No laps recorded yet",
                    color = LightGrayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLandscape) 170.dp else 210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LapPanelSurface)
                    .border(1.dp, CharcoalCard, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                items(laps, key = { it.lapNumber }) { lap ->
                    val splitStr = formatStopwatchTime(lap.splitMillis, prefixPlus = true)
                    val totalStr = formatStopwatchTime(lap.totalMillis, prefixPlus = false)

                    val lapLabelColor = when (lap.lapNumber) {
                        fastestLapNumber -> StartGreen
                        slowestLapNumber -> StopRed
                        else -> LightGrayText
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lap ${String.format("%02d", lap.lapNumber)}",
                            color = lapLabelColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = splitStr,
                            color = LightGrayText,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = totalStr,
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(color = CharcoalCard.copy(alpha = 0.6f))
                }
            }
        }
    }
}

private fun formatStopwatchTime(millis: Long, prefixPlus: Boolean): String {
    val min = millis / 60000
    val sec = (millis % 60000) / 1000
    val cent = (millis % 1000) / 10
    val prefix = if (prefixPlus) "+" else ""
    return String.format("%s%02d:%02d.%02d", prefix, min, sec, cent)
}
