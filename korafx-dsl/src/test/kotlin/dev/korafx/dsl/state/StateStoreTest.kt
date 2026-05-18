package dev.korafx.dsl.state

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StateStoreTest {
    @Test
    fun `state store exposes current state and state flow`() = runTest {
        val store = MutableStateStore(CounterState(count = 1))

        assertEquals(CounterState(count = 1), store.currentState)
        assertEquals(CounterState(count = 1), store.state.first())

        store.set(CounterState(count = 2))
        assertEquals(CounterState(count = 2), store.currentState)

        store.update { it.copy(count = it.count + 1) }
        assertEquals(CounterState(count = 3), store.currentState)
    }

    @Test
    fun `event stream emits events to active collectors`() = runTest {
        val stream = UiEventStream<String>()
        val collected = mutableListOf<String>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            stream.events.take(2).toList(collected)
        }

        stream.emit("created")
        assertTrue(stream.tryEmit("updated"))

        job.join()
        assertEquals(listOf("created", "updated"), collected)
    }

    private data class CounterState(
        val count: Int,
    )
}
