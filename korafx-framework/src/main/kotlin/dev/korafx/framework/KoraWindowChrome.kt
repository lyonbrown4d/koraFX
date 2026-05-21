package dev.korafx.framework

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.ButtonBase
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.control.TextInputControl
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import kotlin.math.max

internal object KoraWindowChrome {
    fun wrap(
        app: KoraApplication,
        stage: Stage,
        content: Parent,
        spec: KoraWindowSpec,
    ): Parent {
        val titleBar = titleBar(app, stage, spec)
        val frame = VBox(0.0).apply {
            styleClass += "kora-window-frame"
            if (spec.titleBar.transparentBackground) {
                styleClass += "kora-window-transparent"
            }
            if (spec.titleBar.cornerRadius > 0.0) {
                styleClass += "kora-window-rounded"
                style += "-fx-background-radius: ${spec.titleBar.cornerRadius}px;"
            }
            children += titleBar
            children += content
            VBox.setVgrow(content, Priority.ALWAYS)
        }
        installRoundedClip(frame, spec)

        if (spec.resizable && spec.titleBar.resizeEdges) {
            installResize(frame, stage, spec)
        }

        return frame
    }

    private fun titleBar(
        app: KoraApplication,
        stage: Stage,
        spec: KoraWindowSpec,
    ): HBox {
        val titleBarSpec = spec.titleBar
        val side = titleBarSpec.controlSide.resolve()
        val controls = windowControls(stage, spec, side)
        val contentSlot = HBox(8.0).apply {
            styleClass += "kora-window-titlebar-content"
            alignment = Pos.CENTER_LEFT
            HBox.setHgrow(this, Priority.ALWAYS)
            titleBarSpec.contentFactory?.invoke(app)?.let { content ->
                children += content
                if (content is Region) {
                    HBox.setHgrow(content, Priority.ALWAYS)
                }
            }
        }

        return HBox(10.0).apply {
            styleClass += "kora-window-titlebar"
            styleClass += "kora-window-controls-${side.name.lowercase()}"
            alignment = Pos.CENTER_LEFT
            minHeight = titleBarSpec.height
            prefHeight = titleBarSpec.height
            maxHeight = titleBarSpec.height

            if (side == KoraWindowControlSide.LEFT) {
                children += controls
            }
            if (titleBarSpec.showTitle) {
                children += titleStack(spec)
            }
            children += contentSlot
            if (side == KoraWindowControlSide.RIGHT) {
                children += controls
            }

            installDragToMove(this, stage, spec)
        }
    }

    private fun installRoundedClip(
        frame: Region,
        spec: KoraWindowSpec,
    ) {
        val radius = spec.titleBar.cornerRadius
        if (radius <= 0.0) {
            return
        }

        frame.clip = Rectangle().apply {
            arcWidth = radius * 2
            arcHeight = radius * 2
            widthProperty().bind(frame.widthProperty())
            heightProperty().bind(frame.heightProperty())
        }
    }

    private fun titleStack(spec: KoraWindowSpec): VBox =
        VBox(1.0).apply {
            styleClass += "kora-window-title-stack"
            children += Label(spec.titleBar.title ?: spec.title).apply {
                styleClass += "kora-window-title"
            }
            spec.titleBar.subtitle?.let { subtitle ->
                children += Label(subtitle).apply {
                    styleClass += "kora-window-subtitle"
                }
            }
        }

    private fun windowControls(
        stage: Stage,
        spec: KoraWindowSpec,
        side: KoraWindowControlSide,
    ): HBox {
        val titleBar = spec.titleBar
        val minimize =
            if (titleBar.showMinimize) {
                windowButton("_", "kora-window-minimize-button") {
                    stage.isIconified = true
                }
            } else {
                null
            }
        val maximize =
            if (titleBar.showMaximize) {
                windowButton("[ ]", "kora-window-maximize-button") {
                    if (spec.resizable) {
                        stage.isMaximized = !stage.isMaximized
                    }
                }.apply {
                    isDisable = !spec.resizable
                }
            } else {
                null
            }
        val close =
            if (titleBar.showClose) {
                windowButton("X", "kora-window-close-button") {
                    stage.close()
                }
            } else {
                null
            }
        val controls =
            if (side == KoraWindowControlSide.LEFT) {
                listOfNotNull(close, minimize, maximize)
            } else {
                listOfNotNull(minimize, maximize, close)
            }

        return HBox(4.0).apply {
            styleClass += "kora-window-controls"
            styleClass += "kora-window-controls-${side.name.lowercase()}"
            alignment = Pos.CENTER
            children += controls
        }
    }

    private fun windowButton(
        text: String,
        styleClass: String,
        action: () -> Unit,
    ): Button =
        Button(text).apply {
            this.styleClass += "kora-window-button"
            this.styleClass += styleClass
            isFocusTraversable = false
            setOnAction {
                action()
            }
        }

    private fun installDragToMove(
        titleBar: HBox,
        stage: Stage,
        spec: KoraWindowSpec,
    ) {
        if (!spec.titleBar.dragToMove) {
            return
        }

        var offsetX = 0.0
        var offsetY = 0.0
        var dragging = false
        var previousOpacity = stage.opacity

        titleBar.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
            if (event.button != MouseButton.PRIMARY || event.target.isInteractiveTarget()) {
                return@addEventFilter
            }
            offsetX = event.screenX - stage.x
            offsetY = event.screenY - stage.y
            previousOpacity = stage.opacity
        }
        titleBar.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (
                event.button == MouseButton.PRIMARY &&
                event.clickCount == 2 &&
                spec.resizable &&
                spec.titleBar.doubleClickMaximize &&
                !event.target.isInteractiveTarget()
            ) {
                stage.isMaximized = !stage.isMaximized
                event.consume()
            }
        }
        titleBar.addEventFilter(MouseEvent.MOUSE_DRAGGED) { event ->
            if (!event.isPrimaryButtonDown || stage.isMaximized || event.target.isInteractiveTarget()) {
                return@addEventFilter
            }
            if (!dragging) {
                dragging = true
                if (spec.titleBar.dragOpacity < 1.0) {
                    stage.opacity = spec.titleBar.dragOpacity
                }
            }
            stage.x = event.screenX - offsetX
            stage.y = event.screenY - offsetY
            event.consume()
        }
        titleBar.addEventFilter(MouseEvent.MOUSE_RELEASED) {
            if (dragging) {
                stage.opacity = previousOpacity
                dragging = false
            }
        }
    }

    private fun installResize(
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

    private fun KoraWindowControlSide.resolve(): KoraWindowControlSide =
        when (this) {
            KoraWindowControlSide.AUTO ->
                if (System.getProperty("os.name").contains("mac", ignoreCase = true)) {
                    KoraWindowControlSide.LEFT
                } else {
                    KoraWindowControlSide.RIGHT
                }
            else -> this
        }

    private fun Any?.isInteractiveTarget(): Boolean {
        var node = this as? Node ?: return false
        while (true) {
            when (node) {
                is ButtonBase,
                is TextInputControl,
                is ComboBoxBase<*>,
                is ChoiceBox<*>,
                is Slider,
                -> return true
                else -> Unit
            }
            node = node.parent ?: return false
        }
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
