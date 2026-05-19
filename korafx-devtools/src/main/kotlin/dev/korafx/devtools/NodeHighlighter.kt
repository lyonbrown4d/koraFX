package dev.korafx.devtools

import dev.korafx.dsl.popup
import dev.korafx.dsl.rectangle
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage

internal class NodeHighlighter(
    private val owner: Stage,
) {
    private var boundsLimit: Node? = null
    private var overlayHost: Pane? = null
    private val inlineRectangle = rectangle {
        fill = Color.rgb(14, 165, 233, 0.12)
        stroke = Color.rgb(14, 165, 233, 0.92)
        strokeWidth = 2.0
        isMouseTransparent = true
        isManaged = false
        isVisible = false
    }
    private val rectangle = rectangle {
        fill = Color.rgb(14, 165, 233, 0.12)
        stroke = Color.rgb(14, 165, 233, 0.92)
        strokeWidth = 2.0
        isMouseTransparent = true
    }
    private val popup = popup(
        autoFix = false,
        autoHide = false,
        hideOnEscape = false,
    ) {
        add(rectangle)
    }

    fun limitTo(node: Node?) {
        boundsLimit = node
    }

    fun renderIn(host: Pane?) {
        overlayHost?.children?.remove(inlineRectangle)
        overlayHost = host
        if (host != null && inlineRectangle !in host.children) {
            host.children += inlineRectangle
        }
        hide()
    }

    fun show(node: Node?) {
        if (node == null || node.scene == null || !node.isVisible) {
            hide()
            return
        }

        val host = overlayHost
        if (host != null) {
            showInline(host, node)
            return
        }

        val bounds = node.localToScreen(node.boundsInLocal)
        if (bounds == null || bounds.width <= 0.0 || bounds.height <= 0.0) {
            hide()
            return
        }

        val limitNode = boundsLimit
        val limit = limitNode?.localToScreen(limitNode.boundsInLocal)
        val minX = maxOf(bounds.minX, limit?.minX ?: bounds.minX)
        val minY = maxOf(bounds.minY, limit?.minY ?: bounds.minY)
        val maxX = minOf(bounds.maxX, limit?.maxX ?: bounds.maxX)
        val maxY = minOf(bounds.maxY, limit?.maxY ?: bounds.maxY)
        val width = maxX - minX
        val height = maxY - minY
        if (width <= 0.0 || height <= 0.0) {
            hide()
            return
        }

        rectangle.width = width
        rectangle.height = height

        if (!popup.isShowing) {
            popup.show(owner, minX, minY)
        } else {
            popup.x = minX
            popup.y = minY
        }
    }

    fun hide() {
        popup.hide()
        inlineRectangle.isVisible = false
    }

    private fun showInline(
        host: Pane,
        node: Node,
    ) {
        val bounds = host.sceneToLocal(node.localToScene(node.boundsInLocal))
        val minX = maxOf(bounds.minX, 0.0)
        val minY = maxOf(bounds.minY, 0.0)
        val maxX = minOf(bounds.maxX, host.width)
        val maxY = minOf(bounds.maxY, host.height)
        val width = maxX - minX
        val height = maxY - minY
        if (width <= 0.0 || height <= 0.0) {
            hide()
            return
        }

        popup.hide()
        inlineRectangle.x = minX
        inlineRectangle.y = minY
        inlineRectangle.width = width
        inlineRectangle.height = height
        inlineRectangle.isVisible = true
    }
}
