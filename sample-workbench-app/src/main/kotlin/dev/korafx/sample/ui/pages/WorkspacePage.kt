package dev.korafx.sample.ui.pages

import dev.korafx.components.card
import dev.korafx.components.tabWorkspace
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClasses
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.sourceeditor.queryEditor

fun NodeContainerBuilder.workspacePage() {
    tabWorkspace(
        emptyText = "Open a tab...",
        init = {
            prefHeight = 340.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        tab("overview", "Overview", closable = false, select = true) {
            card {
                label("Workbench tab") {
                    styleClasses(ThemeStyleClass.Headline)
                }
                label("TabWorkspace is intended for Git files, query editors and database object previews.")
            }
        }
        tab("query", "Query.sql", dirty = true) {
            queryEditor(
                text = "select name, owner, status from modules;",
                init = {
                    prefHeight = 260.0
                },
            )
        }
    }
}
