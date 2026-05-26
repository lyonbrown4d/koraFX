package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.Accordion
import javafx.scene.control.TitledPane
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane

class AccordionBuilder internal constructor(
    private val accordion: Accordion,
) {
    fun pane(
        title: String,
        expanded: Boolean = true,
        init: TitledPane.() -> Unit = {},
        content: () -> Node,
    ): TitledPane =
        titledPane(title, expanded, init, content).also {
            accordion.panes += it
        }
}

class TreeViewBuilder<T> internal constructor(
    private val treeView: TreeView<T>,
) {
    private var textRenderer: ((T) -> String)? = null
    private var nodeRenderer: (CellContentBuilder.(T) -> Unit)? = null
    private var rowClickCount: Int = 2
    private var rowMouseButton: MouseButton = MouseButton.PRIMARY
    private var rowActionHandler: ((T) -> Unit)? = null

    fun root(
        value: T,
        expanded: Boolean = true,
        content: TreeItemBuilder<T>.() -> Unit = {},
    ): TreeItem<T> =
        treeItem(value, expanded, content).also {
            treeView.root = it
        }

    fun showRoot(show: Boolean) {
        treeView.isShowRoot = show
    }

    fun render(textOf: (T) -> String) {
        textRenderer = textOf
        nodeRenderer = null
        installCellFactory()
    }

    fun cell(content: CellContentBuilder.(T) -> Unit) {
        nodeRenderer = content
        textRenderer = null
        installCellFactory()
    }

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        rowClickCount = clickCount
        rowMouseButton = mouseButton
        rowActionHandler = handler
        installCellFactory()
    }

    fun onSelect(handler: (T?) -> Unit) {
        treeView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue?.value)
        }
    }

    private fun installCellFactory() {
        treeView.setCellFactory {
            object : TreeCell<T>() {
                init {
                    setOnMouseClicked { event ->
                        if (
                            event.button == rowMouseButton &&
                            event.clickCount == rowClickCount &&
                            item != null &&
                            !isEmpty
                        ) {
                            rowActionHandler?.invoke(item)
                        }
                    }
                }

                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (empty || item == null) {
                        text = null
                        graphic = null
                        return
                    }

                    val content = nodeRenderer
                    if (content != null) {
                        text = null
                        graphic = StackPane().apply {
                            CellContentBuilder(this).content(item)
                        }
                        return
                    }

                    text = textRenderer?.invoke(item) ?: item.toString()
                    graphic = treeItem?.graphic
                }
            }
        }
    }
}

class TreeItemBuilder<T> internal constructor(
    private val treeItem: TreeItem<T>,
) {
    fun item(
        value: T,
        expanded: Boolean = true,
        content: TreeItemBuilder<T>.() -> Unit = {},
    ): TreeItem<T> =
        treeItem(value, expanded, content).also {
            treeItem.children += it
        }
}
