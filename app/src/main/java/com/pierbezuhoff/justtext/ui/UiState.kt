package com.pierbezuhoff.justtext.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

data class UiState(
    val loadedFromDisk: Boolean = false,
    /** aka 'saved' */
    val syncedToDisk: Boolean = false,
    // cursorLocation = tfValue.selection.start
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
