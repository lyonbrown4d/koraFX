package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

fun workbenchLayout(
    init: BorderPane.() -> Unit = {},
    content: WorkbenchLayoutBuilder.() -> Unit,
): BorderPane =
    borderPane(init = init) {
        WorkbenchLayoutBuilder(this).content()
    }

class WorkbenchLayoutBuilder internal constructor(
    private val delegate: BorderPaneBuilder,
) {
    fun topBar(node: Node) {
        delegate.top(node)
    }

    fun topBar(factory: () -> Node) {
        delegate.top(factory)
    }

    fun navigation(node: Node) {
        delegate.left(node)
    }

    fun navigation(factory: () -> Node) {
        delegate.left(factory)
    }

    fun content(node: Node) {
        delegate.center(node)
    }

    fun content(factory: () -> Node) {
        delegate.center(factory)
    }

    fun footer(node: Node) {
        delegate.bottom(node)
    }

    fun footer(factory: () -> Node) {
        delegate.bottom(factory)
    }
}

fun panel(
    spacing: Double = 16.0,
    padding: Double = 24.0,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit,
): VBox =
    vbox(
        spacing = spacing,
        init = {
            styleClass("panel")
            paddingAll(padding)
            init()
        },
        content = content,
    )

fun sidebar(
    width: Double = 220.0,
    spacing: Double = 10.0,
    init: VBox.() -> Unit = {},
    content: VBoxBuilder.() -> Unit,
): VBox =
    vbox(
        spacing = spacing,
        init = {
            styleClass("nav-rail")
            prefWidth = width
            init()
        },
        content = content,
    )

fun statusBar(
    spacing: Double = 16.0,
    padding: Double = 16.0,
    init: HBox.() -> Unit = {},
    content: HBoxBuilder.() -> Unit,
): HBox =
    hbox(
        spacing = spacing,
        init = {
            styleClass("status-strip")
            paddingAll(padding)
            init()
        },
        content = content,
    )

fun ghostButton(
    text: String,
    init: Button.() -> Unit = {},
): Button =
    Button(text).apply {
        styleClass("ghost-button")
        init()
    }

fun navButton(
    text: String,
    active: Boolean = false,
    init: Button.() -> Unit = {},
): Button =
    Button(text).apply {
        styleClass("nav-button")
        if (active) {
            styleClass("nav-button-active")
        } else {
            styleClass("ghost-button")
        }
        init()
    }

fun NodeContainerBuilder.ghostButton(
    text: String,
    init: Button.() -> Unit = {},
): Button = add(dev.korafx.dsl.ghostButton(text, init))

fun NodeContainerBuilder.navButton(
    text: String,
    active: Boolean = false,
    init: Button.() -> Unit = {},
): Button = add(dev.korafx.dsl.navButton(text, active, init))
