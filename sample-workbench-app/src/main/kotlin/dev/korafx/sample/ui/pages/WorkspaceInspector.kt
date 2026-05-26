package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.TabWorkspace
import dev.korafx.components.badge
import dev.korafx.dsl.onAction
import dev.korafx.inspector.InspectorPanel
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.viewmodel.WorkbenchAction
import javafx.scene.control.Button

internal fun refreshWorkspaceInspector(
    panel: InspectorPanel,
    context: WorkbenchPageContext,
    workspace: TabWorkspace,
    activeTabId: String,
) {
    val activeTabs = workspace.workspaceTabs

    panel.clearMetadata()
    panel.clearBody()
    panel.clearActions()

    panel.setHeader("Workspace Inspector", "Active workspace state")
    panel.addMetadata(
        badge("Theme: ${context.themeManager.currentTheme().displayName}", ComponentTone.INFO),
    )
    panel.addMetadata(
        badge("Tabs: ${activeTabs.size}", ComponentTone.SUCCESS),
    )
    panel.addProperty("Active tab", workspaceTabTitle(activeTabId))
    panel.addProperty("Target", WorkspacePageState.currentTarget)
    panel.addProperty("Route", WorkbenchRoute.Workspace.path)
    panel.addProperty("Executed queries", WorkspacePageState.executedQueryCount.toString())
    panel.addProperty("Log openings", WorkspacePageState.scratchOpenCount.toString())

    panel.addSection("Runtime") {
        val tabText = activeTabs.joinToString(", ") { workspace.tabId(it) ?: "unknown" }.ifBlank { "-" }
        property("Workspace tabs", tabText)
        property("Latest query", WorkspacePageState.recentQueries.firstOrNull() ?: "No query yet.")
    }

    panel.addAction(Button("Open DataGrid").withAction(context, "data-grid"))
    panel.addAction(Button("Open Resource Explorer").withAction(context, "resource-explorer"))
    panel.addAction(Button("Run Navigation Demo").withAction(context, "navigation"))
}

private fun Button.withAction(
    context: WorkbenchPageContext,
    moduleId: String,
): Button =
    apply {
        onAction {
            context.viewModel.dispatch(WorkbenchAction.NavigateModule(moduleId))
        }
    }
