package com.ghost.fullscreenclock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.model.ClockMode
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite

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
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClockMode.entries.forEach { mode ->
                    val isSelected = mode == currentMode
                    ModeTabItem(
                        mode = mode,
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
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PureWhite else Color.Transparent
    val backgroundColor = if (isSelected) CharcoalCard else Color(0xFF161616)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Image(
            painter = painterResource(id = mode.iconRes),
            contentDescription = mode.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = mode.title,
            color = if (isSelected) PureWhite else PureWhite.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = com.ghost.fullscreenclock.theme.RobotoFont
        )
    }
}
