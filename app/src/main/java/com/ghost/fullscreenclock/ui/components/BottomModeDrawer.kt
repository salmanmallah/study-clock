package com.ghost.fullscreenclock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.model.ClockMode
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.RobotoFont

private val DigitalClockModes = setOf(
    ClockMode.DIGITAL_12,
    ClockMode.DIGITAL_24,
    ClockMode.SECONDS_12,
    ClockMode.SECONDS_24
)

private val DrawerModes = listOf(
    ClockMode.DIGITAL_24,
    ClockMode.FLIP_CLOCK,
    ClockMode.POMODORO,
    ClockMode.STOPWATCH,
    ClockMode.TIMER,
    ClockMode.SETTINGS
)

private val IconBadgeBackground = Color(0xFF0C0C0E)
private val IconBadgeBorder = Color(0xFF26262B)
private val TomatoAccent = Color(0xFFFF5A3C)
private val StartGreen = Color(0xFF34C759)
private val WarningAmber = Color(0xFFFFCC00)
private val StopRed = Color(0xFFFF3B30)

@Composable
fun BottomModeDrawer(
    visible: Boolean,
    currentMode: ClockMode,
    onModeSelected: (ClockMode) -> Unit,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
        modifier = modifier
    ) {
        val scrollState = rememberScrollState()

        LaunchedEffect(scrollState.isScrollInProgress) {
            if (scrollState.isScrollInProgress) {
                onInteraction()
            }
        }

        val containerInteractionSource = remember { MutableInteractionSource() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureBlack.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = Color(0xFF222222),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .clickable(
                    indication = null,
                    interactionSource = containerInteractionSource,
                    onClick = { onInteraction() }
                )
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DrawerModes.forEach { mode ->
                    val isDigitalEntry = mode == ClockMode.DIGITAL_24
                    val isSelected = if (isDigitalEntry) {
                        currentMode in DigitalClockModes
                    } else {
                        mode == currentMode
                    }
                    val displayTitle = if (isDigitalEntry) "Digital Clock" else mode.title

                    ModeTabItem(
                        mode = mode,
                        title = displayTitle,
                        isSelected = isSelected,
                        onClick = {
                            onInteraction()
                            onModeSelected(mode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeTabItem(
    mode: ClockMode,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PureWhite else Color(0xFF242428)
    val backgroundColor = if (isSelected) CharcoalCard else Color(0xFF141417)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(102.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(IconBadgeBackground)
                .border(1.dp, IconBadgeBorder, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            DrawerModeIcon(mode = mode, isSelected = isSelected)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            color = if (isSelected) PureWhite else PureWhite.copy(alpha = 0.72f),
            fontSize = 11.sp,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = RobotoFont
        )
    }
}

@Composable
private fun DrawerModeIcon(
    mode: ClockMode,
    isSelected: Boolean
) {
    val density = LocalDensity.current

    when (mode) {
        ClockMode.DIGITAL_12,
        ClockMode.DIGITAL_24,
        ClockMode.SECONDS_12,
        ClockMode.SECONDS_24 -> {
            // 1. Digital Clock Icon: Crisp OLED digital display badge
            val fontSize = with(density) { (13.dp.toPx() / density.fontScale).toSp() }
            Text(
                text = "09:41",
                color = PureWhite,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }

        ClockMode.FLIP_CLOCK -> {
            // 2. Flip Clock Icon: Two miniature split-flap cards with center seam
            val flapFontSize = with(density) { (11.dp.toPx() / density.fontScale).toSp() }
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniFlipCardBadge(text = "09", fontSize = flapFontSize)
                MiniFlipCardBadge(text = "41", fontSize = flapFontSize)
            }
        }

        ClockMode.POMODORO -> {
            // 3. Pomodoro Icon: Tomato-red focus ring with top leaf accent & center dot
            Canvas(modifier = Modifier.size(30.dp)) {
                val stroke = 3.2.dp.toPx()
                val radius = (size.minDimension - stroke) / 2f
                val center = Offset(size.width / 2f, size.height / 2f + 1.dp.toPx())

                // Background ring track
                drawCircle(
                    color = Color(0xFF2C2C30),
                    radius = radius,
                    center = center,
                    style = Stroke(width = stroke)
                )
                // Tomato progress arc
                drawArc(
                    color = TomatoAccent,
                    startAngle = -90f,
                    sweepAngle = 275f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Green stem/leaf accent at top
                drawLine(
                    color = StartGreen,
                    start = Offset(center.x, center.y - radius - 3.dp.toPx()),
                    end = Offset(center.x + 2.5.dp.toPx(), center.y - radius - 0.5.dp.toPx()),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Center focus dot
                drawCircle(
                    color = TomatoAccent,
                    radius = 2.8.dp.toPx(),
                    center = center
                )
            }
        }

        ClockMode.STOPWATCH -> {
            // 4. Stopwatch Icon: Precision stopwatch dial with crown button & green sweep hand
            Canvas(modifier = Modifier.size(30.dp)) {
                val stroke = 2.6.dp.toPx()
                val radius = (size.minDimension - stroke) / 2f - 2.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f + 2.dp.toPx())

                // Top crown stem & T-bar
                val crownTopY = center.y - radius - 4.dp.toPx()
                drawLine(
                    color = PureWhite,
                    start = Offset(center.x - 3.5.dp.toPx(), crownTopY),
                    end = Offset(center.x + 3.5.dp.toPx(), crownTopY),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = PureWhite,
                    start = Offset(center.x, crownTopY),
                    end = Offset(center.x, center.y - radius),
                    strokeWidth = 2.2.dp.toPx()
                )

                // Angled side lap pusher (top-right)
                drawLine(
                    color = StartGreen,
                    start = Offset(center.x + radius * 0.68f, center.y - radius * 0.68f),
                    end = Offset(center.x + radius * 0.95f, center.y - radius * 0.95f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Outer stopwatch bezel
                drawCircle(
                    color = PureWhite.copy(alpha = 0.88f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = stroke)
                )

                // Green sweeping hand pointing to ~2 o'clock
                drawLine(
                    color = StartGreen,
                    start = center,
                    end = Offset(center.x + radius * 0.52f, center.y - radius * 0.45f),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = StartGreen,
                    radius = 2.2.dp.toPx(),
                    center = center
                )
            }
        }

        ClockMode.TIMER -> {
            // 5. Timer Icon: Clockwise depleting ring (Amber -> Red accent) with inner timer hand
            Canvas(modifier = Modifier.size(30.dp)) {
                val stroke = 3.2.dp.toPx()
                val radius = (size.minDimension - stroke) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)

                // Dark track
                drawCircle(
                    color = Color(0xFF2C2C30),
                    radius = radius,
                    center = center,
                    style = Stroke(width = stroke)
                )
                // Clockwise remaining arc on left/bottom in WarningAmber
                drawArc(
                    color = WarningAmber,
                    startAngle = 35f,
                    sweepAngle = 235f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Red alert tip at 12 o'clock
                drawCircle(
                    color = StopRed,
                    radius = 2.2.dp.toPx(),
                    center = Offset(center.x, center.y - radius)
                )
                // Inner countdown pointer
                drawLine(
                    color = PureWhite,
                    start = center,
                    end = Offset(center.x, center.y - radius * 0.48f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = WarningAmber,
                    start = center,
                    end = Offset(center.x + radius * 0.42f, center.y + radius * 0.25f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        ClockMode.SETTINGS -> {
            // 6. Settings Icon: Proper Gear / Cogwheel vector (replaces wrong user avatar)
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = if (isSelected) PureWhite else Color(0xFFD1D1D6),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun MiniFlipCardBadge(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Box(
        modifier = Modifier
            .width(18.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF252529)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = PureWhite,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = RobotoFont,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
        // Center horizontal split-flap seam
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(IconBadgeBackground)
                .align(Alignment.Center)
        )
    }
}
