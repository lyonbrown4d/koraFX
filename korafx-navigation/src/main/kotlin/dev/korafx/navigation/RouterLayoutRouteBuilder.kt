package dev.korafx.navigation

import javafx.scene.Node
import javafx.scene.layout.StackPane

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
