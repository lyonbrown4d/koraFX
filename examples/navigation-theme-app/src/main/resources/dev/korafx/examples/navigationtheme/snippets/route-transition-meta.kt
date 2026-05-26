// Kotlin snippet: 通过 routeMeta 覆盖路由转场
val transitionRoute = DemoRoute(
    id = "route-transition-meta",
    title = "Route Transition Meta",
    path = "/route-transition-meta",
    description = "通过 route meta 覆盖当前路由的转场策略。",
    docResource = "dev/korafx/examples/navigationtheme/docs/route-transition-meta.md",
    sourceResource = "dev/korafx/examples/navigationtheme/snippets/route-transition-meta.kt",
    section = RouteSection.Advanced,
    meta = routeMeta(
        ROUTE_TRANSITION_META_KEY to "fade",
    ),
)

// 进入该路由时不论全局 Profile 如何，都会优先使用 Fade 转场
navigator.navigate(transitionRoute.id)
