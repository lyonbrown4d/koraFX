package dev.korafx.sample.ui.pages

import dev.korafx.components.section
import dev.korafx.datagrid.dataGrid
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.bindSelectedItem
import dev.korafx.dsl.comboBox
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.sample.viewmodel.WorkbenchAction

fun NodeContainerBuilder.themePage(context: WorkbenchPageContext) {
    section(
        title = "Built-in theme switcher",
        description = "ThemeManager exposes typed tokens and generates the JavaFX stylesheet used by all standard controls.",
    ) {
        comboBox<KoraTheme>(
            items = context.themeManager.availableThemes,
            init = {
                prefWidth = 220.0
            },
        ) {
            render { it.displayName }
            onSelect { theme ->
                if (theme != null) {
                    context.viewModel.dispatch(WorkbenchAction.SelectTheme(theme.id))
                }
            }
        }.bindSelectedItem(context.uiScope, context.themeManager.theme)

        dataGrid(
            items = context.themeManager.availableThemes,
            showSearch = false,
            init = {
                prefHeight = 220.0
                maxWidth = Double.MAX_VALUE
            },
        ) {
            constrainedResize()
            textColumn("Theme") { it.displayName }
            textColumn("Primary") { it.tokens.colors.primary }
            textColumn("Surface") { it.tokens.colors.surface }
            textColumn("Radius") { "${it.tokens.radius}px" }
        }
    }
}
