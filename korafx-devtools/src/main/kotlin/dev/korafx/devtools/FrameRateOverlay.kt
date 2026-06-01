package dev.korafx.devtools

import javafx.animation.AnimationTimer
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import java.util.Locale
import kotlin.math.roundToInt

internal data class FrameRateSnapshot(
    val currentFps: Double,
    val averageFrameMillis: Double,
    val sampleCount: Int,
)

internal object FrameRateSnapshotBus {
    @Volatile
    private var latest: FrameRateSnapshot? = null

    fun record(snapshot: FrameRateSnapshot) {
        latest = snapshot
    }

    fun snapshot(): FrameRateSnapshot? = latest

    fun reset() {
        latest = null
    }
}

internal class FpsMeter(
    private val sampleWindow: Int = DefaultSampleWindow,
) {
    private val frameDurations = ArrayDeque<Double>()
    private var previousFrameNanos: Long? = null

    fun recordFrame(nowNanos: Long): FrameRateSnapshot? {
        val previous = previousFrameNanos
        if (previous != null && nowNanos <= previous) {
            return snapshotOrNull()
        }

        previousFrameNanos = nowNanos
        if (previous == null) {
            return snapshotOrNull()
        }

        val frameMillis = (nowNanos - previous) / NanosPerMillisecond
        frameDurations += frameMillis
        while (frameDurations.size > sampleWindow.coerceAtLeast(1)) {
            frameDurations.removeFirst()
        }

        return snapshotOrNull()
    }

    fun reset() {
        previousFrameNanos = null
        frameDurations.clear()
    }

    private fun snapshotOrNull(): FrameRateSnapshot? {
        if (frameDurations.isEmpty()) {
            return null
        }

        val latestFrameMillis = frameDurations.last()
        val averageFrameMillis = frameDurations.average()
        return FrameRateSnapshot(
            currentFps = MillisPerSecond / latestFrameMillis,
            averageFrameMillis = averageFrameMillis,
            sampleCount = frameDurations.size,
        )
    }

    private companion object {
        const val DefaultSampleWindow = 60
        const val MillisPerSecond = 1_000.0
        const val NanosPerMillisecond = 1_000_000.0
    }
}

internal class FrameRateOverlay(
    private val meter: FpsMeter = FpsMeter(),
) {
    private val fpsLabel = Label("-- FPS").apply {
        styleClass += "korafx-devtools-fps-overlay-fps"
    }
    private val frameTimeLabel = Label("avg -- ms").apply {
        styleClass += "korafx-devtools-fps-overlay-frame-time"
    }
    val node: VBox = VBox(1.0, fpsLabel, frameTimeLabel).apply {
        styleClass += "korafx-devtools-fps-overlay"
        isMouseTransparent = true
        isManaged = false
        padding = Insets(7.0, 9.0, 7.0, 9.0)
        alignment = Pos.CENTER_LEFT
        style = """
            -fx-background-color: rgba(15, 23, 42, 0.86);
            -fx-background-radius: 10;
            -fx-border-color: rgba(148, 163, 184, 0.36);
            -fx-border-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.22), 12, 0.2, 0, 3);
        """.trimIndent()
        fpsLabel.style = "-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: white;"
        frameTimeLabel.style = "-fx-font-size: 10px; -fx-text-fill: rgba(226, 232, 240, 0.84);"
    }
    private var lastRenderNanos: Long = 0
    private val timer = object : AnimationTimer() {
        override fun handle(now: Long) {
            val snapshot = meter.recordFrame(now) ?: return
            if (now - lastRenderNanos >= RenderIntervalNanos) {
                render(snapshot)
                lastRenderNanos = now
            }
        }
    }

    fun start() {
        meter.reset()
        lastRenderNanos = 0
        timer.start()
    }

    fun stop() {
        timer.stop()
        meter.reset()
        FrameRateSnapshotBus.reset()
    }

    fun render(snapshot: FrameRateSnapshot) {
        FrameRateSnapshotBus.record(snapshot)
        fpsLabel.text = "${snapshot.currentFps.roundToInt()} FPS"
        frameTimeLabel.text = "avg ${String.format(Locale.US, "%.1f", snapshot.averageFrameMillis)} ms"
    }

    private companion object {
        const val RenderIntervalNanos = 250_000_000L
    }
}

internal fun StackPane.attachFrameRateOverlay(overlay: FrameRateOverlay) {
    val node = overlay.node
    node.layoutX = 0.0
    node.layoutY = 0.0
    StackPane.setAlignment(node, Pos.TOP_RIGHT)
    StackPane.setMargin(node, Insets(12.0))
    children += node
    overlay.start()
}
