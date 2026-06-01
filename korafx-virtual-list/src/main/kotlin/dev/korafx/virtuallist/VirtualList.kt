package dev.korafx.virtuallist

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.ScrollBar
import javafx.scene.control.Skin
import javafx.scene.layout.VBox
import javafx.util.Callback
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

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
    private var skinListener: ChangeListener<Skin<*>?>? = null
    private var disposed = false

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
        if (disposed) {
            return
        }

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
        val listener = ChangeListener<Skin<*>?> { _, _, skin ->
            if (skin != null) {
                maybeAttachScrollListener()
            }
        }
        skinListener = listener
        listView.skinProperty().addListener(listener)
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
        if (disposed) {
            return
        }

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
                    if (disposed || !isCurrentEpoch(epoch)) {
                        return@runLater
                    }

                    items += batch
                    reachedEnd = shouldReachEnd(offset.toLong(), batch.size)
                    loading = false
                    refreshPlaceholder()
                }
            } catch (error: Throwable) {
                Platform.runLater {
                    if (disposed || !isCurrentEpoch(epoch)) {
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
        if (disposed) {
            return
        }

        listView.placeholder =
            when {
                lastError != null && items.isEmpty() -> errorPlaceholder
                loading && items.isEmpty() -> loadingPlaceholder
                items.isEmpty() -> emptyPlaceholder
                else -> null
            }
    }

    fun dispose() {
        if (disposed) {
            return
        }

        disposed = true
        loading = false
        requestEpoch++

        skinListener?.let { listener ->
            listView.skinProperty().removeListener(listener)
        }
        skinListener = null

        verticalScrollBar?.let { bar ->
            verticalScrollListener?.let { bar.valueProperty().removeListener(it) }
        }
        verticalScrollBar = null
        verticalScrollListener = null

        executor.shutdownNow()
        if (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
            executor.shutdownNow()
        }

        lastError = null
        items.clear()
        listView.placeholder = null
    }
}
