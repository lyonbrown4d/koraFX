package dev.korafx.devtools

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

internal class InProcessInspector(
    private val scene: Scene,
    private val selection: DevtoolsSelectionModel,
    private val highlighter: NodeHighlighter,
    private val inspectedRoot: () -> Parent,
    private val excludedRoots: () -> Collection<Node>,
    private val highlightSelection: Boolean,
) {
    private var picking = false
    private var hoveredNode: Node? = null

    private val pickMoveHandler = EventHandler<MouseEvent> { event ->
        if (picking) {
            highlight(hitTestScene(event.sceneX, event.sceneY))
        }
    }

    private val pickExitHandler = EventHandler<MouseEvent> { _ ->
        if (picking) {
            highlight(null)
        }
    }

    private val pickPressHandler = EventHandler<MouseEvent> { event ->
        if (!picking) {
            return@EventHandler
        }

        if (event.button != MouseButton.PRIMARY) {
            return@EventHandler
        }

        val node = hitTestScene(event.sceneX, event.sceneY) ?: return@EventHandler
        event.consume()
        selection.select(node)
        stopPicking()
    }

    fun startPicking() {
        if (picking) {
            return
        }

        picking = true
        hoveredNode = null
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, pickMoveHandler)
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, pickMoveHandler)
        scene.addEventFilter(MouseEvent.MOUSE_EXITED, pickExitHandler)
        scene.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, pickExitHandler)
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, pickPressHandler)
    }

    fun stopPicking() {
        if (!picking) {
            return
        }

        picking = false
        hoveredNode = null
        scene.removeEventFilter(MouseEvent.MOUSE_MOVED, pickMoveHandler)
        scene.removeEventFilter(MouseEvent.MOUSE_DRAGGED, pickMoveHandler)
        scene.removeEventFilter(MouseEvent.MOUSE_EXITED, pickExitHandler)
        scene.removeEventFilter(MouseEvent.MOUSE_EXITED_TARGET, pickExitHandler)
        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, pickPressHandler)
    }

    fun highlight(node: Node?) {
        if (node === hoveredNode) {
            return
        }

        hoveredNode = node
        if (highlightSelection && node != null && !node.isExcluded()) {
            highlighter.show(node)
        } else {
            highlighter.hide()
        }
    }

    fun hideHighlight() {
        hoveredNode = null
        highlighter.hide()
    }

    private fun hitTestScene(
        sceneX: Double,
        sceneY: Double,
    ): Node? =
        NodeHitTester.findDeepestAt(
            root = inspectedRoot(),
            sceneX = sceneX,
            sceneY = sceneY,
            excludedRoots = excludedRoots(),
        )

    private fun Node.isExcluded(): Boolean =
        excludedRoots().any { excluded -> isDescendantOf(excluded) }
}
