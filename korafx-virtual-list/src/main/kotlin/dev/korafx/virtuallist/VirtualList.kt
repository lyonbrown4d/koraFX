package dev.korafx.virtuallist

import dev.korafx.dsl.NodeContainerBuilder
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.ScrollBar
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Callback
import java.util.concurrent.Executors
import kotlin.math.max

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

class VirtualList<T>(
    private val dataLoader: VirtualListDataLoader<T>,
    private val totalCountEstimate: (() -> Int?)? = null,
    pageSize: Int = 50,
    rowHeightMode: VirtualListHeightMode = VirtualListHeightMode.FIXED,
    rowHeight: Double = 34.0,
    initialSelectionMode: VirtualSelectionMode = VirtualSelectionMode.MULTIPLE,
) : VBox() {
    private var pageSizeValue = max(1, pageSize)
    private var currentHeightMode = rowHeightMode
    private var fixedRowHeight = rowHeight
    private var loading = false
    private var reachedEnd = false
    private var requestEpoch = 0
    private var lastError: Throwable? = null
    private val executor = Executors.newSingleThreadExecutor {
        Thread(it, "korafx-virtual-list-loader").apply {
            isDaemon = true
        }
    }
    private val errorHandlers = mutableListOf<VirtualListErrorHandler>()
    private var itemRenderer: (T) -> Node = { item -> Label(item.toString()) }
    private var loadingPlaceholder: Node = Label("Loading...")
    private var emptyPlaceholder: Node = Label("No items")
    private var errorPlaceholder: Node = Label("Failed to load items")
    private val loadTriggerRatio = 0.87
    private var verticalScrollBar: ScrollBar? = null
    private var verticalScrollListener: ChangeListener<Number>? = null

    val listView: ListView<T> = ListView<T>()
    val selectionModel: VirtualListSelectionModel<T> = VirtualListSelectionModel(listView)

    val items: ObservableList<T>
        get() = listView.items

    val state: VirtualListLoadState
        get() =
            VirtualListLoadState(
                isLoading = loading,
                isAtEnd = reachedEnd,
                itemCount = listView.items.size,
                hasError = lastError != null,
            )

    init {
        styleClass.add("virtual-list")
        children += listView

        listView.styleClass.add("virtual-list-list-view")
        listView.prefWidth = Double.MAX_VALUE
        listView.prefHeight = Double.MAX_VALUE
        selectionModel.mode = initialSelectionMode
        applyRowHeight()
        attachCellFactory()
        attachScrollListener()
        requestNextPage()
    }

    fun setPageSize(size: Int) {
        pageSizeValue = max(1, size)
    }

    fun setRowHeightMode(
        mode: VirtualListHeightMode,
        fixedHeight: Double = fixedRowHeight,
    ) {
        currentHeightMode = mode
        fixedRowHeight = fixedHeight
        applyRowHeight()
    }

    fun setItemRenderer(renderer: (T) -> Node) {
        itemRenderer = renderer
        listView.refresh()
    }

    fun setItemRendererFromScope(renderer: VirtualListItemRendererScope<T>.() -> Unit) {
        itemRenderer = { item ->
            VirtualListItemRendererScope(item).also(renderer).root()
        }
        listView.refresh()
    }

    fun setLoadingPlaceholder(node: Node) {
        loadingPlaceholder = node
        refreshPlaceholder()
    }

    fun setEmptyPlaceholder(node: Node) {
        emptyPlaceholder = node
        refreshPlaceholder()
    }

    fun setErrorPlaceholder(node: Node) {
        errorPlaceholder = node
        refreshPlaceholder()
    }

    fun onError(handler: VirtualListErrorHandler) {
        errorHandlers += handler
    }

    fun onSelect(handler: (List<T>) -> Unit) {
        selectionModel.onSelect(handler)
    }

    fun loadMore() {
        requestNextPage()
    }

    fun reload() {
        requestEpoch++
        reachedEnd = false
        loading = false
        lastError = null
        Platform.runLater {
            items.clear()
            refreshPlaceholder()
            requestNextPage()
        }
    }

    private fun attachCellFactory() {
        listView.setCellFactory(
            Callback {
                object : ListCell<T>() {
                    override fun updateItem(item: T?, empty: Boolean) {
                        super.updateItem(item, empty)

                        if (empty || item == null) {
                            text = null
                            graphic = null
                            return
                        }

                        text = null
                        graphic = itemRenderer(item)
                    }
                }
            },
        )
    }

    private fun applyRowHeight() {
        listView.fixedCellSize =
            when (currentHeightMode) {
                VirtualListHeightMode.FIXED -> fixedRowHeight
                VirtualListHeightMode.DYNAMIC -> -1.0
            }
    }

    private fun attachScrollListener() {
        listView.skinProperty().addListener { _, _, skin ->
            if (skin != null) {
                maybeAttachScrollListener()
            }
        }
        maybeAttachScrollListener()
    }

    private fun maybeAttachScrollListener() {
        val bar = findVerticalScrollBar() ?: return
        if (verticalScrollBar == bar) {
            return
        }

        verticalScrollBar?.also { existing ->
            verticalScrollListener?.let { existing.valueProperty().removeListener(it) }
        }

        val listener =
            ChangeListener<Number> { _, _, value ->
                if (value.toDouble() >= loadTriggerRatio) {
                    requestNextPage()
                }
            }

        bar.valueProperty().addListener(listener)
        verticalScrollBar = bar
        verticalScrollListener = listener
        if (bar.value >= loadTriggerRatio) {
            requestNextPage()
        }
    }

    private fun findVerticalScrollBar(): ScrollBar? {
        return listView.lookupAll(".scroll-bar")
            .firstOrNull {
                it is ScrollBar && it.orientation == Orientation.VERTICAL
            } as? ScrollBar
    }

    private fun requestNextPage() {
        if (!canLoadMore()) {
            return
        }

        val offset = items.size
        val epoch = requestEpoch
        loading = true
        lastError = null
        refreshPlaceholder()

        executor.submit {
            try {
                val batch = dataLoader(offset.toLong(), pageSizeValue).toList()
                Platform.runLater {
                    if (!isCurrentEpoch(epoch)) {
                        return@runLater
                    }

                    items += batch
                    reachedEnd = shouldReachEnd(offset.toLong(), batch.size)
                    loading = false
                    refreshPlaceholder()
                }
            } catch (error: Throwable) {
                Platform.runLater {
                    if (!isCurrentEpoch(epoch)) {
                        return@runLater
                    }

                    loading = false
                    lastError = error
                    errorHandlers.forEach { it(error) }
                    refreshPlaceholder()
                }
            }
        }
    }

    private fun canLoadMore(): Boolean {
        if (loading || reachedEnd) {
            return false
        }

        val estimate = totalCountEstimate?.invoke() ?: return true
        if (estimate <= 0) {
            reachedEnd = true
            return false
        }

        if (items.size >= estimate) {
            reachedEnd = true
            return false
        }
        return true
    }

    private fun shouldReachEnd(
        offset: Long,
        loaded: Int,
    ): Boolean {
        if (loaded <= 0) {
            return true
        }

        if (loaded < pageSizeValue) {
            return true
        }

        val estimate = totalCountEstimate?.invoke() ?: return false
        return estimate <= offset + loaded
    }

    private fun isCurrentEpoch(epoch: Int): Boolean = epoch == requestEpoch

    private fun refreshPlaceholder() {
        listView.placeholder =
            when {
                lastError != null && items.isEmpty() -> errorPlaceholder
                loading && items.isEmpty() -> loadingPlaceholder
                items.isEmpty() -> emptyPlaceholder
                else -> null
            }
    }
}

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
        virtualList(
            dataLoader = dataLoader,
            totalCountEstimate = totalCountEstimate,
            pageSize = pageSize,
            heightMode = heightMode,
            rowHeight = rowHeight,
            selectionMode = selectionMode,
            init = init,
            content = content,
        ),
    )
