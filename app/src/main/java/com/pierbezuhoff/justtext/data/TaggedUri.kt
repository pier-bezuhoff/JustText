package com.pierbezuhoff.justtext.data

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class TaggedUri(
    val uri: Uri,
    val id: Int = idCounter,
) {
    init {
        idCounter += 1
    }

    companion object {
        private var idCounter = 0
    }
}