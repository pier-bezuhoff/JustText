package com.pierbezuhoff.justtext.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// primary: #1A4CAB
// secondary: #769CDF
// -------   light-L  dark-L
// primary     40       80
// on-prim    100       20
// pri-cont    90       30
// on-pri-c    10       90
// inv-prim    80       40
private val primaryLight = Color(0xFF_003587)
private val onPrimaryLight = Color(0xFF_ffffff)
private val primaryContainerLight = Color(0xFF_1a4cab)
private val onPrimaryContainerLight = Color(0xFF_afc4ff)
private val secondaryLight = Color(0xFF_4FC3F7)
private val onSecondaryLight = Color(0xFF_01579B)
private val secondaryContainerLight = Color(0xFF_0277BD)
private val onSecondaryContainerLight = Color(0xFF_E1F5FE)

private val primaryDark = Color(0xFF_b2c5ff)
private val onPrimaryDark = Color(0xFF_002c72)
private val primaryContainerDark = Color(0xFF_1a4cab)
private val onPrimaryContainerDark = Color(0xFF_afc4ff)
private val secondaryDark = Color(0xFF_0288D1)
private val onSecondaryDark = Color(0xFF_B3E5FC)
private val secondaryContainerDark = Color(0xFF_B3E5FC)
private val onSecondaryContainerDark = Color(0xFF_01579B)

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

// old palette
private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Pink80 = Color(0xFFEFB8C8)

private val Purple40 = Color(0xFF6650a4)
private val PurpleGrey40 = Color(0xFF625b71)
private val Pink40 = Color(0xFF7D5260)

object JustTextColors {
    val b = Color(0xFF_36618e)
    val g = Color(0xFF_4c662b)
    val t = Color(0xFF_6b5f10)
//    val c = Color(0xFF_)

    val CyanBlue = Color(0xFF_2196f3) // dark primary
    val Lime = Color(0xFF_b6fa64) // dark secondary
    val Yellow = Color(0xFF_fae466) // dark tertiary
}
