package dev.korafx.framework.mvvm

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    @Test
    fun `dispatch updates state`() = runTest {
        val viewModel = CounterViewModel()

        viewModel.dispatch(CounterAction.Increment)

        assertEquals(CounterState(count = 1), viewModel.state.value)
        viewModel.close()
    }

    @Test
    fun `view model emits events`() = runTest {
        val viewModel = CounterViewModel()
        val events = mutableListOf<CounterEvent>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.events.take(1).toList(events)
        }

        viewModel.dispatch(CounterAction.Notify("saved"))

        job.join()
        assertEquals(listOf<CounterEvent>(CounterEvent.Notified("saved")), events)
        viewModel.close()
    }

    @Test
    fun `close cancels launched work`() {
        val dispatcher = StandardTestDispatcher()
        val viewModel = CounterViewModel(coroutineContext = dispatcher)

        viewModel.dispatch(CounterAction.IncrementLater)
        viewModel.close()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(CounterState(count = 0), viewModel.state.value)
    }

    @Test
    fun `close is idempotent and exposes lifecycle state`() {
        val viewModel = CounterViewModel()

        assertFalse(viewModel.isClosed)

        viewModel.close()
        viewModel.close()

        assertTrue(viewModel.isClosed)
    }

    @Test
    fun `dispatch after close does not mutate state or emit events`() = runTest {
        val viewModel = CounterViewModel(coroutineContext = StandardTestDispatcher(testScheduler))
        val events = mutableListOf<CounterEvent>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.events.toList(events)
        }

        viewModel.close()
        viewModel.dispatch(CounterAction.Increment)
        viewModel.dispatch(CounterAction.Notify("closed"))
        advanceUntilIdle()

        assertEquals(CounterState(count = 0), viewModel.state.value)
        assertEquals(emptyList(), events)
        job.cancel()
    }

    @Test
    fun `injected coroutine context makes async actions deterministic`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = CounterViewModel(coroutineContext = dispatcher)

        viewModel.dispatch(CounterAction.IncrementLater)
        assertEquals(CounterState(count = 0), viewModel.state.value)

        advanceUntilIdle()
        assertEquals(CounterState(count = 1), viewModel.state.value)
        viewModel.close()
    }

    @Test
    fun `await helpers observe state and events`() = runTest {
        val viewModel = CounterViewModel(coroutineContext = StandardTestDispatcher(testScheduler))
        val eventDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.awaitEvent { event ->
                event is CounterEvent.Notified
            }
        }

        viewModel.dispatch(CounterAction.IncrementLater)
        viewModel.dispatch(CounterAction.Notify("saved"))
        advanceUntilIdle()

        val state = viewModel.awaitState { it.count == 1 }
        val event = eventDeferred.await()

        assertEquals(CounterState(count = 1), state)
        assertEquals(CounterEvent.Notified("saved"), event)
        viewModel.close()
    }

    @Test
    fun `view model factory creates and closes view models without DI`() {
        val factory = viewModelFactory {
            CounterViewModel()
        }

        val viewModel = factory.create()

        assertIs<CounterViewModel>(viewModel)
        listOf(viewModel).closeAll()
    }

    private data class CounterState(
        val count: Int = 0,
    ) : ViewState

    private sealed interface CounterAction : UiAction {
        data object Increment : CounterAction
        data object IncrementLater : CounterAction
        data class Notify(val message: String) : CounterAction
    }

    private sealed interface CounterEvent : UiEvent {
        data class Notified(val message: String) : CounterEvent
    }

    private class CounterViewModel(
        coroutineContext: CoroutineContext = Dispatchers.Default,
    ) : ViewModel<CounterState, CounterAction, CounterEvent>(
        initialState = CounterState(),
        coroutineContext = coroutineContext,
    ) {
        override fun onAction(action: CounterAction) {
            when (action) {
                CounterAction.Increment -> updateState { it.copy(count = it.count + 1) }
                CounterAction.IncrementLater -> launch {
                    delay(50)
                    updateState { it.copy(count = it.count + 1) }
                }
                is CounterAction.Notify -> launch {
                    emitEvent(CounterEvent.Notified(action.message))
                }
            }
        }
    }
}
