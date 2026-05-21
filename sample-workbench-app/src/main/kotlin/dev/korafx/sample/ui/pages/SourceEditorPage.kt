package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sourceeditor.SourceDiagnostic
import dev.korafx.sourceeditor.sourceEditor

fun NodeContainerBuilder.sourceEditorPage(context: WorkbenchPageContext) {
    sourceEditor(
        title = "Main.kt",
        text = """
            fun main() = koraApplication {
                content { WorkbenchRootView(graph).buildRoot() }
            }
        """.trimIndent(),
        language = "kotlin",
        showSearch = true,
        diagnostics = listOf(
            SourceDiagnostic(1, 14, "Sample diagnostic rendered by SourceEditor.", ComponentTone.INFO),
        ),
        init = {
            prefHeight = 460.0
            minHeight = 320.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        action("Format") {
            context.viewModel.dispatch(WorkbenchAction.SubmitDraft)
        }
    }
}
