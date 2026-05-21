package dev.korafx.navigation

import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

fun <R : Route> ScrollPane.routeScrollRestoration(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    key: String = "scroll",
    defaultVValue: Double? = 0.0,
    defaultHValue: Double? = 0.0,
): Job {
    require(key.isNotBlank()) {
        "Route scroll restoration key cannot be blank."
    }

    val vKey = "$key.v"
    val hKey = "$key.h"
    val verticalListener = ChangeListener<Number> { _, _, value ->
        navigator.saveState(vKey, value.toDouble())
    }
    val horizontalListener = ChangeListener<Number> { _, _, value ->
        navigator.saveState(hKey, value.toDouble())
    }

    vvalueProperty().addListener(verticalListener)
    hvalueProperty().addListener(horizontalListener)

    val job = navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            val restoredVValue = navigator.restoredState<Double>(vKey, state.currentLocation) ?: defaultVValue
            val restoredHValue = navigator.restoredState<Double>(hKey, state.currentLocation) ?: defaultHValue
            restoredVValue?.let { value -> vvalue = value.coerceIn(vmin, vmax) }
            restoredHValue?.let { value -> hvalue = value.coerceIn(hmin, hmax) }
        }
    }
    job.invokeOnCompletion {
        runOnFxThread {
            vvalueProperty().removeListener(verticalListener)
            hvalueProperty().removeListener(horizontalListener)
        }
    }

    return job
}

fun <R : Route, T> ListView<T>.routeSelectionRestoration(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    key: String = "selection",
    clearMissing: Boolean = true,
    keyOf: (T) -> Any? = { item -> item },
): Job {
    require(key.isNotBlank()) {
        "Route selection restoration key cannot be blank."
    }

    val selectedKey = "$key.selected"
    val listener = ChangeListener<T?> { _, _, item ->
        navigator.saveState(selectedKey, item?.let(keyOf))
    }
    selectionModel.selectedItemProperty().addListener(listener)

    val job = navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            val restored = navigator.restoredState<Any>(selectedKey, state.currentLocation)
            val index = items.indexOfFirst { item -> keyOf(item) == restored }
            when {
                index >= 0 -> selectionModel.select(index)
                clearMissing -> selectionModel.clearSelection()
            }
        }
    }
    job.invokeOnCompletion {
        runOnFxThread {
            selectionModel.selectedItemProperty().removeListener(listener)
        }
    }

    return job
}

fun <R : Route, T> TableView<T>.routeSelectionRestoration(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    key: String = "selection",
    clearMissing: Boolean = true,
    keyOf: (T) -> Any? = { item -> item },
): Job {
    require(key.isNotBlank()) {
        "Route selection restoration key cannot be blank."
    }

    val selectedKey = "$key.selected"
    val listener = ChangeListener<T?> { _, _, item ->
        navigator.saveState(selectedKey, item?.let(keyOf))
    }
    selectionModel.selectedItemProperty().addListener(listener)

    val job = navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            val restored = navigator.restoredState<Any>(selectedKey, state.currentLocation)
            val index = items.indexOfFirst { item -> keyOf(item) == restored }
            when {
                index >= 0 -> selectionModel.select(index)
                clearMissing -> selectionModel.clearSelection()
            }
        }
    }
    job.invokeOnCompletion {
        runOnFxThread {
            selectionModel.selectedItemProperty().removeListener(listener)
        }
    }

    return job
}

fun <R : Route> Node.routeFocusRestoration(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    key: String = "focus",
): Job {
    require(key.isNotBlank()) {
        "Route focus restoration key cannot be blank."
    }

    val focusedKey = "$key.focused"
    val listener = ChangeListener<Boolean> { _, _, focused ->
        if (focused) {
            navigator.saveState(focusedKey, true)
        }
    }
    focusedProperty().addListener(listener)

    val job = navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            if (navigator.restoredState<Boolean>(focusedKey, state.currentLocation) == true) {
                requestFocus()
            }
        }
    }
    job.invokeOnCompletion {
        runOnFxThread {
            focusedProperty().removeListener(listener)
        }
    }

    return job
}

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater(block)
    }
}
