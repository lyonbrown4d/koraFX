@file:JvmName("StatefulDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.control.TextInputControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion

fun TextInputControl.bindTextToState(
    scope: CoroutineScope,
    flow: Flow<String>,
    onTextChange: ((String) -> Unit)?,
): Job {
    var updatingFromState = false
    val listener =
        if (onTextChange == null) {
            null
        } else {
            ChangeListener<String> { _, _, newValue ->
                if (!updatingFromState) {
                    onTextChange(newValue.orEmpty())
                }
            }
        }

    runOnFxThread {
        if (listener != null) {
            textProperty().addListener(listener)
        }
    }

    return flow
        .onCompletion {
            runOnFxThread {
                if (listener != null) {
                    textProperty().removeListener(listener)
                }
            }
        }
        .collectLatestIn(scope) { value ->
            runOnFxThread {
                if (text != value) {
                    updatingFromState = true
                    text = value
                    updatingFromState = false
                }
            }
        }
}

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}
