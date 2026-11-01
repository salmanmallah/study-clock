package com.ghost.fullscreenclock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.RobotoFont

// Two-tone charcoal flap backgrounds matching original app:
// Top half RGB [34, 34, 34] (#222222), Bottom half RGB [25, 25, 25] (#191919)
private val CardTopBackground = Color(0xFF222222)
private val CardBottomBackground = Color(0xFF191919)
private val SeamColor = Color(0xFF050505)
private val DigitOffWhite = Color(0xFFF3F3F3)
private val CornerLabelColor = Color(0xFFBDBDBD)

@Composable
fun FlipCard(
    value: String,
    cardWidth: Dp,
    cardHeight: Dp,
    modifier: Modifier = Modifier,
    bottomCornerLabel: String? = null,
    seamHeight: Dp = 4.dp,
    cornerRadius: Dp = 16.dp
) {
    var currentValue by remember { mutableStateOf(value) }
    var previousValue by remember { mutableStateOf(value) }
    var currentLabel by remember { mutableStateOf(bottomCornerLabel) }
    var previousLabel by remember { mutableStateOf(bottomCornerLabel) }
    val flipProgress = remember { Animatable(180f) }

    LaunchedEffect(value, bottomCornerLabel) {
        if (value != currentValue || bottomCornerLabel != currentLabel) {
            previousValue = currentValue
            previousLabel = currentLabel
            currentValue = value
            currentLabel = bottomCornerLabel
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
    }

    val density = LocalDensity.current
    val cardHeightPx = with(density) { cardHeight.roundToPx() }
    val seamHeightPx = with(density) { seamHeight.roundToPx() }
    val topHalfHeightPx = (cardHeightPx - seamHeightPx) / 2
    val bottomHalfHeightPx = cardHeightPx - seamHeightPx - topHalfHeightPx

    val topHalfHeight = with(density) { topHalfHeightPx.toDp() }
    val bottomHalfHeight = with(density) { bottomHalfHeightPx.toDp() }

    val topShape = RoundedCornerShape(
        topStart = cornerRadius,
        topEnd = cornerRadius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    val bottomShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = cornerRadius,
        bottomEnd = cornerRadius
    )

    // Font size tuned so glyph cap height is ~51.4% of card height, matching original
    val fontSize: TextUnit = with(density) {
        val basePx = cardHeightPx * 0.70f
        (basePx / density.fontScale).toSp()
    }

    val isAnimating = flipProgress.value < 180f

    Column(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
    ) {
        // ================= 1. TOP HALF =================
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(topHalfHeight)
        ) {
            // Static background: Top half of CURRENT value
            HalfCardContent(
                text = currentValue,
                isTop = true,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                halfHeight = topHalfHeight,
                seamHeight = seamHeight,
                fontSize = fontSize,
                shape = topShape,
                bottomCornerLabel = currentLabel
            )

            // Folding flap (during 0° -> 90°): Top half of PREVIOUS value
            if (isAnimating && flipProgress.value < 90f) {
                val angle = flipProgress.value
                val shadowAlpha = (angle / 90f) * 0.35f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = -angle
                            transformOrigin = TransformOrigin(0.5f, 1.0f)
                            cameraDistance = 16f * this.density
                        }
                ) {
                    HalfCardContent(
                        text = previousValue,
                        isTop = true,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        halfHeight = topHalfHeight,
                        seamHeight = seamHeight,
                        fontSize = fontSize,
                        shape = topShape,
                        bottomCornerLabel = previousLabel
                    )
                    // Folding shadow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(topShape)
                            .background(Color.Black.copy(alpha = shadowAlpha))
                    )
                }
            }
        }

        // ================= CENTER SEAM GAP =================
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(seamHeight)
                .background(SeamColor)
        )

        // ================= 2. BOTTOM HALF =================
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(bottomHalfHeight)
        ) {
            // Static background: Bottom half of PREVIOUS value while flipping, else CURRENT
            HalfCardContent(
                text = if (isAnimating) previousValue else currentValue,
                isTop = false,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                halfHeight = bottomHalfHeight,
                seamHeight = seamHeight,
                fontSize = fontSize,
                shape = bottomShape,
                bottomCornerLabel = if (isAnimating) previousLabel else currentLabel
            )

            // Falling flap (during 90° -> 180°): Bottom half of CURRENT value
            if (isAnimating && flipProgress.value >= 90f) {
                val angle = 180f - flipProgress.value
                val shadowAlpha = (angle / 90f) * 0.35f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = angle
                            transformOrigin = TransformOrigin(0.5f, 0.0f)
                            cameraDistance = 16f * this.density
                        }
                ) {
                    HalfCardContent(
                        text = currentValue,
                        isTop = false,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        halfHeight = bottomHalfHeight,
                        seamHeight = seamHeight,
                        fontSize = fontSize,
                        shape = bottomShape,
                        bottomCornerLabel = currentLabel
                    )
                    // Landing shadow that lightens to zero
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(bottomShape)
                            .background(Color.Black.copy(alpha = shadowAlpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun HalfCardContent(
    text: String,
    isTop: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    halfHeight: Dp,
    seamHeight: Dp,
    fontSize: TextUnit,
    shape: RoundedCornerShape,
    bottomCornerLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(cardWidth)
            .height(halfHeight)
            .clip(shape)
            .background(if (isTop) CardTopBackground else CardBottomBackground)
    ) {
        Box(
            modifier = Modifier
                .layout { measurable, _ ->
                    val placeable = measurable.measure(
                        Constraints.fixed(
                            width = cardWidth.roundToPx(),
                            height = cardHeight.roundToPx()
                        )
                    )
                    val targetHeight = halfHeight.roundToPx()
                    val seamPx = seamHeight.roundToPx()
                    layout(placeable.width, targetHeight) {
                        val yOffset = if (isTop) 0 else -(targetHeight + seamPx)
                        placeable.place(0, yOffset)
                    }
                }
                .width(cardWidth)
                .height(cardHeight)
        ) {
            Text(
                text = text,
                color = DigitOffWhite,
                fontSize = fontSize,
                fontWeight = FontWeight.Light,
                fontFamily = RobotoFont,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                modifier = Modifier.align(Alignment.Center)
            )

            if (bottomCornerLabel != null) {
                Text(
                    text = bottomCornerLabel,
                    color = CornerLabelColor,
                    fontSize = with(LocalDensity.current) {
                        ((cardHeight * 0.08f).toPx() / fontScale).toSp()
                    },
                    fontWeight = FontWeight.Medium,
                    fontFamily = RobotoFont,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = cardWidth * 0.077f,
                            bottom = cardHeight * 0.068f
                        )
                )
            }
        }
    }
}
