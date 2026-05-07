package dev.korafx.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MutableStateStore<S : Any>(
    initialState: S,
) {
    private val stateFlow = MutableStateFlow(initialState)

    val state: StateFlow<S> = stateFlow.asStateFlow()

    val currentState: S
        get() = stateFlow.value

    fun set(nextState: S) {
        stateFlow.value = nextState
    }

    fun update(reducer: (S) -> S) {
        stateFlow.update(reducer)
    }
}

class UiEventStream<E : Any> {
    private val eventFlow = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 16,
    )

    val events: SharedFlow<E> = eventFlow.asSharedFlow()

    suspend fun emit(event: E) {
        eventFlow.emit(event)
    }

    fun tryEmit(event: E): Boolean = eventFlow.tryEmit(event)
}

fun <T> Flow<T>.collectLatestIn(
    scope: CoroutineScope,
    collector: suspend (T) -> Unit,
): Job = scope.launch {
    collectLatest(collector)
}
