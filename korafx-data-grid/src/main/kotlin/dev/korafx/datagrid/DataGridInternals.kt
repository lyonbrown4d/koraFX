package dev.korafx.datagrid

import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import javafx.scene.control.Button
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.MenuButton
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow

internal data class DataGridBatchAction<T>(
    val button: Button,
    val requireSelection: Boolean,
)

internal data class DataGridSnapshotAction<T>(
    val button: Button,
    val selectedOnly: Boolean,
)

internal fun <T> DataGrid<T>.createBatchAction(
    text: String,
    requireSelection: Boolean,
    init: Button.() -> Unit,
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

internal fun <T> DataGrid<T>.createSnapshotAction(
    text: String,
    selectedOnly: Boolean,
    separator: String,
    includeHeaders: Boolean,
    init: Button.() -> Unit,
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

internal fun <T> DataGrid<T>.syncColumnVisibilityMenu(
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

internal fun <T> DataGrid<T>.buildDataSnapshot(selectedOnly: Boolean): DataGridDataSnapshot<T> {
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

internal fun <T> DataGrid<T>.applyFilter() {
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

internal fun <T> DataGrid<T>.updatePlaceholder() {
    tableView.placeholder =
        if (loading) {
            loadingPlaceholder.apply {
                styleClass("data-grid-loading")
            }
        } else {
            emptyPlaceholder
        }
}

internal fun <T> DataGrid<T>.installRowFactory() {
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

internal fun <T> DataGrid<T>.refreshToolbarVisibility() {
    val visible = searchField.isVisible || toolbarNodes.any { it.isManaged }
    toolbar.isVisible = visible
    toolbar.isManaged = visible
}

internal fun <T> DataGrid<T>.updateSelectionState() {
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

internal fun <T> DataGrid<T>.refreshFooterVisibility() {
    val visible = footerLabel.isManaged || selectionSummaryLabel.isManaged
    footer.isVisible = visible
    footer.isManaged = visible
}
