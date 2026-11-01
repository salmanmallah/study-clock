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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.PomodoroBrickRed
import com.ghost.fullscreenclock.theme.PomodoroPeachBg
import com.ghost.fullscreenclock.theme.PomodoroYellowAccent
import com.ghost.fullscreenclock.theme.RobotoFont
import com.ghost.fullscreenclock.ui.viewmodel.TimersViewModel

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

    val remainingTotalSec = (remainingMillis + 999) / 1000
    val minutes = remainingTotalSec / 60
    val seconds = remainingTotalSec % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PomodoroPeachBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onScreenTap
            ),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val isLandscape = maxWidth > maxHeight

        // Dynamic font size derived from available viewport, ignoring system fontScale
        val pomoFontSize = with(density) {
            val baseDp = if (isLandscape) {
                (maxHeight * 0.35f).coerceIn(90.dp, 140.dp)
            } else {
                (maxWidth * 0.25f).coerceIn(70.dp, 110.dp)
            }
            (baseDp.toPx() / density.fontScale).toSp()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Label & Session Counter Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "FOCUS TIME",
                    color = PomodoroBrickRed.copy(alpha = 0.85f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RobotoFont,
                    letterSpacing = 2.sp
                )
                if (completedSessions > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PomodoroBrickRed)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✓ $completedSessions",
                            color = PomodoroPeachBg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RobotoFont
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Time Display
            Text(
                text = timeFormatted,
                color = PomodoroBrickRed,
                fontSize = pomoFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoFont,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Button(
                    onClick = { viewModel.togglePomodoro() },
                    colors = ButtonDefaults.buttonColors(containerColor = PomodoroBrickRed),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = PomodoroPeachBg,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "PAUSE" else "START",
                        color = PomodoroPeachBg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RobotoFont
                    )
                }

                // Reset Button
                IconButton(
                    onClick = { viewModel.resetPomodoro() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PomodoroYellowAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Pomodoro",
                        tint = PomodoroBrickRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
