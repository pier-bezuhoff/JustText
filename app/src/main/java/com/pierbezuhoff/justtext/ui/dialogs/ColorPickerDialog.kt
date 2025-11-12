package com.pierbezuhoff.justtext.ui.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.github.ajalt.colormath.RenderCondition
import com.github.ajalt.colormath.model.RGB
import com.pierbezuhoff.justtext.R
import com.pierbezuhoff.justtext.ui.CancelButton
import com.pierbezuhoff.justtext.ui.DialogTitle
import com.pierbezuhoff.justtext.ui.OkButton
import com.pierbezuhoff.justtext.ui.SimpleButton
import com.pierbezuhoff.justtext.ui.isCompact
import com.pierbezuhoff.justtext.ui.isExpanded
import com.pierbezuhoff.justtext.ui.isLandscape
import com.pierbezuhoff.justtext.ui.theme.JustTextColors
import ui.colorpicker.ClassicColorPicker
import ui.colorpicker.HsvColor

private val DEFAULT_PALETTE: List<Color> = listOf(
    Color.White, Color.LightGray, Color.Gray, Color.DarkGray, Color.Black,
    // RGB & CMY[-K] -> R Y G C B M
    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta,
    // fun colors
    JustTextColors.peachyPink,
    JustTextColors.pinkishRed,
    JustTextColors.venousBloodRed,
    JustTextColors.deepBrown,
    JustTextColors.orange,
    JustTextColors.orangeOrange,
    JustTextColors.goldenBananaYellow,
    JustTextColors.veryDarkForestyGreen,
    JustTextColors.aquamarine,
    JustTextColors.teal,
    JustTextColors.darkPurple,
    JustTextColors.skyBlue,
)

@Composable
fun ColorPickerDialog(
    currentColor: Color,
    onCancel: () -> Unit,
    onConfirm: (newColor: Color) -> Unit,
    showAlphaBar: Boolean = true,
) {
    val colorState = rememberSaveable(currentColor, stateSaver = HsvColor.Saver) {
        mutableStateOf(HsvColor.from(currentColor))
    }
    val color = colorState.value.toColor()
    val setColor = remember(colorState) { { newColor: Color ->
        colorState.value = HsvColor.from(newColor)
    } }
    val lightDarkVerticalGradientBrush = remember { Brush.verticalGradient(
        0.1f to Color.White,
        0.9f to Color.Black,
    ) } // to grasp how the color looks in different contexts
    val lightDarkHorizontalGradientBrush = remember { Brush.horizontalGradient(
        0.1f to Color.White,
        0.9f to Color.Black,
    ) }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = windowSizeClass.isCompact
    val isMedium =
        windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM &&
        windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.MEDIUM
    val isExpanded = windowSizeClass.isExpanded
    val isLandscape = windowSizeClass.isLandscape
    val maxColorsPerRowLandscape = 11
    val maxColorsPerRowPortrait = if (isMedium) 8 else 6
    val fontSize =
        if (isCompact) 14.sp
        else 24.sp
    val paletteModifier = Modifier
        .padding(4.dp)
        .border(2.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.medium)
//        .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium)
    val swatchBgModifier = Modifier
        .padding(4.dp)
        .size(
            if (isCompact) 30.dp
            else if (isExpanded) 60.dp
            else 45.dp
        )
    val splashIconModifier = Modifier
        .size(
            if (isCompact) 24.dp
            else if (isExpanded) 40.dp
            else 32.dp
        )
    val onConfirm0 = remember(currentColor) { {
        onConfirm(
            // important that we capture states in the closure
            // otherwise changing values would invalidate this lambda
            // and the lambda captured by LaunchedEffect would be outdated one
            // that uses outdated values (i think)
            colorState.value.toColor(),
        )
    } }
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            if (isLandscape) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DialogTitle(
                        R.string.color_picker_title,
                        smallerFont = isCompact,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Row() {
                        Column(horizontalAlignment = Alignment.End) {
                            ColorPickerDisplay(
                                colorState,
                                Modifier.fillMaxHeight(0.8f),
                                showAlphaBar = showAlphaBar,
                            )
                            Row(
                                Modifier
                                    .requiredHeightIn(50.dp, 100.dp) // desperation constraint
                                ,
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                HexInput(
                                    color,
                                    setColor = { colorState.value = HsvColor.from(it) },
                                    onConfirm = onConfirm0
                                )
                                CancelButton(fontSize, onDismissRequest = onCancel)
                                OkButton(fontSize, onConfirm = onConfirm0)
                            }
                        }
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(top = 12.dp, end = 8.dp),
                        ) {
                            Box(
                                Modifier
                                    .padding(start = 4.dp, bottom = 8.dp) // outer offset
                                    .background(
                                        lightDarkVerticalGradientBrush,
                                        MaterialTheme.shapes.medium
                                    )
                                    .padding(12.dp)
                                    .padding(end = 40.dp) // adjust for 2nd circle-box offset
                            ) {
                                Box(
                                    Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(currentColor)
                                        .clickable { setColor(currentColor) }
                                ) {}
                                Box(
                                    Modifier
                                        .offset(x = 40.dp)
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable(enabled = false, onClick = {}) // blocks thru-clicks
                                ) {}
                            }
                            Text(
                                text = stringResource(R.string.color_picker_default_palette_label),
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .offset(y = 4.dp)
                                ,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            FlowRow(
                                paletteModifier,
                                maxItemsInEachRow = maxColorsPerRowLandscape,
                            ) {
                                for (clr in DEFAULT_PALETTE) {
                                    SimpleButton(
                                        painterResource(R.drawable.paint_splash),
                                        "predefined color",
                                        swatchBgModifier,
                                        splashIconModifier,
                                        contentColor = clr,
                                    ) { setColor(clr) }
                                }
                            }
                        }
                    }
                }
            } else { // portrait
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DialogTitle(
                        R.string.color_picker_title,
                        smallerFont = isCompact,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    ColorPickerDisplay(
                        colorState,
                        Modifier.fillMaxWidth(
                            if (isMedium) 0.6f else 0.8f
                        ),
                        showAlphaBar = showAlphaBar,
                    )
                    Row() {
                        Box(
                            Modifier
                                .padding(top = 4.dp, start = 4.dp, end = 8.dp)
                                .background(lightDarkHorizontalGradientBrush, MaterialTheme.shapes.medium)
                                .padding(12.dp)
                                .padding(bottom = 40.dp) // adjust for 2nd circle-box offset
                        ) {
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(currentColor)
                                    .clickable { setColor(currentColor) }
                            ) {}
                            Box(
                                Modifier
                                    .offset(y = 40.dp)
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable(enabled = false, onClick = {}) // blocks thru-clicks
                            ) {}
                        }
                        Column() {
                            Text(
                                text = stringResource(R.string.color_picker_default_palette_label),
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .offset(y = 4.dp)
                                ,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            FlowRow(
                                paletteModifier,
                                maxItemsInEachRow = maxColorsPerRowPortrait,
                            ) {
                                for (clr in DEFAULT_PALETTE) {
                                    SimpleButton(
                                        painterResource(R.drawable.paint_splash),
                                        "predefined color",
                                        swatchBgModifier,
                                        splashIconModifier,
                                        contentColor = clr,
                                    ) { setColor(clr) }
                                }
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HexInput(
                            color,
                            setColor = { colorState.value = HsvColor.from(it) },
                            onConfirm = onConfirm0,
                        )
                        CancelButton(fontSize, noText = isCompact, onDismissRequest = onCancel)
                        OkButton(fontSize, onConfirm = onConfirm0)
                    }
                }
            }
        }
    }
}

private fun computeHexTFV(color: Color): TextFieldValue {
    val hexString = RGB(color.red, color.green, color.blue)
        .toHex(withNumberSign = false, renderAlpha = RenderCondition.NEVER)
    return TextFieldValue(hexString, TextRange(hexString.length))
}

/**
 * @param[hsvColorState] this state is updated internally by [ClassicColorPicker]
 */
@Composable
private fun ColorPickerDisplay(
    hsvColorState: MutableState<HsvColor>,
    modifier: Modifier = Modifier,
    showAlphaBar: Boolean = true,
    onColorChanged: () -> Unit = {},
) {
    ClassicColorPicker(
        modifier
            .aspectRatio(1.1f)
            .padding(16.dp)
        ,
        colorPickerValueState = hsvColorState,
        showAlphaBar = showAlphaBar,
        onColorChanged = { onColorChanged() }
    )
}

/**
 * @param[onConfirm] shortcut confirm exit lambda
 */
@Composable
private fun HexInput(
    color: Color,
    modifier: Modifier = Modifier,
    setColor: (Color) -> Unit,
    onConfirm: () -> Unit,
) {
    var hexTFV by remember(color) {
        mutableStateOf(computeHexTFV(color))
    }
    val windowInfo = LocalWindowInfo.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isError by remember(color) { mutableStateOf(false) }
    OutlinedTextField(
        value = hexTFV,
        onValueChange = { new ->
            hexTFV = new
            val hexString = new.text.let {
                if (it.isNotEmpty() && it[0] == '#')
                    it.drop(1) // drop leading '#'
                else it
            }
            if (hexString.length == 6) { // primitive hex validation
                try {
                    val rgb = RGB(hexString)
                    setColor(Color(rgb.r, rgb.g, rgb.b))
                    isError = false
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    println("cannot parse hex string \"$hexString\"")
                    isError = true
                }
            } else {
                isError = true
            }
        },
//                    textStyle = TextStyle(fontSize = 16.sp),
        label = { Text(stringResource(R.string.hex_name)) },
        placeholder = { Text("RRGGBB", color = LocalContentColor.current.copy(alpha = 0.5f)) },
        isError = isError,
        keyboardOptions = KeyboardOptions( // smart ass enter capturing
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done,
            showKeyboardOnFocus = false, // this sadly does nothing...
        ),
        keyboardActions = KeyboardActions(
            onDone = { onConfirm() }
        ),
        singleLine = true,
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.Enter) {
                    onConfirm()
                    true
                } else false
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // overrides min width of 280.dp defined for TextField
            .widthIn(50.dp, 100.dp)
        ,
//        colors = OutlinedTextFieldDefaults.colors()
//            .copy(unfocusedContainerColor = color.value.toColor())
    )
    // NOTE: this (no focus by default on Android) fix only works 90% of time...
    // reference: https://stackoverflow.com/q/71412537/7143065
    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) { // runs once every time the dialog is opened
                focusRequester.freeFocus()
//                focusRequester.requestFocus(FocusDirection.Exit)
                keyboard?.hide() // suppresses rare auto-showing keyboard bug
            }
        }
    }
}