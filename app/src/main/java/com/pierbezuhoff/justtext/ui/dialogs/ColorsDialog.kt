package com.pierbezuhoff.justtext.ui.dialogs

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private enum class ColorsDialogType {
    TEXT,
    TEXT_BACKGROUND,
    BACKGROUND,
}

@Composable
fun ColorsDialog(
    textColor: Color,
    textBackgroundColor: Color,
    backgroundColor: Color,
    onNewTextColor: (Color) -> Unit,
    onNewTextBackgroundColor: (Color) -> Unit,
    onNewBackgroundColor: (Color) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openedDialogType: ColorsDialogType? by remember { mutableStateOf(null) }
    val buttonModifier = Modifier.fillMaxWidth()
    val buttonColors = ButtonDefaults.textButtonColors()
        .copy(
            containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Column(
                Modifier
                    .width(IntrinsicSize.Max)
                    .padding(16.dp)
            ) {
                TextButton(
                    onClick = {
                        openedDialogType = ColorsDialogType.TEXT
                    },
                    modifier = buttonModifier,
                    colors = buttonColors,
                ) {
                    Text("Change text color")
                }
                TextButton(
                    onClick = {
                        openedDialogType = ColorsDialogType.TEXT_BACKGROUND
                    },
                    modifier = buttonModifier,
                    colors = buttonColors,
                ) {
                    Text("Change text background color")
                }
                TextButton(
                    onClick = {
                        openedDialogType = ColorsDialogType.BACKGROUND
                    },
                    modifier = buttonModifier,
                    colors = buttonColors,
                ) {
                    Text("Change background color")
                }
            }
        }
    }
    when (openedDialogType) {
        ColorsDialogType.TEXT -> {
            ColorPickerDialog(
                textColor,
                onCancel = {
                    openedDialogType = null
                },
                onConfirm = { color ->
                    openedDialogType = null
                    onNewTextColor(color)
                },
                showAlphaBar = false,
            )
        }
        ColorsDialogType.TEXT_BACKGROUND -> {
            ColorPickerDialog(
                textBackgroundColor,
                onCancel = {
                    openedDialogType = null
                },
                onConfirm = { color ->
                    openedDialogType = null
                    onNewTextBackgroundColor(color)
                },
                showAlphaBar = true,
            )
        }
        ColorsDialogType.BACKGROUND -> {
            ColorPickerDialog(
                backgroundColor,
                onCancel = {
                    openedDialogType = null
                },
                onConfirm = { color ->
                    openedDialogType = null
                    onNewBackgroundColor(color)
                },
                showAlphaBar = false,
            )
        }
        null -> {}
    }
}