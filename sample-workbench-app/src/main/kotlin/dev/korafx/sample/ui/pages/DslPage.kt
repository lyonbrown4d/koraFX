package dev.korafx.sample.ui.pages

import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.bindTextBidirectional
import dev.korafx.dsl.form
import dev.korafx.dsl.stateText
import dev.korafx.dsl.styleClasses
import dev.korafx.framework.theme.ThemeStyleClass
import java.time.LocalDate

fun NodeContainerBuilder.dslPage(context: WorkbenchPageContext) {
    section(
        title = "State-bound DSL",
        description = "The DSL stays close to JavaFX, while Flow bindings make component state explicit at the call site.",
    ) {
        form {
            item("Project") {
                textField(context.dslProjectName.value) {
                    bindTextBidirectional(context.uiScope, context.dslProjectName)
                }
            }
            item("Target Date") {
                label(LocalDate.now().plusWeeks(1).toString())
            }
        }

        label {
            styleClasses(ThemeStyleClass.Muted)
        }.stateText(context.uiScope, context.dslProjectName) { "Current project: $it" }
    }
}
