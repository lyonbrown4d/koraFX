package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.VBoxBuilder
import dev.korafx.dsl.button
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
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

enum class ModalActionRole {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE,
}

data class ModalAction(
    val text: String,
    val role: ModalActionRole = ModalActionRole.SECONDARY,
    val closes: Boolean = true,
    val onAction: (() -> Unit)? = null,
)

data class ModalRequest(
    val id: String,
    val title: String,
    val message: String? = null,
    val actions: List<ModalAction> = listOf(ModalAction("Close")),
    val dismissOnBackdropClick: Boolean = true,
    val content: (VBoxBuilder.() -> Unit)? = null,
)

class ModalHost(
    initialRequest: ModalRequest? = null,
) {
    private val ids = AtomicLong(if (initialRequest == null) 0 else 1)
    private val requestState = MutableStateFlow(initialRequest)

    val current: StateFlow<ModalRequest?> = requestState.asStateFlow()

    fun show(
        title: String,
        message: String? = null,
        actions: List<ModalAction> = listOf(ModalAction("Close")),
        dismissOnBackdropClick: Boolean = true,
        content: (VBoxBuilder.() -> Unit)? = null,
    ): ModalRequest {
        val request = ModalRequest(
            id = "modal-${ids.incrementAndGet()}",
            title = title,
            message = message,
            actions = actions,
            dismissOnBackdropClick = dismissOnBackdropClick,
            content = content,
        )
        requestState.value = request
        return request
    }

    fun dismiss(id: String? = null) {
        requestState.update { current ->
            if (id == null || current?.id == id) {
                null
            } else {
                current
            }
        }
    }

    fun clear() {
        requestState.value = null
    }
}

fun modalHost(
    scope: CoroutineScope,
    host: ModalHost,
    init: StackPane.() -> Unit = {},
): StackPane =
    StackPane().apply {
        styleClass("modal-host")
        alignment = Pos.CENTER
        isVisible = false
        isManaged = false
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE
        init()
    }.also { container ->
        host.current.collectLatestIn(scope) { request ->
            runOnFxThread {
                container.children.clear()
                container.isVisible = request != null
                container.isManaged = request != null

                if (request != null) {
                    container.children += modalBackdrop(request, host)
                }
            }
        }
    }

fun NodeContainerBuilder.modalHost(
    scope: CoroutineScope,
    host: ModalHost,
    init: StackPane.() -> Unit = {},
): StackPane =
    add(dev.korafx.components.modalHost(scope, host, init))

private fun modalBackdrop(
    request: ModalRequest,
    host: ModalHost,
): StackPane =
    StackPane().apply {
        styleClass("modal-backdrop")
        alignment = Pos.CENTER
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE

        if (request.dismissOnBackdropClick) {
            onMouseClicked = javafx.event.EventHandler { event ->
                if (event.target == this) {
                    host.dismiss(request.id)
                }
            }
        }

        children += modalCard(request, host)
    }

private fun modalCard(
    request: ModalRequest,
    host: ModalHost,
): VBox =
    vbox(
        spacing = 14.0,
        init = {
            styleClass("modal-card")
            paddingAll(20.0)
            maxWidth = 520.0
        },
    ) {
        label(request.title) {
            styleClasses("modal-title", "headline")
            isWrapText = true
        }

        if (request.message != null) {
            label(request.message) {
                styleClasses("modal-message", "muted")
                isWrapText = true
            }
        }

        val content = request.content
        if (content != null) {
            vbox(
                spacing = 10.0,
                init = {
                    styleClass("modal-content")
                },
            ) {
                content()
            }
        }

        if (request.actions.isNotEmpty()) {
            hbox(
                spacing = 10.0,
                init = {
                    styleClass("modal-actions")
                    alignment = Pos.CENTER_RIGHT
                },
            ) {
                spacer()
                request.actions.forEach { action ->
                    when (action.role) {
                        ModalActionRole.PRIMARY ->
                            button(action.text) {
                                styleClass("modal-primary-action")
                                onAction {
                                    action.onAction?.invoke()
                                    if (action.closes) {
                                        host.dismiss(request.id)
                                    }
                                }
                            }

                        ModalActionRole.SECONDARY ->
                            ghostButton(action.text) {
                                styleClass("modal-secondary-action")
                                onAction {
                                    action.onAction?.invoke()
                                    if (action.closes) {
                                        host.dismiss(request.id)
                                    }
                                }
                            }

                        ModalActionRole.DESTRUCTIVE ->
                            button(action.text) {
                                styleClass("modal-destructive-action")
                                onAction {
                                    action.onAction?.invoke()
                                    if (action.closes) {
                                        host.dismiss(request.id)
                                    }
                                }
                            }
                    }
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
