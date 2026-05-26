// Kotlin snippet: routerHost 的布局与主/副入口
routerHost(
    scope = uiScope,
    navigator = navigator,
) {
    route(DemoRoute.RouterHost) {
        primary { route ->
            // route content
            section("RouterHost for ${route.title}") {}
        }
        outlet("inspector") { context ->
            section("Inspector for ${context.route.title}") {}
        }
    }
    layout("main-layout") {
        shell { context ->
            vbox {
                // shared shell
            }
        }
    }
}
