package dev.korafx.framework

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.stage.Stage

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
            installWindowResize(frame, stage, spec)
        }

        return frame
    }

    private fun titleBar(
        app: KoraApplication,
        stage: Stage,
        spec: KoraWindowSpec,
    ): HBox {
        val titleBarSpec = spec.titleBar
        val side = titleBarSpec.controlSide.resolveWindowControlSide()
        val controls = createWindowControls(stage, spec, side)
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

            installWindowDragToMove(this, stage, spec)
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
}
