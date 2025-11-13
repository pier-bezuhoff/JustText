package com.pierbezuhoff.justtext.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pierbezuhoff.justtext.ui.theme.JustTextTheme

@Composable
fun TextScreen(
    tfValue: TextFieldValue,
    fontSize: Int,
    textColor: Color,
    textBackgroundColor: Color,
    readOnly: Boolean,
    setTFValue: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = textColor,
        fontSize = fontSize.sp,
//        letterSpacing = 0.02f.em,
        lineHeight = (1.1f*fontSize).sp,
        lineBreak = LineBreak.Paragraph,
    )
    // this triggers way too often
//    val annotatedTFValue = tfValue.copy(
//        annotatedString = annotateUrlsInText(tfValue.text, Color.Green)
//    )
    // NOTE: rich text editing is not yet supported (since 2019..):
    //  https://issuetracker.google.com/issues/135556699
    PatchedBasicTextField(
        tfValue,
        onValueChange = setTFValue,
        modifier = modifier,
        readOnly = readOnly,
        textStyle = textStyle,
        minLines = 50,
        maxLines = Int.MAX_VALUE,
        cursorBrush = SolidColor(textColor),
        containerColor = textBackgroundColor,
    )
}

private fun annotateUrlsInText(
    text: String,
    urlColor: Color,
): AnnotatedString {
    val textLinkStyles = TextLinkStyles(
        style = SpanStyle(
            color = urlColor,
            textDecoration = TextDecoration.Underline,
        ),
    )
    // reference: https://stackoverflow.com/a/8943487/7143065
    // and 'www.'... without 'https://' start
    val urlRegex = Regex(
        "(\\b((https?|ftp|file)://|www\\.)[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|])",
        setOf(RegexOption.IGNORE_CASE)
    )
    return buildAnnotatedString {
        var i = 0
        val matches = urlRegex.findAll(text)
        matches.forEach { match ->
            append(text.substring(i until match.range.start))
            val urlText = text.substring(match.range)
            withLink(LinkAnnotation.Url(
                url = urlText,
                styles = textLinkStyles,
                linkInteractionListener = {
                    println("clicked $urlText")
                } // on click
            )) {
                append(urlText)
            }
            i = match.range.last + 1
        }
        val endingText = text.substring(i until text.length)
        append(endingText)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JustTextTheme {
        TextScreen(
            tfValue = TextFieldValue("hi!!!!!"),
            fontSize = 30,
            textColor = Color.Black,
            textBackgroundColor = Color.LightGray,
            readOnly = false,
            setTFValue = {},
        )
    }
}
