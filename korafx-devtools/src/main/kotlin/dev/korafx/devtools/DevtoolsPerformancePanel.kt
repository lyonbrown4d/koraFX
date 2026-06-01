package dev.korafx.devtools

import dev.korafx.components.setKoraIcon
import dev.korafx.dsl.borderPane
import dev.korafx.dsl.checkBox
import dev.korafx.dsl.intSpinner
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.textArea
import dev.korafx.framework.KoraApplication
import dev.korafx.navigation.RouteRenderMetricsSnapshot
import dev.korafx.navigation.RouteRenderHistorySample
import dev.korafx.navigation.routeRenderMetricsBus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javafx.scene.Node
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons

private val timeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

internal fun createDevtoolsPerformancePanel(
    app: KoraApplication,
    messages: DevtoolsMessages,
    jobSink: (Job) -> Unit,
): Node {
    val autoRefreshState = booleanArrayOf(true)
    val refreshIntervalMs = intArrayOf(500)
    val topRoutesLimit = intArrayOf(12)
    val recentSamplesLimit = intArrayOf(20)
    val details = textArea {
        isEditable = false
        isWrapText = false
        prefRowCount = 20
    }

    fun render(snapshot: RouteRenderMetricsSnapshot?) {
        if (snapshot == null || !snapshot.hasData) {
            details.text = messages.noPerformanceData
            return
        }

        details.text =
            snapshot.renderReport(
                frameRateLabel = messages.frameRate,
                avgFrameTimeLabel = messages.avgFrameTime,
                topRoutes = topRoutesLimit[0],
                topRoutesLabel = messages.topRoutes,
                recentSamplesLimit = recentSamplesLimit[0],
                recentSamplesLabel = messages.recentSamples,
            )
    }

    fun refresh() {
        render(routeRenderMetricsBus.snapshot())
    }

    fun copyReport() {
        val text = details.text
        if (text.isBlank()) {
            return
        }
        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent().apply {
            putString(text)
        }
        clipboard.setContent(content)
    }

    val autoRefresh = checkBox(messages.autoRefresh) {
        isSelected = true
        selectedProperty().addListener { _, _, selected ->
            autoRefreshState[0] = selected
            if (!selected) {
                refresh()
            }
        }
    }

    val refreshInterval = intSpinner(
        min = 100,
        max = 5000,
        initialValue = refreshIntervalMs[0],
        amountToStepBy = 100,
    ) {
        isEditable = true
        prefWidth = 72.0
        valueProperty().addListener { _, _, value ->
            val nextValue = value?.let { max(100, it) } ?: refreshIntervalMs[0]
            refreshIntervalMs[0] = nextValue
            refresh()
        }
    }

    val topRoutes = intSpinner(
        min = 3,
        max = 200,
        initialValue = topRoutesLimit[0],
        amountToStepBy = 1,
    ) {
        isEditable = true
        prefWidth = 56.0
        valueProperty().addListener { _, _, value ->
            val nextValue = value?.coerceAtLeast(1) ?: topRoutesLimit[0]
            topRoutesLimit[0] = nextValue
            refresh()
        }
    }

    val recentSamples = intSpinner(
        min = 5,
        max = 120,
        initialValue = recentSamplesLimit[0],
        amountToStepBy = 1,
    ) {
        isEditable = true
        prefWidth = 56.0
        valueProperty().addListener { _, _, value ->
            val nextValue = value?.coerceIn(5, 120) ?: recentSamplesLimit[0]
            recentSamplesLimit[0] = nextValue
            refresh()
        }
    }

    jobSink(
        app.uiScope.launch {
            while (isActive) {
                if (autoRefreshState[0]) {
                    render(routeRenderMetricsBus.snapshot())
                }
                delay(refreshIntervalMs[0].toLong())
            }
        },
    )
    refresh()

    return borderPane {
        top {
            devtoolsToolbar(messages.performance) {
                add(autoRefresh)
                add(label(messages.refreshIntervalMs))
                add(refreshInterval)
                add(label(messages.topRoutes))
                add(topRoutes)
                add(label(messages.historyLimit))
                add(recentSamples)
                spacer()
                button(messages.refresh) {
                    setKoraIcon(BootstrapIcons.ARROW_CLOCKWISE)
                    onAction {
                        refresh()
                    }
                }
                button(messages.copy) {
                    onAction {
                        copyReport()
                    }
                }
                button(messages.clear) {
                    setKoraIcon(BootstrapIcons.X_CIRCLE)
                    onAction {
                        routeRenderMetricsBus.reset()
                        refresh()
                    }
                }
            }
        }
        center(details)
    }
}

private fun RouteRenderMetricsSnapshot.renderReport(
    frameRateLabel: String,
    avgFrameTimeLabel: String,
    topRoutes: Int,
    topRoutesLabel: String,
    recentSamplesLimit: Int,
    recentSamplesLabel: String,
): String =
    buildString {
        val frameRate = FrameRateSnapshotBus.snapshot()
        if (frameRate != null) {
            appendLine("$frameRateLabel = ${frameRate.currentFps.formatFps()} (samples: ${frameRate.sampleCount})")
            appendLine("$avgFrameTimeLabel = ${frameRate.averageFrameMillis.formatMs()} ms")
            appendLine()
        }
        appendLine("Total render count = $totalRenderCount")
        appendLine("Cache hits = $cacheHitCount")
        appendLine("Cache misses = $cacheMissCount")
        appendLine("Cache hit ratio = ${(if (totalRenderCount == 0L) 0.0 else cacheHitCount.toDouble() / totalRenderCount.toDouble() * 100).formatPercent()}")
        appendLine("Page created = $totalPageCreated")
        appendLine("Page reused = $totalPageReused")
        appendLine("Layout created = $totalLayoutCreated")
        appendLine("Layout reused = $totalLayoutReused")
        appendLine("Average render = ${averageRenderMs.formatMs()} ms")
        appendLine("Max render = ${maxRenderMs.formatMs()} ms")
        appendLine("Last render = ${lastRenderMs.formatMs()} ms")
        appendLine("Last route = ${lastRouteTitle ?: "-"}")
        appendLine("Last route id = ${lastRouteId ?: "-"}")
        appendLine()
        appendLine("$topRoutesLabel (${routeSummaries.size})")
        routeSummaries
            .take(topRoutes.coerceAtLeast(1))
            .forEachIndexed { index, summary ->
                appendLine("${index + 1}. ${summary.routeTitle} (${summary.routePath})")
                appendLine("    id = ${summary.routeId}")
                appendLine("    renderCount = ${summary.renderCount}")
                appendLine("    pageCreated = ${summary.pageCreated}")
                appendLine("    pageReused = ${summary.pageReused}")
                appendLine("    layoutCreated = ${summary.layoutCreated}")
                appendLine("    layoutReused = ${summary.layoutReused}")
                appendLine("    avgRenderMs = ${summary.averageRenderMs.formatMs()}")
            }
        appendLine()
        appendLine("$recentSamplesLabel (${recentSamplesLimit})")
        recentSamples
            .takeLast(recentSamplesLimit.coerceAtLeast(1))
            .asReversed()
            .forEachIndexed { index, sample ->
                appendLine("${index + 1}. ${sample.formattedTime()}: ${sample.renderMs.formatMs()} ms")
                appendLine("    route = ${sample.routeTitle} (${sample.routePath})")
                appendLine("    id = ${sample.routeId}")
                appendLine("    host = ${sample.hostType}, policy = ${sample.pageInstancePolicy}")
                appendLine("    pageCreated = ${sample.pageCreated}, pageReused = ${sample.pageReused}, layoutCreated = ${sample.layoutCreated}, layoutReused = ${sample.layoutReused}")
            }
    }

private fun Double.formatMs(): String =
    String.format(Locale.US, "%.2f", this)

private fun Double.formatFps(): String =
    String.format(Locale.US, "%.1f", this)

private fun Double.formatPercent(): String =
    String.format(Locale.US, "%.2f%%", this)

private fun RouteRenderHistorySample.formattedTime(): String =
    timeFormatter.format(
        Instant
            .ofEpochMilli(timestampEpochMillis)
            .atZone(ZoneId.systemDefault())
    )
