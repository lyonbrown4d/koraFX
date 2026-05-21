package dev.korafx.sample.ui.pages

import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.hbox
import dev.korafx.dsl.styleClasses
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.routeButton
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.viewmodel.WorkbenchState

fun NodeContainerBuilder.navigationPage(
    context: WorkbenchPageContext,
    state: WorkbenchState,
) {
    section(
        title = "Typed route navigation",
        description = "Module routes use the same Navigator state as buttons, command palette entries and deep links.",
    ) {
        hbox(spacing = 8.0) {
            add(routeButton(context.uiScope, context.navigator, WorkbenchRoute.Overview))
            add(routeButton(context.uiScope, context.navigator, WorkbenchRoute.Components))
            add(routeButton(context.uiScope, context.navigator, WorkbenchRoute.Theme))
        }
        label("Current route: ${state.currentRouteId}") {
            styleClasses(ThemeStyleClass.Muted)
        }
    }
}
