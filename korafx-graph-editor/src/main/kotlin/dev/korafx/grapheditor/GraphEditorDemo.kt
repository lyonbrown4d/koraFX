package dev.korafx.grapheditor

class GraphEditorDemo {
    fun create(): GraphEditor =
        graphEditor(
            initialGraph = Graph(),
            snapGrid = 10.0,
            content = {
                val source = node("source", "Source", x = 64.0, y = 96.0)
                val transform = node("transform", "Transform", x = 292.0, y = 96.0)
                val sink = node("sink", "Sink", x = 520.0, y = 96.0)

                edge(source, transform, "parse")
                edge(transform, sink, "emit")
            },
        )
}

fun graphEditorDemo(): GraphEditor = GraphEditorDemo().create()
