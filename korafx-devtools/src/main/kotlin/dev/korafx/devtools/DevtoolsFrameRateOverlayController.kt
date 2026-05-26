package dev.korafx.devtools

import javafx.scene.layout.StackPane

internal class DevtoolsFrameRateOverlayController(
    private val spec: KoraDevtoolsSpec,
) {
    private var overlay: FrameRateOverlay? = null

    fun install(host: StackPane) {
        if (!spec.showFpsOverlay || overlay != null) {
            return
        }

        FrameRateOverlay().also { frameRateOverlay ->
            overlay = frameRateOverlay
            host.attachFrameRateOverlay(frameRateOverlay)
        }
    }

    fun uninstall(host: StackPane?) {
        val frameRateOverlay = overlay ?: return
        frameRateOverlay.stop()
        host?.children?.remove(frameRateOverlay.node)
        overlay = null
    }
}
