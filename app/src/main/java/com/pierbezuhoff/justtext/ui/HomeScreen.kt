package com.pierbezuhoff.justtext.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierbezuhoff.justtext.ui.theme.JustTextTheme

@Composable
fun HomeScreen(
    viewModel: JustTextViewModel,
    modifier: Modifier = Modifier,
) {
    val initialText by viewModel.initialTextFlow.collectAsStateWithLifecycle()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    TextScreen(
        initialText,
        viewModel::updateText,
        modifier = modifier
    )
}

@Composable
fun TextScreen(
    initialText: String,
    onNewText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tfValue by remember(initialText) { mutableStateOf(TextFieldValue(initialText)) }
    val lineCount = tfValue.text.count { it == '\n' }
    val lineNumbers = remember(lineCount) {
        (1..lineCount).joinToString("\n")
    }
    val tfStyle = MaterialTheme.typography.bodyLarge.copy(
        lineBreak = LineBreak.Paragraph,
    )
    TextOverflow.Visible
    Column(
        modifier
            .imePadding()
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxSize()
        ) {
            Text(
                text = lineNumbers,
                modifier = Modifier
                    .width(32.dp)
                    .padding(start = 4.dp, end = 4.dp)
                ,
                color = Color.LightGray
            )
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
                minLines = 20,
                maxLines = Int.MAX_VALUE,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JustTextTheme {
        TextScreen("hi!!!!!", {})
    }
}
