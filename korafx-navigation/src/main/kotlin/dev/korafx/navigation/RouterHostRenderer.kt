package dev.korafx.navigation

import dev.korafx.dsl.styleClass
import javafx.scene.Node
import javafx.scene.layout.StackPane

internal class RouterHostRenderer<R : Route>(
    private val graph: RouterHostGraph<R>,
) {
    private val pageCache = linkedMapOf<String, Node>()
    private val layoutCache = linkedMapOf<Any, RouterLayoutInstance>()
    private val transitionHost = ContentTransitionHost()
    private var activeNode: Node? = null

    fun render(
        host: StackPane,
        state: NavigationState<R>,
        transition: RouteTransition = RouteTransition.None,
    ) {
        val route = state.currentRoute
        val location = state.currentLocation
        val routeView = graph.routes[route.id]
        val page = renderPage(
            location = location,
            routeView = routeView,
            outletName = RouterOutlets.PRIMARY_OUTLET,
            pageInstancePolicy = state.pageInstancePolicy,
        )
        val nextNode =
            routeView?.layoutKey?.let { layoutKey ->
                renderLayoutChain(
                    layoutKey = layoutKey,
                    location = location,
                    routeView = routeView,
                    primaryPage = page,
                    pageInstancePolicy = state.pageInstancePolicy,
                )
            } ?: page

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
    ): Node {
        val chain = layoutChain(layoutKey)
        var childNode = primaryPage

        chain.asReversed().forEachIndexed { index, key ->
            val layout = layoutInstance(key)
            if (index == 0) {
                val namedPages = routeView.outlets.mapValues { (outletName, _) ->
                    renderPage(
                        location = location,
                        routeView = routeView,
                        outletName = outletName,
                        pageInstancePolicy = pageInstancePolicy,
                    )
                }
                layout.outlets.render(childNode, namedPages)
            } else {
                layout.outlets.render(childNode, emptyMap())
            }
            childNode = layout.node
        }

        return childNode
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

    private fun layoutInstance(layoutKey: Any): RouterLayoutInstance =
        layoutCache.getOrPut(layoutKey) {
            val layoutView = graph.layouts[layoutKey]
                ?: error("Router route references unknown layout '$layoutKey'.")
            val outlets = RouterOutlets()
            val shell = layoutView.shellFactory(outlets).apply {
                styleClass("router-layout")
            }
            RouterLayoutInstance(
                outlets = outlets,
                node = shell,
            )
        }

    private fun renderPage(
        location: NavigationLocation<R>,
        routeView: RouterRouteView<R>?,
        outletName: String,
        pageInstancePolicy: PageInstancePolicy,
    ): Node {
        val content =
            if (outletName == RouterOutlets.PRIMARY_OUTLET) {
                routeView?.primary ?: graph.fallback
                    ?.let { fallback -> { context: RouterViewContext<R> -> fallback(context.route) } }
            } else {
                routeView?.outlets?.get(outletName)
            }
            ?: error("No router view registered for route '${location.route.id}'.")
        val context = RouterViewContext(
            route = location.route,
            location = location,
        )

        return when (pageInstancePolicy) {
            PageInstancePolicy.RECREATE -> content(context)
            PageInstancePolicy.KEEP_ALIVE,
            PageInstancePolicy.SINGLETON_IN_WINDOW,
            -> pageCache.getOrPut("${location.fullPath}@$outletName") { content(context) }
        }
    }
}
