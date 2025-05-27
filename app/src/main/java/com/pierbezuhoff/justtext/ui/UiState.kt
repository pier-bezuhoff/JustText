package com.pierbezuhoff.justtext.ui

import kotlinx.serialization.Serializable

@Serializable
data class UiState(
    val text: String,
    val cursorLineNumber: Int = 0,
    val cursorColumnNumber: Int = 0,
) {
}