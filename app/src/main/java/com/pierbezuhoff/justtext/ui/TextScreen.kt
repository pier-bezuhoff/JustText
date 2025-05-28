package com.pierbezuhoff.justtext.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak

@Composable
fun TextScreen(
    initialText: String,
    textColor: Color,
    textFieldBackgroundColor: Color,
    onNewText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tfValue by remember(initialText) { mutableStateOf(TextFieldValue(initialText)) }
    val tfStyle = MaterialTheme.typography.bodyLarge.copy(
        color = textColor,
        lineBreak = LineBreak.Paragraph,
    )
    Column(
        modifier
            .imePadding()
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxSize()
        ) {
            TextField(
                tfValue,
                onValueChange = {
                    tfValue = it
                    onNewText(it.text)
                },
                modifier = Modifier
                    .weight(1f)
                ,
                textStyle = tfStyle,
                minLines = 30,
                maxLines = Int.MAX_VALUE,
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = textFieldBackgroundColor,
                    unfocusedContainerColor = textFieldBackgroundColor,
                )
            )
        }
    }
}

