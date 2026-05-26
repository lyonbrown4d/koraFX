package dev.korafx.dsl

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox

class BorderPaneBuilder internal constructor(
    private val pane: BorderPane,
) {
    fun top(node: Node) {
        pane.top = node
    }

    fun top(factory: () -> Node) {
        top(factory())
    }

    fun right(node: Node) {
        pane.right = node
    }

    fun right(factory: () -> Node) {
        right(factory())
    }

    fun bottom(node: Node) {
        pane.bottom = node
    }

    fun bottom(factory: () -> Node) {
        bottom(factory())
    }

    fun left(node: Node) {
        pane.left = node
    }

    fun left(factory: () -> Node) {
        left(factory())
    }

    fun center(node: Node) {
        pane.center = node
    }

    fun center(factory: () -> Node) {
        center(factory())
    }
}

class VBoxBuilder internal constructor(
    box: VBox,
) : PaneBuilder<VBox>(box) {
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

class HBoxBuilder internal constructor(
    box: HBox,
) : PaneBuilder<HBox>(box) {
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

class StackPaneBuilder internal constructor(
    pane: StackPane,
) : PaneBuilder<StackPane>(pane) {
    fun alignment(value: Pos) {
        pane.alignment = value
    }
}

class FlowPaneBuilder internal constructor(
    pane: FlowPane,
) : PaneBuilder<FlowPane>(pane)

class TilePaneBuilder internal constructor(
    pane: TilePane,
) : PaneBuilder<TilePane>(pane)

class AnchorPaneBuilder internal constructor(
    private val pane: AnchorPane,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        pane.children += node
    }

    fun anchor(
        node: Node,
        top: Double? = null,
        right: Double? = null,
        bottom: Double? = null,
        left: Double? = null,
    ): Node =
        add(node).also {
            AnchorPane.setTopAnchor(it, top)
            AnchorPane.setRightAnchor(it, right)
            AnchorPane.setBottomAnchor(it, bottom)
            AnchorPane.setLeftAnchor(it, left)
        }

    fun anchor(
        top: Double? = null,
        right: Double? = null,
        bottom: Double? = null,
        left: Double? = null,
        factory: () -> Node,
    ): Node = anchor(factory(), top, right, bottom, left)
}
