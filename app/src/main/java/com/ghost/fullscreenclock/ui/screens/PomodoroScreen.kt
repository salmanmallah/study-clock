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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.ui.viewmodel.TimersViewModel

// Tomato accent used on the dark theme (same surfaces/shapes as the Timer screen)
private val TomatoAccent = Color(0xFFFF5A3C)
private val MutedText = Color(0xFF8E8E93)

@Composable
fun PomodoroScreen(
    viewModel: TimersViewModel,
    onScreenTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingMillis by viewModel.pomodoroRemainingMillis.collectAsState()
    val isRunning by viewModel.pomodoroRunning.collectAsState()
    val completedSessions by viewModel.pomodoroCompletedSessions.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    val totalMillis = viewModel.pomodoroTotalMillis.coerceAtLeast(1000L)
    val progress = (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    val remainingTotalSec = (remainingMillis + 999) / 1000
    val timeFormatted = String.format("%02d:%02d", remainingTotalSec / 60, remainingTotalSec % 60)
    val isIdle = !isRunning && remainingMillis >= totalMillis
    val status = when {
        isRunning -> "Focus"
        isIdle -> "Ready"
        else -> "Paused"
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
        val timerFontSize = with(density) {
            ((dialSize * 0.21f).toPx() / density.fontScale).toSp()
        }

        val dial: @Composable () -> Unit = {
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
                    color = TomatoAccent,
                    strokeWidth = 8.dp,
                    trackColor = Color.Transparent
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        color = PureWhite,
                        fontSize = timerFontSize,
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

        val controls: @Composable () -> Unit = {
            Column(
                modifier = if (isLandscape) Modifier.width(470.dp) else Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "POMODORO",
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CharcoalCard)
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = if (completedSessions > 0) "? $completedSessions sessions done" else "25 min focus session",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.togglePomodoro() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color(0xFFFF3B30) else TomatoAccent
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
                        onClick = { viewModel.resetPomodoro() },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CharcoalCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Pomodoro",
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
