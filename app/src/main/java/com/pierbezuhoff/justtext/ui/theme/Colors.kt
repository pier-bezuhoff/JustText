package com.pierbezuhoff.justtext.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// primary: #1A4CAB
// secondary: #769CDF
// -------   light-L  dark-L
// primary     40       80
// on-prim    100       20
// pri-cont    90       90
// on-pri-c    10       30
private val primaryLight = Color(0xFF_1a4cab)
private val onPrimaryLight = Color(0xFF_ffffff)
private val primaryContainerLight = Color(0xFF_003587)
private val onPrimaryContainerLight = Color(0xFF_afc4ff) // low contrast
private val secondaryLight = Color(0xFF_4FC3F7)
private val onSecondaryLight = Color(0xFF_01579B) // low contrast
private val secondaryContainerLight = Color(0xFF_0277BD)
private val onSecondaryContainerLight = Color(0xFF_E1F5FE)

private val primaryDark = Color(0xFF_b2c5ff)
private val onPrimaryDark = Color(0xFF_002c72)
private val primaryContainerDark = Color(0xFF_1a4cab)
private val onPrimaryContainerDark = Color(0xFF_afc4ff)
private val secondaryDark = Color(0xFF_0288D1)
private val onSecondaryDark = Color(0xFF_B3E5FC)
private val secondaryContainerDark = Color(0xFF_01579B)
private val onSecondaryContainerDark = Color(0xFF_B3E5FC)

internal val LightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
)

internal val DarkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
)

object JustTextColors {
    val peachyPink = Color(0xFF_FFC0CB)
    val pinkishRed = Color(0xFF_FF7373)
    val venousBloodRed = Color(0xFF_800000)
    val deepBrown = Color(0xFF_321E1E)
    val orange = Color(0xFF_F08A5D)
    val orangeOrange = Color(0xFF_FFA500) // my naming is very original
    val goldenBananaYellow = Color(0xFF_FFD700)
    val veryDarkForestyGreen = Color(0xFF_065535)
    val aquamarine = Color(0xFF_08D9D6) // visually similar to cyan
    val teal = Color(0xFF_008080)
    val darkPurple = Color(0xFF_6A2C70)
    val skyBlue = Color(0xff_2ca3ff)
}
