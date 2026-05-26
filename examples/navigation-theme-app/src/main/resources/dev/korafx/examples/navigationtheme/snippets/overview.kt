// Kotlin snippet: demo route 与 routeHost 的基础结构
val navigator = Navigator(
    initialRoute = DemoRoute.Overview,
    routes = DemoRoute.all,
)

appShell {
    navigation {
        sidebar {
            DemoRoute.bySection(RouteSection.Core).forEach { route ->
                routeButton(scope = uiScope, navigator = navigator, route = route)
            }
        }
    }
    content {
        routeHost(uiScope, navigator) { route ->
            when (route) {
                DemoRoute.Overview -> buildOverview()
                DemoRoute.PathRouting -> buildPathRouting()
                else -> section("unsupported") {}
            }
        }
    }
}
