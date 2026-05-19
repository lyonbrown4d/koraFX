package dev.korafx.devtools

import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.SubScene

internal object NodeHitTester {
    fun findDeepestAt(
        root: Node,
        screenX: Double,
        screenY: Double,
        excludedRoots: Collection<Node> = emptyList(),
    ): Node? {
        if (root.isExcludedBy(excludedRoots)) {
            return null
        }

        if (!root.isVisible || root.isMouseTransparent) {
            return null
        }

        val localPoint = runCatching {
            root.screenToLocal(Point2D(screenX, screenY))
        }.getOrNull() ?: return null
        if (!root.contains(localPoint)) {
            return null
        }

        val children = root.inspectableChildren()
        for (index in children.size - 1 downTo 0) {
            val child = children[index]
            findDeepestAt(child, screenX, screenY, excludedRoots)?.let { return it }
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
