package com.pierbezuhoff.justtext.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class ColorTheme {
    Light, Dark,
    Auto,
    // Dynamic color is available on Android 12+
    Dynamic,
}

@Composable
fun JustTextTheme(
    colorTheme: ColorTheme = ColorTheme.Auto,
    content: @Composable () -> Unit
) {
    val darkTheme = when (colorTheme) {
        ColorTheme.Light -> false
        ColorTheme.Dark -> true
        ColorTheme.Auto -> isSystemInDarkTheme()
        ColorTheme.Dynamic -> isSystemInDarkTheme()
    }
    val dynamicColor = colorTheme == ColorTheme.Dynamic
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}