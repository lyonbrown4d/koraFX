package dev.korafx.sample.ui.pages

import dev.korafx.datagrid.dataGrid
import dev.korafx.datagrid.DataGrid
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.button
import dev.korafx.dsl.hbox
import dev.korafx.dsl.onAction
import dev.korafx.sample.domain.EditableModule
import dev.korafx.sample.viewmodel.WorkbenchAction
import javafx.scene.control.SelectionMode

private data class DataGridDemoRow(
    val id: String,
    var name: String,
    var owner: String,
    var status: String,
    var dirty: Boolean = false,
)

private fun buildDemoRows(modules: List<EditableModule>): List<DataGridDemoRow> =
    modules.mapIndexed { index, module ->
        DataGridDemoRow(
            id = "${module.name.lowercase()}-${index + 1}",
            name = module.name,
            owner = module.owner,
            status = module.status,
            dirty = index == 0,
        )
    }

private fun updateRowStatusLabel(row: DataGridDemoRow) {
    row.status =
        when {
            row.dirty -> "READY"
            row.name.isBlank() || row.owner.isBlank() -> "WARN"
            else -> "ACTIVE"
        }
}

fun NodeContainerBuilder.dataGridPage(context: WorkbenchPageContext) {
    val rows = buildDemoRows(context.catalog.editableModules).toMutableList()
    var currentGrid: DataGrid<DataGridDemoRow>? = null

    currentGrid =
        dataGrid(
        items = rows,
        searchPrompt = "Search module rows...",
        init = {
            prefHeight = 340.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        selectionMode(SelectionMode.MULTIPLE)
        constrainedResize()
        search(textOf = { "${it.name} ${it.owner} ${it.status}" })
        selectionSummary { summary ->
            val dirtyCount = rows.count { it.dirty }
            val total = summary.visibleRowCount
            val selectedCount = summary.selectedCount
            if (selectedCount == 0) {
                "${total} rows, ${dirtyCount} dirty"
            } else {
                "${selectedCount}/${total} selected, ${dirtyCount} dirty"
            }
        }

        toolbarNode(
            hbox(spacing = 8.0) {
                button("Filter Ready") {
                    onAction {
                        searchText("READY")
                    }
                }
                button("Clear filter") {
                    onAction {
                        searchText("")
                    }
                }
            },
        )

        toolbar {
            columnVisibility()
            snapshotAction("Copy visible") { snapshot ->
                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Copied ${snapshot.rows.size} rows"))
            }
            toolbarBatchAction("Mark Dirty") { selected ->
                selected.forEach { row ->
                    row.dirty = true
                    updateRowStatusLabel(row)
                }
                currentGrid?.tableView?.refresh()
                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Marked ${selected.size} rows dirty."))
            }
            toolbarBatchAction("Mark Clean") { selected ->
                selected.forEach { row ->
                    row.dirty = false
                    updateRowStatusLabel(row)
                }
                currentGrid?.tableView?.refresh()
                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Marked ${selected.size} rows clean."))
            }
            toolbarBatchAction("Append summary") { selected ->
                if (selected.isEmpty()) {
                    context.viewModel.dispatch(WorkbenchAction.UpdateDraft("No row selected"))
                } else {
                    val summary = selected.joinToString(", ") { row -> row.name }
                    context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Selected rows: $summary"))
                }
            }
            toolbarAction("Deselect All") {
                currentGrid?.let { grid ->
                    grid.clearSelection()
                }
            }
        }

        onSelect { row ->
            if (row != null) {
                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Grid selected: ${row.name}"))
            }
        }

        rowAction { row ->
            context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Open ${row.name} (owner=${row.owner})"))
        }

        dirtyRows { row -> row.dirty }

        editableTextColumn("Module", valueOf = DataGridDemoRow::name) { row, value ->
            val target = rows.firstOrNull { it.id == row.id } ?: row
            target.name = value
            updateRowStatusLabel(target)
            currentGrid?.tableView?.refresh()
        }
        editableTextColumn("Owner", valueOf = DataGridDemoRow::owner) { row, value ->
            val target = rows.firstOrNull { it.id == row.id } ?: row
            target.owner = value
            updateRowStatusLabel(target)
            currentGrid?.tableView?.refresh()
        }
        textColumn("Status") { it.status }
        actionColumn("Actions", "Open") { row ->
            context.viewModel.dispatch(WorkbenchAction.NavigateModule("workspace"))
            context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Open module row: ${row.name}"))
        }
        actionColumn("Actions", "Inspect") { row ->
            context.viewModel.dispatch(WorkbenchAction.NavigateModule("inspector-panel"))
            context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Inspect module row: ${row.name}"))
        }
    }
}
