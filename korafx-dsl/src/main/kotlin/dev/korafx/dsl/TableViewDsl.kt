package dev.korafx.dsl

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane

class TableViewBuilder<T> internal constructor(
    private val tableView: TableView<T>,
) {
    fun items(items: Iterable<T>) {
        tableView.items.setAll(items.toList())
    }

    fun selectionMode(mode: SelectionMode) {
        tableView.selectionModel.selectionMode = mode
    }

    fun placeholder(text: String, init: Label.() -> Unit = {}) {
        tableView.placeholder = Label(text).apply(init)
    }

    fun placeholder(node: Node) {
        tableView.placeholder = node
    }

    fun constrainedResize() {
        tableView.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
    }

    fun clearColumns() {
        tableView.columns.clear()
    }

    fun onSelect(handler: (T?) -> Unit) {
        tableView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue)
        }
    }

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        tableView.setRowFactory {
            object : TableRow<T>() {
                init {
                    setOnMouseClicked { event ->
                        if (event.button == mouseButton && event.clickCount == clickCount && item != null && !isEmpty) {
                            handler(item)
                        }
                    }
                }
            }
        }
    }

    fun <R> column(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
    ): TableColumn<T, R> =
        TableColumn<T, R>(title).apply {
            setCellValueFactory { features ->
                ReadOnlyObjectWrapper(valueOf(features.value))
            }
            init()
        }.also {
            tableView.columns += it
        }

    fun textColumn(
        title: String,
        valueOf: (T) -> Any?,
    ): TableColumn<T, String> =
        textColumn(title, valueOf, init = {})

    fun textColumn(
        title: String,
        valueOf: (T) -> Any?,
        init: TableColumn<T, String>.() -> Unit,
    ): TableColumn<T, String> =
        column(title, valueOf = { row -> valueOf(row)?.toString().orEmpty() }, init = init)

    fun <R> column(
        title: String,
        valueOf: (T) -> R,
        render: (R) -> String,
        init: TableColumn<T, R>.() -> Unit = {},
    ): TableColumn<T, R> =
        column(title, valueOf) {
            setCellFactory {
                object : TableCell<T, R>() {
                    override fun updateItem(item: R?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty || item == null) null else render(item)
                    }
                }
            }
            init()
        }

    fun <R> columnNode(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
        content: CellContentBuilder.(R) -> Unit,
    ): TableColumn<T, R> =
        column(title, valueOf) {
            setCellFactory {
                object : TableCell<T, R>() {
                    override fun updateItem(item: R?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = null
                        graphic =
                            if (empty || item == null) {
                                null
                            } else {
                                StackPane().apply {
                                    CellContentBuilder(this).content(item)
                                }
                            }
                    }
                }
            }
            init()
        }

    fun actionColumn(
        title: String = "",
        text: String,
        init: Button.() -> Unit = {},
        handler: (T) -> Unit,
    ): TableColumn<T, T> =
        columnNode(
            title = title,
            valueOf = { row -> row },
        ) { row ->
            button(text) {
                init()
                onAction {
                    handler(row)
                }
            }
        }
}
