package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.button
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.progressIndicator
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.vbox
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.VBox

fun feedbackState(
    title: String,
    message: String? = null,
    graphic: Node? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    stateStyleClass: String? = null,
    init: VBox.() -> Unit = {},
): VBox =
    vbox(
        spacing = 12.0,
        init = {
            alignment = Pos.CENTER
            styleClass("feedback-state")
            stateStyleClass?.let { styleClass(it) }
            paddingAll(32.0)
            init()
        },
    ) {
        if (graphic != null) {
            add(graphic)
        }

        label(title) {
            styleClasses("feedback-title", "headline")
            isWrapText = true
        }

        if (message != null) {
            label(message) {
                styleClasses("feedback-message", "muted")
                isWrapText = true
            }
        }

        if (actionText != null) {
            hbox(spacing = 8.0, init = { alignment = Pos.CENTER }) {
                button(actionText) {
                    onAction?.let { handler ->
                        setOnAction {
                            handler()
                        }
                    }
                }
            }
        }
    }

fun emptyState(
    title: String = "No content",
    message: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    init: VBox.() -> Unit = {},
): VBox =
    feedbackState(
        title = title,
        message = message,
        actionText = actionText,
        onAction = onAction,
        stateStyleClass = "empty-state",
        init = init,
    )

fun loadingState(
    message: String = "Loading...",
    progress: Double = ProgressIndicator.INDETERMINATE_PROGRESS,
    init: VBox.() -> Unit = {},
): VBox =
    feedbackState(
        title = message,
        graphic = progressIndicator(progress) {
            styleClass("loading-state-indicator")
        },
        stateStyleClass = "loading-state",
        init = init,
    )

fun errorState(
    title: String = "Something went wrong",
    message: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    init: VBox.() -> Unit = {},
): VBox =
    feedbackState(
        title = title,
        message = message,
        actionText = actionText,
        onAction = onAction,
        stateStyleClass = "error-state",
        init = init,
    )

fun NodeContainerBuilder.feedbackState(
    title: String,
    message: String? = null,
    graphic: Node? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    stateStyleClass: String? = null,
    init: VBox.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.feedbackState(
            title = title,
            message = message,
            graphic = graphic,
            actionText = actionText,
            onAction = onAction,
            stateStyleClass = stateStyleClass,
            init = init,
        ),
    )

fun NodeContainerBuilder.emptyState(
    title: String = "No content",
    message: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    init: VBox.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.emptyState(
            title = title,
            message = message,
            actionText = actionText,
            onAction = onAction,
            init = init,
        ),
    )

fun NodeContainerBuilder.loadingState(
    message: String = "Loading...",
    progress: Double = ProgressIndicator.INDETERMINATE_PROGRESS,
    init: VBox.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.loadingState(
            message = message,
            progress = progress,
            init = init,
        ),
    )

fun NodeContainerBuilder.errorState(
    title: String = "Something went wrong",
    message: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    init: VBox.() -> Unit = {},
): VBox =
    add(
        dev.korafx.components.errorState(
            title = title,
            message = message,
            actionText = actionText,
            onAction = onAction,
            init = init,
        ),
    )
