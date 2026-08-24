package com.pierbezuhoff.justtext.ui

import androidx.compose.runtime.Immutable

@Immutable
enum class ContentStatus {
    LOADING,
    LOADING_FAILED,
    /** loaded & saved */
    SYNCED,
    UNSAVED,
    SAVING,
    SAVING_FAILED,
}