package dev.korafx.grapheditor

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import kotlin.math.sqrt

private const val HANDLE_RADIUS = 7.0
private const val MIN_ZOOM = 0.2
private const val MAX_ZOOM = 3.0
private const val DEFAULT_STYLESHEET = "/dev/korafx/grapheditor/graph-editor.css"

class GraphEditor internal constructor(
    initialGraph: Graph = Graph(),
    enableZoomAndPan: Boolean = true,
    private val snapGrid: Double = 0.0,
) : Pane() {
    val graph: Graph = initialGraph

    internal val nodesLayer = Pane()
    private val edgesLayer = Pane()
    private val viewport = Pane(edgesLayer, nodesLayer)
    private val connectionPreview = Line()

    private val nodeViews = HashMap<String, NodeVisual>()
    private val edgeViews = HashMap<String, EdgeVisual>()
    private var dragging: NodeDragState? = null
    private var panning = false
    private var panStartX = 0.0
    private var panStartY = 0.0
    private var zoom = 1.0

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

    private fun installInteraction(enableZoomAndPan: Boolean) {
        if (!enableZoomAndPan) {
            return
        }

        addEventFilter(ScrollEvent.SCROLL) { event ->
            if (!event.isControlDown) {
                return@addEventFilter
            }
            val factor = if (event.deltaY > 0) 1.1 else 0.9
            zoom = (zoom * factor).coerceIn(MIN_ZOOM, MAX_ZOOM)
            viewport.scaleX = zoom
            viewport.scaleY = zoom
            event.consume()
        }

        addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            if (event.button != MouseButton.MIDDLE) {
                return@addEventHandler
            }
            panning = true
            panStartX = event.x
            panStartY = event.y
        }

        addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
            if (!panning || event.button != MouseButton.MIDDLE) {
                return@addEventHandler
            }
            val deltaX = event.x - panStartX
            val deltaY = event.y - panStartY
            panStartX = event.x
            panStartY = event.y
            viewport.translateX += deltaX
            viewport.translateY += deltaY
        }

        addEventHandler(MouseEvent.MOUSE_RELEASED) { event ->
            if (event.button == MouseButton.MIDDLE) {
                panning = false
            }
        }

        setOnMouseMoved { event ->
            val source = graph.connectingFrom ?: return@setOnMouseMoved
            if (!connectionPreview.isVisible) {
                return@setOnMouseMoved
            }
            val target = sceneToViewport(event.sceneX, event.sceneY)
            connectionPreview.endX = target.x
            connectionPreview.endY = target.y
            if (!graph.nodes.contains(source)) {
                graph.cancelConnection()
            }
        }
    }

    private fun addNodeView(node: GraphNode) {
        if (nodeViews.containsKey(node.id)) {
            return
        }

        val body = Rectangle().apply {
            styleClass("graph-editor-node")
            arcWidth = 14.0
            arcHeight = 14.0
        }

        val text = Label(node.label).apply {
            styleClass("graph-editor-node-label")
            isWrapText = true
            maxWidth = node.width - 24.0
        }

        val inHandle = Circle(HANDLE_RADIUS).apply {
            styleClass("graph-editor-handle")
        }

        val outHandle = Circle(HANDLE_RADIUS).apply {
            styleClass("graph-editor-handle")
        }

        val root = Group(body, text, inHandle, outHandle).apply {
            isPickOnBounds = true
            styleClass("graph-editor-node-container")
            styleClass("graph-editor-node-group")
        }

        val visual = NodeVisual(node, root, body, text, inHandle, outHandle)
        nodeViews[node.id] = visual
        nodesLayer.children += root
        refreshNodeView(visual)
        installNodeInput(visual)
        installHandleInput(visual)
    }

    private fun addEdgeView(edge: GraphEdge) {
        if (edgeViews.containsKey(edge.id)) {
            return
        }

        val line = Line().apply {
            styleClass("graph-editor-edge")
        }

        val arrow = Polygon().apply {
            styleClass("graph-editor-edge-arrow")
        }

        val label = edge.label?.let {
            if (it.isBlank()) null else Label(it).apply {
                styleClass("graph-editor-edge-label")
            }
        }

        val root = if (label == null) {
            Group(line, arrow)
        } else {
            Group(line, arrow, label)
        }

        val visual = EdgeVisual(edge, root, line, arrow, label)
        edgeViews[edge.id] = visual
        edgesLayer.children += root

        refreshEdgeView(visual)

        root.styleClass("graph-editor-edge-group")
        root.isPickOnBounds = false
        root.onMouseClicked = { event ->
            if (event.button == MouseButton.PRIMARY) {
                graph.selectEdge(edge)
                event.consume()
            }
        }
        line.onMouseClicked = root.onMouseClicked
        arrow.onMouseClicked = root.onMouseClicked
        label?.onMouseClicked = root.onMouseClicked
    }

    private fun removeNodeView(node: GraphNode) {
        nodeViews.remove(node.id)?.let { view ->
            nodesLayer.children.remove(view.root)
        }
    }

    private fun removeEdgeView(edge: GraphEdge) {
        edgeViews.remove(edge.id)?.let { view ->
            edgesLayer.children.remove(view.root)
        }
    }

    private fun installNodeInput(visual: NodeVisual) {
        val root = visual.root

        root.onMousePressed = { event ->
            if (event.button == MouseButton.PRIMARY) {
                val localPoint = sceneToViewport(event.sceneX, event.sceneY)
                dragging = NodeDragState(
                    node = visual.node,
                    startLocalX = localPoint.x,
                    startLocalY = localPoint.y,
                    startNodeX = visual.node.x,
                    startNodeY = visual.node.y,
                    dragged = false,
                )
                graph.selectNode(visual.node)
                event.consume()
            }
        }

        root.onMouseDragged = { event ->
            val state = dragging
            if (state != null) {
                val localPoint = sceneToViewport(event.sceneX, event.sceneY)
                state.dragged = true
                val nextX = state.startNodeX + localPoint.x - state.startLocalX
                val nextY = state.startNodeY + localPoint.y - state.startLocalY
                graph.moveNode(state.node, snap(nextX), snap(nextY))
                refreshNodeView(visual)
                refreshEdgesForNode(visual.node.id)
                event.consume()
            }
        }

        root.onMouseReleased = { _ ->
            dragging = null
        }

        root.onMouseClicked = { event ->
            if (event.button == MouseButton.PRIMARY) {
                dragging?.let { state ->
                    if (!state.dragged) {
                        val connecting = graph.connectingFrom
                        if (connecting == null) {
                            val source = graph.selectedNode
                            if (source != null && source != visual.node) {
                                graph.connect(source, visual.node)
                                graph.selectNode(visual.node)
                            } else {
                                graph.selectNode(visual.node)
                            }
                        } else if (connecting == visual.node) {
                            graph.cancelConnection()
                            graph.selectNode(visual.node)
                        } else {
                            graph.connect(connecting, visual.node)
                        }
                        event.consume()
                    }
                } ?: graph.selectNode(visual.node)
                event.consume()
            }
        }
    }

    private fun installHandleInput(visual: NodeVisual) {
        visual.outHandle.onMousePressed = {
            graph.beginConnectionFrom(visual.node)
            it.consume()
        }
        visual.outHandle.onMouseClicked = { event ->
            if (event.button == MouseButton.PRIMARY) {
                graph.beginConnectionFrom(visual.node)
                event.consume()
            }
        }

        visual.inHandle.onMousePressed = { event ->
            if (graph.connectingFrom == null) {
                graph.selectNode(visual.node)
                event.consume()
            }
        }
        visual.inHandle.onMouseClicked = { event ->
            if (event.button == MouseButton.PRIMARY) {
                if (graph.connectingFrom == null) {
                    graph.selectNode(visual.node)
                } else {
                    graph.connectById(visual.node.id)
                }
                event.consume()
            }
        }
    }

    private fun refreshNodeView(visual: NodeVisual) {
        visual.body.apply {
            x = visual.node.x
            y = visual.node.y
            width = visual.node.width
            height = visual.node.height
        }

        visual.label.apply {
            text = visual.node.label
            layoutX = visual.node.x + 8.0
            layoutY = visual.node.y + 10.0
            maxWidth = visual.node.width - 24.0
        }

        visual.inHandle.apply {
            centerX = visual.node.x
            centerY = visual.node.y + visual.node.height / 2
        }

        visual.outHandle.apply {
            centerX = visual.node.x + visual.node.width
            centerY = visual.node.y + visual.node.height / 2
        }
    }

    private fun refreshEdgeView(visual: EdgeVisual) {
        val source = graph.nodeOf(visual.edge.sourceId)
        val target = graph.nodeOf(visual.edge.targetId)
        if (source == null || target == null) {
            visual.root.isVisible = false
            return
        }
        visual.root.isVisible = true

        val sourceX = source.x + source.width
        val sourceY = source.y + source.height / 2
        val targetX = target.x
        val targetY = target.y + target.height / 2

        visual.line.startX = sourceX
        visual.line.startY = sourceY
        visual.line.endX = targetX
        visual.line.endY = targetY

        val deltaX = targetX - sourceX
        val deltaY = targetY - sourceY
        val length = sqrt(deltaX * deltaX + deltaY * deltaY)

        if (length > 0) {
            val unitX = deltaX / length
            val unitY = deltaY / length
            val arrowSize = 10.0
            val arrowWidth = 6.0
            val tipX = targetX - unitX * arrowSize
            val tipY = targetY - unitY * arrowSize
            val leftX = tipX - unitY * arrowWidth
            val leftY = tipY + unitX * arrowWidth
            val rightX = tipX + unitY * arrowWidth
            val rightY = tipY - unitX * arrowWidth
            visual.arrow.points.setAll(tipX, tipY, leftX, leftY, rightX, rightY)
        }

        if (visual.label != null) {
            visual.label.layoutX = (sourceX + targetX) / 2
            visual.label.layoutY = (sourceY + targetY) / 2 - 9.0
        }
    }

    private fun refreshAllEdges() {
        edgeViews.values.forEach(::refreshEdgeView)
    }

    private fun refreshEdgesForNode(nodeId: String) {
        edgeViews.values.forEach { visual ->
            if (visual.edge.sourceId == nodeId || visual.edge.targetId == nodeId) {
                refreshEdgeView(visual)
            }
        }
    }

    private fun refreshSelectionStyles() {
        nodeViews.values.forEach { visual ->
            if (graph.selectedNode == visual.node) {
                visual.body.styleClass.add("graph-editor-node-selected")
                visual.inHandle.styleClass.add("graph-editor-handle-selected")
                visual.outHandle.styleClass.add("graph-editor-handle-selected")
            } else {
                visual.body.styleClass.remove("graph-editor-node-selected")
                visual.inHandle.styleClass.remove("graph-editor-handle-selected")
                visual.outHandle.styleClass.remove("graph-editor-handle-selected")
            }
        }

        edgeViews.values.forEach { visual ->
            if (graph.selectedEdge == visual.edge) {
                visual.line.styleClass.add("graph-editor-edge-selected")
                visual.arrow.styleClass.add("graph-editor-edge-selected")
            } else {
                visual.line.styleClass.remove("graph-editor-edge-selected")
                visual.arrow.styleClass.remove("graph-editor-edge-selected")
            }
        }
    }

    private fun snap(value: Double): Double =
        if (snapGrid > 0.0) {
            kotlin.math.round(value / snapGrid) * snapGrid
        } else {
            value
        }

    private fun sceneToViewport(sceneX: Double, sceneY: Double): Point2D {
        return viewport.sceneToLocal(sceneX, sceneY)
    }

    internal fun nodeViewIds(): List<String> = nodeViews.keys.toList()

    private data class NodeVisual(
        val node: GraphNode,
        val root: Group,
        val body: Rectangle,
        val label: Label,
        val inHandle: Circle,
        val outHandle: Circle,
    )

    private data class EdgeVisual(
        val edge: GraphEdge,
        val root: Group,
        val line: Line,
        val arrow: Polygon,
        val label: Label?,
    )

    private data class NodeDragState(
        val node: GraphNode,
        val startLocalX: Double,
        val startLocalY: Double,
        val startNodeX: Double,
        val startNodeY: Double,
        var dragged: Boolean,
    )
}

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
