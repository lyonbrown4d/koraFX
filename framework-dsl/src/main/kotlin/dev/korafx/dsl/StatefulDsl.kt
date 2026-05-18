package dev.korafx.dsl

import dev.korafx.state.collectLatestIn
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.control.Separator
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

fun <S, N : Node> stateful(
    scope: CoroutineScope,
    state: Flow<S>,
    content: StatefulRootBuilder<S>.() -> N,
): N =
    StatefulRootBuilder(scope, state).content()

fun <S, N : Node> NodeContainerBuilder.stateful(
    scope: CoroutineScope,
    state: Flow<S>,
    content: StatefulRootBuilder<S>.() -> N,
): N =
    add(dev.korafx.dsl.stateful(scope, state, content))

fun <S, N : Node> N.stateVisible(
    scope: CoroutineScope,
    state: Flow<S>,
    manageWhenHidden: Boolean = true,
    visible: (S) -> Boolean,
): N =
    also {
        bindVisible(scope, state.map(visible).distinctUntilChanged(), manageWhenHidden)
    }

fun <S, N : Node> N.stateDisable(
    scope: CoroutineScope,
    state: Flow<S>,
    disabled: (S) -> Boolean,
): N =
    also {
        bindDisable(scope, state.map(disabled).distinctUntilChanged())
    }

fun <S, N : Node> N.stateStyle(
    scope: CoroutineScope,
    state: Flow<S>,
    style: (S) -> CssStyle?,
): N =
    also {
        bindStyle(scope, state.map(style).distinctUntilChanged())
    }

fun <S, N : Node> N.stateStyleClass(
    scope: CoroutineScope,
    state: Flow<S>,
    className: String,
    enabled: (S) -> Boolean,
): N =
    also {
        bindStyleClass(scope, className, state.map(enabled).distinctUntilChanged())
    }

fun <S, L : Labeled> L.stateText(
    scope: CoroutineScope,
    state: Flow<S>,
    text: (S) -> Any?,
): L =
    also {
        bindText(scope, state.map { value -> text(value)?.toString().orEmpty() }.distinctUntilChanged())
    }

fun <S, T : TextInputControl> T.stateText(
    scope: CoroutineScope,
    state: Flow<S>,
    onTextChange: ((String) -> Unit)? = null,
    text: (S) -> String,
): T =
    also {
        bindTextToState(scope, state.map(text).distinctUntilChanged(), onTextChange)
    }

fun <S, L : Label> L.stateValidation(
    scope: CoroutineScope,
    state: Flow<S>,
    message: (S) -> String?,
): L =
    also {
        bindValidation(scope, state.map(message).distinctUntilChanged())
    }

fun <S, T, P : Pane> P.stateList(
    scope: CoroutineScope,
    state: Flow<S>,
    items: (S) -> List<T>,
    empty: FragmentBuilder.() -> Unit = {},
    item: FragmentBuilder.(T) -> Unit,
): P =
    also {
        bindList(
            scope = scope,
            flow = state.map(items).distinctUntilChanged(),
            empty = empty,
            item = item,
        )
    }

class StatefulRootBuilder<S> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
) : StatefulNodeContainerBuilder<S>(scope, state) {
    private var root: Node? = null

    override fun append(node: Node) {
        check(root == null) {
            "stateful root can only contain one root node."
        }
        root = node
    }
}

abstract class StatefulNodeContainerBuilder<S> internal constructor(
    protected val scope: CoroutineScope,
    protected val state: Flow<S>,
) {
    protected abstract fun append(node: Node)

    fun <T : Node> add(node: T): T {
        append(node)
        return node
    }

    fun label(text: String = "", init: Label.() -> Unit = {}): Label =
        add(dev.korafx.dsl.label(text, init))

    fun label(
        text: (S) -> Any?,
        init: Label.() -> Unit = {},
    ): Label =
        add(dev.korafx.dsl.label("", init)).also { label ->
            label.bindStateText(text)
        }

    fun button(
        text: String,
        disabled: ((S) -> Boolean)? = null,
        init: Button.() -> Unit = {},
    ): Button =
        add(dev.korafx.dsl.button(text, init)).also { button ->
            if (disabled != null) {
                button.bindStateDisabled(disabled)
            }
        }

    fun checkBox(text: String, init: CheckBox.() -> Unit = {}): CheckBox =
        add(dev.korafx.dsl.checkBox(text, init))

    fun textField(text: String = "", init: TextField.() -> Unit = {}): TextField =
        add(dev.korafx.dsl.textField(text, init))

    fun textField(
        text: (S) -> String,
        onTextChange: ((String) -> Unit)? = null,
        init: TextField.() -> Unit = {},
    ): TextField =
        add(dev.korafx.dsl.textField("", init)).also { field ->
            field.bindStateText(text, onTextChange)
        }

    fun textArea(text: String = "", init: TextArea.() -> Unit = {}): TextArea =
        add(dev.korafx.dsl.textArea(text, init))

    fun textArea(
        text: (S) -> String,
        onTextChange: ((String) -> Unit)? = null,
        init: TextArea.() -> Unit = {},
    ): TextArea =
        add(dev.korafx.dsl.textArea("", init)).also { area ->
            area.bindStateText(text, onTextChange)
        }

    fun separator(init: Separator.() -> Unit = {}): Separator =
        add(dev.korafx.dsl.separator(init))

    fun region(init: Region.() -> Unit = {}): Region =
        add(dev.korafx.dsl.region(init))

    fun vbox(
        spacing: Double = 0.0,
        init: VBox.() -> Unit = {},
        content: StatefulVBoxBuilder<S>.() -> Unit,
    ): VBox =
        add(
            VBox(spacing).apply(init).apply {
                StatefulVBoxBuilder(scope, state, this).content()
            },
        )

    fun hbox(
        spacing: Double = 0.0,
        init: HBox.() -> Unit = {},
        content: StatefulHBoxBuilder<S>.() -> Unit,
    ): HBox =
        add(
            HBox(spacing).apply(init).apply {
                StatefulHBoxBuilder(scope, state, this).content()
            },
        )

    fun stackPane(
        init: StackPane.() -> Unit = {},
        content: StatefulStackPaneBuilder<S>.() -> Unit,
    ): StackPane =
        add(
            StackPane().apply(init).apply {
                StatefulStackPaneBuilder(scope, state, this).content()
            },
        )

    fun spacer(
        minWidth: Double = 0.0,
        minHeight: Double = 0.0,
        grow: Priority = Priority.ALWAYS,
    ): Region =
        region {
            this.minWidth = minWidth
            this.minHeight = minHeight
            HBox.setHgrow(this, grow)
            VBox.setVgrow(this, grow)
        }

    fun <T> list(
        spacing: Double = 0.0,
        items: (S) -> List<T>,
        init: VBox.() -> Unit = {},
        empty: FragmentBuilder.() -> Unit = {},
        item: FragmentBuilder.(T) -> Unit,
    ): VBox =
        add(VBox(spacing).apply(init)).also { container ->
            container.bindList(
                scope = scope,
                flow = state.map(items).distinctUntilChanged(),
                empty = empty,
                item = item,
            )
        }

    fun Node.stateVisible(
        manageWhenHidden: Boolean = true,
        visible: (S) -> Boolean,
    ): Job =
        bindStateVisible(visible, manageWhenHidden)

    fun Node.stateDisable(disabled: (S) -> Boolean): Job =
        bindStateDisabled(disabled)

    fun Node.stateStyle(
        style: (S) -> CssStyle?,
    ): Job =
        bindStateStyle(style)

    fun Node.stateStyleClass(
        className: String,
        enabled: (S) -> Boolean,
    ): Job =
        bindStateStyleClass(className, enabled)

    fun Labeled.stateText(text: (S) -> Any?): Job =
        bindStateText(text)

    fun Label.stateValidation(message: (S) -> String?): Job =
        bindValidation(scope, state.map(message).distinctUntilChanged())

    protected fun Node.bindStateVisible(
        visible: (S) -> Boolean,
        manageWhenHidden: Boolean = true,
    ): Job =
        bindVisible(scope, state.map(visible).distinctUntilChanged(), manageWhenHidden)

    protected fun Node.bindStateDisabled(disabled: (S) -> Boolean): Job =
        bindDisable(scope, state.map(disabled).distinctUntilChanged())

    protected fun Node.bindStateStyleClass(
        className: String,
        enabled: (S) -> Boolean,
    ): Job =
        bindStyleClass(scope, className, state.map(enabled).distinctUntilChanged())

    protected fun Node.bindStateStyle(
        style: (S) -> CssStyle?,
    ): Job =
        bindStyle(scope, state.map(style).distinctUntilChanged())

    protected fun Labeled.bindStateText(text: (S) -> Any?): Job =
        bindText(scope, state.map { value -> text(value)?.toString().orEmpty() }.distinctUntilChanged())

    protected fun TextInputControl.bindStateText(
        text: (S) -> String,
        onTextChange: ((String) -> Unit)? = null,
    ): Job =
        bindTextToState(scope, state.map(text).distinctUntilChanged(), onTextChange)
}

abstract class StatefulPaneBuilder<S, T : Pane> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    protected val pane: T,
) : StatefulNodeContainerBuilder<S>(scope, state) {
    override fun append(node: Node) {
        pane.children += node
    }
}

class StatefulVBoxBuilder<S> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    box: VBox,
) : StatefulPaneBuilder<S, VBox>(scope, state, box) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }

    fun fillWidth(value: Boolean = true) {
        pane.isFillWidth = value
    }

    fun spacing(value: Double) {
        pane.spacing = value
    }
}

class StatefulHBoxBuilder<S> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    box: HBox,
) : StatefulPaneBuilder<S, HBox>(scope, state, box) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }

    fun fillHeight(value: Boolean = true) {
        pane.isFillHeight = value
    }

    fun spacing(value: Double) {
        pane.spacing = value
    }
}

class StatefulStackPaneBuilder<S> internal constructor(
    scope: CoroutineScope,
    state: Flow<S>,
    pane: StackPane,
) : StatefulPaneBuilder<S, StackPane>(scope, state, pane) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }
}

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
