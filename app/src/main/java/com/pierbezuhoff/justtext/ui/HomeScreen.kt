package com.pierbezuhoff.justtext.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pierbezuhoff.justtext.R
import com.pierbezuhoff.justtext.ui.theme.JustTextTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JustTextViewModel,
    quitApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.loadNewBackgroundImage(uri)
        } else {
            println("PhotoPicker: No media selected")
        }
    }
    val initialText by viewModel.initialTextFlow.collectAsStateWithLifecycle()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val backgroundImageUri: Uri? by viewModel.backgroundImageUri.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize()) {
        backgroundImageUri?.let { uri ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(500)
                    .build()
                ,
                contentDescription = "background",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                alpha = 1f,
            )
        }
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                quitApp()
                            },
                        ) {
                            Icon(
                                painterResource(R.drawable.close),
                                "quit"
                            )
                        }
                    },
                    title = {},
                    actions = {
                        IconButton(
                            onClick = {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        ) {
                            Icon(
                                painterResource(R.drawable.background_image),
                                "choose bg"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
            ) {
                TextScreen(
                    initialText,
                    viewModel::updateText,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun TextScreen(
    initialText: String,
    onNewText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tfValue by remember(initialText) { mutableStateOf(TextFieldValue(initialText)) }
//    val lineCount = tfValue.text.count { it == '\n' }
//    val lineNumbers = remember(lineCount) {
//        (1..lineCount).joinToString("\n")
//    }
    val tfStyle = MaterialTheme.typography.bodyLarge.copy(
        lineBreak = LineBreak.Paragraph,
    )
//    TextOverflow.Visible
    Column(
        modifier
            .imePadding()
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxSize()
        ) {
//            Text(
//                text = lineNumbers,
//                modifier = Modifier
//                    .width(32.dp)
//                    .padding(start = 4.dp, end = 4.dp)
//                ,
//                color = Color.LightGray
//            )
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
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.4f,
                    ),
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.2f,
                    ),
                )
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
