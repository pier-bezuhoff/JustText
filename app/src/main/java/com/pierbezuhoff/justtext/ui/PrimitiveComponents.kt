package com.pierbezuhoff.justtext.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pierbezuhoff.justtext.R

@Composable
fun SimpleButton(
    iconPainter: Painter,
    name: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current,
    containerColor: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        interactionSource = interactionSource,
        modifier = modifier,
    ) {
        Icon(
            iconPainter,
            contentDescription = name,
            modifier = iconModifier,
        )
    }
}

@Composable
fun DialogTitle(
    @StringRes
    titleStringResource: Int,
    modifier: Modifier = Modifier,
    smallerFont: Boolean = false,
) {
    Text(
        text = stringResource(titleStringResource),
        modifier = modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.primary,
        style =
            if (smallerFont) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.titleLarge,
    )
}

@Composable
fun OkButton(
    fontSize: TextUnit = 24.sp,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
) {
    Button(
        onClick = { onConfirm() },
        modifier = modifier.padding(4.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shape = CircleShape,
    ) {
        Icon(painterResource(R.drawable.confirm), "ok")
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(stringResource(R.string.ok_name), fontSize = fontSize)
    }
}

@Composable
fun CancelButton(
    fontSize: TextUnit = 24.sp,
    noText: Boolean = false,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    OutlinedButton(
        onClick = { onDismissRequest() },
        modifier = modifier.padding(4.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shape = CircleShape,
    ) {
        Icon(painterResource(R.drawable.close), "cancel")
        if (!noText) {
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.cancel_name), fontSize = fontSize)
        }
    }
}
