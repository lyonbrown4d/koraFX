@file:JvmName("BindingDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.control.DatePicker
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.ToggleButton
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
