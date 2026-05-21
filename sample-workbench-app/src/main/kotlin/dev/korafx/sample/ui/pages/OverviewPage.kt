package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.metricCard
import dev.korafx.components.section
import dev.korafx.datagrid.dataGrid
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.hbox
import dev.korafx.sample.domain.ModuleCategory
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sample.viewmodel.WorkbenchState
import javafx.scene.control.SelectionMode

fun NodeContainerBuilder.overviewPage(
    context: WorkbenchPageContext,
    state: WorkbenchState,
) {
    hbox(spacing = 12.0) {
        metricCard("Core modules", state.moduleDirectory.count { it.category == ModuleCategory.CORE }.toString(), "Framework, DSL, MVVM, navigation, theme", ComponentTone.PRIMARY)
        metricCard("Advanced components", state.moduleDirectory.count { it.category == ModuleCategory.ADVANCED_COMPONENT }.toString(), "Independent Gradle modules", ComponentTone.INFO)
        metricCard("Sample shape", "2 panes", "Module preview plus source code", ComponentTone.SUCCESS)
    }

    section(
        title = "Module directory",
        description = "The left rail is the product map: every module and advanced component owns a route, preview and source snippet.",
    ) {
        dataGrid(
            items = state.moduleDirectory,
            showSearch = true,
            searchPrompt = "Search modules...",
            init = {
                prefHeight = 330.0
                maxWidth = Double.MAX_VALUE
            },
        ) {
            selectionMode(SelectionMode.SINGLE)
            constrainedResize()
            textColumn("Module") { it.artifactName }
            textColumn("Category") { it.category.title }
            textColumn("Purpose") { it.summary }
            actionColumn(title = "Open", text = "Open") { module ->
                context.viewModel.dispatch(WorkbenchAction.NavigateModule(module.id))
            }
        }
    }
}
