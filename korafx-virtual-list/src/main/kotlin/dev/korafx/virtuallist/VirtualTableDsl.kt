package dev.korafx.virtuallist

import dev.korafx.dsl.NodeContainerBuilder
import javafx.scene.Node
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView

class VirtualTableBuilder<T> internal constructor(
    private val table: VirtualTable<T>,
) {
    fun pageSize(size: Int) {
        table.setPageSize(size)
    }

    fun loadingPlaceholder(node: Node) {
        table.setLoadingPlaceholder(node)
    }

    fun emptyPlaceholder(node: Node) {
        table.setEmptyPlaceholder(node)
    }

    fun errorPlaceholder(node: Node) {
        table.setErrorPlaceholder(node)
    }

    fun selectionMode(mode: VirtualSelectionMode) {
        table.selectionModel.mode = mode
    }

    fun constrainedResize() {
        table.tableView.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
    }

    fun clearColumns() {
        table.clearColumns()
    }

    fun <R> column(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
    ): TableColumn<T, R> = table.column(title, valueOf, init)

    fun textColumn(
        title: String,
        valueOf: (T) -> Any?,
        init: TableColumn<T, String>.() -> Unit = {},
    ): TableColumn<T, String> = table.textColumn(title, valueOf, init)

    fun <R> columnText(
        title: String,
        valueOf: (T) -> R,
        render: (R) -> String,
        init: TableColumn<T, R>.() -> Unit = {},
    ): TableColumn<T, R> = table.columnText(title, valueOf, render, init)

    fun <R> columnNode(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
        content: (R) -> Node,
    ): TableColumn<T, R> = table.columnNode(title, valueOf, init, content)

    fun onError(handler: VirtualTableErrorHandler) {
        table.onError(handler)
    }

    fun onSelect(handler: (List<T>) -> Unit) {
        table.onSelect(handler)
    }

    fun loadMore() {
        table.loadMore()
    }
}

fun <T> virtualTable(
    dataLoader: VirtualTableDataLoader<T>,
    totalCountEstimate: (() -> Int?)? = null,
    pageSize: Int = 50,
    selectionMode: VirtualSelectionMode = VirtualSelectionMode.MULTIPLE,
    init: VirtualTable<T>.() -> Unit = {},
    columns: VirtualTableBuilder<T>.() -> Unit = {},
): VirtualTable<T> =
    VirtualTable(
        dataLoader = dataLoader,
        totalCountEstimate = totalCountEstimate,
        pageSize = pageSize,
        initialSelectionMode = selectionMode,
    ).apply(init).apply {
        VirtualTableBuilder(this).columns()
    }

fun <T> NodeContainerBuilder.virtualTable(
    dataLoader: VirtualTableDataLoader<T>,
    totalCountEstimate: (() -> Int?)? = null,
    pageSize: Int = 50,
    selectionMode: VirtualSelectionMode = VirtualSelectionMode.MULTIPLE,
    init: VirtualTable<T>.() -> Unit = {},
    columns: VirtualTableBuilder<T>.() -> Unit = {},
): VirtualTable<T> =
    add(
        dev.korafx.virtuallist.virtualTable(
            dataLoader = dataLoader,
            totalCountEstimate = totalCountEstimate,
            pageSize = pageSize,
            selectionMode = selectionMode,
            init = init,
            columns = columns,
        ),
    )
