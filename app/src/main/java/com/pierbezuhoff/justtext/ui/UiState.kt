package com.pierbezuhoff.justtext.ui

import kotlinx.serialization.Serializable

@Serializable
data class UiState(
    val text: String,
    val textColor: ULong,
    val textFieldBackgroundColor: ULong,
    val backgroundColor: ULong,
)