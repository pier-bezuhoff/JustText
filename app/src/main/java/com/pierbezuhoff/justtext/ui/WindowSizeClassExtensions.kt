package com.pierbezuhoff.justtext.ui

import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/** Either of dimensions is compact */
val WindowSizeClass.isCompact get() =
    windowWidthSizeClass == WindowWidthSizeClass.COMPACT ||
    windowHeightSizeClass == WindowHeightSizeClass.COMPACT

/** Both dimensions are expanded */
val WindowSizeClass.isExpanded: Boolean get() =
    windowWidthSizeClass == WindowWidthSizeClass.EXPANDED &&
    windowHeightSizeClass == WindowHeightSizeClass.EXPANDED

val WindowSizeClass.isLandscape: Boolean get() =
    windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
    windowWidthSizeClass == WindowWidthSizeClass.MEDIUM && windowHeightSizeClass == WindowHeightSizeClass.COMPACT
// (Medium, Medium) is the size in portrait tablet browser