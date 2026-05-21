package dev.korafx.sample.ui.pages

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.grapheditor.graphEditor

fun NodeContainerBuilder.graphEditorPage() {
    graphEditor(
        snapGrid = 8.0,
        init = {
            prefHeight = 360.0
            maxWidth = Double.MAX_VALUE
        },
    ) {
        val catalog = node(id = "catalog", label = "Catalog", x = 70.0, y = 80.0)
        val viewModel = node(id = "view-model", label = "ViewModel", x = 310.0, y = 80.0)
        val ui = node(id = "ui", label = "JavaFX UI", x = 190.0, y = 240.0)
        edge(catalog, viewModel, "feeds")
        edge(viewModel, ui, "state")
    }
}
