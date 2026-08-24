package com.pierbezuhoff.justtext.data

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TextSource {
    data object LocalFile : TextSource

    data class CloudFile(
        val endpoint: String,
    ) : TextSource
}