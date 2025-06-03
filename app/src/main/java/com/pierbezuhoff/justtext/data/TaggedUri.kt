package com.pierbezuhoff.justtext.data

import android.net.Uri

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