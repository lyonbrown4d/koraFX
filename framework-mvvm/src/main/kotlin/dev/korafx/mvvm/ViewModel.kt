package dev.korafx.mvvm

import dev.korafx.state.MutableStateStore
import dev.korafx.state.UiEventStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface ViewState

interface UiAction

interface UiEvent

abstract class ViewModel<S : ViewState, A : UiAction, E : UiEvent>(
    initialState: S,
    coroutineContext: CoroutineContext = Dispatchers.Default,
) : AutoCloseable {
    private val stateStore = MutableStateStore(initialState)
    private val eventStream = UiEventStream<E>()
    private val viewModelScope = CoroutineScope(SupervisorJob() + coroutineContext)
    private val closed = AtomicBoolean(false)

    val state: StateFlow<S> = stateStore.state
    val events: SharedFlow<E> = eventStream.events
    val isClosed: Boolean
        get() = closed.get()

    protected val currentState: S
        get() = stateStore.currentState

    fun dispatch(action: A) {
        if (isClosed) {
            return
        }

        onAction(action)
    }

    protected abstract fun onAction(action: A)

    protected fun setState(nextState: S) {
        if (isClosed) {
            return
        }

        stateStore.set(nextState)
    }

    protected fun updateState(reducer: (S) -> S) {
        if (isClosed) {
            return
        }

        stateStore.update(reducer)
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        if (isClosed) {
            Job().apply { cancel() }
        } else {
            viewModelScope.launch(block = block)
        }

    protected suspend fun emitEvent(event: E) {
        if (isClosed) {
            return
        }

        eventStream.emit(event)
    }

    protected fun tryEmitEvent(event: E): Boolean =
        !isClosed && eventStream.tryEmit(event)

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            viewModelScope.cancel()
        }
    }
}
