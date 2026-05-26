package dev.korafx.sample.ui.pages

import dev.korafx.components.pageHeader
import dev.korafx.components.section
import dev.korafx.components.statusBar
import dev.korafx.components.statusItem
import dev.korafx.components.workspaceLayout
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.bindContent
import dev.korafx.dsl.button
import dev.korafx.dsl.hbox
import dev.korafx.dsl.onAction
import dev.korafx.dsl.vbox
import dev.korafx.inspector.InspectorPanel
import dev.korafx.inspector.inspectorPanel
import dev.korafx.resourceexplorer.ResourceExplorer
import dev.korafx.resourceexplorer.resourceExplorer
import dev.korafx.sample.domain.ExplorerResource
import dev.korafx.sample.viewmodel.WorkbenchAction
import javafx.scene.control.Label
import kotlinx.coroutines.flow.MutableStateFlow

fun NodeContainerBuilder.resourceExplorerPage(context: WorkbenchPageContext) {
    val selected = MutableStateFlow<ExplorerResource?>(null)
    var explorerRef: ResourceExplorer<ExplorerResource>? = null
    var inspectorRef: InspectorPanel? = null

    section(
        title = "Resource Explorer",
        description = "Tree-based explorer + details inspector for resource-oriented workflows.",
    ) {
        workspaceLayout {
            navigation {
                vbox(spacing = 12.0) {
                    pageHeader(
                        title = "Repository / Schema Resources",
                        subtitle = "Select one node to inspect metadata and available actions.",
                        eyebrow = "korafx-resource-explorer",
                    )
                    explorerRef = resourceExplorer(
                        items = context.catalog.explorerResources,
                        childrenOf = ExplorerResource::children,
                        textOf = ExplorerResource::name,
                        init = {
                            prefHeight = 240.0
                            maxWidth = Double.MAX_VALUE
                        },
                    ) {
                        search(prompt = "Search repository or database...")
                        breadcrumb(separator = " > ")
                        secondaryText { resource ->
                            if (resource.children.isEmpty()) {
                                "Leaf resource"
                            } else {
                                "${resource.children.size} children"
                            }
                        }
                        status { resource ->
                            resourceStatusText(resource)
                        }
                        emptyState("No matching resources")
                        rowAction { resource ->
                            context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Open ${resource.name} from explorer"))
                        }
                        onSelect { resource ->
                            selected.value = resource
                            inspectorRef?.let {
                                refreshResourceInspector(
                                    inspector = it,
                                    context = context,
                                    selected = resource,
                                    path = selectedPathText(context.catalog.explorerResources, resource),
                                )
                            }
                        }
                        contextMenu {
                            actionItem("Open") { resource ->
                                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Open resource: ${resource.name}"))
                            }
                            separator()
                            actionItem("Copy path") { resource ->
                                context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Copy resource path: ${resource.name}"))
                            }
                            actionItem("Focus query module") { _ ->
                                context.viewModel.dispatch(WorkbenchAction.NavigateModule("data-grid"))
                            }
                        }
                    }

                    hbox(spacing = 8.0) {
                        button("Expand all") {
                            onAction {
                                explorerRef?.expandAll()
                            }
                        }
                        button("Collapse all") {
                            onAction {
                                explorerRef?.collapseAll()
                            }
                        }
                        button("Clear selection") {
                            onAction {
                                explorerRef?.clearSelection()
                            }
                        }
                        button("Focus DB") {
                            onAction {
                                val db = context.catalog.explorerResources.firstOrNull { it.name == "Database" }
                                if (db != null) {
                                    explorerRef?.selectPath(listOf(db))
                                }
                            }
                        }
                    }
                }
            }

            val detailPane = vbox(spacing = 12.0) { }
            content {
                detailPane.bindContent(context.uiScope, selected) { resource: ExplorerResource? ->
                    renderResourceDetail(context, resource, context.catalog.explorerResources)
                }
                detailPane
            }

            details {
                val panel = inspectorPanel(
                    title = "Resource Inspector",
                    subtitle = "Selection metadata and quick actions.",
                    init = {
                        prefWidth = 320.0
                    },
                ) {
                    emptyState("No resource selected.")
                }
                inspectorRef = panel
                refreshResourceInspector(
                    inspector = panel,
                    context = context,
                    selected = null,
                    path = "",
                )
                panel
            }

            status {
                val statusPane = vbox(spacing = 8.0) { }
                statusPane.bindContent(context.uiScope, selected) { resource: ExplorerResource? ->
                    statusBar {
                        statusItem(
                            if (resource == null) {
                                "No selection"
                            } else {
                                "Selected: ${resource.name}"
                            },
                        )
                        statusItem("Total roots: ${context.catalog.explorerResources.size}")
                        statusItem("Children: ${selectedPathSegmentCount(context.catalog.explorerResources, resource)}")
                    }
                }
                statusPane
            }
        }
    }

}

private fun selectedPathSegmentCount(
    resources: List<ExplorerResource>,
    resource: ExplorerResource?,
): Int {
    if (resource == null) {
        return 0
    }

    val path = selectedPathText(resources, resource)
    return path.split(" > ").count { it.isNotBlank() }
}

private fun resourceStatusText(resource: ExplorerResource): String? =
    when {
        resource.name.endsWith(".kt") -> "KT"
        resource.name.endsWith(".md") -> "DOC"
        resource.name in setOf("users", "orders") -> "TABLE"
        resource.name == "analytics" -> "VIEW"
        resource.children.isNotEmpty() -> "GROUP"
        else -> null
    }

private fun selectedPathText(
    resources: List<ExplorerResource>,
    resource: ExplorerResource?,
): String =
    if (resource == null) {
        "No selection"
    } else {
        findResourcePath(resources, resource)
            ?.joinToString(" > ") { it.name }
            ?: resource.name
    }

private fun findResourcePath(
    candidates: List<ExplorerResource>,
    target: ExplorerResource,
): List<ExplorerResource>? {
    for (current in candidates) {
        if (current == target) {
            return listOf(current)
        }

        if (current.children.isNotEmpty()) {
            val nested = findResourcePath(current.children, target)
            if (nested != null) {
                return listOf(current) + nested
            }
        }
    }
    return null
}

private fun NodeContainerBuilder.renderResourceDetail(
    context: WorkbenchPageContext,
    resource: ExplorerResource?,
    roots: List<ExplorerResource>,
) {
    val subtitle = selectedPathText(roots, resource)
    pageHeader(
        title = resource?.name ?: "No selection",
        subtitle = subtitle,
        eyebrow = "Details",
    )

    hbox(spacing = 12.0) {
        button("Open in workspace") {
            isDisable = resource == null
            onAction {
                resource?.let { selected ->
                    context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Open ${selected.name} in workspace"))
                }
            }
        }
        button("Run query") {
            isDisable = resource == null
            onAction {
                context.viewModel.dispatch(WorkbenchAction.NavigateModule("data-grid"))
            }
        }
        if (resource == null) {
            Label("Choose a resource to preview details.")
        } else {
            section("Quick facts") {
                val tag = if (resource.children.isEmpty()) "file" else "group"
                label("Type: $tag")
                label("Children: ${resource.children.size}")
                label("Status: ${resourceStatusText(resource) ?: "normal"}")
            }
        }
    }
}

private fun refreshResourceInspector(
    inspector: InspectorPanel,
    context: WorkbenchPageContext,
    selected: ExplorerResource?,
    path: String,
) {
    inspector.clearMetadata()
    inspector.clearBody()
    inspector.clearActions()

    if (selected == null) {
        inspector.showEmpty("No resource selected.")
        return
    }

    inspector.setHeader(
        "Resource Inspector",
        path,
    )
    inspector.addMetadata(dev.korafx.components.badge("Explorer", dev.korafx.components.ComponentTone.INFO))
    inspector.addMetadata(dev.korafx.components.badge("Selection", dev.korafx.components.ComponentTone.SUCCESS))
    inspector.addProperty("Name", selected.name)
    inspector.addProperty("Path", path)
    inspector.addProperty("Children", "${selected.children.size}")
    inspector.addProperty("Status", resourceStatusText(selected) ?: "normal")
    inspector.addSection("Navigation") {
        property("Route", "/components/resource-explorer")
        property("Total roots", "${context.catalog.explorerResources.size}")
    }
    inspector.addAction(
        javafx.scene.control.Button("Open in data grid").apply {
            onAction {
                context.viewModel.dispatch(WorkbenchAction.NavigateModule("data-grid"))
            }
        },
    )
}
