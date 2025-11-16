package com.pierbezuhoff.justtext.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch

// reference: https://issuetracker.google.com/issues/237190748#comment4
/** [BasicTextField] wrapper that fixes scroll-cursor-into-view-on-ime-keyboard-open bug */
@Composable
fun PatchedBasicTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    containerColor: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    scrollState: ScrollState = rememberScrollState(),
) {
    val coroutineScope = rememberCoroutineScope()
    var height by remember { mutableIntStateOf(0) }
    var layoutResult: TextLayoutResult? by remember { mutableStateOf(null) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                coroutineScope.launch {
                    val cursorInView = value.isCursorInView(
                        layoutResult = layoutResult!!,
                        height = it.height.toFloat(),
                        scrollValue = scrollState.value.toFloat()
                    )
                    if (!cursorInView && height > it.height) {
                        scrollState.scrollBy(
                            value.calculateRequiredSizeScroll(
                                layoutResult = layoutResult!!,
                                oldHeight = height.toFloat(),
                                newHeight = it.height.toFloat(),
                                scrollValue = scrollState.value.toFloat()
                            )
                        )
                    }
                    height = it.height
                }
            }
        ,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = { layoutResult = it },
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier
                .background(containerColor, shape)
                .verticalScroll(scrollState)
            ) {
                innerTextField()
            }
        },
    )
    LaunchedEffect(value.selection) {
        val cursorInView = value.isCursorInView(
            layoutResult = layoutResult!!,
            height = height.toFloat(),
            scrollValue = scrollState.value.toFloat()
        )
        if (!cursorInView) {
            scrollState.scrollBy(
                value.calculateRequiredSelectionScroll(
                    layoutResult = layoutResult!!,
                    height = height.toFloat(),
                    scrollValue = scrollState.value.toFloat()
                )
            )
        }
    }
}

private fun TextFieldValue.isCursorInView(
    layoutResult: TextLayoutResult,
    height: Float,
    scrollValue: Float
) = with(layoutResult) {
    val currentLine = try {
        getLineForOffset(selection.min)
    } catch (_: IllegalArgumentException) {
        System.err.println("Corrected Wrong Offset!")
        getLineForOffset(selection.min - 1)
    }
    val lineBottom = getLineBottom(currentLine)
    val lineTop = getLineTop(currentLine)
    lineBottom <= height + scrollValue && lineTop >= scrollValue
}

private fun TextFieldValue.calculateRequiredSelectionScroll(
    layoutResult: TextLayoutResult,
    height: Float,
    scrollValue: Float
) = with(layoutResult) {
    val currentLine = try {
        getLineForOffset(selection.min)
    } catch (_: IllegalArgumentException) {
        System.err.println("Corrected Wrong Offset!")
        getLineForOffset(selection.min - 1)
    }
    val lineTop = getLineTop(currentLine)
    val lineBottom = getLineBottom(currentLine)
    if (lineTop < scrollValue) -(scrollValue - lineTop)
    else if (lineBottom > height + scrollValue) lineBottom - (height + scrollValue)
    else 0f
}

private fun TextFieldValue.calculateRequiredSizeScroll(
    layoutResult: TextLayoutResult,
    oldHeight: Float,
    newHeight: Float,
    scrollValue: Float
) = with(layoutResult) {
    val currentLine = try {
        getLineForOffset(selection.min)
    } catch (_: IllegalArgumentException) {
        System.err.println("Corrected Wrong Offset!")
        getLineForOffset(selection.min - 1)
    }
    val sizeDifference = oldHeight - newHeight
    val lineBottom = getLineBottom(currentLine)
    if (lineBottom in (newHeight + scrollValue)..(oldHeight + scrollValue))
        sizeDifference - (oldHeight - (lineBottom - scrollValue))
    else 0f
}
