package dev.korafx.devtools

import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.SubScene

internal object NodeHitTester {
    fun findDeepestAtScreen(
        root: Node,
        screenX: Double,
        screenY: Double,
        excludedRoots: Collection<Node> = emptyList(),
    ): Node? =
        findDeepestAtPoint(
            root = root,
            excludedRoots = excludedRoots,
            pointInRoot = runCatching { root.screenToLocal(Point2D(screenX, screenY)) }.getOrNull(),
        ) { node ->
            runCatching { node.screenToLocal(Point2D(screenX, screenY)) }.getOrNull()
        }

    fun findDeepestAt(
        root: Node,
        sceneX: Double,
        sceneY: Double,
        excludedRoots: Collection<Node> = emptyList(),
    ): Node? =
        findDeepestAtPoint(
            root = root,
            excludedRoots = excludedRoots,
            pointInRoot = runCatching { root.sceneToLocal(Point2D(sceneX, sceneY)) }.getOrNull(),
        ) { node ->
            runCatching { node.sceneToLocal(Point2D(sceneX, sceneY)) }.getOrNull()
        }

    private fun findDeepestAtPoint(
        root: Node,
        pointInRoot: Point2D?,
        excludedRoots: Collection<Node>,
        toPoint: (Node) -> Point2D?,
    ): Node? {
        if (!root.isVisible || root.isMouseTransparent) {
            return null
        }

        if (pointInRoot == null || root.isExcludedBy(excludedRoots)) {
            return null
        }

        if (!root.contains(pointInRoot)) {
            return null
        }

        val children = root.inspectableChildren()
        for (index in children.size - 1 downTo 0) {
            val child = children[index]
            findDeepestAtPoint(child, toPoint(child), excludedRoots, toPoint)?.let { return it }
        }

        return root
    }

    private fun Node.inspectableChildren(): List<Node> =
        when (this) {
            is Parent -> childrenUnmodifiable
            is SubScene -> listOf(root)
            else -> emptyList()
        }

    private fun Node.isExcludedBy(excludedRoots: Collection<Node>): Boolean =
        excludedRoots.any { excluded -> isDescendantOf(excluded) }
}

internal fun Node?.isDescendantOf(root: Node): Boolean {
    var current = this
    while (current != null) {
        if (current === root) {
            return true
        }
        current = current.parent
    }
    return false
}
