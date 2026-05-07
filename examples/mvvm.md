# MVVM Examples

## Constructor Injection

```kotlin
data class CounterState(
    val count: Int = 0,
) : ViewState

sealed interface CounterAction : UiAction {
    data object Increment : CounterAction
}

sealed interface CounterEvent : UiEvent

class CounterViewModel : ViewModel<CounterState, CounterAction, CounterEvent>(
    initialState = CounterState(),
) {
    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment -> updateState { state ->
                state.copy(count = state.count + 1)
            }
        }
    }
}

val viewModel = CounterViewModel()
```

## Factory Without DI

```kotlin
class WorkspaceViewModel(
    private val repository: WorkspaceRepository,
) : ViewModel<WorkspaceState, WorkspaceAction, WorkspaceEvent>(
    initialState = WorkspaceState(),
) {
    override fun onAction(action: WorkspaceAction) {
        // update state and emit events
    }
}

val repository = WorkspaceRepository()
val factory = viewModelFactory {
    WorkspaceViewModel(repository)
}

val viewModel = factory.create()

// Close ViewModels explicitly from Application.stop or your screen lifecycle.
listOf(viewModel).closeAll()

// close() is idempotent. After close, dispatch and built-in state/event helpers are no-ops.
check(viewModel.isClosed)
```

## Deterministic Tests

```kotlin
class CounterViewModel(
    coroutineContext: CoroutineContext = Dispatchers.Default,
) : ViewModel<CounterState, CounterAction, CounterEvent>(
    initialState = CounterState(),
    coroutineContext = coroutineContext,
) {
    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.IncrementLater -> launch {
                delay(50)
                updateState { state -> state.copy(count = state.count + 1) }
            }
            is CounterAction.Notify -> launch {
                emitEvent(CounterEvent.Notified(action.message))
            }
        }
    }
}

@Test
fun `counter increments later`() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = CounterViewModel(coroutineContext = dispatcher)
    val event = async {
        viewModel.awaitEvent { it is CounterEvent.Notified }
    }

    viewModel.dispatch(CounterAction.IncrementLater)
    viewModel.dispatch(CounterAction.Notify("saved"))
    advanceUntilIdle()

    viewModel.awaitState { it.count == 1 }
    assertEquals(CounterEvent.Notified("saved"), event.await())
    viewModel.close()
}
```

## Action Bound JavaFX View

```kotlin
data class NotesState(
    val draft: String = "",
    val notes: List<String> = emptyList(),
) : ViewState

sealed interface NotesAction : UiAction {
    data class UpdateDraft(val value: String) : NotesAction
    data object Submit : NotesAction
}

sealed interface NotesEvent : UiEvent {
    data class Submitted(val value: String) : NotesEvent
}

class NotesViewModel : ViewModel<NotesState, NotesAction, NotesEvent>(
    initialState = NotesState(),
) {
    override fun onAction(action: NotesAction) {
        when (action) {
            is NotesAction.UpdateDraft -> updateState { state ->
                state.copy(draft = action.value)
            }
            NotesAction.Submit -> submit()
        }
    }

    private fun submit() {
        val note = currentState.draft.trim()
        if (note.isEmpty()) return

        updateState { state ->
            state.copy(
                draft = "",
                notes = listOf(note) + state.notes,
            )
        }
        tryEmitEvent(NotesEvent.Submitted(note))
    }
}

val scope = MainScope()
val viewModel = NotesViewModel()

val view = section(
    title = "MVVM Notes",
    description = "The view dispatches actions; the ViewModel owns state and events.",
) {
    lateinit var draftField: TextField
    lateinit var submitButton: Button
    lateinit var notesList: ListView<String>

    draftField = textField {
        promptText = "Type a note"
        textProperty().addListener { _, _, newValue ->
            val nextValue = newValue.orEmpty()
            if (viewModel.state.value.draft != nextValue) {
                viewModel.dispatch(NotesAction.UpdateDraft(nextValue))
            }
        }
    }

    actionBar {
        submitButton = button("Submit") {
            onAction {
                viewModel.dispatch(NotesAction.Submit)
            }
        }
    }

    notesList = listView<String> {
        render { it }
    }

    draftField.bindText(scope, viewModel.state.map { it.draft })
    submitButton.bindDisable(scope, viewModel.state.map { it.draft.isBlank() })
    notesList.bindItems(scope, viewModel.state.map { it.notes })
}
```
