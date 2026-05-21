package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.actionBar
import dev.korafx.components.badge
import dev.korafx.components.chip
import dev.korafx.components.heroBanner
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.onAction
import dev.korafx.sample.ui.WorkbenchIcons

fun NodeContainerBuilder.componentsPage(context: WorkbenchPageContext) {
    heroBanner(
        title = "KoraFX Components",
        subtitle = "Base components provide the visual language used by every advanced module.",
        eyebrow = "korafx-components",
        icon = WorkbenchIcons.Samples,
    ) {
        badge("Material", ComponentTone.INFO)
        chip("cards", ComponentTone.NEUTRAL)
        chip("status", ComponentTone.SUCCESS)
    }

    section("Controls") {
        actionBar(alignEnd = false) {
            button("Primary") {}
            ghostButton("Ghost") {}
            button("Command Palette") {
                onAction {
                    context.commandPaletteHost.show()
                }
            }
        }
    }
}
