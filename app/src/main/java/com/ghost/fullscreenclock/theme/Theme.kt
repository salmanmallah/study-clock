package com.ghost.fullscreenclock.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = CharcoalCard,
    onSurface = PureWhite
)

@Composable
fun FullscreenClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
