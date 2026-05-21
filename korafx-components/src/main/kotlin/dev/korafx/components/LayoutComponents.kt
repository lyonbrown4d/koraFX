package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import javafx.scene.Node
import javafx.scene.layout.BorderPane

fun borderLayout(
    init: BorderPane.() -> Unit = {},
    content: BorderLayoutBuilder.() -> Unit,
): BorderPane =
    BorderPane().apply {
        styleClass("border-layout")
        init()
        BorderLayoutBuilder(this).content()
    }

class BorderLayoutBuilder internal constructor(
    private val pane: BorderPane,
) {
    fun top(node: Node) {
        pane.top = node.withSlot("border-layout-top")
    }

    fun top(factory: () -> Node) {
        top(factory())
    }

    fun header(node: Node) {
        top(node)
    }

    fun header(factory: () -> Node) {
        top(factory)
    }

    fun right(node: Node) {
        pane.right = node.withSlot("border-layout-right")
    }

    fun right(factory: () -> Node) {
        right(factory())
    }

    fun bottom(node: Node) {
        pane.bottom = node.withSlot("border-layout-bottom")
    }

    fun bottom(factory: () -> Node) {
        bottom(factory())
    }

    fun footer(node: Node) {
        bottom(node)
    }

    fun footer(factory: () -> Node) {
        bottom(factory)
    }

    fun left(node: Node) {
        pane.left = node.withSlot("border-layout-left")
    }

    fun left(factory: () -> Node) {
        left(factory())
    }

    fun sidebar(node: Node) {
        left(node)
    }

    fun sidebar(factory: () -> Node) {
        left(factory)
    }

    fun center(node: Node) {
        pane.center = node.withSlot("border-layout-center")
    }

    fun center(factory: () -> Node) {
        center(factory())
    }

    fun content(node: Node) {
        center(node)
    }

    fun content(factory: () -> Node) {
        center(factory)
    }

    private fun Node.withSlot(styleClass: String): Node =
        apply {
            styleClass(styleClass)
        }
}

fun NodeContainerBuilder.borderLayout(
    init: BorderPane.() -> Unit = {},
    content: BorderLayoutBuilder.() -> Unit,
): BorderPane =
    add(
        dev.korafx.components.borderLayout(
            init = init,
            content = content,
        ),
    )
