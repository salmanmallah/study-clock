package com.ghost.fullscreenclock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.ghost.fullscreenclock.theme.CardSeamDivider
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.RobotoFont

@Composable
fun FlipCard(
    value: String,
    cardWidth: Dp,
    cardHeight: Dp,
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableStateOf(value) }
    var previousValue by remember { mutableStateOf(value) }
    val flipProgress = remember { Animatable(180f) }

    LaunchedEffect(value) {
        if (value != currentValue) {
            previousValue = currentValue
            currentValue = value
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }

    val density = LocalDensity.current
    val halfHeight = cardHeight / 2
    val topShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val bottomShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 14.dp, bottomEnd = 14.dp)

    // Calculate dp-derived font size that ignores system fontScale to prevent overflowing card
    val fontSize: TextUnit = with(density) {
        val baseDp = cardHeight * 0.58f
        (baseDp.toPx() / density.fontScale).toSp()
    }

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
    ) {
        // Upper & Lower Halves Container (total height exactly equals cardHeight)
        Column(modifier = Modifier.fillMaxSize()) {
            // ================= 1. TOP HALF =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(halfHeight)
            ) {
                // Static background: Top half of CURRENT new value
                HalfCardContent(
                    text = currentValue,
                    isTop = true,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    fontSize = fontSize,
                    shape = topShape,
                    modifier = Modifier.fillMaxSize()
                )

                // Flipping top flap: Top half of PREVIOUS value (rotates 0° -> -90°)
                // Reading flipProgress inside graphicsLayer lambda to prevent recomposition on every tick
                HalfCardContent(
                    text = previousValue,
                    isTop = true,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    fontSize = fontSize,
                    shape = topShape,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val angle = flipProgress.value
                            rotationX = -angle
                            alpha = if (angle < 90f) 1f else 0f
                            transformOrigin = TransformOrigin(0.5f, 1.0f)
                            cameraDistance = 16f * this.density
                        }
                )
            }

            // ================= 2. BOTTOM HALF =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(halfHeight)
            ) {
                // Static background: Bottom half of PREVIOUS value while flipping, else CURRENT
                HalfCardContent(
                    text = if (flipProgress.value < 180f) previousValue else currentValue,
                    isTop = false,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    fontSize = fontSize,
                    shape = bottomShape,
                    modifier = Modifier.fillMaxSize()
                )

                // Falling bottom flap: Bottom half of CURRENT value (rotates 90° -> 0°)
                HalfCardContent(
                    text = currentValue,
                    isTop = false,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    fontSize = fontSize,
                    shape = bottomShape,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val angle = 180f - flipProgress.value
                            rotationX = angle
                            alpha = if (flipProgress.value >= 90f) 1f else 0f
                            transformOrigin = TransformOrigin(0.5f, 0.0f)
                            cameraDistance = 16f * this.density
                        }
                )
            }
        }

        // 3dp Center Seam Overlaid exactly across the card center (does not increase total cardHeight)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.Center)
                .background(CardSeamDivider)
        )

        // Mechanical side hinge notches
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(12.dp)
                .align(Alignment.CenterStart)
                .background(CardSeamDivider, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
        )
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(12.dp)
                .align(Alignment.CenterEnd)
                .background(CardSeamDivider, RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
        )
    }
}

@Composable
private fun HalfCardContent(
    text: String,
    isTop: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    fontSize: TextUnit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(CharcoalCard)
            .clipToBounds()
    ) {
        // Inner Box of full cardHeight aligned so only top or bottom half is visible
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .align(if (isTop) Alignment.TopCenter else Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = PureWhite,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoFont,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}
