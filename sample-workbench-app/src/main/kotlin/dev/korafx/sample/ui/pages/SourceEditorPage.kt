package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sourceeditor.SourceDiagnostic
import dev.korafx.sourceeditor.SourceEditor
import dev.korafx.sourceeditor.queryEditor
import dev.korafx.sourceeditor.sourceEditor
import javafx.scene.control.Label

fun NodeContainerBuilder.sourceEditorPage(context: WorkbenchPageContext) {
    section("Source Editor") {
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
                SourceDiagnostic(
                    1,
                    14,
                    "Sample diagnostic rendered by SourceEditor.",
                    ComponentTone.INFO,
                ),
            ),
            init = {
                prefHeight = 420.0
                maxHeight = 420.0
                maxWidth = Double.MAX_VALUE
            },
        ) {
            action("Format") {
                context.viewModel.dispatch(WorkbenchAction.SubmitDraft)
            }
            markIdle("Source ready.")
        }
    }

    section("Query Editor") {
        var query: SourceEditor? = null

        val sql = """
            select id, owner, status
            from modules
            where status = 'Ready';
        """.trimIndent()

        query = queryEditor(
            title = "query.sql",
            text = sql,
            showSearch = true,
            onRun = { rawSql ->
                query?.markRunning("Running query")
                if (rawSql.contains("select", ignoreCase = true)) {
                    query?.setResult(
                        queryResultPanel("Demo result", rawSql.lines().size),
                        title = "Execution result",
                    )
                    query?.markSuccess("Query succeeded")
                    query?.setDiagnostics(
                        listOf(
                            SourceDiagnostic(
                                line = 1,
                                column = 1,
                                message = "This is a demo result generated in sample mode.",
                                tone = ComponentTone.SUCCESS,
                            ),
                        ),
                    )
                } else {
                    query?.setDiagnostics(
                        listOf(
                            SourceDiagnostic(
                                line = 1,
                                column = 1,
                                message = "Use SELECT statements for demo execution.",
                                tone = ComponentTone.DANGER,
                            ),
                        ),
                    )
                    query?.markError("Validation failed")
                }
            },
            onStop = {
                query?.markError("Run aborted")
            },
            init = {
                prefHeight = 260.0
                maxWidth = Double.MAX_VALUE
            },
        ) {
            markRunning("Ready")
        }
    }
}

private fun queryResultPanel(title: String, count: Int): javafx.scene.layout.VBox =
    javafx.scene.layout.VBox(8.0).apply {
        children += Label(title).apply {
            styleClass("component-title")
        }
        children += Label("Rows: $count").apply {
            styleClass("source-editor-result-content")
        }
    }
