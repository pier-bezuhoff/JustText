package com.pierbezuhoff.justtext.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun TextScreen(
    initialText: String,
    initialCursorLocation: Int,
    textColor: Color,
    textBackgroundColor: Color,
    onNewText: (String) -> Unit,
    onNewCursorLocation: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var tfValue by remember(initialText, initialCursorLocation) { mutableStateOf(
        TextFieldValue(initialText, selection = TextRange(initialCursorLocation))
    ) }
    val tfStyle = MaterialTheme.typography.bodyLarge.copy(
        color = textColor,
        lineBreak = LineBreak.Paragraph,
    )
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
//    val focusManager = LocalFocusManager.current
//    var isFocusable by remember { mutableStateOf(false) }
    PatchedBasicTextField(
        tfValue,
        onValueChange = {
            tfValue = it
            onNewText(it.text)
            onNewCursorLocation(it.selection.start)
        },
        modifier = modifier,
        textStyle = tfStyle,
        minLines = 70,
        maxLines = Int.MAX_VALUE,
        cursorBrush = SolidColor(textColor),
    )

//    BasicTextField(
//        tfValue,
//        onValueChange = {
//            tfValue = it
//            onNewText(it.text)
//            onNewCursorLocation(it.selection.start)
//        },
//        modifier = modifier,
//        textStyle = tfStyle,
//        minLines = 70,
//        maxLines = Int.MAX_VALUE,
//        cursorBrush = SolidColor(textColor),
//    )

//    TextField(
//        tfValue,
//        onValueChange = {
//            tfValue = it
//            onNewText(it.text)
//            onNewCursorLocation(it.selection.start)
//        },
//        modifier = modifier
////                    .focusProperties { canFocus = isFocusable }
////                    .focusRequester(focusRequester)
////                    .fillMaxHeight()
//        ,
//        textStyle = tfStyle,
//        minLines = 70,
//        maxLines = Int.MAX_VALUE,
//        colors = TextFieldDefaults.colors().copy(
//            focusedContainerColor = textBackgroundColor,
//            unfocusedContainerColor = textBackgroundColor,
//            cursorColor = textColor,
//        )
//    )

    LaunchedEffect(Unit) {
//        focusRequester.freeFocus()
//        focusRequester.requestFocus(FocusDirection.Exit)
//        focusManager.clearFocus()
//        isFocusable = true
    }
    LaunchedEffect(tfValue.selection) {
    }
}
