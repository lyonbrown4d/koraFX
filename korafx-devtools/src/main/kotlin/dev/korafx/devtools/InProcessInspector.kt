package dev.korafx.devtools

import javafx.animation.AnimationTimer
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.scene.robot.Robot

internal class InProcessInspector(
    private val scene: Scene,
    private val selection: DevtoolsSelectionModel,
    private val highlighter: NodeHighlighter,
    private val inspectedRoot: () -> Parent,
    private val excludedRoots: () -> Collection<Node>,
    private val highlightSelection: Boolean,
) {
    private var picking = false
    private var pickTimer: AnimationTimer? = null
    private var robot: Robot? = null
    private var hoveredNode: Node? = null

    private val pickMoveHandler = EventHandler<MouseEvent> { event ->
        if (picking) {
            highlight(hitTest(event.screenX, event.screenY))
        }
    }
    private val pickPressHandler = EventHandler<MouseEvent> { event ->
        if (picking) {
            val node = hitTest(event.screenX, event.screenY) ?: return@EventHandler
            event.consume()
            selection.select(node)
            stopPicking()
        }
    }

    fun startPicking() {
        if (picking) {
            return
        }

        picking = true
        robot = Robot()
        pickTimer = createPickTimer().also(AnimationTimer::start)
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, pickMoveHandler)
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, pickPressHandler)
    }

    fun stopPicking() {
        if (!picking) {
            return
        }

        picking = false
        pickTimer?.stop()
        pickTimer = null
        robot = null
        hoveredNode = null
        scene.removeEventFilter(MouseEvent.MOUSE_MOVED, pickMoveHandler)
        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, pickPressHandler)
    }

    fun highlight(node: Node?) {
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

    private fun createPickTimer(): AnimationTimer =
        object : AnimationTimer() {
            override fun handle(now: Long) {
                val pointer = robot ?: return
                val node = hitTest(pointer.mouseX, pointer.mouseY)
                if (node !== hoveredNode) {
                    highlight(node)
                }
            }
        }

    private fun hitTest(
        screenX: Double,
        screenY: Double,
    ): Node? =
        NodeHitTester.findDeepestAt(
            root = inspectedRoot(),
            screenX = screenX,
            screenY = screenY,
            excludedRoots = excludedRoots(),
        )

    private fun Node.isExcluded(): Boolean =
        excludedRoots().any { excluded -> isDescendantOf(excluded) }
}
