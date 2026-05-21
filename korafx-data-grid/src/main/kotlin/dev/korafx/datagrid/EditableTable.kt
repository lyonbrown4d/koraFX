package dev.korafx.datagrid

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane

fun <T> editableTable(
    items: Iterable<T> = emptyList(),
    init: TableView<T>.() -> Unit = {},
    content: EditableTableBuilder<T>.() -> Unit = {},
): TableView<T> =
    TableView<T>().apply {
        styleClass("editable-table")
        isEditable = true
        this.items.setAll(items.toList())
        init()
        EditableTableBuilder(this).content()
    }

class EditableTableBuilder<T> internal constructor(
    private val tableView: TableView<T>,
) {
    fun items(items: Iterable<T>) {
        tableView.items.setAll(items.toList())
    }

    fun selectionMode(mode: SelectionMode) {
        tableView.selectionModel.selectionMode = mode
    }

    fun placeholder(text: String, init: Label.() -> Unit = {}) {
        tableView.placeholder = Label(text).apply {
            styleClass("editable-table-placeholder")
            init()
        }
    }

    fun placeholder(node: Node) {
        node.styleClass("editable-table-placeholder")
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

    fun readOnlyTextColumn(
        title: String,
        init: TableColumn<T, String>.() -> Unit = {},
        valueOf: (T) -> Any?,
    ): TableColumn<T, String> =
        textColumn(
            title = title,
            init = init,
            valueOf = valueOf,
        )

    fun textColumn(
        title: String,
        init: TableColumn<T, String>.() -> Unit = {},
        valueOf: (T) -> Any?,
    ): TableColumn<T, String> =
        TableColumn<T, String>(title).apply {
            styleClass += "editable-table-text-column"
            setCellValueFactory { features ->
                ReadOnlyObjectWrapper(valueOf(features.value)?.toString().orEmpty())
            }
            init()
        }.also {
            tableView.columns += it
        }

    fun editableTextColumn(
        title: String,
        valueOf: (T) -> String?,
        init: TableColumn<T, String>.() -> Unit = {},
        onCommit: (row: T, value: String) -> Unit,
    ): TableColumn<T, String> =
        TableColumn<T, String>(title).apply {
            isEditable = true
            styleClass += "editable-table-column"
            styleClass += "editable-table-editable-text-column"
            setCellValueFactory { features ->
                ReadOnlyObjectWrapper(valueOf(features.value).orEmpty())
            }
            setCellFactory(TextFieldTableCell.forTableColumn())
            setOnEditCommit { event ->
                onCommit(event.rowValue, event.newValue.orEmpty())
                tableView.refresh()
            }
            init()
        }.also {
            tableView.columns += it
        }

    fun <R> columnNode(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
        content: (R) -> Node,
    ): TableColumn<T, R> =
        TableColumn<T, R>(title).apply {
            styleClass += "editable-table-node-column"
            setCellValueFactory { features ->
                ReadOnlyObjectWrapper(valueOf(features.value))
            }
            setCellFactory {
                object : TableCell<T, R>() {
                    override fun updateItem(item: R?, empty: Boolean) {
                        super.updateItem(item, empty)
                        this.text = null
                        this.graphic =
                            if (empty || item == null) {
                                null
                            } else {
                                StackPane(content(item)).apply {
                                    styleClass("editable-table-cell-node")
                                }
                            }
                    }
                }
            }
            init()
        }.also {
            tableView.columns += it
        }

    fun actionColumn(
        title: String = "",
        text: String,
        init: Button.() -> Unit = {},
        handler: (T) -> Unit,
    ): TableColumn<T, T> =
        TableColumn<T, T>(title).apply {
            styleClass += "editable-table-action-column"
            setCellValueFactory { features ->
                ReadOnlyObjectWrapper(features.value)
            }
            setCellFactory {
                object : TableCell<T, T>() {
                    private val action = Button(text).apply {
                        styleClass("editable-table-action")
                        init()
                    }

                    override fun updateItem(item: T?, empty: Boolean) {
                        super.updateItem(item, empty)
                        val rowItem = item
                        this.text = null
                        this.graphic =
                            if (empty || rowItem == null) {
                                null
                            } else {
                                action.apply {
                                    onAction {
                                        handler(rowItem)
                                    }
                                }
                            }
                    }
                }
            }
        }.also {
            tableView.columns += it
        }
}

fun <T> NodeContainerBuilder.editableTable(
    items: Iterable<T> = emptyList(),
    init: TableView<T>.() -> Unit = {},
    content: EditableTableBuilder<T>.() -> Unit = {},
): TableView<T> =
    add(
        dev.korafx.datagrid.editableTable(
            items = items,
            init = init,
            content = content,
        ),
    )
