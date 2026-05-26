package dev.korafx.navigation

import javafx.scene.Node

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
