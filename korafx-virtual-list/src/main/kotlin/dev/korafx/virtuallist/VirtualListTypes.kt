package dev.korafx.virtuallist

import javafx.collections.ListChangeListener
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Region

typealias VirtualListDataLoader<T> = (offset: Long, limit: Int) -> Collection<T>
typealias VirtualListErrorHandler = (Throwable) -> Unit

data class VirtualListLoadState(
    val isLoading: Boolean,
    val isAtEnd: Boolean,
    val itemCount: Int,
    val hasError: Boolean,
)

enum class VirtualListHeightMode {
    FIXED,
    DYNAMIC,
}

enum class VirtualSelectionMode {
    SINGLE,
    MULTIPLE,
}

class VirtualListSelectionModel<T>(
    private val listView: ListView<T>,
) {
    var mode: VirtualSelectionMode
        get() =
            when (listView.selectionModel.selectionMode) {
                SelectionMode.SINGLE -> VirtualSelectionMode.SINGLE
                else -> VirtualSelectionMode.MULTIPLE
            }
        set(value) {
            listView.selectionModel.selectionMode =
                when (value) {
                    VirtualSelectionMode.SINGLE -> SelectionMode.SINGLE
                    VirtualSelectionMode.MULTIPLE -> SelectionMode.MULTIPLE
                }
        }

    val selectedItem: T?
        get() = listView.selectionModel.selectedItem

    val selectedItems: List<T>
        get() = listView.selectionModel.selectedItems.toList()

    val selectedIndices: List<Int>
        get() = listView.selectionModel.selectedIndices.toList()

    fun clearSelection() {
        listView.selectionModel.clearSelection()
    }

    fun select(index: Int) {
        listView.selectionModel.select(index)
    }

    fun select(item: T) {
        listView.selectionModel.select(item)
    }

    fun onSelect(handler: (List<T>) -> Unit) {
        listView.selectionModel.selectedItems.addListener(
            ListChangeListener {
                handler(selectedItems)
            },
        )
    }
}

class VirtualListItemRendererScope<T>(
    val item: T,
    private val container: HBox = HBox(8.0),
) {
    internal fun root(): Node = container

    fun label(
        text: String,
        init: Label.() -> Unit = {},
    ): Label =
        Label(text).apply(init).also {
            container.children += it
        }

    fun text(value: Any?): Label = label(value?.toString().orEmpty())

    fun node(node: Node): Node =
        node.also {
            container.children += it
        }

    fun region(init: Region.() -> Unit = {}): Region =
        Region().apply(init).also {
            container.children += it
        }
}
