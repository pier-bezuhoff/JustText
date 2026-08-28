package com.pierbezuhoff.justtext.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pierbezuhoff.justtext.R
import com.pierbezuhoff.justtext.data.EncryptedData
import com.pierbezuhoff.justtext.data.TaggedUri
import com.pierbezuhoff.justtext.data.TextCloudRepo
import com.pierbezuhoff.justtext.ui.dialogs.ColorsDialog
import com.pierbezuhoff.justtext.ui.dialogs.DialogType
import com.pierbezuhoff.justtext.ui.dialogs.FontSizeDialog
import com.pierbezuhoff.justtext.ui.theme.ColorTheme
import com.pierbezuhoff.justtext.ui.theme.JustTextTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

// MAYBE: add quick in-text search button
@Suppress("ParamsComparedByRef")
@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel,
    quitApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setBackgroundImage(uri)
        } else {
            println("PhotoPicker: No media selected")
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backgroundImageUri: TaggedUri? by viewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val encryptedData by viewModel.encryptedData.collectAsStateWithLifecycle(EncryptedData())
    var openedDialogType: DialogType? by remember { mutableStateOf(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    HomeScreen(
        uiState = uiState,
        backgroundImageUri = backgroundImageUri,
        encryptedData = encryptedData,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        quitApp = {
            viewModel.persistState()
            viewModel.freeResources()
            quitApp()
        },
        save = viewModel::save,
        switchTextSource = viewModel::switchTextSource,
        setCloudRepoProperties = viewModel::setCloudRepoProperties,
        openFontSizeDialog = { openedDialogType = DialogType.FONT_SIZE },
        openColorsDialog = { openedDialogType = DialogType.COLORS },
        openBackgroundImagePicker = {
            pickMedia.launch(PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            ))
        },
        onNewTFValue = viewModel::onNewTFValue,
    )
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
            val textColor = uiState.textColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
            val textBackgroundColor = uiState.textBackgroundColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            val imageBackgroundColor = uiState.imageBackgroundColor?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
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
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Default) {
                while (isActive) {
                    delay(PERIODIC_SAVE_DELAY)
                    println("periodic save")
                    viewModel.save()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    uiState: UiState,
    backgroundImageUri: TaggedUri?,
    encryptedData: EncryptedData,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    quitApp: () -> Unit = {},
    save: () -> Unit = {},
    switchTextSource: () -> Unit = {},
    setCloudRepoProperties: (TextCloudRepo.Properties) -> Unit = {},
    openFontSizeDialog: () -> Unit = {},
    openColorsDialog: () -> Unit = {},
    openBackgroundImagePicker: () -> Unit = {},
    onNewTFValue: (TextFieldValue) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                isLocal = uiState.isLocal,
                contentStatus = uiState.contentStatus,
                encryptedData = encryptedData,
                quitApp = quitApp,
                save = save,
                switchTextSource = switchTextSource,
                setCloudRepoProperties = setCloudRepoProperties,
                openFontSizeDialog = openFontSizeDialog,
                openColorsDialog = openColorsDialog,
                openBackgroundImagePicker = openBackgroundImagePicker,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        val imageBackgroundColor = uiState.imageBackgroundColor?.let { Color(it) }
            ?: MaterialTheme.colorScheme.surface
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
                BackgroundImage(taggedUri)
            }
            val textBackgroundColor = uiState.textBackgroundColor?.let { Color(it) }
                ?: MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            Surface(
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = innerPadding.calculateBottomPadding(),
                    )
                    .consumeWindowInsets(innerPadding)
                    .safeDrawingPadding()
                    .drawBehind {
                        drawRect(textBackgroundColor)
                    }
                ,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
            ) {
                val initialTFVState = remember(uiState.initialText) {
                    mutableStateOf(
                        TextFieldValue(uiState.initialText, uiState.textSelection)
                    )
                }
                val textColor = uiState.textColor?.let { Color(it) }
                    ?: MaterialTheme.colorScheme.primary
                TextScreen(
                    initialTFVState = initialTFVState,
                    fontSize = uiState.fontSize,
                    textColor = textColor,
                    readOnly = when (uiState.contentStatus) {
                        ContentStatus.LOADING -> true
                        else -> false
                    },
                    onNewTFValue = onNewTFValue,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    JustTextTheme(ColorTheme.Dark) {
        HomeScreen(
            uiState = UiState(
                contentStatus = ContentStatus.SYNCED,
                initialText = "hi!!!!!",
                fontSize = 30,
            ),
            backgroundImageUri = null,
            encryptedData = EncryptedData(),
        )
    }
}

@Composable
private fun BackgroundImage(taggedUri: TaggedUri) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    isLocal: Boolean,
    contentStatus: ContentStatus,
    encryptedData: EncryptedData,
    quitApp: () -> Unit = {},
    save: () -> Unit = {},
    switchTextSource: () -> Unit = {},
    setCloudRepoProperties: (TextCloudRepo.Properties) -> Unit = {},
    openFontSizeDialog: () -> Unit = {},
    openColorsDialog: () -> Unit = {},
    openBackgroundImagePicker: () -> Unit = {},
) {
    var showTextSourcePropertiesPopup: Boolean by remember { mutableStateOf(false) }
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = quitApp) {
                Icon(painterResource(R.drawable.power),
                    "quit"
                )
            }
        },
        title = {
            TextButton(
                onClick = save,
                modifier = Modifier.padding(horizontal = 12.dp),
                enabled = contentStatus == ContentStatus.UNSAVED,
                colors = ButtonDefaults.textButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        .copy(alpha = 0.5f)
                    ,
                )
            ) {
                Text(
                    text = when (contentStatus) {
                        ContentStatus.LOADING -> "Loading..."
                        ContentStatus.LOADING_FAILED -> "Loading failed."
                        ContentStatus.SYNCED -> "Synced"
                        ContentStatus.UNSAVED -> "Save"
                        ContentStatus.SAVING -> "Saving..."
                        ContentStatus.SAVING_FAILED -> "Saving failed."
                    }
                    ,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        actions = {
            IconButton(onClick = openFontSizeDialog) {
                Icon(painterResource(R.drawable.text_size),
                    "choose font size"
                )
            }
            IconButton(onClick = openColorsDialog) {
                Icon(painterResource(R.drawable.palette),
                    "choose ui colors"
                )
            }
            IconButton(onClick = openBackgroundImagePicker) {
                Icon(painterResource(R.drawable.background_image),
                    "choose bg image"
                )
            }
            Box {
                SwitchTextSourceButton(
                    isLocal = isLocal,
                    switchTextSource = switchTextSource,
                    openTextSourceProperties = {
                        showTextSourcePropertiesPopup = !showTextSourcePropertiesPopup
                    },
                )
                if (showTextSourcePropertiesPopup) {
                    TextSourcePropertiesPopup(
                        initialEndpoint = encryptedData.noteEndpoint ?: "example.com",
                        initialPassword = "", // you could show it but
                        dismiss = { showTextSourcePropertiesPopup = false },
                        setCloudRepoProperties = {
                            setCloudRepoProperties(it)
                            if (isLocal)
                                switchTextSource()
                        },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    )
}

/** shows text source properties on long click */
@Composable
private fun SwitchTextSourceButton(
    isLocal: Boolean,
    modifier: Modifier = Modifier,
    switchTextSource: () -> Unit = {},
    openTextSourceProperties: () -> Unit = {},
) {
    IconButtonWithCombinedClickable (
        modifier = modifier,
        onClick = switchTextSource,
        onLongClick = openTextSourceProperties,
    ) {
        Icon(
            if (isLocal)
                painterResource(R.drawable.cloud_download)
            else
                painterResource(R.drawable.no_internet)
            ,
            "switch text source"
        )
    }
}

@Composable
private fun BoxScope.TextSourcePropertiesPopup(
    initialEndpoint: String,
    initialPassword: String,
    dismiss: () -> Unit = {},
    setCloudRepoProperties: (TextCloudRepo.Properties) -> Unit = {},
) {
    var endpoint by remember { mutableStateOf(initialEndpoint) }
    var password by remember { mutableStateOf(initialPassword) }
    val confirm by rememberUpdatedState {
        setCloudRepoProperties(
            TextCloudRepo.Properties(endpoint, password)
        )
        dismiss()
    }
    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = IntOffset(
                x = anchorBounds.center.x - popupContentSize.width/2,
                y = anchorBounds.bottom + 20,
            )
        },
        onDismissRequest = dismiss,
        properties = PopupProperties(
            focusable = true,
        ),
    ) {
        Surface(
            modifier = Modifier.padding(8.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 12.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Endpoint")
                    StringTextFieldWithConfirmOnEnter(
                        value = endpoint,
                        onValueChange = { endpoint = it },
                        validateValue = { it.isNotBlank() },
                        onConfirm = confirm,
                        confirmOnEnter = true,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Password")
                    StringTextFieldWithConfirmOnEnter(
                        value = password,
                        onValueChange = { password = it },
                        onConfirm = confirm,
                        confirmOnEnter = true,
                    )
                }
                IconButton(
                    onClick = confirm,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(painterResource(R.drawable.confirm), "ok")
                }
            }
        }
    }
}

@Composable
private fun StringTextFieldWithConfirmOnEnter(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    validateValue: (String) -> Boolean = { true },
    onConfirm: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.primary,
    @StringRes
    placeholderStringResource: Int? = null,
    captureFocus: Boolean = false,
    confirmOnEnter: Boolean = false,
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    val focusRequester = remember { FocusRequester() }
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            textFieldValue = newTextFieldValue
            val s = newTextFieldValue.text
            if (s != value && validateValue(s)) {
                onValueChange(s)
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .then(
                if (confirmOnEnter)
                    Modifier.onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Enter) {
                            onConfirm()
                            true
                        } else false
                    }
                else Modifier
            )
        ,
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = placeholderStringResource?.let {
            { Text(stringResource(placeholderStringResource)) }
        },
        isError = !validateValue(textFieldValue.text),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (confirmOnEnter) ImeAction.Done else ImeAction.Unspecified,
            showKeyboardOnFocus = true,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onConfirm() }
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = color,
            focusedLabelColor = color,
            focusedBorderColor = color,
            selectionColors = TextSelectionColors(
                color,
                color.copy(alpha = 0.4f),
            )
        ),
    )
    LaunchedEffect(focusRequester, captureFocus) {
        if (captureFocus) {
            focusRequester.requestFocus(FocusDirection.Enter)
        }
    }
}

@Composable
private fun IconButtonWithCombinedClickable(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = IconButtonDefaults.standardShape,
    content: @Composable () -> Unit,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // always enabled
    Box(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(shape)
                .background(color = colors.containerColor, shape = shape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    enabled = true,
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
        ,
        contentAlignment = Alignment.Center,
    ) {
        val contentColor = colors.contentColor
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}