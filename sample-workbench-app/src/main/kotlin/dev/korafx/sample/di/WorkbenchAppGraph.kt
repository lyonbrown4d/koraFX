package dev.korafx.sample.di

import dev.korafx.commandpalette.CommandPaletteCommand
import dev.korafx.commandpalette.CommandPaletteHost
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
        single { WorkbenchViewModel(get(), get(), get()) }
        single { CommandPaletteHost(commandPaletteCommands(get())) }
    }

private fun commandPaletteCommands(viewModel: WorkbenchViewModel): List<CommandPaletteCommand> =
    listOf(WorkbenchRoute.Overview)
        .plus(WorkbenchRoute.moduleRoutes)
        .map { route ->
            CommandPaletteCommand(
                id = "route.${route.id}",
                title = "Open ${route.title}",
                description = route.summary,
                group = "Navigation",
            ) {
                viewModel.dispatch(WorkbenchAction.NavigatePath(route.path))
            }
        } + listOf(
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
