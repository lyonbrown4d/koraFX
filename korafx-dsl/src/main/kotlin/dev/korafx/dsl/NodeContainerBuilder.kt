package dev.korafx.dsl

import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.ToolBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox

abstract class NodeContainerBuilder internal constructor() {
    protected abstract fun append(node: Node)

    fun <T : Node> add(node: T): T {
        append(node)
        return node
    }

    fun label(text: String = "", init: Label.() -> Unit = {}): Label =
        add(dev.korafx.dsl.label(text, init))

    fun button(text: String, init: Button.() -> Unit = {}): Button =
        add(dev.korafx.dsl.button(text, init))

    fun checkBox(text: String, init: CheckBox.() -> Unit = {}): CheckBox =
        add(dev.korafx.dsl.checkBox(text, init))

    fun textField(text: String = "", init: TextField.() -> Unit = {}): TextField =
        add(dev.korafx.dsl.textField(text, init))

    fun textArea(text: String = "", init: TextArea.() -> Unit = {}): TextArea =
        add(dev.korafx.dsl.textArea(text, init))

    fun separator(init: Separator.() -> Unit = {}): Separator =
        add(dev.korafx.dsl.separator(init))

    fun region(init: Region.() -> Unit = {}): Region =
        add(dev.korafx.dsl.region(init))

    fun vbox(
        spacing: Double = 0.0,
        init: VBox.() -> Unit = {},
        content: VBoxBuilder.() -> Unit,
    ): VBox = add(dev.korafx.dsl.vbox(spacing, init, content))

    fun hbox(
        spacing: Double = 0.0,
        init: HBox.() -> Unit = {},
        content: HBoxBuilder.() -> Unit,
    ): HBox = add(dev.korafx.dsl.hbox(spacing, init, content))

    fun stackPane(
        init: StackPane.() -> Unit = {},
        content: StackPaneBuilder.() -> Unit,
    ): StackPane = add(dev.korafx.dsl.stackPane(init, content))

    fun flowPane(
        hgap: Double = 0.0,
        vgap: Double = 0.0,
        init: FlowPane.() -> Unit = {},
        content: FlowPaneBuilder.() -> Unit,
    ): FlowPane = add(dev.korafx.dsl.flowPane(hgap, vgap, init, content))

    fun tilePane(
        hgap: Double = 0.0,
        vgap: Double = 0.0,
        init: TilePane.() -> Unit = {},
        content: TilePaneBuilder.() -> Unit,
    ): TilePane = add(dev.korafx.dsl.tilePane(hgap, vgap, init, content))

    fun anchorPane(
        init: AnchorPane.() -> Unit = {},
        content: AnchorPaneBuilder.() -> Unit,
    ): AnchorPane = add(dev.korafx.dsl.anchorPane(init, content))

    fun gridPane(
        hgap: Double = 0.0,
        vgap: Double = 0.0,
        init: GridPane.() -> Unit = {},
        content: GridPaneBuilder.() -> Unit,
    ): GridPane = add(dev.korafx.dsl.gridPane(hgap, vgap, init, content))

    fun toolbar(
        init: ToolBar.() -> Unit = {},
        content: ToolBarBuilder.() -> Unit,
    ): ToolBar = add(dev.korafx.dsl.toolbar(init, content))

    fun scrollPane(
        init: ScrollPane.() -> Unit = {},
        content: ScrollPaneBuilder.() -> Unit,
    ): ScrollPane = add(dev.korafx.dsl.scrollPane(init, content))

    fun splitPane(
        orientation: Orientation = Orientation.HORIZONTAL,
        init: SplitPane.() -> Unit = {},
        content: SplitPaneBuilder.() -> Unit,
    ): SplitPane = add(dev.korafx.dsl.splitPane(orientation, init, content))

    fun tabPane(
        init: TabPane.() -> Unit = {},
        content: TabPaneBuilder.() -> Unit,
    ): TabPane = add(dev.korafx.dsl.tabPane(init, content))

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
}

abstract class PaneBuilder<T : Pane> internal constructor(
    protected val pane: T,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        pane.children += node
    }
}
