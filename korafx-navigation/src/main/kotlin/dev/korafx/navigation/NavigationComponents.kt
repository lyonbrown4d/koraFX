package dev.korafx.navigation

import dev.korafx.components.emptyState
import dev.korafx.components.errorState
import dev.korafx.components.loadingState
import dev.korafx.components.setKoraIcon
import dev.korafx.dsl.FragmentBuilder
import dev.korafx.dsl.RenderState
import dev.korafx.dsl.fragment
import dev.korafx.dsl.navButton
import dev.korafx.dsl.onAction
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.vbox
import dev.korafx.dsl.state.collectLatestIn
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.kordamp.ikonli.Ikon

data class NavigationRailItem<R : Route>(
    val route: R,
    val active: Boolean,
)

data class RouteLinkState<R : Route>(
    val currentLocation: NavigationLocation<R>,
    val active: Boolean,
)

fun <R : Route> navigationRail(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    width: Double = 220.0,
    spacing: Double = 10.0,
    icon: (R) -> Ikon? = { null },
    iconSize: Int = 16,
    init: VBox.() -> Unit = {},
    buttonInit: Button.(NavigationRailItem<R>) -> Unit = {},
): VBox =
    sidebar(
        width = width,
        spacing = spacing,
        init = init,
    ) {}.also { rail ->
        navigator.state.collectLatestIn(scope) { state ->
            runOnFxThread {
                rail.children.setAll(
                    state.routes.map { route ->
                        val item = NavigationRailItem(
                            route = route,
                            active = route.id == state.currentRoute.id,
                        )

                        navButton(route.title, active = item.active) {
                            maxWidth = Double.MAX_VALUE
                            icon(route)?.let { routeIcon ->
                                setKoraIcon(routeIcon, iconSize)
                            }
                            onAction {
                                scope.launch {
                                    navigator.navigateAsync(route.id)
                                }
                            }
                            buttonInit(item)
                        }
                    },
                )
            }
        }
    }

fun <R : Route> routeButton(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    route: R,
    text: String = route.title,
    exact: Boolean = true,
    init: Button.() -> Unit = {},
    stateChanged: Button.(RouteLinkState<R>) -> Unit = {},
): Button =
    Button(text).apply {
        styleClass += "route-button"
        init()
        onAction {
            scope.launch {
                navigator.navigateAsync(route.id)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-button-active",
            inactiveClass = "route-button-inactive",
            active = { state ->
                if (exact) {
                    state.currentRoute.id == route.id
                } else {
                    state.currentLocation.route.id == route.id
                }
            },
            stateChanged = stateChanged,
        )
    }

fun <R : Route> pathButton(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    path: String,
    text: String = path,
    exact: Boolean = true,
    init: Button.() -> Unit = {},
    stateChanged: Button.(RouteLinkState<R>) -> Unit = {},
): Button =
    Button(text).apply {
        styleClass += "route-button"
        init()
        onAction {
            scope.launch {
                navigator.navigatePathAsync(path)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-button-active",
            inactiveClass = "route-button-inactive",
            active = { state -> state.currentLocation.isActivePath(path, exact) },
            stateChanged = stateChanged,
        )
    }

fun <R : Route> routeLink(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    route: R,
    text: String = route.title,
    exact: Boolean = true,
    init: Hyperlink.() -> Unit = {},
    stateChanged: Hyperlink.(RouteLinkState<R>) -> Unit = {},
): Hyperlink =
    Hyperlink(text).apply {
        styleClass += "route-link"
        init()
        onAction {
            scope.launch {
                navigator.navigateAsync(route.id)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-link-active",
            inactiveClass = "route-link-inactive",
            active = { state ->
                if (exact) {
                    state.currentRoute.id == route.id
                } else {
                    state.currentLocation.route.id == route.id
                }
            },
            stateChanged = stateChanged,
        )
    }

fun <R : Route> pathLink(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    path: String,
    text: String = path,
    exact: Boolean = true,
    init: Hyperlink.() -> Unit = {},
    stateChanged: Hyperlink.(RouteLinkState<R>) -> Unit = {},
): Hyperlink =
    Hyperlink(text).apply {
        styleClass += "route-link"
        init()
        onAction {
            scope.launch {
                navigator.navigatePathAsync(path)
            }
        }
        bindRouteLinkState(
            scope = scope,
            navigator = navigator,
            activeClass = "route-link-active",
            inactiveClass = "route-link-inactive",
            active = { state -> state.currentLocation.isActivePath(path, exact) },
            stateChanged = stateChanged,
        )
    }

fun <R : Route> routeHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    init: StackPane.() -> Unit = {},
    content: (route: R) -> Node,
): StackPane {
    val cache = linkedMapOf<String, Node>()
    val host = StackPane().apply(init)

    navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            val route = state.currentRoute
            val node =
                when (state.pageInstancePolicy) {
                    PageInstancePolicy.RECREATE -> content(route)
                    PageInstancePolicy.KEEP_ALIVE,
                    PageInstancePolicy.SINGLETON_IN_WINDOW,
                    -> cache.getOrPut(route.id) { content(route) }
                }

            host.children.setAll(node)
        }
    }

    return host
}

fun <R : Route> routerHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    init: StackPane.() -> Unit = {},
    content: RouterHostBuilder<R>.() -> Unit,
): StackPane {
    val graph = RouterHostBuilder<R>().apply(content).build()
    val renderer = RouterHostRenderer(graph)
    val host = StackPane().apply {
        styleClass("router-host")
        init()
    }

    navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            renderer.render(host, state)
        }
    }

    return host
}

fun interface RouterModule<R : Route> {
    fun register(router: RouterHostBuilder<R>)
}

class RouterHostBuilder<R : Route> internal constructor() {
    private val routes = linkedMapOf<String, RouterRouteView<R>>()
    private val layouts = linkedMapOf<Any, RouterLayoutView>()
    private var fallback: ((R) -> Node)? = null

    fun layout(
        key: Any,
        configure: RouterLayoutBuilder<R>.() -> Unit,
    ) {
        require(key !in layouts) {
            "Router layout '$key' is already registered."
        }

        val builder = RouterLayoutBuilder(this, key, parentKey = null).apply(configure)
        layouts[key] = builder.build()
    }

    fun index(
        route: R,
        content: (route: R) -> Node,
    ) {
        route(route, content)
    }

    fun index(
        routeId: String,
        content: (route: R) -> Node,
    ) {
        route(routeId, content)
    }

    fun route(
        route: R,
        content: (route: R) -> Node,
    ) {
        addRoute(route.id, layoutKey = null, content = content)
    }

    fun route(
        routeId: String,
        content: (route: R) -> Node,
    ) {
        addRoute(routeId, layoutKey = null, content = content)
    }

    fun routeLazy(
        route: R,
        content: () -> (route: R) -> Node,
    ) {
        addLazyRoute(route.id, layoutKey = null, content = content)
    }

    fun routeLazy(
        routeId: String,
        content: () -> (route: R) -> Node,
    ) {
        addLazyRoute(routeId, layoutKey = null, content = content)
    }

    fun indexView(
        route: R,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        routeView(route, configure)
    }

    fun indexView(
        routeId: String,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        routeView(routeId, configure)
    }

    fun routeView(
        route: R,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        addRoute(route.id, layoutKey = null, configure = configure)
    }

    fun routeView(
        routeId: String,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        addRoute(routeId, layoutKey = null, configure = configure)
    }

    fun route(
        route: R,
        layout: Any,
        content: (route: R) -> Node,
    ) {
        addRoute(route.id, layoutKey = layout, content = content)
    }

    fun route(
        routeId: String,
        layout: Any,
        content: (route: R) -> Node,
    ) {
        addRoute(routeId, layoutKey = layout, content = content)
    }

    fun routeLazy(
        route: R,
        layout: Any,
        content: () -> (route: R) -> Node,
    ) {
        addLazyRoute(route.id, layoutKey = layout, content = content)
    }

    fun routeLazy(
        routeId: String,
        layout: Any,
        content: () -> (route: R) -> Node,
    ) {
        addLazyRoute(routeId, layoutKey = layout, content = content)
    }

    fun routeView(
        route: R,
        layout: Any,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        addRoute(route.id, layoutKey = layout, configure = configure)
    }

    fun routeView(
        routeId: String,
        layout: Any,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        addRoute(routeId, layoutKey = layout, configure = configure)
    }

    fun fallback(content: (route: R) -> Node) {
        fallback = content
    }

    fun include(module: RouterModule<R>) {
        module.register(this)
    }

    internal fun addRoute(
        routeId: String,
        layoutKey: Any?,
        content: (route: R) -> Node,
    ) {
        require(routeId !in routes) {
            "Router route '$routeId' is already registered."
        }
        routes[routeId] = RouterRouteView(
            layoutKey = layoutKey,
            primary = { context -> content(context.route) },
            outlets = emptyMap(),
        )
    }

    internal fun addLazyRoute(
        routeId: String,
        layoutKey: Any?,
        content: () -> (route: R) -> Node,
    ) {
        val lazyContent = lazy(LazyThreadSafetyMode.NONE, content)
        addRoute(
            routeId = routeId,
            layoutKey = layoutKey,
        ) { route ->
            lazyContent.value(route)
        }
    }

    @JvmName("addRouteWithBuilder")
    internal fun addRoute(
        routeId: String,
        layoutKey: Any?,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        require(routeId !in routes) {
            "Router route '$routeId' is already registered."
        }

        val routeView = RouterRouteBuilder<R>().apply(configure).build(layoutKey)
        routes[routeId] = routeView
    }

    internal fun addLayout(
        key: Any,
        parentKey: Any?,
        configure: RouterLayoutBuilder<R>.() -> Unit,
    ) {
        require(key !in layouts) {
            "Router layout '$key' is already registered."
        }

        val builder = RouterLayoutBuilder(this, key, parentKey).apply(configure)
        layouts[key] = builder.build()
    }

    internal fun build(): RouterHostGraph<R> {
        routes.values.mapNotNull(RouterRouteView<R>::layoutKey).forEach { layoutKey ->
            require(layoutKey in layouts) {
                "Router route references unknown layout '$layoutKey'."
            }
        }
        routes.values.filter { it.layoutKey == null && it.outlets.isNotEmpty() }.forEach {
            error("Router route with named outlets must be registered inside a layout.")
        }
        layouts.forEach { (key, layout) ->
            layout.parentKey?.let { parentKey ->
                require(parentKey in layouts) {
                    "Router layout '$key' references unknown parent layout '$parentKey'."
                }
            }
        }

        return RouterHostGraph(
            routes = routes.toMap(),
            layouts = layouts.toMap(),
            fallback = fallback,
        )
    }
}

class RouterLayoutBuilder<R : Route> internal constructor(
    private val router: RouterHostBuilder<R>,
    private val key: Any,
    private val parentKey: Any?,
) {
    private var shellFactory: ((outlets: RouterOutlets) -> Node)? = null

    fun shell(factory: (outlet: StackPane) -> Node) {
        shellFactory = { outlets -> factory(outlets.primary) }
    }

    fun shellWithOutlets(factory: (outlets: RouterOutlets) -> Node) {
        shellFactory = factory
    }

    fun layout(
        key: Any,
        configure: RouterLayoutBuilder<R>.() -> Unit,
    ) {
        router.addLayout(key, parentKey = this.key, configure = configure)
    }

    fun index(
        route: R,
        content: (route: R) -> Node,
    ) {
        route(route, content)
    }

    fun index(
        routeId: String,
        content: (route: R) -> Node,
    ) {
        route(routeId, content)
    }

    fun route(
        route: R,
        content: (route: R) -> Node,
    ) {
        router.addRoute(route.id, layoutKey = key, content = content)
    }

    fun route(
        routeId: String,
        content: (route: R) -> Node,
    ) {
        router.addRoute(routeId, layoutKey = key, content = content)
    }

    fun routeLazy(
        route: R,
        content: () -> (route: R) -> Node,
    ) {
        router.addLazyRoute(route.id, layoutKey = key, content = content)
    }

    fun routeLazy(
        routeId: String,
        content: () -> (route: R) -> Node,
    ) {
        router.addLazyRoute(routeId, layoutKey = key, content = content)
    }

    fun indexView(
        route: R,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        routeView(route, configure)
    }

    fun indexView(
        routeId: String,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        routeView(routeId, configure)
    }

    fun routeView(
        route: R,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        router.addRoute(route.id, layoutKey = key, configure = configure)
    }

    fun routeView(
        routeId: String,
        configure: RouterRouteBuilder<R>.() -> Unit,
    ) {
        router.addRoute(routeId, layoutKey = key, configure = configure)
    }

    internal fun build(): RouterLayoutView =
        RouterLayoutView(
            parentKey = parentKey,
            shellFactory = shellFactory ?: { outlets -> outlets.primary },
        )
}

class RouterRouteBuilder<R : Route> internal constructor() {
    private var primaryFactory: ((context: RouterViewContext<R>) -> Node)? = null
    private val outletFactories = linkedMapOf<String, (context: RouterViewContext<R>) -> Node>()

    fun primary(content: (route: R) -> Node) {
        primaryFactory = { context -> content(context.route) }
    }

    fun primaryWithLocation(content: (context: RouterViewContext<R>) -> Node) {
        primaryFactory = content
    }

    fun content(content: (route: R) -> Node) {
        primary(content)
    }

    fun contentWithLocation(content: (context: RouterViewContext<R>) -> Node) {
        primaryWithLocation(content)
    }

    fun outlet(
        name: String,
        content: (route: R) -> Node,
    ) {
        require(name.isNotBlank()) {
            "Router outlet name cannot be blank."
        }
        require(name != RouterOutlets.PRIMARY_OUTLET) {
            "Use primary { ... } for the primary router outlet."
        }
        require(name !in outletFactories) {
            "Router outlet '$name' is already registered for this route."
        }

        outletFactories[name] = { context -> content(context.route) }
    }

    fun outletWithLocation(
        name: String,
        content: (context: RouterViewContext<R>) -> Node,
    ) {
        require(name.isNotBlank()) {
            "Router outlet name cannot be blank."
        }
        require(name != RouterOutlets.PRIMARY_OUTLET) {
            "Use primary { ... } for the primary router outlet."
        }
        require(name !in outletFactories) {
            "Router outlet '$name' is already registered for this route."
        }

        outletFactories[name] = content
    }

    internal fun build(layoutKey: Any?): RouterRouteView<R> =
        RouterRouteView(
            layoutKey = layoutKey,
            primary = primaryFactory ?: error("Router route requires primary content."),
            outlets = outletFactories.toMap(),
        )
}

data class RouterViewContext<R : Route>(
    val route: R,
    val location: NavigationLocation<R>,
) {
    val params: Map<String, String>
        get() = location.params

    val query: RouteQuery
        get() = location.query

    val hash: String?
        get() = location.hash

    val meta: RouteMeta
        get() = location.meta
}

class RouterOutlets internal constructor() {
    private val outletMap = linkedMapOf<String, StackPane>()

    val primary: StackPane
        get() = outlet(PRIMARY_OUTLET)

    fun outlet(name: String): StackPane {
        require(name.isNotBlank()) {
            "Router outlet name cannot be blank."
        }

        return outletMap.getOrPut(name) {
            StackPane().apply {
                styleClass("router-layout-outlet")
                styleClass("router-layout-outlet-${name.toStyleClassSuffix()}")
            }
        }
    }

    internal fun render(
        primaryNode: Node,
        namedNodes: Map<String, Node>,
    ) {
        primary.children.setAll(primaryNode)
        outletMap
            .filterKeys { it != PRIMARY_OUTLET }
            .forEach { (name, outlet) ->
                val node = namedNodes[name]
                if (node == null) {
                    outlet.children.clear()
                } else {
                    outlet.children.setAll(node)
                }
            }
    }

    companion object {
        internal const val PRIMARY_OUTLET = "primary"
    }
}

internal data class RouterHostGraph<R : Route>(
    val routes: Map<String, RouterRouteView<R>>,
    val layouts: Map<Any, RouterLayoutView>,
    val fallback: ((R) -> Node)?,
)

internal data class RouterRouteView<R : Route>(
    val layoutKey: Any?,
    val primary: (context: RouterViewContext<R>) -> Node,
    val outlets: Map<String, (context: RouterViewContext<R>) -> Node>,
)

internal data class RouterLayoutView(
    val parentKey: Any?,
    val shellFactory: (outlets: RouterOutlets) -> Node,
)

internal data class RouterLayoutInstance(
    val outlets: RouterOutlets,
    val node: Node,
)

internal class RouterHostRenderer<R : Route>(
    private val graph: RouterHostGraph<R>,
) {
    private val pageCache = linkedMapOf<String, Node>()
    private val layoutCache = linkedMapOf<Any, RouterLayoutInstance>()

    fun render(
        host: StackPane,
        state: NavigationState<R>,
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

        host.children.setAll(nextNode)
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

private fun String.toStyleClassSuffix(): String =
    lowercase()
        .map { character ->
            when {
                character.isLetterOrDigit() -> character
                else -> '-'
            }
        }
        .joinToString("")
        .trim('-')
        .ifBlank { "unnamed" }

private fun <R : Route, T : Node> T.bindRouteLinkState(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    activeClass: String,
    inactiveClass: String,
    active: (NavigationState<R>) -> Boolean,
    stateChanged: T.(RouteLinkState<R>) -> Unit,
) {
    navigator.state.collectLatestIn(scope) { state ->
        runOnFxThread {
            val isActive = active(state)
            updateActiveStyle(
                active = isActive,
                activeClass = activeClass,
                inactiveClass = inactiveClass,
            )
            stateChanged(
                RouteLinkState(
                    currentLocation = state.currentLocation,
                    active = isActive,
                ),
            )
        }
    }
}

private fun Node.updateActiveStyle(
    active: Boolean,
    activeClass: String,
    inactiveClass: String,
) {
    styleClass.removeAll(activeClass, inactiveClass)
    styleClass += if (active) activeClass else inactiveClass
}

private fun <R : Route> NavigationLocation<R>.isActivePath(
    path: String,
    exact: Boolean,
): Boolean {
    val target = dev.korafx.navigation.RoutePattern.parseLocation(path)
    return if (exact) {
        fullPath == target.fullPath
    } else {
        this.path == target.path || this.path.startsWith("${target.path.trimEnd('/')}/")
    }
}

fun <R : Route, T> routeDataHost(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    init: StackPane.() -> Unit = {},
    controller: RouteDataController = RouteDataController(),
    cache: Boolean = false,
    cacheKey: (context: RouterViewContext<R>) -> Any = { context -> context.location.fullPath },
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
    val dataCache = linkedMapOf<Any, T>()

    navigator.state.collectLatestIn(scope) { state ->
        val context = RouterViewContext(
            route = state.currentRoute,
            location = state.currentLocation,
        )

        controller.requests.onStart { emit(RouteDataRequest.Initial) }.collectLatest { request ->
            val key = cacheKey(context)
            val cachedValue = dataCache[key]

            if (cache && request != RouteDataRequest.Revalidate && cachedValue != null) {
                runOnFxThread {
                    host.children.setAll(content(context, cachedValue))
                }
                return@collectLatest
            }

            runOnFxThread {
                host.children.setAll(loading(context))
            }

            try {
                val value = load(context)
                if (cache) {
                    dataCache[key] = value
                }
                runOnFxThread {
                    host.children.setAll(content(context, value))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                runOnFxThread {
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
                runOnFxThread {
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

private fun runOnFxThread(block: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        block()
    } else {
        Platform.runLater {
            block()
        }
    }
}
