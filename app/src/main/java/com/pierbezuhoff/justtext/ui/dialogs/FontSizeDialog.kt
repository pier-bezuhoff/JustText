package com.pierbezuhoff.justtext.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pierbezuhoff.justtext.R
import com.pierbezuhoff.justtext.ui.CancelButton
import com.pierbezuhoff.justtext.ui.DialogTitle
import com.pierbezuhoff.justtext.ui.OkButton
import kotlin.math.roundToInt

private const val MIN_FONT_SIZE = 10 //15
private const val MAX_FONT_SIZE = 60 //33

@Composable
fun FontSizeDialog(
    fontSize: Int,
    setFontSize: (Int) -> Unit,
    onCancel: () -> Unit,
) {
    val fontSizeState = remember(fontSize) { mutableFloatStateOf(fontSize.toFloat()) }
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DialogTitle(
                    R.string.font_size_title,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Slider(
                    value = fontSizeState.floatValue,
                    onValueChange = { fontSizeState.floatValue = it },
                    valueRange = MIN_FONT_SIZE.toFloat() .. MAX_FONT_SIZE.toFloat(),
                    steps = (MAX_FONT_SIZE - MIN_FONT_SIZE) - 1,
                    colors = SliderDefaults.colors(),
                )
                BasicText(
                    text = fontSizeState.floatValue.roundToInt().toString(),
                    style = MaterialTheme.typography.titleMedium
                        .copy(color = MaterialTheme.colorScheme.primary),
                )
                Box(Modifier.fillMaxWidth().height(8.dp))
                Row(Modifier,
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CancelButton(onDismissRequest = onCancel)
                    OkButton(onConfirm = { setFontSize(fontSizeState.floatValue.roundToInt()) })
                }
            }
        }
    }
}