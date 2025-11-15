package com.pierbezuhoff.justtext.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

@Preview
@Composable
private fun ColorsTest() {
    JustTextTheme(colorTheme = ColorTheme.Light) {
        Surface(
            Modifier.size(600.dp, 200.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row {
                Column(
                    Modifier.padding(8.dp).width(180.dp),
                ) {
                    Text("Primary", color = MaterialTheme.colorScheme.primary)
                    TextButton(
                        onClick = {},
                        colors = ButtonDefaults.textButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    ) {
                        Text("Sample on primary")
                    }
                    TextButton(
                        onClick = {},
                        colors = ButtonDefaults.textButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    ) {
                        Text("Sample on primary container")
                    }
                }
                Column(
                    Modifier.padding(8.dp).width(250.dp),
                ) {
                    Text("Secondary", color = MaterialTheme.colorScheme.secondary)
                    TextButton(
                        onClick = {},
                        colors = ButtonDefaults.textButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        )
                    ) {
                        Text("Sample on secondary")
                    }
                    TextButton(
                        onClick = {},
                        colors = ButtonDefaults.textButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    ) {
                        Text("Sample on secondary container")
                    }
                }
            }
        }
    }
}