package com.pierbezuhoff.justtext.ui

import kotlinx.serialization.Serializable

@Serializable
data class UiState(
    val text: String,
    // TextFieldValue.selection.start
    val cursorLocation: Int = 0,
    val textColor: ULong? = null,
    val textBackgroundColor: ULong? = null,
    val imageBackgroundColor: ULong? = null,
)