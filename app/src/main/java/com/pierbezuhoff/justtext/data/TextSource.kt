package com.pierbezuhoff.justtext.data

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TextSource {
    data object LocalFile : TextSource

    data object CloudFile : TextSource
}