package dev.korafx.sample

import dev.korafx.mvvm.UiAction
import dev.korafx.mvvm.UiEvent
import dev.korafx.mvvm.ViewModel
import dev.korafx.mvvm.ViewState
import dev.korafx.navigation.Navigator
import dev.korafx.theme.ThemeManager
import kotlinx.coroutines.flow.collectLatest

data class WorkbenchState(
    val currentRouteId: String,
    val title: String,
    val summary: String,
    val document: String,
    val currentThemeName: String,
    val statusItems: List<String>,
    val feedbackMessage: String,
    val mvvmCount: Int,
    val mvvmDraft: String,
    val mvvmNotes: List<String>,
) : ViewState

sealed interface WorkbenchAction : UiAction {
    data class Navigate(val routeId: String) : WorkbenchAction
    data object ToggleTheme : WorkbenchAction
    data object IncrementCounter : WorkbenchAction
    data object DecrementCounter : WorkbenchAction
    data object ResetCounter : WorkbenchAction
    data class UpdateDraft(val value: String) : WorkbenchAction
    data object SubmitDraft : WorkbenchAction
    data class RecallNote(val value: String) : WorkbenchAction
    data object ClearNotes : WorkbenchAction
}

sealed interface WorkbenchEvent : UiEvent {
    data class Feedback(val message: String) : WorkbenchEvent
}

class WorkbenchViewModel(
    private val themeManager: ThemeManager,
    private val navigator: Navigator<WorkbenchRoute>,
) : ViewModel<WorkbenchState, WorkbenchAction, WorkbenchEvent>(
    initialState = WorkbenchState(
        currentRouteId = navigator.currentRoute.id,
        title = navigator.currentRoute.title,
        summary = navigator.currentRoute.summary,
        document = navigator.currentRoute.document,
        currentThemeName = themeManager.currentTheme().displayName,
        statusItems = listOf("DSL", "StateFlow", "MVVM", "Navigation", "Theme"),
        feedbackMessage = "Workbench started without DI or starter framework.",
        mvvmCount = 0,
        mvvmDraft = "StateFlow keeps JavaFX views predictable.",
        mvvmNotes = emptyList(),
    ),
) {
    init {
        rebuild(navigator.currentRoute)

        launch {
            navigator.state.collectLatest { navigation ->
                rebuild(navigation.currentRoute)
            }
        }

        launch {
            themeManager.theme.collectLatest { theme ->
                updateState { it.copy(currentThemeName = theme.displayName) }
            }
        }
    }

    override fun onAction(action: WorkbenchAction) {
        when (action) {
            is WorkbenchAction.Navigate -> navigate(action.routeId)
            WorkbenchAction.ToggleTheme -> toggleTheme()
            WorkbenchAction.IncrementCounter -> changeCounter(1)
            WorkbenchAction.DecrementCounter -> changeCounter(-1)
            WorkbenchAction.ResetCounter -> resetCounter()
            is WorkbenchAction.UpdateDraft -> updateDraft(action.value)
            WorkbenchAction.SubmitDraft -> submitDraft()
            is WorkbenchAction.RecallNote -> recallNote(action.value)
            WorkbenchAction.ClearNotes -> clearNotes()
        }
    }

    private fun navigate(routeId: String) {
        launch {
            val changed = navigator.navigate(routeId)
            val message =
                if (changed) {
                    "Navigated to ${navigator.currentRoute.title}."
                } else {
                    "Route not found: $routeId"
                }
            announce(message)
        }
    }

    private fun toggleTheme() {
        launch {
            themeManager.toggle()
            announce("Theme switched to ${themeManager.currentTheme().displayName}.")
        }
    }

    private fun changeCounter(delta: Int) {
        updateState { state ->
            state.copy(mvvmCount = state.mvvmCount + delta)
        }
        record("Counter changed to ${currentState.mvvmCount}.")
    }

    private fun resetCounter() {
        updateState { state ->
            state.copy(mvvmCount = 0)
        }
        record("Counter reset.")
    }

    private fun updateDraft(value: String) {
        updateState { state ->
            state.copy(mvvmDraft = value)
        }
    }

    private fun submitDraft() {
        val note = currentState.mvvmDraft.trim()
        if (note.isEmpty()) {
            record("Draft is empty.")
            return
        }

        updateState { state ->
            state.copy(
                mvvmDraft = "",
                mvvmNotes = listOf(note) + state.mvvmNotes,
            )
        }
        record("Submitted MVVM note.")
    }

    private fun recallNote(value: String) {
        updateState { state ->
            state.copy(mvvmDraft = value)
        }
        record("Recalled note into draft.")
    }

    private fun clearNotes() {
        if (currentState.mvvmNotes.isEmpty()) {
            record("No notes to clear.")
            return
        }

        updateState { state ->
            state.copy(mvvmNotes = emptyList())
        }
        record("Cleared MVVM notes.")
    }

    private fun rebuild(route: WorkbenchRoute) {
        updateState {
            it.copy(
                currentRouteId = route.id,
                title = route.title,
                summary = route.summary,
                document = route.document,
            )
        }
    }

    private suspend fun announce(message: String) {
        updateState { it.copy(feedbackMessage = message) }
        emitEvent(WorkbenchEvent.Feedback(message))
    }

    private fun record(message: String) {
        updateState { it.copy(feedbackMessage = message) }
        tryEmitEvent(WorkbenchEvent.Feedback(message))
    }
}
