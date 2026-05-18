package dev.korafx.components

import dev.korafx.dsl.styleClass
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane

fun appShell(
    init: StackPane.() -> Unit = {},
    content: AppShellBuilder.() -> Unit,
): StackPane {
    val layout = BorderPane().apply {
        styleClass("app-shell-layout")
    }
    val overlay = StackPane().apply {
        isPickOnBounds = false
        styleClass("app-shell-overlay")
    }

    return StackPane(layout, overlay).apply {
        styleClass("app-shell")
        init()
        AppShellBuilder(layout, overlay).content()
    }
}

class AppShellBuilder internal constructor(
    private val layout: BorderPane,
    private val overlay: StackPane,
) {
    fun topBar(node: Node) {
        layout.top = node
    }

    fun topBar(factory: () -> Node) {
        topBar(factory())
    }

    fun navigation(node: Node) {
        layout.left = node
    }

    fun navigation(factory: () -> Node) {
        navigation(factory())
    }

    fun content(node: Node) {
        layout.center = node
    }

    fun content(factory: () -> Node) {
        content(factory())
    }

    fun footer(node: Node) {
        layout.bottom = node
    }

    fun footer(factory: () -> Node) {
        footer(factory())
    }

    fun overlay(
        node: Node,
        alignment: Pos = Pos.BOTTOM_RIGHT,
        margin: Insets = Insets(16.0),
    ) {
        StackPane.setAlignment(node, alignment)
        StackPane.setMargin(node, margin)
        overlay.children += node
    }

    fun overlay(
        alignment: Pos = Pos.BOTTOM_RIGHT,
        margin: Insets = Insets(16.0),
        factory: () -> Node,
    ) {
        overlay(factory(), alignment, margin)
    }
}
