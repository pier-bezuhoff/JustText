package com.pierbezuhoff.justtext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

fun byteArrayOf(vararg numbers: Int): ByteArray =
    numbers.map { it.toByte() }.toByteArray()

context(viewModel: ViewModel)
fun <T> Flow<T>.stateInWhileSubscribed(initialValue: T): StateFlow<T> =
    stateIn(
        scope = viewModel.viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = initialValue,
    )

