package dev.korafx.devtools

import dev.korafx.dsl.popup
import dev.korafx.dsl.rectangle
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.stage.Stage

internal class NodeHighlighter(
    private val owner: Stage,
) {
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

    fun show(node: Node?) {
        if (node == null || node.scene == null || !node.isVisible) {
            hide()
            return
        }

        val bounds = node.localToScreen(node.boundsInLocal)
        if (bounds == null || bounds.width <= 0.0 || bounds.height <= 0.0) {
            hide()
            return
        }

        rectangle.width = bounds.width
        rectangle.height = bounds.height

        if (!popup.isShowing) {
            popup.show(owner, bounds.minX, bounds.minY)
        } else {
            popup.x = bounds.minX
            popup.y = bounds.minY
        }
    }

    fun hide() {
        popup.hide()
    }
}
