package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.vbox
import dev.korafx.state.collectLatestIn
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

enum class ToastTone {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
}

data class ToastMessage(
    val id: String,
    val message: String,
    val title: String? = null,
    val tone: ToastTone = ToastTone.INFO,
    val actionText: String? = null,
    val onAction: (() -> Unit)? = null,
)

class ToastHost(
    initialMessages: List<ToastMessage> = emptyList(),
) {
    private val ids = AtomicLong(initialMessages.size.toLong())
    private val messageState = MutableStateFlow(initialMessages)

    val messages: StateFlow<List<ToastMessage>> = messageState.asStateFlow()

    fun show(
        message: String,
        title: String? = null,
        tone: ToastTone = ToastTone.INFO,
        actionText: String? = null,
        onAction: (() -> Unit)? = null,
    ): ToastMessage {
        val nextMessage = ToastMessage(
            id = "toast-${ids.incrementAndGet()}",
            message = message,
            title = title,
            tone = tone,
            actionText = actionText,
            onAction = onAction,
        )
        messageState.update { it + nextMessage }
        return nextMessage
    }

    fun dismiss(id: String) {
        messageState.update { messages -> messages.filterNot { it.id == id } }
    }

    fun clear() {
        messageState.value = emptyList()
    }
}

fun snackbar(
    toast: ToastMessage,
    onDismiss: () -> Unit,
    init: HBox.() -> Unit = {},
): HBox =
    hbox(
        spacing = 12.0,
        init = {
            alignment = Pos.CENTER_LEFT
            styleClasses("snackbar", "toast-${toast.tone.name.lowercase()}")
            paddingAll(12.0)
            init()
        },
    ) {
        val textBox = vbox(spacing = 4.0) {
            if (toast.title != null) {
                label(toast.title) {
                    styleClass("snackbar-title")
                }
            }
            label(toast.message) {
                styleClass("snackbar-message")
                isWrapText = true
            }
        }
        HBox.setHgrow(textBox, Priority.ALWAYS)

        if (toast.actionText != null && toast.onAction != null) {
            ghostButton(toast.actionText) {
                styleClass("snackbar-action")
                onAction {
                    toast.onAction.invoke()
                }
            }
        }

        ghostButton("Dismiss") {
            styleClass("snackbar-dismiss")
            onAction(onDismiss)
        }
    }

fun toastHost(
    scope: CoroutineScope,
    host: ToastHost,
    maxVisible: Int = 3,
    init: VBox.() -> Unit = {},
): VBox =
    vbox(
        spacing = 10.0,
        init = {
            styleClass("toast-host")
            isPickOnBounds = false
            init()
        },
    ) {}.also { container ->
        host.messages.collectLatestIn(scope) { messages ->
            runOnFxThread {
                container.children.setAll(
                    messages.takeLast(maxVisible).map { toast ->
                        snackbar(
                            toast = toast,
                            onDismiss = {
                                host.dismiss(toast.id)
                            },
                        )
                    },
                )
            }
        }
    }

fun NodeContainerBuilder.snackbar(
    toast: ToastMessage,
    onDismiss: () -> Unit,
    init: HBox.() -> Unit = {},
): HBox =
    add(dev.korafx.components.snackbar(toast, onDismiss, init))

fun NodeContainerBuilder.toastHost(
    scope: CoroutineScope,
    host: ToastHost,
    maxVisible: Int = 3,
    init: VBox.() -> Unit = {},
): VBox =
    add(dev.korafx.components.toastHost(scope, host, maxVisible, init))

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}
