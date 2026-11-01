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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.ui.viewmodel.TimersViewModel

@Composable
fun TimerScreen(
    viewModel: TimersViewModel,
    onScreenTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val setMinutes by viewModel.timerSetMinutes.collectAsState()
    val remainingMillis by viewModel.timerRemainingMillis.collectAsState()
    val isRunning by viewModel.timerRunning.collectAsState()
    val canAdjust by viewModel.canAdjustTimer.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    val totalMillis = (setMinutes * 60 * 1000L).coerceAtLeast(1000L)
    val progress = (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)

    val remainingTotalSec = (remainingMillis + 999) / 1000
    val displayMin = remainingTotalSec / 60
    val displaySec = remainingTotalSec % 60
    val timeFormatted = String.format("%02d:%02d", displayMin, displaySec)

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

        // Responsive circle sizing derived from available viewport height/width
        val dialSize = if (isLandscape) {
            (maxHeight * 0.68f).coerceIn(160.dp, 240.dp)
        } else {
            (maxWidth * 0.65f).coerceIn(180.dp, 280.dp)
        }

        // Fixed dp-derived font size ignoring system fontScale
        val timerFontSize = with(density) {
            val baseDp = dialSize * 0.22f
            (baseDp.toPx() / density.fontScale).toSp()
        }

        if (isLandscape) {
            // Landscape layout: Row with dial on left and controls on right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Dial
                Box(
                    modifier = Modifier.size(dialSize),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = PureWhite,
                        strokeWidth = 6.dp,
                        trackColor = CharcoalCard
                    )

                    Text(
                        text = timeFormatted,
                        color = PureWhite,
                        fontSize = timerFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                // Controls Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Duration Adjuster
                    if (canAdjust) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.adjustTimerMinutes(-1) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalCard)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Minute",
                                    tint = PureWhite
                                )
                            }

                            Text(
                                text = "$setMinutes min",
                                color = PureWhite.copy(alpha = 0.85f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            IconButton(
                                onClick = { viewModel.adjustTimerMinutes(1) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalCard)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Minute",
                                    tint = PureWhite
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isRunning) "Timer Running" else "Reset to change duration",
                            color = PureWhite.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play / Pause Button
                        Button(
                            onClick = { viewModel.toggleTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color(0xFFFF3B30) else Color(0xFF34C759)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .width(135.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Start",
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "PAUSE" else "START",
                                color = PureWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Reset Button
                        IconButton(
                            onClick = { viewModel.resetTimer() },
                            modifier = Modifier
                                .size(50.dp)
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
        } else {
            // Portrait Layout: Stacked Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(dialSize),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = PureWhite,
                        strokeWidth = 6.dp,
                        trackColor = CharcoalCard
                    )

                    Text(
                        text = timeFormatted,
                        color = PureWhite,
                        fontSize = timerFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (canAdjust) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.adjustTimerMinutes(-1) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CharcoalCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease Minute",
                                tint = PureWhite
                            )
                        }

                        Text(
                            text = "$setMinutes min",
                            color = PureWhite.copy(alpha = 0.85f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            onClick = { viewModel.adjustTimerMinutes(1) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CharcoalCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase Minute",
                                tint = PureWhite
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (isRunning) "Timer Running" else "Reset to change duration",
                        color = PureWhite.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color(0xFFFF3B30) else Color(0xFF34C759)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .width(135.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                            .size(52.dp)
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
    }
}
