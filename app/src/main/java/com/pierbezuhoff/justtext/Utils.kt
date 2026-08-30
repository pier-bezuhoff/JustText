package com.pierbezuhoff.justtext

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
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

// NOTE: alternatively use .flowWithLifecycle() inside LaunchedEffect
@Suppress("ComposableNaming")
@Composable
inline fun <T> Flow<T>?.collectWithLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline collector: suspend CoroutineScope.(T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            // switching to Main.immediate prevents losing events in very rare cases
            // during configuration changes (default is Dispatchers.Main), idc tho
//            withContext(Dispatchers.Main.immediate) {
            this@collectWithLifecycle?.collect { event ->
                collector(event)
            }
//            }
        }
    }
}

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

fun TextFieldState.setTextAndSelection(
    text: String,
    selection: TextRange = TextRange.Zero,
) = edit {
    replace(0, length, text)
    this.selection = selection
}