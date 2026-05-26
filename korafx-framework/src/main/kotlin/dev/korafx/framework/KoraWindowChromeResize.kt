package dev.korafx.framework

import javafx.scene.Cursor
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.stage.Stage
import kotlin.math.max

internal fun installWindowResize(
    frame: Region,
    stage: Stage,
    spec: KoraWindowSpec,
) {
    var resize = ResizeSession.NONE

    frame.addEventFilter(MouseEvent.MOUSE_MOVED) { event ->
        if (resize.edge == ResizeEdge.NONE) {
            frame.cursor = edgeAt(event, stage, spec).cursor
        }
    }
    frame.addEventFilter(MouseEvent.MOUSE_EXITED) {
        if (resize.edge == ResizeEdge.NONE) {
            frame.cursor = Cursor.DEFAULT
        }
    }
    frame.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
        if (event.button != MouseButton.PRIMARY || stage.isMaximized) {
            return@addEventFilter
        }
        val edge = edgeAt(event, stage, spec)
        if (edge != ResizeEdge.NONE) {
            resize = ResizeSession(
                edge = edge,
                screenX = event.screenX,
                screenY = event.screenY,
                stageX = stage.x,
                stageY = stage.y,
                width = stage.width,
                height = stage.height,
            )
            event.consume()
        }
    }
    frame.addEventFilter(MouseEvent.MOUSE_DRAGGED) { event ->
        val session = resize
        if (session.edge == ResizeEdge.NONE) {
            return@addEventFilter
        }
        applyResize(stage, spec, session, event)
        event.consume()
    }
    frame.addEventFilter(MouseEvent.MOUSE_RELEASED) { event ->
        resize = ResizeSession.NONE
        frame.cursor = edgeAt(event, stage, spec).cursor
    }
}

private fun edgeAt(
    event: MouseEvent,
    stage: Stage,
    spec: KoraWindowSpec,
): ResizeEdge {
    if (!spec.resizable || stage.isMaximized) {
        return ResizeEdge.NONE
    }

    val border = spec.titleBar.resizeBorderWidth
    if (border <= 0.0) {
        return ResizeEdge.NONE
    }

    val scene = stage.scene ?: return ResizeEdge.NONE
    val left = event.sceneX <= border
    val right = event.sceneX >= scene.width - border
    val top = event.sceneY <= border
    val bottom = event.sceneY >= scene.height - border

    return when {
        top && left -> ResizeEdge.NORTH_WEST
        top && right -> ResizeEdge.NORTH_EAST
        bottom && left -> ResizeEdge.SOUTH_WEST
        bottom && right -> ResizeEdge.SOUTH_EAST
        top -> ResizeEdge.NORTH
        right -> ResizeEdge.EAST
        bottom -> ResizeEdge.SOUTH
        left -> ResizeEdge.WEST
        else -> ResizeEdge.NONE
    }
}

private fun applyResize(
    stage: Stage,
    spec: KoraWindowSpec,
    session: ResizeSession,
    event: MouseEvent,
) {
    val dx = event.screenX - session.screenX
    val dy = event.screenY - session.screenY
    val minWidth = max(stage.minWidth, spec.minWidth)
    val minHeight = max(stage.minHeight, spec.minHeight)

    if (session.edge.west) {
        val width = (session.width - dx).coerceAtLeast(minWidth)
        stage.x = session.stageX + session.width - width
        stage.width = width
    }
    if (session.edge.east) {
        stage.width = (session.width + dx).coerceAtLeast(minWidth)
    }
    if (session.edge.north) {
        val height = (session.height - dy).coerceAtLeast(minHeight)
        stage.y = session.stageY + session.height - height
        stage.height = height
    }
    if (session.edge.south) {
        stage.height = (session.height + dy).coerceAtLeast(minHeight)
    }
}

private data class ResizeSession(
    val edge: ResizeEdge,
    val screenX: Double,
    val screenY: Double,
    val stageX: Double,
    val stageY: Double,
    val width: Double,
    val height: Double,
) {
    companion object {
        val NONE = ResizeSession(
            edge = ResizeEdge.NONE,
            screenX = 0.0,
            screenY = 0.0,
            stageX = 0.0,
            stageY = 0.0,
            width = 0.0,
            height = 0.0,
        )
    }
}

private enum class ResizeEdge(
    val cursor: Cursor,
    val north: Boolean = false,
    val east: Boolean = false,
    val south: Boolean = false,
    val west: Boolean = false,
) {
    NONE(Cursor.DEFAULT),
    NORTH(Cursor.N_RESIZE, north = true),
    EAST(Cursor.E_RESIZE, east = true),
    SOUTH(Cursor.S_RESIZE, south = true),
    WEST(Cursor.W_RESIZE, west = true),
    NORTH_EAST(Cursor.NE_RESIZE, north = true, east = true),
    NORTH_WEST(Cursor.NW_RESIZE, north = true, west = true),
    SOUTH_EAST(Cursor.SE_RESIZE, south = true, east = true),
    SOUTH_WEST(Cursor.SW_RESIZE, south = true, west = true),
}
