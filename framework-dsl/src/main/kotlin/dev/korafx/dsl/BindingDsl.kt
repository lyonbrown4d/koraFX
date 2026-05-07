package dev.korafx.dsl

import dev.korafx.state.collectLatestIn
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Labeled
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.TableView
import javafx.scene.control.TextInputControl
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Pane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import java.time.LocalDate

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}

fun Labeled.bindText(
    scope: CoroutineScope,
    flow: Flow<String>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        text = value
    }
}

fun TextInputControl.bindText(
    scope: CoroutineScope,
    flow: Flow<String>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        if (text != value) {
            text = value
        }
    }
}

fun TextInputControl.bindTextBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<String>,
): Job {
    val listener = ChangeListener<String> { _, _, newValue ->
        val nextValue = newValue.orEmpty()
        if (state.value != nextValue) {
            state.value = nextValue
        }
    }

    runOnFxThread {
        textProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                textProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { value ->
            runOnFxThread {
                if (text != value) {
                    text = value
                }
            }
        }
}

fun Node.bindVisible(
    scope: CoroutineScope,
    flow: Flow<Boolean>,
    manageWhenHidden: Boolean = true,
): Job = flow.collectLatestIn(scope) { visible ->
    runOnFxThread {
        visibleWhen(visible, manageWhenHidden)
    }
}

fun Node.bindManaged(
    scope: CoroutineScope,
    flow: Flow<Boolean>,
): Job = flow.collectLatestIn(scope) { managed ->
    runOnFxThread {
        isManaged = managed
    }
}

fun Node.bindDisable(
    scope: CoroutineScope,
    flow: Flow<Boolean>,
): Job = flow.collectLatestIn(scope) { disabled ->
    runOnFxThread {
        isDisable = disabled
    }
}

fun Node.bindOpacity(
    scope: CoroutineScope,
    flow: Flow<Double>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        opacity = value
    }
}

fun Node.bindStyleClass(
    scope: CoroutineScope,
    className: String,
    flow: Flow<Boolean>,
): Job = flow.collectLatestIn(scope) { enabled ->
    runOnFxThread {
        toggleStyleClass(className, enabled)
    }
}

fun Node.bindInvalid(
    scope: CoroutineScope,
    flow: Flow<Boolean>,
): Job = flow.collectLatestIn(scope) { invalid ->
    runOnFxThread {
        invalidWhen(invalid)
    }
}

fun Label.bindValidation(
    scope: CoroutineScope,
    flow: Flow<String?>,
    manageWhenHidden: Boolean = true,
): Job = flow.collectLatestIn(scope) { message ->
    runOnFxThread {
        val visible = !message.isNullOrBlank()
        text = message.orEmpty()
        visibleWhen(visible, manageWhenHidden)
    }
}

fun <T> Pane.bindChildren(
    scope: CoroutineScope,
    flow: Flow<List<T>>,
    factory: (T) -> Node,
): Job = flow.collectLatestIn(scope) { values ->
    runOnFxThread {
        val nodes = values.map(factory)
        children.setAll(nodes)
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

fun ToggleButton.bindSelected(
    scope: CoroutineScope,
    flow: Flow<Boolean>,
): Job = flow.collectLatestIn(scope) { selected ->
    runOnFxThread {
        isSelected = selected
    }
}

fun ToggleButton.bindSelectedBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<Boolean>,
): Job {
    val listener = ChangeListener<Boolean> { _, _, newValue ->
        val nextValue = newValue == true
        if (state.value != nextValue) {
            state.value = nextValue
        }
    }

    runOnFxThread {
        selectedProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                selectedProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { selected ->
            runOnFxThread {
                if (isSelected != selected) {
                    isSelected = selected
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

fun Slider.bindValue(
    scope: CoroutineScope,
    flow: Flow<Double>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        if (this.value != value) {
            this.value = value
        }
    }
}

fun Slider.bindValueBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<Double>,
): Job {
    val listener = ChangeListener<Number> { _, _, newValue ->
        val nextValue = newValue?.toDouble() ?: 0.0
        if (state.value != nextValue) {
            state.value = nextValue
        }
    }

    runOnFxThread {
        valueProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                valueProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { value ->
            runOnFxThread {
                if (this.value != value) {
                    this.value = value
                }
            }
        }
}

fun <T> Spinner<T>.bindValue(
    scope: CoroutineScope,
    flow: Flow<T>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        valueFactory?.value = value
    }
}

fun <T> Spinner<T>.bindValueBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<T>,
): Job {
    val listener = ChangeListener<T> { _, _, newValue ->
        if (state.value != newValue && newValue != null) {
            state.value = newValue
        }
    }

    runOnFxThread {
        valueProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                valueProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { value ->
            runOnFxThread {
                if (this.value != value) {
                    valueFactory?.value = value
                }
            }
        }
}

fun DatePicker.bindValue(
    scope: CoroutineScope,
    flow: Flow<LocalDate?>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        if (this.value != value) {
            this.value = value
        }
    }
}

fun DatePicker.bindValueBidirectional(
    scope: CoroutineScope,
    state: MutableStateFlow<LocalDate?>,
): Job {
    val listener = ChangeListener<LocalDate> { _, _, newValue ->
        if (state.value != newValue) {
            state.value = newValue
        }
    }

    runOnFxThread {
        valueProperty().addListener(listener)
    }

    return state
        .onCompletion {
            runOnFxThread {
                valueProperty().removeListener(listener)
            }
        }
        .collectLatestIn(scope) { value ->
            runOnFxThread {
                if (this.value != value) {
                    this.value = value
                }
            }
        }
}

fun ProgressBar.bindProgress(
    scope: CoroutineScope,
    flow: Flow<Double>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        progress = value
    }
}

fun ProgressIndicator.bindProgress(
    scope: CoroutineScope,
    flow: Flow<Double>,
): Job = flow.collectLatestIn(scope) { value ->
    runOnFxThread {
        progress = value
    }
}
