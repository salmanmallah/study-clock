package com.ghost.fullscreenclock.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.ui.viewmodel.TimersViewModel

private val StartGreen = Color(0xFF34C759)
private val StopRed = Color(0xFFFF3B30)
private val MutedText = Color(0xFF8E8E93)

/** Quick preset durations in seconds with their chip labels. */
private val TimerPresets = listOf(
    "1m" to 60,
    "5m" to 5 * 60,
    "10m" to 10 * 60,
    "15m" to 15 * 60,
    "25m" to 25 * 60,
    "45m" to 45 * 60
)

private fun formatDuration(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen(
    viewModel: TimersViewModel,
    onScreenTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val setSeconds by viewModel.timerSetSeconds.collectAsState()
    val remainingMillis by viewModel.timerRemainingMillis.collectAsState()
    val isRunning by viewModel.timerRunning.collectAsState()
    val canAdjust by viewModel.canAdjustTimer.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    val totalMillis = (setSeconds * 1000L).coerceAtLeast(1000L)
    val progress = (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    val remainingTotalSec = (remainingMillis + 999) / 1000
    val isFinished = !isRunning && remainingMillis <= 0L
    val timeFormatted = formatDuration(remainingTotalSec)

    val statusText = when {
        isFinished -> "Time's up"
        isRunning -> "Running"
        canAdjust -> "Set duration"
        else -> "Paused"
    }
    val ringColor = when {
        isFinished -> StopRed
        isRunning -> StartGreen
        else -> PureWhite
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
        val density = LocalDensity.current

        val dialSize = if (isLandscape) {
            (maxHeight * 0.72f).coerceIn(180.dp, 300.dp)
        } else {
            (maxWidth * 0.68f).coerceIn(200.dp, 300.dp)
        }
        // Digits sized from dial, ignoring system fontScale so they never overflow the ring
        val digitChars = timeFormatted.length.coerceAtLeast(5)
        val timerFontSize = with(density) {
            val baseDp = dialSize * (if (digitChars > 5) 0.17f else 0.21f)
            (baseDp.toPx() / density.fontScale).toSp()
        }

        val dial: @Composable () -> Unit = {
            TimerDial(
                dialSize = dialSize,
                progress = progress,
                ringColor = ringColor,
                time = timeFormatted,
                status = statusText,
                fontSize = timerFontSize
            )
        }

        val controls: @Composable () -> Unit = {
            Column(
                modifier = if (isLandscape) Modifier.width(470.dp) else Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (canAdjust) {
                    Text(
                        text = "QUICK SET",
                        color = MutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimerPresets.forEach { (label, secs) ->
                            PresetChip(
                                label = label,
                                selected = setSeconds == secs,
                                onClick = { viewModel.setTimerDurationSeconds(secs) }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdjustChip("−1m") { viewModel.adjustTimerSeconds(-60) }
                        AdjustChip("−10s") { viewModel.adjustTimerSeconds(-10) }
                        AdjustChip("+10s") { viewModel.adjustTimerSeconds(10) }
                        AdjustChip("+1m") { viewModel.adjustTimerSeconds(60) }
                    }
                } else {
                    Text(
                        text = "of ${formatDuration(setSeconds.toLong())}",
                        color = MutedText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) StopRed else StartGreen
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .width(150.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) "PAUSE" else "START",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CharcoalCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = PureWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 56.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dial()
                controls()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                dial()
                Spacer(Modifier.height(28.dp))
                controls()
            }
        }
    }
}

@Composable
private fun TimerDial(
    dialSize: Dp,
    progress: Float,
    ringColor: Color,
    time: String,
    status: String,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Box(modifier = Modifier.size(dialSize), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = CharcoalCard,
            strokeWidth = 8.dp,
            trackColor = CharcoalCard
        )
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            strokeWidth = 8.dp,
            trackColor = Color.Transparent
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = time,
                color = PureWhite,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = status.uppercase(),
                color = MutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun PresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) PureWhite else CharcoalCard)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) PureBlack else PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun AdjustChip(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .border(BorderStroke(1.dp, Color(0xFF3A3A3C)), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = PureWhite.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
