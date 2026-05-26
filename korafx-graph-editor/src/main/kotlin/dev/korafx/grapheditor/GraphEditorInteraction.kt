package dev.korafx.grapheditor

import javafx.geometry.Point2D
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import kotlin.math.round

internal fun GraphEditor.installInteraction(enableZoomAndPan: Boolean) {
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

internal fun GraphEditor.installNodeInput(visual: NodeVisual) {
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
                    finishNodeClick(visual)
                    event.consume()
                }
            } ?: graph.selectNode(visual.node)
            event.consume()
        }
    }
}

internal fun GraphEditor.installHandleInput(visual: NodeVisual) {
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

private fun GraphEditor.finishNodeClick(visual: NodeVisual) {
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
}

private fun GraphEditor.snap(value: Double): Double =
    if (snapGrid > 0.0) {
        round(value / snapGrid) * snapGrid
    } else {
        value
    }

private fun GraphEditor.sceneToViewport(sceneX: Double, sceneY: Double): Point2D =
    viewport.sceneToLocal(sceneX, sceneY)
