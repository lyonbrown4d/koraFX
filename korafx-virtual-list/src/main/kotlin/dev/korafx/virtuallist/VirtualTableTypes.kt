package dev.korafx.virtuallist

import javafx.collections.ListChangeListener
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView

typealias VirtualTableDataLoader<T> = (offset: Long, limit: Int) -> Collection<T>
typealias VirtualTableErrorHandler = (Throwable) -> Unit

data class VirtualTableLoadState(
    val isLoading: Boolean,
    val isAtEnd: Boolean,
    val rowCount: Int,
    val hasError: Boolean,
)

class VirtualTableSelectionModel<T>(
    private val tableView: TableView<T>,
) {
    var mode: VirtualSelectionMode
        get() =
            when (tableView.selectionModel.selectionMode) {
                SelectionMode.SINGLE -> VirtualSelectionMode.SINGLE
                else -> VirtualSelectionMode.MULTIPLE
            }
        set(value) {
            tableView.selectionModel.selectionMode =
                when (value) {
                    VirtualSelectionMode.SINGLE -> SelectionMode.SINGLE
                    VirtualSelectionMode.MULTIPLE -> SelectionMode.MULTIPLE
                }
        }

    val selectedItem: T?
        get() = tableView.selectionModel.selectedItem

    val selectedItems: List<T>
        get() = tableView.selectionModel.selectedItems.toList()

    val selectedIndices: List<Int>
        get() = tableView.selectionModel.selectedIndices.toList()

    fun clearSelection() {
        tableView.selectionModel.clearSelection()
    }

    fun select(index: Int) {
        tableView.selectionModel.select(index)
    }

    fun select(item: T) {
        tableView.selectionModel.select(item)
    }

    fun onSelect(handler: (List<T>) -> Unit) {
        tableView.selectionModel.selectedItems.addListener(
            ListChangeListener {
                handler(selectedItems)
            },
        )
    }
}
