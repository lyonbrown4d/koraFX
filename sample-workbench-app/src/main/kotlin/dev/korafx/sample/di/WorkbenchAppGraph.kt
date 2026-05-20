package dev.korafx.sample.di

import dev.korafx.components.CommandPaletteCommand
import dev.korafx.components.CommandPaletteHost
import dev.korafx.framework.KoraApplication
import dev.korafx.navigation.Navigator
import dev.korafx.framework.theme.ThemeManager
import dev.korafx.sample.data.InMemoryWorkbenchCatalog
import dev.korafx.sample.data.WorkbenchCatalog
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sample.viewmodel.WorkbenchViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module

class WorkbenchAppGraph(
    val uiScope: CoroutineScope,
    val catalog: WorkbenchCatalog,
    val themeManager: ThemeManager,
    val navigator: Navigator<WorkbenchRoute>,
    val viewModel: WorkbenchViewModel,
    val commandPaletteHost: CommandPaletteHost,
) {
    companion object {
        fun from(app: KoraApplication): WorkbenchAppGraph =
            WorkbenchAppGraph(
                uiScope = app.uiScope,
                catalog = app.get(),
                themeManager = app.get(),
                navigator = app.get(),
                viewModel = app.get(),
                commandPaletteHost = app.get(),
            )
    }
}

fun workbenchModule(): Module =
    module {
        single<WorkbenchCatalog> { InMemoryWorkbenchCatalog() }
        single { WorkbenchViewModel(get(), get()) }
        single { CommandPaletteHost(commandPaletteCommands(get())) }
    }

private fun commandPaletteCommands(viewModel: WorkbenchViewModel): List<CommandPaletteCommand> =
    listOf(
        CommandPaletteCommand(
            id = "route.dsl",
            title = "Open DSL Route",
            description = "Navigate to Kotlin DSL examples.",
            group = "Navigation",
        ) {
            viewModel.dispatch(WorkbenchAction.Navigate(WorkbenchRoute.Dsl.id))
        },
        CommandPaletteCommand(
            id = "route.components",
            title = "Open Components Route",
            description = "Navigate to the component gallery.",
            group = "Navigation",
        ) {
            viewModel.dispatch(WorkbenchAction.Navigate(WorkbenchRoute.Components.id))
        },
        CommandPaletteCommand(
            id = "route.mvvm",
            title = "Open MVVM Route",
            description = "Navigate to MVVM overview.",
            group = "Navigation",
        ) {
            viewModel.dispatch(WorkbenchAction.Navigate(WorkbenchRoute.Mvvm.id))
        },
        CommandPaletteCommand(
            id = "route.theme",
            title = "Open Theme Route",
            description = "Navigate to theme preview.",
            group = "Navigation",
        ) {
            viewModel.dispatch(WorkbenchAction.Navigate(WorkbenchRoute.Theme.id))
        },
        CommandPaletteCommand(
            id = "theme.previous",
            title = "Previous Theme",
            description = "Switch to the previous built-in theme preset.",
            group = "Theme",
        ) {
            viewModel.dispatch(WorkbenchAction.PreviousTheme)
        },
        CommandPaletteCommand(
            id = "theme.next",
            title = "Next Theme",
            description = "Switch to the next built-in theme preset.",
            group = "Theme",
        ) {
            viewModel.dispatch(WorkbenchAction.NextTheme)
        },
        CommandPaletteCommand(
            id = "theme.toggle",
            title = "Toggle Light / Dark",
            description = "Toggle between the default light and dark presets.",
            group = "Theme",
        ) {
            viewModel.dispatch(WorkbenchAction.ToggleTheme)
        },
    )
