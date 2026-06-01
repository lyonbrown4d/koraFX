package dev.korafx.devtools

import dev.korafx.dsl.borderPane
import dev.korafx.dsl.checkBox
import dev.korafx.dsl.onAction
import dev.korafx.dsl.textArea
import dev.korafx.framework.KoraApplication
import dev.korafx.navigation.RouteRenderMetricsSnapshot
import dev.korafx.navigation.routeRenderMetricsBus
import dev.korafx.components.setKoraIcon
import javafx.scene.Node
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import dev.korafx.dsl.intSpinner
import dev.korafx.dsl.label
import java.util.Locale
import kotlin.math.max

internal fun createDevtoolsPerformancePanel(
    app: KoraApplication,
    messages: DevtoolsMessages,
    jobSink: (Job) -> Unit,
): Node {
    val autoRefreshState = booleanArrayOf(true)
    val refreshIntervalMs = intArrayOf(500)
    val topRoutesLimit = intArrayOf(12)
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

        details.text = snapshot.renderReport(topRoutesLimit[0], messages.topRoutes)
    }

    fun refresh() {
        render(routeRenderMetricsBus.snapshot())
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
                spacer()
                button(messages.refresh) {
                    setKoraIcon(BootstrapIcons.ARROW_CLOCKWISE)
                    onAction {
                        refresh()
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

private fun RouteRenderMetricsSnapshot.renderReport(topRoutes: Int, topRoutesLabel: String): String =
    buildString {
        appendLine("Total render count = $totalRenderCount")
        appendLine("Cache hits = $cacheHitCount")
        appendLine("Cache misses = $cacheMissCount")
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
        appendLine("$topRoutesLabel (${
            routeSummaries
                .size
        })")
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
    }

private fun Double.formatMs(): String =
    String.format(Locale.US, "%.2f", this)
