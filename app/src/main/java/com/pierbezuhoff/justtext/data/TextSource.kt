package com.pierbezuhoff.justtext.data

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TextSource {
    object LocalFile : TextSource

    object CloudFile : TextSource
}