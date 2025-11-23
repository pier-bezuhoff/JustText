package com.pierbezuhoff.justtext.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pierbezuhoff.justtext.R
import com.pierbezuhoff.justtext.data.TaggedUri
import com.pierbezuhoff.justtext.ui.dialogs.ColorsDialog
import com.pierbezuhoff.justtext.ui.dialogs.DialogType
import com.pierbezuhoff.justtext.ui.dialogs.FontSizeDialog

// MAYBE: add quick in-text search button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JustTextViewModel,
    quitApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.setBackgroundImage(uri)
        } else {
            println("PhotoPicker: No media selected")
        }
    }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val backgroundImageUri: TaggedUri? by viewModel.backgroundImageUri.collectAsStateWithLifecycle()
    var openedDialogType: DialogType? by remember { mutableStateOf(null) }
    val textColor = uiState.textColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    val textBackgroundColor = uiState.textBackgroundColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
    val imageBackgroundColor = uiState.imageBackgroundColor?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
    Scaffold(
        modifier = modifier,
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
                title = {
                    TextButton(
                        onClick = viewModel::save,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        enabled = !uiState.syncedToDisk,
                        colors = ButtonDefaults.textButtonColors().copy(
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                .copy(alpha = 0.5f)
                            ,
                        )
                    ) {
                        Text(
                            text =
                                if (uiState.syncedToDisk)
                                    "Saved"
                                else "Save"
                            ,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            openedDialogType = DialogType.FONT_SIZE
                        }
                    ) {
                        Icon(
                            painterResource(R.drawable.text_size),
                            "choose font size"
                        )
                    }
                    IconButton(
                        onClick = {
                            openedDialogType = DialogType.COLORS
                        }
                    ) {
                        Icon(
                            painterResource(R.drawable.palette),
                            "choose ui colors"
                        )
                    }
                    IconButton(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    ) {
                        Icon(
                            painterResource(R.drawable.background_image),
                            "choose bg image"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box(
            Modifier
                // this weird padding chemistry is needed to hide random white rect at the bottom
                .padding(
                    top = innerPadding.calculateTopPadding(),
                )
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        uiState.imageBackgroundColor?.let { Color(it) } ?: imageBackgroundColor
                    )
                }
        ) {
            backgroundImageUri?.let { taggedUri ->
                key(taggedUri) { // essential
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(taggedUri.uri)
                            // since we have always the same uri, but diff content
                            // caching is a no-go
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .crossfade(500)
                            .build()
                        ,
                        contentDescription = "background",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 1f,
                    )
                }
            }
            // BUG: when keyboard appears, in its future place image overlays becomes
            //  bright alpha=0
            //  (seemingly only on older Android versions)
            Surface(
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = innerPadding.calculateBottomPadding(),
                    )
                    .consumeWindowInsets(innerPadding)
                    .safeDrawingPadding()
                ,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
            ) {
                TextScreen(
                    tfValue = uiState.tfValue,
                    fontSize = uiState.fontSize,
                    textColor = textColor,
                    textBackgroundColor = textBackgroundColor,
                    readOnly = !uiState.loadedFromDisk,
                    setTFValue = viewModel::setTFValue,
                )
            }
        }
    }
    when (openedDialogType) {
        DialogType.FONT_SIZE -> {
            FontSizeDialog(
                fontSize = uiState.fontSize,
                setFontSize = { fontSize ->
                    viewModel.setFontSize(fontSize)
                    openedDialogType = null
                },
                onCancel = {
                    openedDialogType = null
                }
            )
        }
        DialogType.COLORS -> {
            ColorsDialog(
                textColor = textColor,
                textBackgroundColor = textBackgroundColor,
                imageBackgroundColor = imageBackgroundColor,
                setTextColor = { color ->
                    viewModel.setTextColor(color)
                    openedDialogType = null
                },
                setTextBackgroundColor = { color ->
                    viewModel.setTextBackgroundColor(color)
                    openedDialogType = null
                },
                setImageBackgroundColor = { color ->
                    viewModel.setImageBackgroundColor(color)
                    openedDialogType = null
                },
                onDismiss = {
                    openedDialogType = null
                },
            )
        }
        null -> {}
    }
    LaunchedEffect(viewModel) {
        if (!uiState.loadedFromDisk) {
            viewModel.startLoadingData()
        }
    }
}

