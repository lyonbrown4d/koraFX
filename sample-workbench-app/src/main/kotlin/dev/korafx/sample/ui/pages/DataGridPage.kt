package dev.korafx.sample.ui.pages

import dev.korafx.datagrid.dataGrid
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.sample.domain.EditableModule
import dev.korafx.sample.viewmodel.WorkbenchAction
import javafx.scene.control.SelectionMode

fun NodeContainerBuilder.dataGridPage(context: WorkbenchPageContext) {
    dataGrid(
        items = context.catalog.editableModules,
        searchPrompt = "Search module rows...",
        init = {
            prefHeight = 340.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        selectionMode(SelectionMode.MULTIPLE)
        constrainedResize()
        search(textOf = { "${it.name} ${it.owner} ${it.status}" })
        selectionSummary()
        toolbar {
            columnVisibility()
            snapshotAction("Copy visible") { snapshot ->
                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Copied ${snapshot.rows.size} rows"))
            }
        }
        editableTextColumn("Module", valueOf = EditableModule::name) { row, value ->
            row.name = value
        }
        editableTextColumn("Owner", valueOf = EditableModule::owner) { row, value ->
            row.owner = value
        }
        readOnlyTextColumn("Status") { it.status }
    }
}
