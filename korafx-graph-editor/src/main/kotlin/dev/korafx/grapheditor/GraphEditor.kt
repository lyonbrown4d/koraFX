package dev.korafx.grapheditor

import dev.korafx.dsl.styleClass
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Line

class GraphEditor internal constructor(
    initialGraph: Graph = Graph(),
    enableZoomAndPan: Boolean = true,
    internal val snapGrid: Double = 0.0,
) : Pane() {
    val graph: Graph = initialGraph

    internal val nodesLayer = Pane()
    internal val edgesLayer = Pane()
    internal val viewport = Pane(edgesLayer, nodesLayer)
    internal val connectionPreview = Line()

    internal val nodeViews = HashMap<String, NodeVisual>()
    internal val edgeViews = HashMap<String, EdgeVisual>()
    internal var dragging: NodeDragState? = null
    internal var panning = false
    internal var panStartX = 0.0
    internal var panStartY = 0.0
    internal var zoom = 1.0

    private val selectionNodeListener = ChangeListener<GraphNode?> { _, _, _ ->
        refreshSelectionStyles()
    }

    private val selectionEdgeListener = ChangeListener<GraphEdge?> { _, _, _ ->
        refreshSelectionStyles()
    }

    private val connectingListener = ChangeListener<GraphNode?> { _, _, source ->
        if (source == null) {
            connectionPreview.isVisible = false
            return@ChangeListener
        }

        val sourceNode = graph.nodeOf(source.id) ?: return@ChangeListener
        connectionPreview.isVisible = true
        connectionPreview.startX = sourceNode.x + sourceNode.width
        connectionPreview.startY = sourceNode.y + sourceNode.height / 2
        connectionPreview.endX = sourceNode.x + sourceNode.width
        connectionPreview.endY = sourceNode.y + sourceNode.height / 2
    }

    private val nodeListChangeListener = ListChangeListener<GraphNode> { change ->
        while (change.next()) {
            if (change.wasAdded()) {
                change.addedSubList.forEach(::addNodeView)
            }
            if (change.wasRemoved()) {
                change.removed.forEach(::removeNodeView)
            }
        }
        refreshSelectionStyles()
    }

    private val edgeListChangeListener = ListChangeListener<GraphEdge> { change ->
        while (change.next()) {
            if (change.wasAdded()) {
                change.addedSubList.forEach(::addEdgeView)
            }
            if (change.wasRemoved()) {
                change.removed.forEach(::removeEdgeView)
            }
        }
        refreshAllEdges()
        refreshSelectionStyles()
    }

    init {
        styleClass("graph-editor")
        isFocusTraversable = true
        prefWidth = Double.MAX_VALUE
        prefHeight = Double.MAX_VALUE
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE
        javaClass.getResource(DEFAULT_STYLESHEET)?.toExternalForm()?.let { stylesheet ->
            stylesheets += stylesheet
        }

        viewport.children.add(connectionPreview)
        viewport.isPickOnBounds = true
        children += viewport

        connectionPreview.isVisible = false
        connectionPreview.isMouseTransparent = true
        connectionPreview.styleClass("graph-editor-connection-preview")

        graph.selectedNodeProperty.addListener(selectionNodeListener)
        graph.selectedEdgeProperty.addListener(selectionEdgeListener)
        graph.connectingFromProperty.addListener(connectingListener)
        graph.nodes.addListener(nodeListChangeListener)
        graph.edges.addListener(edgeListChangeListener)

        graph.nodes.forEach(::addNodeView)
        graph.edges.forEach(::addEdgeView)
        refreshAllEdges()
        refreshSelectionStyles()

        installInteraction(enableZoomAndPan)

        addEventFilter(MouseEvent.MOUSE_PRESSED) {
            requestFocus()
        }
        setOnKeyPressed { event ->
            when (event.code) {
                KeyCode.DELETE, KeyCode.BACK_SPACE -> {
                    graph.deleteSelected()
                    event.consume()
                }
                else -> Unit
            }
        }
    }

    fun addNode(
        id: String? = null,
        label: String = "Node",
        x: Double = 40.0,
        y: Double = 40.0,
        width: Double = 140.0,
        height: Double = 64.0,
    ): GraphNode = graph.addNode(id, label, x, y, width, height)

    fun addEdge(
        source: GraphNode,
        target: GraphNode,
        label: String? = null,
    ): GraphEdge = graph.addEdge(source, target, label = label)

    fun deleteSelected(): Boolean = graph.deleteSelected()

    fun clear() {
        graph.clear()
    }

    fun zoomTo(value: Double) {
        zoom = value.coerceIn(MIN_ZOOM, MAX_ZOOM)
        viewport.scaleX = zoom
        viewport.scaleY = zoom
    }

    internal fun nodeViewIds(): List<String> = nodeViews.keys.toList()
}
