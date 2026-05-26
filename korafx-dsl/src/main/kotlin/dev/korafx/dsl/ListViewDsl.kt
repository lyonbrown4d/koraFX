package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane

class ListViewBuilder<T> internal constructor(
    private val listView: ListView<T>,
) {
    private var textRenderer: ((T) -> String)? = null
    private var nodeRenderer: (CellContentBuilder.(T) -> Unit)? = null
    private var rowClickCount: Int = 2
    private var rowMouseButton: MouseButton = MouseButton.PRIMARY
    private var rowActionHandler: ((T) -> Unit)? = null

    fun items(items: Iterable<T>) {
        listView.items.setAll(items.toList())
    }

    fun items(vararg items: T) {
        listView.items.setAll(items.toList())
    }

    fun <R> render(textOf: (T) -> R) {
        textRenderer = { item -> textOf(item).toString() }
        nodeRenderer = null
        installCellFactory()
    }

    fun cell(content: CellContentBuilder.(T) -> Unit) {
        nodeRenderer = content
        textRenderer = null
        installCellFactory()
    }

    fun selectionMode(mode: SelectionMode) {
        listView.selectionModel.selectionMode = mode
    }

    fun placeholder(text: String, init: Label.() -> Unit = {}) {
        listView.placeholder =
            Label(text).apply(init)
    }

    fun placeholder(node: Node) {
        listView.placeholder = node
    }

    fun onSelect(handler: (T?) -> Unit) {
        listView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue)
        }
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

    private fun installCellFactory() {
        listView.setCellFactory {
            object : ListCell<T>() {
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
                    graphic = null
                }
            }
        }
    }
}
