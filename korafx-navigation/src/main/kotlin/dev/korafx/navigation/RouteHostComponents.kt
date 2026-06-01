@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import dev.korafx.dsl.state.collectLatestIn
import dev.korafx.dsl.styleClass
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import java.util.LinkedHashMap

fun <R : Route> routeHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    transition: RouteTransition = RouteTransition.None,
    init: StackPane.() -> Unit = {},
    content: (route: R) -> Node,
): StackPane = routeHost(
    scope = scope,
    navigator = navigator,
    transition = flowOf(transition),
    init = init,
    content = content,
)

fun <R : Route> routeHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    transition: Flow<RouteTransition>,
    init: StackPane.() -> Unit = {},
    content: (route: R) -> Node,
): StackPane {
    val detachFromParent = { node: Node ->
        val parent = node.parent
        if (parent is Pane && parent.children.contains(node)) {
            parent.children.remove(node)
        }
    }

    val cache = object : LinkedHashMap<String, Node>(RouteHostCacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Node>): Boolean {
            if (size <= RouteHostCacheSize) {
                return false
            }

            detachFromParent(eldest.value)
            return true
        }
    }
    val host = StackPane().apply(init)
    val transitionHost = ContentTransitionHost()
    val activeTransition = AtomicReference<RouteTransition>(RouteTransition.None)

    scope.launch {
        transition.collectLatest { nextTransition ->
            activeTransition.set(nextTransition)
        }
    }

    navigator.state.collectLatestIn(scope) { state ->
        runNavigationOnFxThread {
            val route = state.currentRoute
            val node =
                when (state.pageInstancePolicy) {
                    PageInstancePolicy.RECREATE -> content(route)
                    PageInstancePolicy.KEEP_ALIVE,
                    PageInstancePolicy.SINGLETON_IN_WINDOW,
                    -> cache.getOrPut(route.id) { content(route) }
                }

            transitionHost.render(
                host,
                node,
                activeTransition.get(),
            )
        }
    }

    return host
}

fun <R : Route> routerHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    transition: RouteTransition = RouteTransition.None,
    init: StackPane.() -> Unit = {},
    content: RouterHostBuilder<R>.() -> Unit,
): StackPane = routerHost(
    scope = scope,
    navigator = navigator,
    transition = flowOf(transition),
    init = init,
    content = content,
)

fun <R : Route> routerHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    transition: Flow<RouteTransition>,
    init: StackPane.() -> Unit = {},
    content: RouterHostBuilder<R>.() -> Unit,
): StackPane {
    val graph = RouterHostBuilder<R>().apply(content).build()
    val renderer = RouterHostRenderer(graph)
    val host = StackPane().apply {
        styleClass("router-host")
        init()
    }
    val activeTransition = AtomicReference<RouteTransition>(RouteTransition.None)

    scope.launch {
        transition.collectLatest { nextTransition ->
            activeTransition.set(nextTransition)
        }
    }

    navigator.state.collectLatestIn(scope) { state ->
        runNavigationOnFxThread {
            renderer.render(
                host = host,
                state = state,
                transition = activeTransition.get(),
            )
        }
    }

    return host
}

fun interface RouterModule<R : Route> {
    fun register(router: RouterHostBuilder<R>)
}

private const val RouteHostCacheSize = 32
