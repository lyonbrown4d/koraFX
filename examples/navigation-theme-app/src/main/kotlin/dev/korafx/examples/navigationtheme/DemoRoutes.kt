package dev.korafx.examples.navigationtheme

import dev.korafx.navigation.ROUTE_TRANSITION_META_KEY
import dev.korafx.navigation.PathRoute
import dev.korafx.navigation.RouteMeta
import dev.korafx.navigation.routeMeta

internal data class DemoRoute(
    override val id: String,
    override val title: String,
    override val path: String,
    val description: String,
    val docResource: String,
    val sourceResource: String,
    val section: RouteSection,
    override val meta: RouteMeta = RouteMeta.Empty,
) : PathRoute {
    companion object {
        val Overview = DemoRoute(
            id = "overview",
            title = "Overview",
            path = "/",
            description = "Route + theme + transition 的能力总览。",
            docResource = "dev/korafx/examples/navigationtheme/docs/overview.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/overview.kt",
            section = RouteSection.Core,
            meta = routeMeta("level" to "core"),
        )

        val PathRouting = DemoRoute(
            id = "path-routing",
            title = "Path Routing",
            path = "/routes/:projectId/:section?",
            description = "支持动态参数、可选参数和 query/hash 的完整路径导航。",
            docResource = "dev/korafx/examples/navigationtheme/docs/path-routing.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/path-routing.kt",
            section = RouteSection.Core,
        )

        val History = DemoRoute(
            id = "history",
            title = "History",
            path = "/history",
            description = "后退/前进栈与 replace、popToRoot 的交互效果。",
            docResource = "dev/korafx/examples/navigationtheme/docs/history.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/history.kt",
            section = RouteSection.Advanced,
        )

        val Guards = DemoRoute(
            id = "guards",
            title = "Navigation Guards",
            path = "/guards",
            description = "同步/异步 Guard 与可选重定向规则。",
            docResource = "dev/korafx/examples/navigationtheme/docs/guards.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/guards.kt",
            section = RouteSection.Advanced,
        )

        val RouterHost = DemoRoute(
            id = "router-host",
            title = "Router Host",
            path = "/router/:layout?",
            description = "layout + outlet 的子树渲染方式，支持多级共享 shell。",
            docResource = "dev/korafx/examples/navigationtheme/docs/router-host.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/router-host.kt",
            section = RouteSection.Advanced,
        )

        val StateRestoration = DemoRoute(
            id = "state",
            title = "State Restoration",
            path = "/state/:scope?",
            description = "按路由 location 键控的状态持久化与恢复。",
            docResource = "dev/korafx/examples/navigationtheme/docs/state-restoration.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/state-restoration.kt",
            section = RouteSection.Advanced,
        )

        val Transitions = DemoRoute(
            id = "transitions",
            title = "Animation",
            path = "/transitions",
            description = "基于 navigation action 的转场策略。",
            docResource = "dev/korafx/examples/navigationtheme/docs/transitions.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/transitions.kt",
            section = RouteSection.Advanced,
        )

        val RouteData = DemoRoute(
            id = "route-data",
            title = "Route Data",
            path = "/route-data",
            description = "路由级异步数据加载与 loading/error/revalidate。",
            docResource = "dev/korafx/examples/navigationtheme/docs/route-data.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/route-data.kt",
            section = RouteSection.Advanced,
        )

        val LazyRouter = DemoRoute(
            id = "lazy-router",
            title = "Lazy Router",
            path = "/lazy-router",
            description = "路由懒加载与按需初始化 page 的真实示例。",
            docResource = "dev/korafx/examples/navigationtheme/docs/lazy-router.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/lazy-router.kt",
            section = RouteSection.Advanced,
        )

        val RouteResult = DemoRoute(
            id = "route-result",
            title = "Route Result",
            path = "/route-result",
            description = "使用 setResult / results / awaitResult 构建路由返回值。",
            docResource = "dev/korafx/examples/navigationtheme/docs/route-result.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/route-result.kt",
            section = RouteSection.Advanced,
        )

        val RouteTransitionMeta = DemoRoute(
            id = "route-transition-meta",
            title = "Route Transition Meta",
            path = "/route-transition-meta",
            description = "通过 route meta 覆盖当前路由的转场策略。",
            docResource = "dev/korafx/examples/navigationtheme/docs/route-transition-meta.md",
            sourceResource = "dev/korafx/examples/navigationtheme/snippets/route-transition-meta.kt",
            section = RouteSection.Advanced,
            meta = routeMeta(ROUTE_TRANSITION_META_KEY to "fade"),
        )

        val all: List<DemoRoute> = listOf(
            Overview,
            PathRouting,
            History,
            Guards,
            RouterHost,
            StateRestoration,
            RouteData,
            LazyRouter,
            RouteResult,
            RouteTransitionMeta,
            Transitions,
        )

        fun bySection(section: RouteSection): List<DemoRoute> =
            all.filter { it.section == section }
    }
}

internal enum class RouteSection(val label: String) {
    Core("核心能力"),
    Advanced("高级能力"),
}

internal enum class LazyRouterDemoRoute(
    override val id: String,
    override val title: String,
    override val path: String,
) : PathRoute {
    Home(id = "lazy-home", title = "Lazy Home", path = "/"),
    Detail(id = "lazy-detail", title = "Lazy Detail", path = "/detail/:itemId"),
    LazyPanel(id = "lazy-panel", title = "Lazy Panel", path = "/panel"),
    ;

    companion object {
        val all: List<LazyRouterDemoRoute> = listOf(Home, Detail, LazyPanel)
    }
}

internal enum class RouteResultDemoRoute(
    override val id: String,
    override val title: String,
    override val path: String,
) : PathRoute {
    Entry(id = "result-home", title = "Result Home", path = "/"),
    Picker(id = "result-picker", title = "Result Picker", path = "/picker"),
    ;

    companion object {
        val all: List<RouteResultDemoRoute> = listOf(Entry, Picker)
    }
}
