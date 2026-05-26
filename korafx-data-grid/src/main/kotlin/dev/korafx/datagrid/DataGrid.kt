package dev.korafx.datagrid

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import javafx.collections.ListChangeListener
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.Label
import javafx.scene.control.MenuButton
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
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

    private val emptyPlaceholder = Label("No rows")
    private val loadingPlaceholder = Label("Loading...")
    private val sourceItems = mutableListOf<T>()
    private val toolbarNodes = mutableListOf<Node>()
    private val batchActions = mutableListOf<DataGridBatchAction<T>>()
    private val snapshotActions = mutableListOf<DataGridSnapshotAction<T>>()

    private var loading = false
    private var searchTextOf: (T) -> String = { it.toString() }
    private var searchMatcher: (T, String) -> Boolean = { item, query ->
        searchTextOf(item).contains(query, ignoreCase = true)
    }
    private var dirtyPredicate: (T) -> Boolean = { false }
    private var rowActionHandler: ((T) -> Unit)? = null
    private var rowClickCount: Int = 2
    private var rowMouseButton: MouseButton = MouseButton.PRIMARY
    private var selectionSummaryFormatter: ((DataGridSelectionSummary<T>) -> String?)? = null

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
        Button(text).apply {
            styleClass("data-grid-toolbar-action")
            styleClass("data-grid-toolbar-batch-action")
            init()
            onAction {
                handler(selectedItems())
            }
        }.also { button ->
            batchActions += DataGridBatchAction(button, requireSelection)
            addToolbarNode(button)
            updateSelectionState()
        }

    fun addSnapshotAction(
        text: String,
        selectedOnly: Boolean = false,
        separator: String = "\t",
        includeHeaders: Boolean = true,
        init: Button.() -> Unit = {},
        handler: (DataGridDataSnapshot<T>) -> Unit,
    ): Button =
        Button(text).apply {
            styleClass("data-grid-toolbar-action")
            styleClass("data-grid-toolbar-snapshot-action")
            init()
            onAction {
                handler(
                    createDataSnapshot(selectedOnly).also { snapshot ->
                        snapshot.defaultSeparator = separator
                        snapshot.defaultIncludeHeaders = includeHeaders
                    },
                )
            }
        }.also { button ->
            snapshotActions += DataGridSnapshotAction(button, selectedOnly)
            addToolbarNode(button)
            updateSelectionState()
        }

    fun addColumnVisibilityMenu(
        text: String = "Columns",
        includeColumn: (TableColumn<T, *>) -> Boolean = { true },
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

    private fun syncColumnVisibilityMenu(
        menu: MenuButton,
        includeColumn: (TableColumn<T, *>) -> Boolean,
    ) {
        menu.items.setAll(
            tableView.columns
                .filter(includeColumn)
                .map { column ->
                    CheckMenuItem(column.text.ifBlank { "Column" }).apply {
                        styleClass += "data-grid-column-visibility-item"
                        isSelected = column.isVisible
                        selectedProperty().addListener { _, _, selected ->
                            column.isVisible = selected
                        }
                        column.visibleProperty().addListener { _, _, visible ->
                            if (isSelected != visible) {
                                isSelected = visible
                            }
                        }
                    }
                },
        )
    }

    fun selectedItems(): List<T> =
        tableView.selectionModel.selectedItems.toList()

    fun clearSelection() {
        tableView.selectionModel.clearSelection()
    }

    fun createDataSnapshot(selectedOnly: Boolean = false): DataGridDataSnapshot<T> {
        val columns = tableView.columns.filter { it.isVisible }
        val rows =
            if (selectedOnly) {
                selectedItems()
            } else {
                tableView.items.toList()
            }
        return DataGridDataSnapshot(
            sourceRows = rows,
            headers = columns.map { it.text },
            rows = rows.map { row ->
                columns.map { column ->
                    column.getCellObservableValue(row)?.value?.toString().orEmpty()
                }
            },
        )
    }

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

    private fun applyFilter() {
        if (loading) {
            tableView.items.clear()
            updatePlaceholder()
            return
        }

        val query = searchField.text.orEmpty().trim()
        val visibleItems =
            if (query.isBlank()) {
                sourceItems
            } else {
                sourceItems.filter { item -> searchMatcher(item, query) }
            }
        tableView.items.setAll(visibleItems)
        updatePlaceholder()
        updateSelectionState()
    }

    private fun updatePlaceholder() {
        tableView.placeholder =
            if (loading) {
                loadingPlaceholder.apply {
                    styleClass("data-grid-loading")
                }
            } else {
                emptyPlaceholder
            }
    }

    private fun installRowFactory() {
        tableView.setRowFactory {
            object : TableRow<T>() {
                init {
                    setOnMouseClicked { event ->
                        val rowItem = item
                        if (
                            event.button == rowMouseButton &&
                            event.clickCount == rowClickCount &&
                            rowItem != null &&
                            !isEmpty
                        ) {
                            rowActionHandler?.invoke(rowItem)
                        }
                    }
                }

                override fun updateItem(
                    item: T?,
                    empty: Boolean,
                ) {
                    super.updateItem(item, empty)
                    updateRowStyle(this, item, empty)
                }
            }
        }
    }

    private fun refreshToolbarVisibility() {
        val visible = searchField.isVisible || toolbarNodes.any { it.isManaged }
        toolbar.isVisible = visible
        toolbar.isManaged = visible
    }

    private fun updateSelectionState() {
        val selected = selectedItems()
        val summaryText =
            selectionSummaryFormatter?.invoke(
                DataGridSelectionSummary(
                    selectedItems = selected,
                    visibleRowCount = tableView.items.size,
                    totalRowCount = sourceItems.size,
                    loading = loading,
                ),
            )
        val summaryVisible = !summaryText.isNullOrBlank()
        selectionSummaryLabel.text = summaryText.orEmpty()
        selectionSummaryLabel.isVisible = summaryVisible
        selectionSummaryLabel.isManaged = summaryVisible

        batchActions.forEach { action ->
            action.button.isDisable = action.requireSelection && selected.isEmpty()
        }
        snapshotActions.forEach { action ->
            action.button.isDisable = action.selectedOnly && selected.isEmpty()
        }
        refreshFooterVisibility()
    }

    private fun refreshFooterVisibility() {
        val visible = footerLabel.isManaged || selectionSummaryLabel.isManaged
        footer.isVisible = visible
        footer.isManaged = visible
    }
}

data class DataGridSelectionSummary<T>(
    val selectedItems: List<T>,
    val visibleRowCount: Int,
    val totalRowCount: Int,
    val loading: Boolean,
) {
    val selectedCount: Int = selectedItems.size
}

data class DataGridDataSnapshot<T>(
    val sourceRows: List<T>,
    val headers: List<String>,
    val rows: List<List<String>>,
) {
    internal var defaultSeparator: String = "\t"
    internal var defaultIncludeHeaders: Boolean = true

    fun toDelimitedText(
        separator: String = defaultSeparator,
        includeHeaders: Boolean = defaultIncludeHeaders,
    ): String {
        val lines = mutableListOf<List<String>>()
        if (includeHeaders) {
            lines += headers
        }
        lines += rows
        return lines.joinToString("\n") { cells ->
            cells.joinToString(separator) { cell ->
                cell.sanitizeDelimitedCell(separator)
            }
        }
    }

    private fun String.sanitizeDelimitedCell(separator: String): String =
        replace("\r", " ")
            .replace("\n", " ")
            .replace(separator, " ")
}

private data class DataGridBatchAction<T>(
    val button: Button,
    val requireSelection: Boolean,
)

private data class DataGridSnapshotAction<T>(
    val button: Button,
    val selectedOnly: Boolean,
)

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
