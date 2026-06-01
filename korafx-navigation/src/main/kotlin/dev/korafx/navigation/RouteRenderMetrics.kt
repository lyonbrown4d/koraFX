package dev.korafx.navigation

import java.util.LinkedHashMap
import kotlin.math.max

data class RouteRenderEvent(
    val routeId: String,
    val routeTitle: String,
    val routePath: String,
    val hostType: RouteHostType,
    val pageInstancePolicy: PageInstancePolicy,
    val renderDurationNanos: Long,
    val pageCreated: Long,
    val pageReused: Long,
    val layoutCreated: Long,
    val layoutReused: Long,
    val createdPageKeys: List<String> = emptyList(),
)

data class RouteRenderRouteSummary(
    val routeId: String,
    val routeTitle: String,
    val routePath: String,
    val renderCount: Long,
    val pageCreated: Long,
    val pageReused: Long,
    val layoutCreated: Long,
    val layoutReused: Long,
    val totalRenderNanos: Long,
    val averageRenderMs: Double,
)

data class RouteRenderMetricsSnapshot(
    val totalRenderCount: Long,
    val cacheHitCount: Long,
    val cacheMissCount: Long,
    val totalPageCreated: Long,
    val totalPageReused: Long,
    val totalLayoutCreated: Long,
    val totalLayoutReused: Long,
    val averageRenderMs: Double,
    val maxRenderMs: Double,
    val lastRenderMs: Double,
    val lastRouteId: String?,
    val lastRouteTitle: String?,
    val routeSummaries: List<RouteRenderRouteSummary>,
) {
    val hasData: Boolean
        get() = totalRenderCount > 0
}

enum class RouteHostType {
    SIMPLE_ROUTE_HOST,
    ROUTER_HOST,
}

interface RouteRenderMetrics {
    fun record(event: RouteRenderEvent)
    fun snapshot(): RouteRenderMetricsSnapshot
    fun reset()
}

class RouteRenderMetricsCollector : RouteRenderMetrics {
    private var totalRenderCount = 0L
    private var cacheHitCount = 0L
    private var cacheMissCount = 0L
    private var totalPageCreated = 0L
    private var totalPageReused = 0L
    private var totalLayoutCreated = 0L
    private var totalLayoutReused = 0L
    private var totalRenderNanos = 0L
    private var maxRenderNanos = 0L
    private var lastRenderNanos = 0L
    private var lastRouteId: String? = null
    private var lastRouteTitle: String? = null
    private val perRoute = LinkedHashMap<String, RouteRenderRouteAccumulator>()

    override fun record(event: RouteRenderEvent) {
        synchronized(this) {
            totalRenderCount++
            cacheHitCount += event.pageReused
            cacheMissCount += event.pageCreated
            totalPageCreated += event.pageCreated
            totalPageReused += event.pageReused
            totalLayoutCreated += event.layoutCreated
            totalLayoutReused += event.layoutReused
            totalRenderNanos += event.renderDurationNanos
            maxRenderNanos = max(maxRenderNanos, event.renderDurationNanos)
            lastRenderNanos = event.renderDurationNanos
            lastRouteId = event.routeId
            lastRouteTitle = event.routeTitle

            val route = perRoute.getOrPut(event.routeId) {
                RouteRenderRouteAccumulator(
                    routeId = event.routeId,
                    routeTitle = event.routeTitle,
                    routePath = event.routePath,
                )
            }
            route.record(event)
        }
    }

    override fun snapshot(): RouteRenderMetricsSnapshot =
        synchronized(this) {
            val totalRenders = max(totalRenderCount, 1)
            val snapshots =
                perRoute.values
                    .map { it.snapshot() }
                    .sortedByDescending { it.renderCount }
            RouteRenderMetricsSnapshot(
                totalRenderCount = totalRenderCount,
                cacheHitCount = cacheHitCount,
                cacheMissCount = cacheMissCount,
                totalPageCreated = totalPageCreated,
                totalPageReused = totalPageReused,
                totalLayoutCreated = totalLayoutCreated,
                totalLayoutReused = totalLayoutReused,
                averageRenderMs = totalRenderNanos.toDouble() / totalRenders / NanosPerMillisecond,
                maxRenderMs = maxRenderNanos.toDouble() / NanosPerMillisecond,
                lastRenderMs = lastRenderNanos.toDouble() / NanosPerMillisecond,
                lastRouteId = lastRouteId,
                lastRouteTitle = lastRouteTitle,
                routeSummaries = snapshots,
            )
        }

    override fun reset() {
        synchronized(this) {
            totalRenderCount = 0L
            cacheHitCount = 0L
            cacheMissCount = 0L
            totalPageCreated = 0L
            totalPageReused = 0L
            totalLayoutCreated = 0L
            totalLayoutReused = 0L
            totalRenderNanos = 0L
            maxRenderNanos = 0L
            lastRenderNanos = 0L
            lastRouteId = null
            lastRouteTitle = null
            perRoute.clear()
        }
    }

    private companion object {
        const val NanosPerMillisecond = 1_000_000.0
    }
}

class RouteRenderMetricsBus(
    initialMetrics: RouteRenderMetrics? = null,
) {
    @Volatile
    private var collector: RouteRenderMetrics? = initialMetrics

    fun install(metrics: RouteRenderMetrics?) {
        collector = metrics
    }

    fun record(event: RouteRenderEvent) {
        collector?.record(event)
    }

    fun snapshot(): RouteRenderMetricsSnapshot? = collector?.snapshot()

    fun isEnabled(): Boolean = collector != null

    fun reset() {
        collector?.reset()
    }
}

val routeRenderMetricsBus = RouteRenderMetricsBus()

private data class RouteRenderRouteAccumulator(
    val routeId: String,
    val routeTitle: String,
    val routePath: String,
) {
    private var renderCount = 0L
    private var pageCreated = 0L
    private var pageReused = 0L
    private var layoutCreated = 0L
    private var layoutReused = 0L
    private var totalRenderNanos = 0L

    fun record(event: RouteRenderEvent) {
        renderCount++
        pageCreated += event.pageCreated
        pageReused += event.pageReused
        layoutCreated += event.layoutCreated
        layoutReused += event.layoutReused
        totalRenderNanos += event.renderDurationNanos
    }

    fun snapshot(): RouteRenderRouteSummary {
        val divisor = renderCount.coerceAtLeast(1).toDouble()
        return RouteRenderRouteSummary(
            routeId = routeId,
            routeTitle = routeTitle,
            routePath = routePath,
            renderCount = renderCount,
            pageCreated = pageCreated,
            pageReused = pageReused,
            layoutCreated = layoutCreated,
            layoutReused = layoutReused,
            totalRenderNanos = totalRenderNanos,
            averageRenderMs = totalRenderNanos / divisor / NanosPerMillisecond,
        )
    }

    private companion object {
        const val NanosPerMillisecond = 1_000_000.0
    }
}
