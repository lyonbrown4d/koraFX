package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.actionBar
import dev.korafx.components.alertBanner
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.onAction
import dev.korafx.sample.viewmodel.WorkbenchAction

fun NodeContainerBuilder.frameworkPage(context: WorkbenchPageContext) {
    section(
        title = "Application framework",
        description = "The framework owns application boot, DI, theme binding, navigation and lifecycle disposal.",
    ) {
        alertBanner(
            title = "Koin-backed app graph",
            message = "The sample resolves ThemeManager, Navigator and WorkbenchViewModel from the KoraApplication graph.",
            tone = ComponentTone.INFO,
        )
        actionBar(alignEnd = false) {
            button("Open Commands") {
                onAction {
                    context.commandPaletteHost.show()
                }
            }
            ghostButton("Next Theme") {
                onAction {
                    context.viewModel.dispatch(WorkbenchAction.NextTheme)
                }
            }
        }
    }
}
