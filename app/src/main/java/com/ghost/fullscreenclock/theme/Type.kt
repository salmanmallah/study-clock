package com.ghost.fullscreenclock.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.ghost.fullscreenclock.R

/**
 * Roboto Variable Font Family with explicit FontVariation.weight settings
 * for each weight tier (Thin 100 to Black 900).
 *
 * This resolves the issue where a variable font resource without variationSettings
 * defaulted to weight 400 (Regular) across all Compose Text calls.
 */
@OptIn(ExperimentalTextApi::class)
val RobotoFont = FontFamily(
    Font(
        resId = R.font.roboto_regular,
        weight = FontWeight.Thin,
        variationSettings = FontVariation.Settings(FontVariation.weight(100))
    ),
    Font(
        resId = R.font.roboto_regular,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        resId = R.font.roboto_regular,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.roboto_regular,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.roboto_regular,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.roboto_regular,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.roboto_regular,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    )
)
