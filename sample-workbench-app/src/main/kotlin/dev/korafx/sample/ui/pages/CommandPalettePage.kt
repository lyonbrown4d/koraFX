package dev.korafx.sample.ui.pages

import dev.korafx.components.actionBar
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.onAction
import dev.korafx.sample.viewmodel.WorkbenchAction

fun NodeContainerBuilder.commandPalettePage(context: WorkbenchPageContext) {
    section(
        title = "Command palette host",
        description = "Commands are registered once and can navigate modules, switch theme or run application actions.",
    ) {
        actionBar(alignEnd = false) {
            button("Open Command Palette") {
                onAction {
                    context.commandPaletteHost.show()
                }
            }
            ghostButton("Go to Source Editor") {
                onAction {
                    context.viewModel.dispatch(WorkbenchAction.NavigateModule("source-editor"))
                }
            }
        }
    }
}
