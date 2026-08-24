package com.pierbezuhoff.justtext.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Immutable
data class UiState(
    val contentStatus: ContentStatus = ContentStatus.LOADING,
    val isLocal: Boolean = true,
    val showTextSourcePropertiesPopup: Boolean = false,
    val tfValue: TextFieldValue =
        TextFieldValue(DEFAULT_TEXT, TextRange(DEFAULT_TEXT.length)),
    /** main body text font size in `sp` */
    val fontSize: Int = 18,
    // Color.value: ULong
    val textColor: ULong? = null,
    val textBackgroundColor: ULong? = null,
    val imageBackgroundColor: ULong? = null,
) {
    companion object {
        private const val DEFAULT_TEXT = "Welcome!"
    }
}
