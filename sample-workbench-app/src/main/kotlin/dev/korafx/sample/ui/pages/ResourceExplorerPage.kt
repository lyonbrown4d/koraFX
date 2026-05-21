package dev.korafx.sample.ui.pages

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.resourceexplorer.resourceExplorer
import dev.korafx.sample.domain.ExplorerResource
import dev.korafx.sample.viewmodel.WorkbenchAction

fun NodeContainerBuilder.resourceExplorerPage(context: WorkbenchPageContext) {
    resourceExplorer(
        items = context.catalog.explorerResources,
        childrenOf = ExplorerResource::children,
        textOf = ExplorerResource::name,
        init = {
            prefHeight = 360.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        search(prompt = "Search repository or database...")
        breadcrumb(separator = " > ")
        secondaryText { resource ->
            if (resource.children.isEmpty()) "Leaf resource" else "${resource.children.size} children"
        }
        status { resource -> resourceStatusText(resource) }
        emptyState("No matching resources")
        rowAction { resource ->
            context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Open ${resource.name}"))
        }
    }
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
