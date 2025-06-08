package com.pierbezuhoff.justtext.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

@Composable
fun TextScreen(
    initialText: String,
    initialCursorLocation: Int,
    fontSize: Int,
    textColor: Color,
    textBackgroundColor: Color,
    onNewText: (String) -> Unit,
    onNewCursorLocation: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var tfValue by remember(initialText, initialCursorLocation) { mutableStateOf(
        TextFieldValue(initialText, selection = TextRange(initialCursorLocation))
    ) }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = textColor,
        fontSize = fontSize.sp,
        lineBreak = LineBreak.Paragraph,
    )
    PatchedBasicTextField(
        tfValue,
        onValueChange = {
            tfValue = it
            onNewText(it.text)
            onNewCursorLocation(it.selection.start)
        },
        modifier = modifier,
        textStyle = textStyle,
        minLines = 70,
        maxLines = Int.MAX_VALUE,
        cursorBrush = SolidColor(textColor),
        containerColor = textBackgroundColor,
    )
}
