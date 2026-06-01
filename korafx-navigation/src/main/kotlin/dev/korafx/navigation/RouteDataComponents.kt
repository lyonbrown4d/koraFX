@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import dev.korafx.components.emptyState
import dev.korafx.components.errorState
import dev.korafx.components.loadingState
import dev.korafx.dsl.FragmentBuilder
import dev.korafx.dsl.RenderState
import dev.korafx.dsl.fragment
import dev.korafx.dsl.state.collectLatestIn
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.vbox
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart

fun <R : Route, T> routeDataHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    init: StackPane.() -> Unit = {},
    controller: RouteDataController = RouteDataController(),
    cache: Boolean = false,
    cacheKey: (context: RouterViewContext<R>) -> Any = { context -> context.location.fullPath },
    cacheSize: Int = RouteDataCacheSize,
    load: suspend (context: RouterViewContext<R>) -> T,
    loading: (context: RouterViewContext<R>) -> Node = { context ->
        loadingState("Loading ${context.route.title}...")
    },
    failed: (context: RouterViewContext<R>, error: Throwable) -> Node = { context, error ->
        errorState(
            title = "${context.route.title} failed to load",
            message = error.message ?: error::class.java.simpleName,
        )
    },
    content: (context: RouterViewContext<R>, value: T) -> Node,
): StackPane {
    val host = StackPane().apply {
        styleClass("route-data-host")
        init()
    }
    val resolvedCacheSize = cacheSize.coerceAtLeast(1)
    val boundedCache = object : LinkedHashMap<Any, T>(resolvedCacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Any, T>): Boolean {
            return size > resolvedCacheSize
        }
    }

    navigator.state.collectLatestIn(scope) { state ->
        val context = RouterViewContext(
            route = state.currentRoute,
            location = state.currentLocation,
        )

        controller.requests.onStart { emit(RouteDataRequest.Initial) }.collectLatest { request ->
            val key = cacheKey(context)
                val cachedValue = boundedCache[key]

            if (cache && request != RouteDataRequest.Revalidate && cachedValue != null) {
                runNavigationOnFxThread {
                    host.children.setAll(content(context, cachedValue))
                }
                return@collectLatest
            }

            runNavigationOnFxThread {
                host.children.setAll(loading(context))
            }

            try {
                val value = load(context)
                if (cache) {
                    boundedCache[key] = value
                }
                runNavigationOnFxThread {
                    host.children.setAll(content(context, value))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                runNavigationOnFxThread {
                    host.children.setAll(failed(context, error))
                }
            }
        }
    }

    return host
}

class RouteDataController {
    private val requestStream = MutableSharedFlow<RouteDataRequest>(
        replay = 0,
        extraBufferCapacity = 16,
    )

    internal val requests: Flow<RouteDataRequest> = requestStream.asSharedFlow()

    fun revalidate(): Boolean =
        requestStream.tryEmit(RouteDataRequest.Revalidate)
}

internal enum class RouteDataRequest {
    Initial,
    Revalidate,
}

private const val RouteDataCacheSize = 64

fun <R : Route, T> routeStateHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    spacing: Double = 16.0,
    init: VBox.() -> Unit = {},
    stateFor: (route: R) -> Flow<RenderState<T>>,
    loading: FragmentBuilder.(route: R) -> Unit = { route ->
        add(loadingState("Loading ${route.title}..."))
    },
    empty: FragmentBuilder.(route: R) -> Unit = { route ->
        add(
            emptyState(
                title = route.title,
                message = "No content is available for this route.",
            ),
        )
    },
    failed: FragmentBuilder.(route: R, failure: RenderState.Failed) -> Unit = { route, failure ->
        add(
            errorState(
                title = "${route.title} failed to load",
                message = failure.message,
            ),
        )
    },
    content: FragmentBuilder.(route: R, value: T) -> Unit,
): VBox =
    vbox(
        spacing = spacing,
        init = {
            styleClass("route-state-host")
            init()
        },
    ) {}.also { host ->
        navigator.state.collectLatestIn(scope) { navigation ->
            val route = navigation.currentRoute
            stateFor(route).collectLatest { state ->
                runNavigationOnFxThread {
                    host.children.setAll(
                        fragment {
                            when (state) {
                                RenderState.Loading -> loading(route)
                                RenderState.Empty -> empty(route)
                                is RenderState.Failed -> failed(route, state)
                                is RenderState.Content -> content(route, state.value)
                            }
                        },
                    )
                }
            }
        }
    }
