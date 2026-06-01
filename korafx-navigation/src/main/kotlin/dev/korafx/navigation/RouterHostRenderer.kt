package dev.korafx.navigation

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalListener
import dev.korafx.dsl.styleClass
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane

internal class RouterHostRenderer<R : Route>(
    private val graph: RouterHostGraph<R>,
    private val collectMetrics: Boolean = false,
) {
    private val pageCache = Caffeine.newBuilder()
        .maximumSize(RouterHostPageCacheSize.toLong())
        .removalListener(
            RemovalListener<String, Node> { _, node, _ ->
                if (node != null) {
                    Platform.runLater { detachFromParent(node) }
                }
            },
        )
        .build<String, Node>()
    private val layoutCache = linkedMapOf<Any, RouterLayoutInstance>()
    private val transitionHost = ContentTransitionHost()
    private var activeNode: Node? = null

    fun render(
        host: StackPane,
        state: NavigationState<R>,
        transition: RouteTransition = RouteTransition.None,
    ) {
        val startNanos = System.nanoTime()
        val route = state.currentRoute
        val location = state.currentLocation

        var pageCreated = 0L
        var pageReused = 0L
        var layoutCreated = 0L
        var layoutReused = 0L

        val routeView = graph.routes[route.id]
        val page = renderPage(
            location = location,
            routeView = routeView,
            outletName = RouterOutlets.PRIMARY_OUTLET,
            pageInstancePolicy = state.pageInstancePolicy,
        )
        pageCreated += page.pageCreated
        pageReused += page.pageReused

        val nextNode =
            routeView?.layoutKey?.let { layoutKey ->
                val layoutResult = renderLayoutChain(
                    layoutKey = layoutKey,
                    location = location,
                    routeView = routeView,
                    primaryPage = page.node,
                    pageInstancePolicy = state.pageInstancePolicy,
                )
                pageCreated += layoutResult.pageCreated
                pageReused += layoutResult.pageReused
                layoutCreated += layoutResult.layoutCreated
                layoutReused += layoutResult.layoutReused
                layoutResult.node
            } ?: page.node

        val renderNanos = System.nanoTime() - startNanos
        if (collectMetrics || routeRenderMetricsBus.isEnabled()) {
            routeRenderMetricsBus.record(
                RouteRenderEvent(
                    routeId = route.id,
                    routeTitle = route.title,
                    routePath = location.path,
                    hostType = RouteHostType.ROUTER_HOST,
                    pageInstancePolicy = state.pageInstancePolicy,
                    renderDurationNanos = renderNanos,
                    pageCreated = pageCreated,
                    pageReused = pageReused,
                    layoutCreated = layoutCreated,
                    layoutReused = layoutReused,
                ),
            )
        }

        val current = activeNode
        if (current === nextNode) {
            if (host.children.singleOrNull() !== nextNode) {
                host.children.setAll(nextNode)
            }
            return
        }

        transitionHost.render(
            host = host,
            nextNode = nextNode,
            transition = transition,
        ) { activeNode = nextNode }
    }

    private fun renderLayoutChain(
        layoutKey: Any,
        location: NavigationLocation<R>,
        routeView: RouterRouteView<R>,
        primaryPage: Node,
        pageInstancePolicy: PageInstancePolicy,
    ): LayoutChainResult {
        val chain = layoutChain(layoutKey)
        var layoutCreated = 0L
        var layoutReused = 0L
        var pageCreated = 0L
        var pageReused = 0L
        var childNode = primaryPage

        chain.asReversed().forEachIndexed { index, key ->
            val layoutDecision = layoutInstance(key)
            if (layoutDecision.created) {
                layoutCreated++
            } else {
                layoutReused++
            }

            if (index == 0) {
                val namedPages = routeView.outlets.entries.associate { (outletName, _) ->
                    val decision = renderPage(
                        location = location,
                        routeView = routeView,
                        outletName = outletName,
                        pageInstancePolicy = pageInstancePolicy,
                    )
                    pageCreated += decision.pageCreated
                    pageReused += decision.pageReused
                    outletName to decision.node
                }
                layoutDecision.instance.outlets.render(childNode, namedPages)
            } else {
                layoutDecision.instance.outlets.render(childNode, emptyMap())
            }
            childNode = layoutDecision.instance.node
        }

        return LayoutChainResult(
            node = childNode,
            pageCreated = pageCreated,
            pageReused = pageReused,
            layoutCreated = layoutCreated,
            layoutReused = layoutReused,
        )
    }

    private fun layoutChain(layoutKey: Any): List<Any> {
        val chain = mutableListOf<Any>()
        var cursor: Any? = layoutKey

        while (cursor != null) {
            chain += cursor
            cursor = graph.layouts[cursor]?.parentKey
        }

        return chain.asReversed()
    }

    private fun layoutInstance(layoutKey: Any): LayoutInstanceDecision {
        val existing = layoutCache[layoutKey]
        if (existing != null) {
            return LayoutInstanceDecision(existing, false)
        }

        val layoutView = graph.layouts[layoutKey]
            ?: error("Router route references unknown layout '$layoutKey'.")
        val outlets = RouterOutlets()
        val shell = layoutView.shellFactory(outlets).apply {
            styleClass("router-layout")
        }
        val instance = RouterLayoutInstance(
            outlets = outlets,
            node = shell,
        )
        layoutCache[layoutKey] = instance
        return LayoutInstanceDecision(instance, true)
    }

    private fun renderPage(
        location: NavigationLocation<R>,
        routeView: RouterRouteView<R>?,
        outletName: String,
        pageInstancePolicy: PageInstancePolicy,
    ): RenderedPageDecision {
        val content =
            if (outletName == RouterOutlets.PRIMARY_OUTLET) {
                routeView?.primary
                    ?: graph.fallback?.let { fallback ->
                        { context: RouterViewContext<R> -> fallback(context.route) }
                    }
            } else {
                routeView?.outlets?.get(outletName)
            }
                ?: error("No router view registered for route '${location.route.id}'.")
        val context = RouterViewContext(
            route = location.route,
            location = location,
        )

        return when (pageInstancePolicy) {
            PageInstancePolicy.RECREATE ->
                RenderedPageDecision(
                    node = content(context),
                    pageCreated = 1L,
                    pageReused = 0L,
                )
            PageInstancePolicy.KEEP_ALIVE,
            PageInstancePolicy.SINGLETON_IN_WINDOW -> {
                val key = "${location.fullPath}@$outletName"
                val cached = pageCache.getIfPresent(key)
                if (cached == null) {
                    RenderedPageDecision(
                        node = content(context).also { pageCache.put(key, it) },
                        pageCreated = 1L,
                        pageReused = 0L,
                    )
                } else {
                    RenderedPageDecision(
                        node = cached,
                        pageCreated = 0L,
                        pageReused = 1L,
                    )
                }
            }
        }
    }

    private fun detachFromParent(node: Node) {
        val parent = node.parent
        if (parent is Pane && parent.children.contains(node)) {
            parent.children.remove(node)
        }
    }
}

private data class RenderedPageDecision(
    val node: Node,
    val pageCreated: Long,
    val pageReused: Long,
)

private data class LayoutInstanceDecision(
    val instance: RouterLayoutInstance,
    val created: Boolean,
)

private data class LayoutChainResult(
    val node: Node,
    val pageCreated: Long,
    val pageReused: Long,
    val layoutCreated: Long,
    val layoutReused: Long,
)

private const val RouterHostPageCacheSize = 64
