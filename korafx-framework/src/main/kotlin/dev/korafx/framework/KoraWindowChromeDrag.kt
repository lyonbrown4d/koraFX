package dev.korafx.framework

import javafx.scene.Node
import javafx.scene.control.ButtonBase
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.Slider
import javafx.scene.control.TextInputControl
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.stage.Stage

internal fun installWindowDragToMove(
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
