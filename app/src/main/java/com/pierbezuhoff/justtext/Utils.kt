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

inline fun <R, reified E1> runCatching1(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        when (e) {
            is E1 -> Result.failure(e)
            else -> throw e
        }
    }
}

inline fun <R, reified E1, reified E2> runCatching2(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        when (e) {
            is E1, is E2 -> Result.failure(e)
            else -> throw e
        }
    }
}

inline fun <T, R, reified E1> Result<T>.flatMapCatching1(
    block: (T) -> Result<R>
): Result<R> =
    fold(
        onSuccess = { t ->
            try {
                block(t)
            } catch (e: Exception) {
                when (e) {
                    is E1 -> Result.failure(e)
                    else -> throw e
                }
            }
        },
        onFailure = { e ->
            Result.failure(e)
        }
    )

inline fun <T, R, reified E1, reified E2> Result<T>.flatMapCatching2(
    block: (T) -> Result<R>
): Result<R> =
    fold(
        onSuccess = { t ->
            try {
                block(t)
            } catch (e: Exception) {
                when (e) {
                    is E1, is E2 -> Result.failure(e)
                    else -> throw e
                }
            }
        },
        onFailure = { e ->
            Result.failure(e)
        }
    )
