package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.inspector.inspectorPanel
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.viewmodel.WorkbenchAction

fun NodeContainerBuilder.inspectorPage(context: WorkbenchPageContext) {
    inspectorPanel(
        title = "Module",
        subtitle = "Selected component metadata",
        init = {
            prefHeight = 340.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        badge("Advanced", ComponentTone.INFO)
        property("Artifact", "korafx-inspector-panel")
        property("Route", WorkbenchRoute.InspectorPanel.path)
        section("State") {
            property("Selection", "Resource / row / graph node")
            property("Use case", "Database tools and Git workbenches")
        }
        actions {
            action("Inspect") {
                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Inspect current module"))
            }
        }
    }
}
