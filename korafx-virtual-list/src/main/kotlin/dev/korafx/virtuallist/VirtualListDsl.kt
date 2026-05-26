package dev.korafx.virtuallist

import dev.korafx.dsl.NodeContainerBuilder
import javafx.scene.Node

class VirtualListBuilder<T> internal constructor(
    private val list: VirtualList<T>,
) {
    fun pageSize(size: Int) {
        list.setPageSize(size)
    }

    fun loadingPlaceholder(node: Node) {
        list.setLoadingPlaceholder(node)
    }

    fun emptyPlaceholder(node: Node) {
        list.setEmptyPlaceholder(node)
    }

    fun errorPlaceholder(node: Node) {
        list.setErrorPlaceholder(node)
    }

    fun heightMode(
        mode: VirtualListHeightMode,
        fixedHeight: Double = 34.0,
    ) {
        list.setRowHeightMode(mode, fixedHeight)
    }

    fun fixedHeight(height: Double) {
        list.setRowHeightMode(VirtualListHeightMode.FIXED, height)
    }

    fun dynamicHeight() {
        list.setRowHeightMode(VirtualListHeightMode.DYNAMIC)
    }

    fun selectionMode(mode: VirtualSelectionMode) {
        list.selectionModel.mode = mode
    }

    fun item(renderer: VirtualListItemRendererScope<T>.() -> Unit) {
        list.setItemRendererFromScope(renderer)
    }

    fun itemOf(renderer: (T) -> Node) {
        list.setItemRenderer(renderer)
    }

    fun onError(handler: VirtualListErrorHandler) {
        list.onError(handler)
    }

    fun onSelect(handler: (List<T>) -> Unit) {
        list.onSelect(handler)
    }

    fun loadMore() {
        list.loadMore()
    }
}

fun <T> virtualList(
    dataLoader: VirtualListDataLoader<T>,
    totalCountEstimate: (() -> Int?)? = null,
    pageSize: Int = 50,
    heightMode: VirtualListHeightMode = VirtualListHeightMode.FIXED,
    rowHeight: Double = 34.0,
    selectionMode: VirtualSelectionMode = VirtualSelectionMode.MULTIPLE,
    init: VirtualList<T>.() -> Unit = {},
    content: VirtualListBuilder<T>.() -> Unit = {},
): VirtualList<T> =
    VirtualList(
        dataLoader = dataLoader,
        totalCountEstimate = totalCountEstimate,
        pageSize = pageSize,
        rowHeightMode = heightMode,
        rowHeight = rowHeight,
        initialSelectionMode = selectionMode,
    ).apply(init).apply {
        VirtualListBuilder(this).content()
    }

fun <T> NodeContainerBuilder.virtualList(
    dataLoader: VirtualListDataLoader<T>,
    totalCountEstimate: (() -> Int?)? = null,
    pageSize: Int = 50,
    heightMode: VirtualListHeightMode = VirtualListHeightMode.FIXED,
    rowHeight: Double = 34.0,
    selectionMode: VirtualSelectionMode = VirtualSelectionMode.MULTIPLE,
    init: VirtualList<T>.() -> Unit = {},
    content: VirtualListBuilder<T>.() -> Unit = {},
): VirtualList<T> =
    add(
        VirtualList(
            dataLoader = dataLoader,
            totalCountEstimate = totalCountEstimate,
            pageSize = pageSize,
            rowHeightMode = heightMode,
            rowHeight = rowHeight,
            initialSelectionMode = selectionMode,
        ).apply(init).apply {
            VirtualListBuilder(this).content()
        },
    )
