package dev.korafx.virtuallist

import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollBar
import javafx.scene.control.Skin
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

class VirtualTable<T>(
    private val dataLoader: VirtualTableDataLoader<T>,
    private val totalCountEstimate: (() -> Int?)? = null,
    pageSize: Int = 50,
    initialSelectionMode: VirtualSelectionMode = VirtualSelectionMode.MULTIPLE,
) : VBox() {
    private var pageSizeValue = max(1, pageSize)
    private var loading = false
    private var reachedEnd = false
    private var requestEpoch = 0
    private var lastError: Throwable? = null
    private val executor = Executors.newSingleThreadExecutor {
        Thread(it, "korafx-virtual-table-loader").apply {
            isDaemon = true
        }
    }
    private val errorHandlers = mutableListOf<VirtualTableErrorHandler>()
    private var loadingPlaceholder: Node = Label("Loading...")
    private var emptyPlaceholder: Node = Label("No rows")
    private var errorPlaceholder: Node = Label("Failed to load rows")
    private val loadTriggerRatio = 0.87
    private var verticalScrollBar: ScrollBar? = null
    private var verticalScrollListener: ChangeListener<Number>? = null
    private var skinListener: ChangeListener<Skin<*>?>? = null
    private var disposed = false

    val tableView: TableView<T> = TableView<T>()
    val selectionModel: VirtualTableSelectionModel<T> = VirtualTableSelectionModel(tableView)

    val items: ObservableList<T>
        get() = tableView.items

    val state: VirtualTableLoadState
        get() =
            VirtualTableLoadState(
                isLoading = loading,
                isAtEnd = reachedEnd,
                rowCount = tableView.items.size,
                hasError = lastError != null,
            )

    init {
        styleClass += "virtual-table"
        children += tableView

        tableView.styleClass += "virtual-table-table-view"
        tableView.prefWidth = Double.MAX_VALUE
        tableView.prefHeight = Double.MAX_VALUE
        selectionModel.mode = initialSelectionMode
        attachScrollListener()
        requestNextPage()
    }

    fun setPageSize(size: Int) { pageSizeValue = max(1, size) }

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

    fun onError(handler: VirtualTableErrorHandler) { errorHandlers += handler }

    fun onSelect(handler: (List<T>) -> Unit) { selectionModel.onSelect(handler) }

    fun loadMore() { requestNextPage() }

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

    fun clearColumns() { tableView.columns.clear() }

    fun <R> column(
        title: String,
        valueOf: (T) -> R,
        init: TableColumn<T, R>.() -> Unit = {},
    ): TableColumn<T, R> =
        TableColumn<T, R>(title).apply {
            styleClass += "virtual-table-column"
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
        init: TableColumn<T, String>.() -> Unit = {},
    ): TableColumn<T, String> =
        column(title, valueOf = { row -> valueOf(row)?.toString().orEmpty() }, init = init)

    fun <R> columnText(
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
        content: (R) -> Node,
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
                                StackPane(content(item)).apply {
                                    styleClass += "virtual-table-cell-node"
                                }
                            }
                    }
                }
            }
            init()
        }

    private fun attachScrollListener() {
        val listener = ChangeListener<Skin<*>?> { _, _, skin ->
            if (skin != null) {
                maybeAttachScrollListener()
            }
        }
        skinListener = listener
        tableView.skinProperty().addListener(listener)
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
        return tableView.lookupAll(".scroll-bar")
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

        tableView.placeholder =
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
            tableView.skinProperty().removeListener(listener)
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
        tableView.placeholder = null
    }
}
