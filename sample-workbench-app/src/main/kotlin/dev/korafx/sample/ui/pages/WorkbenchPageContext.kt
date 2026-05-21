package dev.korafx.sample.ui.pages

import dev.korafx.commandpalette.CommandPaletteHost
import dev.korafx.framework.theme.ThemeManager
import dev.korafx.navigation.Navigator
import dev.korafx.sample.data.WorkbenchCatalog
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.viewmodel.WorkbenchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class WorkbenchPageContext(
    val uiScope: CoroutineScope,
    val catalog: WorkbenchCatalog,
    val themeManager: ThemeManager,
    val navigator: Navigator<WorkbenchRoute>,
    val viewModel: WorkbenchViewModel,
    val commandPaletteHost: CommandPaletteHost,
    val dslProjectName: MutableStateFlow<String>,
)
