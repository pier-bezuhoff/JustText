package com.pierbezuhoff.justtext.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Color(uiState.backgroundColor))
            }
    ) {
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
                                viewModel.persistState()
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
                    initialText = initialText,
                    textColor = Color(uiState.textColor),
                    textFieldBackgroundColor = Color(uiState.textFieldBackgroundColor),
                    onNewText = viewModel::updateText,
                    modifier = modifier
                )
            }
        }
    }
    val defaultTextColor = MaterialTheme.colorScheme.primary
    val defaultTextFieldBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    val defaultBackgroundColor = MaterialTheme.colorScheme.surface
    LaunchedEffect(viewModel, defaultTextColor, defaultTextFieldBackgroundColor, defaultBackgroundColor) {
        viewModel.setDefaultColors(
            defaultTextColor, defaultTextFieldBackgroundColor, defaultBackgroundColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JustTextTheme {
//        TextScreen("hi!!!!!", {})
    }
}
