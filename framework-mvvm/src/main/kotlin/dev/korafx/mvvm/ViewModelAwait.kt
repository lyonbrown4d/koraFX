package dev.korafx.mvvm

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

suspend fun <S : ViewState, A : UiAction, E : UiEvent> ViewModel<S, A, E>.awaitState(
    timeoutMillis: Long = 1_000,
    predicate: (S) -> Boolean,
): S =
    withTimeout(timeoutMillis) {
        state.first(predicate)
    }

suspend fun <S : ViewState, A : UiAction, E : UiEvent> ViewModel<S, A, E>.awaitEvent(
    timeoutMillis: Long = 1_000,
    predicate: (E) -> Boolean = { true },
): E =
    withTimeout(timeoutMillis) {
        events.first(predicate)
    }
