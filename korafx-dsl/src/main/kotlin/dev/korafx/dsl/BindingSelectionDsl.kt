@file:JvmName("BindingDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}

fun <T> ListView<T>.bindItems(
    scope: CoroutineScope,
    flow: Flow<List<T>>,
): Job = flow.collectLatestIn(scope) { values ->
    runOnFxThread {
        items.setAll(values)
    }
}

fun <T> ComboBox<T>.bindItems(
    scope: CoroutineScope,
    flow: Flow<List<T>>,
): Job = flow.collectLatestIn(scope) { values ->
    runOnFxThread {
        items.setAll(values)
    }
}

fun <T> ChoiceBox<T>.bindItems(
    scope: CoroutineScope,
    flow: Flow<List<T>>,
): Job = flow.collectLatestIn(scope) { values ->
    runOnFxThread {
        items.setAll(values)
    }
}

fun <T> TableView<T>.bindItems(
    scope: CoroutineScope,
    flow: Flow<List<T>>,
): Job = flow.collectLatestIn(scope) { values ->
    runOnFxThread {
        items.setAll(values)
    }
}

fun <T> ListView<T>.bindSelectedItem(
    scope: CoroutineScope,
    flow: Flow<T?>,
): Job = flow.collectLatestIn(scope) { selected ->
    runOnFxThread {
        if (selected == null) {
            selectionModel.clearSelection()
        } else {
            selectionModel.select(selected)
        }
    }
}

fun <T> ListView<T>.bindSelectedItemBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<T?>,
): Job {
    val listener = ChangeListener<T> { _, _, newValue ->
        if (state.value != newValue) {
            state.value = newValue
        }
    }

    runOnFxThread {
        selectionModel.selectedItemProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                selectionModel.selectedItemProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { selected ->
            runOnFxThread {
                if (selectionModel.selectedItem != selected) {
                    if (selected == null) {
                        selectionModel.clearSelection()
                    } else {
                        selectionModel.select(selected)
                    }
                }
            }
        }
}

fun <T> TableView<T>.bindSelectedItem(
    scope: CoroutineScope,
    flow: Flow<T?>,
): Job = flow.collectLatestIn(scope) { selected ->
    runOnFxThread {
        if (selected == null) {
            selectionModel.clearSelection()
        } else {
            selectionModel.select(selected)
        }
    }
}

fun <T> TableView<T>.bindSelectedItemBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<T?>,
): Job {
    val listener = ChangeListener<T> { _, _, newValue ->
        if (state.value != newValue) {
            state.value = newValue
        }
    }

    runOnFxThread {
        selectionModel.selectedItemProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                selectionModel.selectedItemProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { selected ->
            runOnFxThread {
                if (selectionModel.selectedItem != selected) {
                    if (selected == null) {
                        selectionModel.clearSelection()
                    } else {
                        selectionModel.select(selected)
                    }
                }
            }
        }
}

fun <T> ComboBox<T>.bindSelectedItem(
    scope: CoroutineScope,
    flow: Flow<T?>,
): Job = flow.collectLatestIn(scope) { selected ->
    runOnFxThread {
        if (selected == null) {
            selectionModel.clearSelection()
        } else {
            selectionModel.select(selected)
        }
    }
}

fun <T> ComboBox<T>.bindSelectedItemBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<T?>,
): Job {
    val listener = ChangeListener<T> { _, _, newValue ->
        if (state.value != newValue) {
            state.value = newValue
        }
    }

    runOnFxThread {
        selectionModel.selectedItemProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                selectionModel.selectedItemProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { selected ->
            runOnFxThread {
                if (selectionModel.selectedItem != selected) {
                    if (selected == null) {
                        selectionModel.clearSelection()
                    } else {
                        selectionModel.select(selected)
                    }
                }
            }
        }
}

fun <T> ChoiceBox<T>.bindSelectedItem(
    scope: CoroutineScope,
    flow: Flow<T?>,
): Job = flow.collectLatestIn(scope) { selected ->
    runOnFxThread {
        if (selected == null) {
            selectionModel.clearSelection()
        } else {
            selectionModel.select(selected)
        }
    }
}

fun <T> ChoiceBox<T>.bindSelectedItemBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<T?>,
): Job {
    val listener = ChangeListener<T> { _, _, newValue ->
        if (state.value != newValue) {
            state.value = newValue
        }
    }

    runOnFxThread {
        selectionModel.selectedItemProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                selectionModel.selectedItemProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { selected ->
            runOnFxThread {
                if (selectionModel.selectedItem != selected) {
                    if (selected == null) {
                        selectionModel.clearSelection()
                    } else {
                        selectionModel.select(selected)
                    }
                }
            }
        }
}
