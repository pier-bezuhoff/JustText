package com.pierbezuhoff.justtext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
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

/** alternative to [runCatching], but only catching exceptions satisfying
 * [catchFilter], non-cancellation exceptions by default */
inline fun <C, R> C.runCatchingOnly(
    crossinline catchFilter: (Throwable) -> Boolean = {
        it is Exception && it !is CancellationException
    },
    block: C.() -> R,
): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        if (catchFilter(e))
            Result.failure(e)
        else
            throw e
    }
}

inline fun <T, R> Result<T>.flatMapCatchingOnly(
    crossinline catchFilter: (Throwable) -> Boolean = {
        it is Exception && it !is CancellationException
    },
    block: (T) -> Result<R>,
): Result<R> {
    return fold(
        onSuccess = { t ->
            try {
                block(t)
            } catch (e: Exception) {
                if (catchFilter(e))
                    Result.failure(e)
                else
                    throw e
            }
        },
        onFailure = { e ->
            Result.failure(e)
        }
    )
}
