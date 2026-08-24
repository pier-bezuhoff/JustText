package com.pierbezuhoff.justtext.ui

import androidx.compose.runtime.Immutable

@Immutable
enum class ContentStatus {
    LOADING,
    /** loaded & saved */
    SYNCED,
    UNSAVED,
    SAVING,
}