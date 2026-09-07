package com.pierbezuhoff.justtext.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.contextmenu.builder.TextContextMenuBuilderScope
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pierbezuhoff.justtext.ui.theme.ColorTheme
import com.pierbezuhoff.justtext.ui.theme.JustTextTheme

private sealed interface SelectionContextAction {
    data object SelectLine : SelectionContextAction
    data object DeleteSelection : SelectionContextAction
}

@Composable
fun TextScreen(
    tfState: TextFieldState,
    fontSize: Int,
    textColor: Color,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = textColor,
        fontSize = fontSize.sp,
//        letterSpacing = 0.02f.em,
        lineHeight = (1.1f*fontSize).sp,
        lineBreak = LineBreak.Paragraph,
    )
    // NOTE: rich text editing is not yet supported (since 2019..):
    //  https://issuetracker.google.com/issues/135556699
    TextField(
        state = tfState,
        modifier = modifier
            .fillMaxWidth()
            .appendTextContextMenuComponents {
                selectLineContextAction(tfState)
            }
        ,
        readOnly = readOnly,
        textStyle = textStyle,
        outputTransformation = CustomOutputTransformation(
            linkColor = textColor,
        ),
        lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 50),
        colors = TextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            disabledTextColor = textColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = textColor, // vertical blinking '|'
            focusedIndicatorColor = textColor, // horizontal line at the bottom
            unfocusedIndicatorColor = textColor,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    )
}

private fun TextContextMenuBuilderScope.selectLineContextAction(
    tfState: TextFieldState
) {
    item(SelectionContextAction.SelectLine, "Select line") {
        // note that this doesn't detect soft wraps
        val text = tfState.text
        val selection = tfState.selection
        val min = selection.min
        val max = selection.max
        val previousLineBreak = text.withIndex().lastOrNull { (i, char) ->
            i <= min && char == '\n'
        }?.index ?: -1
        val nextLineBreak = text.withIndex().firstOrNull { (i, char) ->
            i >= max && char == '\n'
        }?.index ?: text.length
        val newSelection = TextRange(previousLineBreak + 1, nextLineBreak)
        tfState.edit {
            this.selection = newSelection
        }
    }
}

// kinda same as Cut
private fun TextContextMenuBuilderScope.deleteSelectionContextAction(
    tfState: TextFieldState
) {
    item(SelectionContextAction.DeleteSelection, "Delete") {
        val selection = tfState.selection
        // not undoable, tho built-in cut is also not undoable
        tfState.edit {
            replace(selection.min, selection.max, "")
        }
        close()
    }
}

@Immutable
private data class CustomOutputTransformation(
    val linkColor: Color,
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        annotateUrlsInText(linkColor = linkColor)
    }
}

private fun TextFieldBuffer.annotateUrlsInText(
    linkColor: Color,
) {
    val linkStyle = SpanStyle(
        textDecoration = TextDecoration.Underline,
        fontStyle = FontStyle.Italic,
        color = linkColor,
    )
    // reference: https://stackoverflow.com/a/8943487/7143065
    // and 'www.'... without 'https://' start
    val urlRegex = Regex(
        "(\\b((https?|ftp|file)://|www\\.)[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|])",
        setOf(RegexOption.IGNORE_CASE)
    )
    val text = asCharSequence()
    val matches = urlRegex.findAll(text)
    // we cannot display annotated string with clickable links and stuff (yet),
    // only add span/paragraph styles
    matches.forEach { match ->
        addStyle(
            linkStyle,
            match.range.first,
            match.range.last + 1,
        )
    }
//    val textLinkStyles = TextLinkStyles(
//        style = linkStyle
//    )
//    var i = 0
//    buildAnnotatedString {
//        matches.forEach { match ->
//            append(text.substring(i until match.range.first))
//            val urlText = match.value
//            withLink(LinkAnnotation.Url(
//                url = urlText,
//                styles = textLinkStyles,
//                linkInteractionListener = {
//                    println("clicked $urlText")
//                } // on click
//            )) {
//                append(urlText)
//            }
//            i = match.range.last + 1
//        }
//        val endingText = text.substring(i until text.length)
//        append(endingText)
//    }
}

@Preview(showBackground = true)
@Composable
private fun TextScreenPreview() {
    val initialTFVState = remember { mutableStateOf(
        TextFieldValue("hi!!!!!")
    ) }
    JustTextTheme(ColorTheme.Dark) {
        TextScreen(
            tfState = TextFieldState(initialTFVState.value.text),
            fontSize = 30,
            textColor = Color.Black,
            readOnly = false,
        )
    }
}
