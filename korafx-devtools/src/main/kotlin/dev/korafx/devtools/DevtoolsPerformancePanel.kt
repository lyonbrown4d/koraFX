package dev.korafx.devtools

import dev.korafx.dsl.borderPane
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
import java.util.Locale

internal fun createDevtoolsPerformancePanel(
    app: KoraApplication,
    messages: DevtoolsMessages,
    jobSink: (Job) -> Unit,
): Node {
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

        details.text = snapshot.renderReport()
    }

    jobSink(
        app.uiScope.launch {
            while (isActive) {
                render(routeRenderMetricsBus.snapshot())
                delay(500)
            }
        },
    )
    render(routeRenderMetricsBus.snapshot())

    return borderPane {
        top {
            devtoolsToolbar(messages.performance) {
                button(messages.refresh) {
                    setKoraIcon(BootstrapIcons.ARROW_CLOCKWISE)
                    onAction {
                        render(routeRenderMetricsBus.snapshot())
                    }
                }
                button(messages.clear) {
                    setKoraIcon(BootstrapIcons.X_CIRCLE)
                    onAction {
                        routeRenderMetricsBus.reset()
                        render(routeRenderMetricsBus.snapshot())
                    }
                }
            }
        }
        center(details)
    }
}

private fun RouteRenderMetricsSnapshot.renderReport(): String =
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
        appendLine("Top routes")
        routeSummaries
            .take(12)
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
