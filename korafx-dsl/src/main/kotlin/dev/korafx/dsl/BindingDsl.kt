@file:JvmName("BindingDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.Labeled
import javafx.scene.control.Label
import javafx.scene.control.TextInputControl
import javafx.scene.layout.Pane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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

fun Node.bindStyle(
    scope: CoroutineScope,
    flow: Flow<CssStyle?>,
): Job = flow.distinctUntilChanged().collectLatestIn(scope) { css ->
    runOnFxThread {
        if (css == null) {
            style = ""
        } else {
            cssStyle(css)
        }
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
