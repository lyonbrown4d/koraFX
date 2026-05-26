package dev.korafx.grapheditor

import dev.korafx.dsl.NodeContainerBuilder

class GraphEditorBuilder internal constructor(
    private val editor: GraphEditor,
) {
    fun node(
        id: String? = null,
        label: String = "Node",
        x: Double = 40.0,
        y: Double = 40.0,
        width: Double = 140.0,
        height: Double = 64.0,
    ): GraphNode = editor.addNode(id, label, x, y, width, height)

    fun edge(
        source: GraphNode,
        target: GraphNode,
        label: String? = null,
        handler: ((GraphEdge) -> Unit)? = null,
    ): GraphEdge {
        val edge = editor.addEdge(source, target, label)
        if (handler != null) {
            handler(edge)
        }
        return edge
    }

    fun onNodeSelected(handler: (GraphNode?) -> Unit) {
        editor.graph.onSelectedNodeChanged(handler)
    }

    fun onEdgeSelected(handler: (GraphEdge?) -> Unit) {
        editor.graph.onSelectedEdgeChanged(handler)
    }

    fun onConnectionCreated(handler: (GraphEdge) -> Unit) {
        editor.graph.onEdgeCreated(handler)
    }

    fun deleteSelected() {
        editor.deleteSelected()
    }
}

fun graphEditor(
    initialGraph: Graph = Graph(),
    enableZoomAndPan: Boolean = true,
    snapGrid: Double = 0.0,
    init: GraphEditor.() -> Unit = {},
    content: GraphEditorBuilder.() -> Unit = {},
): GraphEditor =
    GraphEditor(
        initialGraph = initialGraph,
        enableZoomAndPan = enableZoomAndPan,
        snapGrid = snapGrid,
    ).apply(init).apply {
        GraphEditorBuilder(this).content()
    }

fun NodeContainerBuilder.graphEditor(
    initialGraph: Graph = Graph(),
    enableZoomAndPan: Boolean = true,
    snapGrid: Double = 0.0,
    init: GraphEditor.() -> Unit = {},
    content: GraphEditorBuilder.() -> Unit = {},
): GraphEditor =
    add(
        dev.korafx.grapheditor.graphEditor(
            initialGraph = initialGraph,
            enableZoomAndPan = enableZoomAndPan,
            snapGrid = snapGrid,
            init = init,
            content = content,
        ),
    )
