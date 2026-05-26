package dev.korafx.grapheditor

import dev.korafx.dsl.styleClass
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import kotlin.math.sqrt

internal data class NodeVisual(
    val node: GraphNode,
    val root: Group,
    val body: Rectangle,
    val label: Label,
    val inHandle: Circle,
    val outHandle: Circle,
)

internal data class EdgeVisual(
    val edge: GraphEdge,
    val root: Group,
    val line: Line,
    val arrow: Polygon,
    val label: Label?,
)

internal data class NodeDragState(
    val node: GraphNode,
    val startLocalX: Double,
    val startLocalY: Double,
    val startNodeX: Double,
    val startNodeY: Double,
    var dragged: Boolean,
)

internal fun GraphEditor.addNodeView(node: GraphNode) {
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

internal fun GraphEditor.addEdgeView(edge: GraphEdge) {
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
    val root = if (label == null) Group(line, arrow) else Group(line, arrow, label)

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

internal fun GraphEditor.removeNodeView(node: GraphNode) {
    nodeViews.remove(node.id)?.let { view ->
        nodesLayer.children.remove(view.root)
    }
}

internal fun GraphEditor.removeEdgeView(edge: GraphEdge) {
    edgeViews.remove(edge.id)?.let { view ->
        edgesLayer.children.remove(view.root)
    }
}

internal fun GraphEditor.refreshNodeView(visual: NodeVisual) {
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

internal fun GraphEditor.refreshEdgeView(visual: EdgeVisual) {
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

internal fun GraphEditor.refreshAllEdges() {
    edgeViews.values.forEach(::refreshEdgeView)
}

internal fun GraphEditor.refreshEdgesForNode(nodeId: String) {
    edgeViews.values.forEach { visual ->
        if (visual.edge.sourceId == nodeId || visual.edge.targetId == nodeId) {
            refreshEdgeView(visual)
        }
    }
}

internal fun GraphEditor.refreshSelectionStyles() {
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
