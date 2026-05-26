package dev.korafx.datagrid

import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.MenuButton
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class DataGridBuilder<T> internal constructor(
    private val grid: DataGrid<T>,
) {
    private val tableBuilder = EditableTableBuilder(grid.tableView)

    fun items(items: Iterable<T>) {
        grid.setItems(items)
    }

    fun search(
        prompt: String = "Search rows...",
        visible: Boolean = true,
        textOf: ((T) -> String)? = null,
    ) {
        grid.setSearchPrompt(prompt)
        grid.setSearchVisible(visible)
        if (textOf != null) {
            grid.setSearchIndex(textOf)
        }
    }

    fun searchText(text: String) {
        grid.setSearchText(text)
    }

    fun filter(matcher: (T, String) -> Boolean) {
        grid.setSearchMatcher(matcher)
    }

    fun dirtyRows(predicate: (T) -> Boolean) {
        grid.setDirtyPredicate(predicate)
    }

    fun loading(
        loading: Boolean = true,
        text: String = "Loading...",
    ) {
        grid.setLoading(loading, text)
    }

    fun emptyState(text: String) {
        grid.setEmptyText(text)
    }

    fun footer(text: String?) {
        grid.setFooterText(text)
    }

    fun selectionSummary(
        formatter: (DataGridSelectionSummary<T>) -> String? = { summary ->
            val rowLabel = if (summary.visibleRowCount == 1) "row" else "rows"
            if (summary.selectedCount == 0) {
                "${summary.visibleRowCount} $rowLabel"
            } else {
                "${summary.selectedCount} selected of ${summary.visibleRowCount} $rowLabel"
            }
        },
    ) {
        grid.setSelectionSummary(formatter)
    }

    fun toolbar(content: DataGridToolbarBuilder<T>.() -> Unit) {
        DataGridToolbarBuilder(grid).content()
    }

    fun toolbarNode(node: Node): Node =
        grid.addToolbarNode(node)

    fun toolbarAction(
        text: String,
        init: Button.() -> Unit = {},
        handler: () -> Unit,
    ): Button =
        DataGridToolbarBuilder(grid).action(text, init, handler)

    fun toolbarBatchAction(
        text: String,
        requireSelection: Boolean = true,
        init: Button.() -> Unit = {},
        handler: (List<T>) -> Unit,
    ): Button =
        grid.addBatchAction(text, requireSelection, init, handler)

    fun toolbarSnapshotAction(
        text: String,
        selectedOnly: Boolean = false,
        separator: String = "\t",
        includeHeaders: Boolean = true,
        init: Button.() -> Unit = {},
        handler: (DataGridDataSnapshot<T>) -> Unit,
    ): Button =
        grid.addSnapshotAction(text, selectedOnly, separator, includeHeaders, init, handler)

    fun columnVisibility(
        text: String = "Columns",
        includeColumn: (TableColumn<T, *>) -> Boolean = { true },
        init: MenuButton.() -> Unit = {},
    ): MenuButton =
        grid.addColumnVisibilityMenu(text, includeColumn, init)

    fun dataSnapshot(selectedOnly: Boolean = false): DataGridDataSnapshot<T> =
        grid.createDataSnapshot(selectedOnly)

    fun copyText(
        selectedOnly: Boolean = false,
        separator: String = "\t",
        includeHeaders: Boolean = true,
    ): String =
        grid.copyText(selectedOnly, separator, includeHeaders)

    fun selectionMode(mode: SelectionMode) {
        tableBuilder.selectionMode(mode)
    }

    fun constrainedResize() {
        tableBuilder.constrainedResize()
    }

    fun clearColumns() {
        tableBuilder.clearColumns()
    }

    fun onSelect(handler: (T?) -> Unit) {
        tableBuilder.onSelect(handler)
    }

    fun clearSelection() {
        grid.clearSelection()
    }

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        grid.rowAction(clickCount, mouseButton, handler)
    }

    fun readOnlyTextColumn(
        title: String,
        init: TableColumn<T, String>.() -> Unit = {},
        valueOf: (T) -> Any?,
    ): TableColumn<T, String> =
        tableBuilder.readOnlyTextColumn(title, init, valueOf)

    fun textColumn(
        title: String,
        init: TableColumn<T, String>.() -> Unit = {},
        valueOf: (T) -> Any?,
    ): TableColumn<T, String> =
        tableBuilder.textColumn(title, init, valueOf)

    fun editableTextColumn(
        title: String,
        valueOf: (T) -> String?,
        init: TableColumn<T, String>.() -> Unit = {},
        onCommit: (row: T, value: String) -> Unit,
    ): TableColumn<T, String> =
        tableBuilder.editableTextColumn(title, valueOf, init, onCommit)

    fun <R> columnNode(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
        content: (R) -> Node,
    ): TableColumn<T, R> =
        tableBuilder.columnNode(title, valueOf, init, content)

    fun actionColumn(
        title: String = "",
        text: String,
        init: Button.() -> Unit = {},
        handler: (T) -> Unit,
    ): TableColumn<T, T> =
        tableBuilder.actionColumn(title, text, init, handler)
}

class DataGridToolbarBuilder<T> internal constructor(
    private val grid: DataGrid<T>,
) {
    fun node(node: Node): Node =
        grid.addToolbarNode(node)

    fun action(
        text: String,
        init: Button.() -> Unit = {},
        handler: () -> Unit,
    ): Button =
        Button(text).apply {
            styleClass("data-grid-toolbar-action")
            init()
            onAction {
                handler()
            }
        }.also {
            grid.addToolbarNode(it)
        }

    fun batchAction(
        text: String,
        requireSelection: Boolean = true,
        init: Button.() -> Unit = {},
        handler: (List<T>) -> Unit,
    ): Button =
        grid.addBatchAction(text, requireSelection, init, handler)

    fun snapshotAction(
        text: String,
        selectedOnly: Boolean = false,
        separator: String = "\t",
        includeHeaders: Boolean = true,
        init: Button.() -> Unit = {},
        handler: (DataGridDataSnapshot<T>) -> Unit,
    ): Button =
        grid.addSnapshotAction(text, selectedOnly, separator, includeHeaders, init, handler)

    fun columnVisibility(
        text: String = "Columns",
        includeColumn: (TableColumn<T, *>) -> Boolean = { true },
        init: MenuButton.() -> Unit = {},
    ): MenuButton =
        grid.addColumnVisibilityMenu(text, includeColumn, init)

    fun spacer(): Region =
        Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }.also {
            grid.addToolbarNode(it)
        }
}
