package dev.korafx.datagrid

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import javafx.collections.ListChangeListener
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.MenuButton
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class DataGrid<T> internal constructor(
    items: Iterable<T>,
    showSearch: Boolean,
    searchPrompt: String,
) : VBox(8.0) {
    val toolbar: HBox = HBox(8.0)
    val searchField: TextField = TextField()
    val tableView: TableView<T> = editableTable()
    val footer: HBox = HBox(8.0)
    val footerLabel: Label = Label()
    val selectionSummaryLabel: Label = Label()

    internal val emptyPlaceholder = Label("No rows")
    internal val loadingPlaceholder = Label("Loading...")
    internal val sourceItems = mutableListOf<T>()
    internal val toolbarNodes = mutableListOf<Node>()
    internal val batchActions = mutableListOf<DataGridBatchAction<T>>()
    internal val snapshotActions = mutableListOf<DataGridSnapshotAction<T>>()

    internal var loading = false
    internal var searchTextOf: (T) -> String = { it.toString() }
    internal var searchMatcher: (T, String) -> Boolean = { item, query ->
        searchTextOf(item).contains(query, ignoreCase = true)
    }
    internal var dirtyPredicate: (T) -> Boolean = { false }
    internal var rowActionHandler: ((T) -> Unit)? = null
    internal var rowClickCount: Int = 2
    internal var rowMouseButton: MouseButton = MouseButton.PRIMARY
    internal var selectionSummaryFormatter: ((DataGridSelectionSummary<T>) -> String?)? = null

    init {
        styleClass("data-grid")
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE

        toolbar.apply {
            styleClass("data-grid-toolbar")
            maxWidth = Double.MAX_VALUE
        }

        searchField.apply {
            styleClass("data-grid-search")
            promptText = searchPrompt
            maxWidth = Double.MAX_VALUE
            HBox.setHgrow(this, Priority.ALWAYS)
            textProperty().addListener { _, _, _ ->
                applyFilter()
            }
        }
        toolbar.children += searchField
        setSearchVisible(showSearch)

        tableView.apply {
            styleClass("data-grid-table")
            placeholder = emptyPlaceholder.apply {
                styleClass("data-grid-empty")
            }
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        footer.apply {
            styleClass("data-grid-footer")
            isVisible = false
            isManaged = false
            children += footerLabel.apply {
                styleClass("data-grid-footer-label")
                isVisible = false
                isManaged = false
            }
            children += selectionSummaryLabel.apply {
                styleClass("data-grid-selection-summary")
                isVisible = false
                isManaged = false
            }
        }

        children += toolbar
        children += tableView
        children += footer

        installRowFactory()
        tableView.selectionModel.selectedItems.addListener(
            ListChangeListener {
                updateSelectionState()
            },
        )
        setItems(items)
    }

    fun setItems(items: Iterable<T>) {
        sourceItems.clear()
        sourceItems += items
        applyFilter()
        updateSelectionState()
    }

    fun setSearchVisible(visible: Boolean) {
        searchField.isVisible = visible
        searchField.isManaged = visible
        refreshToolbarVisibility()
    }

    fun setSearchPrompt(prompt: String) {
        searchField.promptText = prompt
    }

    fun setSearchText(text: String) {
        searchField.text = text
    }

    fun setSearchIndex(textOf: (T) -> String) {
        searchTextOf = textOf
        applyFilter()
    }

    fun setSearchMatcher(matcher: (T, String) -> Boolean) {
        searchMatcher = matcher
        applyFilter()
    }

    fun setDirtyPredicate(predicate: (T) -> Boolean) {
        dirtyPredicate = predicate
        tableView.refresh()
    }

    fun isDirty(item: T): Boolean =
        dirtyPredicate(item)

    fun setLoading(
        loading: Boolean,
        text: String = "Loading...",
    ) {
        this.loading = loading
        loadingPlaceholder.text = text
        if (loading) {
            tableView.items.clear()
        } else {
            applyFilter()
        }
        updatePlaceholder()
        updateSelectionState()
    }

    fun setEmptyText(text: String) {
        emptyPlaceholder.text = text
        updatePlaceholder()
    }

    fun setFooterText(text: String?) {
        footerLabel.text = text.orEmpty()
        val visible = !text.isNullOrBlank()
        footer.isVisible = visible
        footerLabel.isVisible = visible
        footerLabel.isManaged = visible
        refreshFooterVisibility()
    }

    fun setSelectionSummary(
        formatter: ((DataGridSelectionSummary<T>) -> String?)?,
    ) {
        selectionSummaryFormatter = formatter
        updateSelectionState()
    }

    fun addToolbarNode(node: Node): Node =
        node.apply {
            styleClass("data-grid-toolbar-item")
            toolbarNodes += this
            toolbar.children += this
            refreshToolbarVisibility()
        }

    fun addBatchAction(
        text: String,
        requireSelection: Boolean = true,
        init: Button.() -> Unit = {},
        handler: (List<T>) -> Unit,
    ): Button =
        createBatchAction(text, requireSelection, init, handler)

    fun addSnapshotAction(
        text: String,
        selectedOnly: Boolean = false,
        separator: String = "\t",
        includeHeaders: Boolean = true,
        init: Button.() -> Unit = {},
        handler: (DataGridDataSnapshot<T>) -> Unit,
    ): Button =
        createSnapshotAction(text, selectedOnly, separator, includeHeaders, init, handler)

    fun addColumnVisibilityMenu(
        text: String = "Columns",
        includeColumn: (javafx.scene.control.TableColumn<T, *>) -> Boolean = { true },
        init: MenuButton.() -> Unit = {},
    ): MenuButton =
        MenuButton(text).apply {
            styleClass("data-grid-toolbar-action")
            styleClass("data-grid-column-visibility")
            syncColumnVisibilityMenu(this, includeColumn)
            setOnShowing {
                syncColumnVisibilityMenu(this, includeColumn)
            }
            init()
        }.also {
            addToolbarNode(it)
        }

    fun selectedItems(): List<T> =
        tableView.selectionModel.selectedItems.toList()

    fun clearSelection() {
        tableView.selectionModel.clearSelection()
    }

    fun createDataSnapshot(selectedOnly: Boolean = false): DataGridDataSnapshot<T> =
        buildDataSnapshot(selectedOnly)

    fun copyText(
        selectedOnly: Boolean = false,
        separator: String = "\t",
        includeHeaders: Boolean = true,
    ): String =
        createDataSnapshot(selectedOnly).toDelimitedText(separator, includeHeaders)

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        rowClickCount = clickCount
        rowMouseButton = mouseButton
        rowActionHandler = handler
    }

    internal fun updateRowStyle(
        row: TableRow<T>,
        item: T?,
        empty: Boolean,
    ) {
        val dirtyStyle = "data-grid-row-dirty"
        row.styleClass.remove(dirtyStyle)
        if (!empty && item != null && dirtyPredicate(item)) {
            row.styleClass += dirtyStyle
        }
    }
}

fun <T> dataGrid(
    items: Iterable<T> = emptyList(),
    showSearch: Boolean = true,
    searchPrompt: String = "Search rows...",
    init: DataGrid<T>.() -> Unit = {},
    content: DataGridBuilder<T>.() -> Unit = {},
): DataGrid<T> =
    DataGrid(
        items = items,
        showSearch = showSearch,
        searchPrompt = searchPrompt,
    ).apply(init).apply {
        DataGridBuilder(this).content()
    }

fun <T> NodeContainerBuilder.dataGrid(
    items: Iterable<T> = emptyList(),
    showSearch: Boolean = true,
    searchPrompt: String = "Search rows...",
    init: DataGrid<T>.() -> Unit = {},
    content: DataGridBuilder<T>.() -> Unit = {},
): DataGrid<T> =
    add(
        dev.korafx.datagrid.dataGrid(
            items = items,
            showSearch = showSearch,
            searchPrompt = searchPrompt,
            init = init,
            content = content,
        ),
    )
