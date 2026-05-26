package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.appToolbar
import dev.korafx.components.badge
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.button
import dev.korafx.dsl.onAction
import dev.korafx.inspector.InspectorPanel
import dev.korafx.inspector.inspectorPanel
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.viewmodel.WorkbenchAction
import javafx.scene.control.Button

private enum class InspectorDemoMode {
    Module,
    Resource,
    Workspace,
}

fun NodeContainerBuilder.inspectorPage(context: WorkbenchPageContext) {
    var currentMode = InspectorDemoMode.Module
    var panel: InspectorPanel? = null

    appToolbar(
        title = "Inspector Panel",
        subtitle = "Live examples of metadata/sections/actions in different modes",
    ) {
        button("Module") {
            onAction {
                currentMode = InspectorDemoMode.Module
                panel?.let { renderInspectorPanel(context, it, currentMode) }
            }
        }
        button("Resource") {
            onAction {
                currentMode = InspectorDemoMode.Resource
                panel?.let { renderInspectorPanel(context, it, currentMode) }
            }
        }
        button("Workspace") {
            onAction {
                currentMode = InspectorDemoMode.Workspace
                panel?.let { renderInspectorPanel(context, it, currentMode) }
            }
        }
    }

    val inspector = inspectorPanel(
        title = "Inspector",
        subtitle = "Slot-driven metadata surface",
        init = {
            prefHeight = 360.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        emptyState("Waiting for mode...")
    }
    panel = inspector
    renderInspectorPanel(context, inspector, currentMode)
}

private fun renderInspectorPanel(
    context: WorkbenchPageContext,
    panel: InspectorPanel,
    mode: InspectorDemoMode,
) {
    panel.clearMetadata()
    panel.clearBody()
    panel.clearActions()
    panel.setHeader(
        "Inspector",
        when (mode) {
            InspectorDemoMode.Module -> "Module metadata mode"
            InspectorDemoMode.Resource -> "Resource metadata mode"
            InspectorDemoMode.Workspace -> "Workspace metadata mode"
        },
    )

    when (mode) {
        InspectorDemoMode.Module -> {
            panel.addMetadata(badge("Advanced", ComponentTone.INFO))
            panel.addMetadata(badge("Read-only", ComponentTone.NEUTRAL))
            panel.addProperty("Artifact", "korafx-inspector-panel")
            panel.addProperty("Route", WorkbenchRoute.InspectorPanel.path)
            panel.addProperty("Mode", "module")
            panel.addSection("State") {
                property("Selection", "Rows / resources / tabs")
                property("Use case", "Git modules and database entities")
            }
        }
        InspectorDemoMode.Resource -> {
            panel.addMetadata(badge("Resource", ComponentTone.SUCCESS))
            panel.addProperty("Artifact", "korafx-resource-explorer")
            panel.addProperty("Mode", "resource")
            panel.addSection("Capabilities") {
                property("Traversal", "Path aware tree nodes")
                property("Status", "selected / expanded / filtered")
            }
            panel.addSection("Route hints") {
                property("Open", "resource-explorer")
                property("Integration", "inspectorPanel")
            }
        }
        InspectorDemoMode.Workspace -> {
            panel.addMetadata(badge("Workspace", ComponentTone.WARNING))
            panel.addProperty("Artifact", "korafx-components")
            panel.addProperty("Mode", "workspace")
            panel.addSection("Slots") {
                property("Top bar", "toolbar/actions")
                property("Navigation", "explorer or sidebars")
                property("Details", "inspector binding")
            }
            panel.addSection("Examples") {
                property("Default route", "/components/workspace")
                property("Quick action", "Navigate tab actions")
            }
        }
    }

    panel.addAction(
        Button("Open source route").apply {
            onAction {
                context.viewModel.dispatch(WorkbenchAction.NavigateModule("source-editor"))
            }
        },
    )
    panel.addAction(
        Button("Show feedback").apply {
            onAction {
                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Inspector action triggered: ${mode.name.lowercase()}"))
            }
        },
    )
    panel.addAction(
        Button("Reset").apply {
            onAction {
                renderInspectorPanel(context, panel, InspectorDemoMode.Module)
            }
        },
    )
}
