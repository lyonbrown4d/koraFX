package dev.korafx.framework

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.stage.Stage

internal fun createWindowControls(
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

internal fun KoraWindowControlSide.resolveWindowControlSide(): KoraWindowControlSide =
    when (this) {
        KoraWindowControlSide.AUTO ->
            if (System.getProperty("os.name").contains("mac", ignoreCase = true)) {
                KoraWindowControlSide.LEFT
            } else {
                KoraWindowControlSide.RIGHT
            }
        else -> this
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
