package dev.korafx.sample.ui

import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.components.chip
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.hbox
import dev.korafx.navigation.RouteTransition
import dev.korafx.sample.domain.ModuleShowcase
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.ui.pages.WorkbenchPageContext
import dev.korafx.sample.ui.pages.commandPalettePage
import dev.korafx.sample.ui.pages.componentsPage
import dev.korafx.sample.ui.pages.dataGridPage
import dev.korafx.sample.ui.pages.dslPage
import dev.korafx.sample.ui.pages.frameworkPage
import dev.korafx.sample.ui.pages.graphEditorPage
import dev.korafx.sample.ui.pages.inspectorPage
import dev.korafx.sample.ui.pages.mvvmPage
import dev.korafx.sample.ui.pages.navigationPage
import dev.korafx.sample.ui.pages.overviewPage
import dev.korafx.sample.ui.pages.resourceExplorerPage
import dev.korafx.sample.ui.pages.sourceEditorPage
import dev.korafx.sample.ui.pages.themePage
import dev.korafx.sample.ui.pages.virtualListPage
import dev.korafx.sample.ui.pages.workspacePage
import dev.korafx.sample.viewmodel.WorkbenchState
import dev.korafx.sourceeditor.codeEditor

internal enum class WorkbenchTransitionMode(
    val label: String,
    val transition: RouteTransition,
) {
    None("None", RouteTransition.None),
    Fade("Fade", RouteTransition.Fade()),
    Slide("Slide", RouteTransition.Slide()),
    Scale("Scale", RouteTransition.Scale()),
}

internal fun NodeContainerBuilder.renderWorkbenchModuleBadges(module: ModuleShowcase?) {
    if (module == null) {
        return
    }
    hbox(spacing = 10.0) {
        badge(module.category.title, ComponentTone.INFO)
        module.tags.take(4).forEach { tag ->
            chip(tag, ComponentTone.NEUTRAL)
        }
    }
}

internal fun NodeContainerBuilder.renderWorkbenchRoute(
    state: WorkbenchState,
    pageContext: WorkbenchPageContext,
) {
    when (state.currentRouteId) {
        WorkbenchRoute.Framework.id -> frameworkPage(pageContext)
        WorkbenchRoute.Dsl.id -> dslPage(pageContext)
        WorkbenchRoute.Mvvm.id -> mvvmPage(pageContext, state)
        WorkbenchRoute.Theme.id -> themePage(pageContext)
        WorkbenchRoute.Navigation.id -> navigationPage(pageContext, state)
        WorkbenchRoute.Components.id -> componentsPage(pageContext)
        WorkbenchRoute.SourceEditor.id -> sourceEditorPage(pageContext)
        WorkbenchRoute.DataGrid.id -> dataGridPage(pageContext)
        WorkbenchRoute.ResourceExplorer.id -> resourceExplorerPage(pageContext)
        WorkbenchRoute.Workspace.id -> workspacePage(pageContext)
        WorkbenchRoute.InspectorPanel.id -> inspectorPage(pageContext)
        WorkbenchRoute.CommandPalette.id -> commandPalettePage(pageContext)
        WorkbenchRoute.GraphEditor.id -> graphEditorPage()
        WorkbenchRoute.VirtualList.id -> virtualListPage()
        else -> overviewPage(pageContext, state)
    }
}

internal fun NodeContainerBuilder.renderWorkbenchSourceCode(state: WorkbenchState) {
    val snippets = state.sourceSnippets.ifEmpty {
        listOf(
            dev.korafx.sample.domain.SourceSnippet(
                title = "Navigate to module",
                language = "kotlin",
                code = """viewModel.dispatch(WorkbenchAction.NavigateModule("${state.currentRouteId}"))""",
                description = "Every module in the left rail is a typed KoraFX route.",
            ),
        )
    }

    snippets.forEach { snippet ->
        section(
            title = snippet.title,
            description = snippet.description.takeIf { it.isNotBlank() },
            padding = 12.0,
        ) {
            codeEditor(
                title = "${snippet.id}.${snippet.language}",
                text = snippet.code,
                language = snippet.language,
                readOnly = true,
                showSearch = true,
                wrapText = false,
                init = {
                    prefHeight = 220.0
                    maxWidth = Double.MAX_VALUE
                },
            )
        }
    }
}
