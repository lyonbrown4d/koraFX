// 使用 routeLazy + 独立子导航器演示按需构建
private fun buildLazyRouter() {
    val localNavigator = Navigator(
        initialRoute = LazyRouterDemoRoute.Home,
        routes = LazyRouterDemoRoute.all,
        pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
    )
    val panelInitCount = AtomicInteger(0)

    section("路由按需加载（routeLazy）") {
        label("本示例使用独立 navigator 演示 routeLazy 何时触发构建。")
        routerHost(
            scope = uiScope,
            navigator = localNavigator,
            transition = RouteTransition.Fade(),
        ) {
            route(LazyRouterDemoRoute.Home) {
                vbox(10.0) {
                    label("当前路由：${localNavigator.currentLocation.fullPath}")
                    actionBar(alignEnd = false) {
                        button("打开详情（参数路由）") {
                            onAction { localNavigator.navigatePath("/detail/007") }
                        }
                        button("打开懒加载面板") {
                            onAction { localNavigator.navigate(LazyRouterDemoRoute.LazyPanel) }
                        }
                    }
                }
            }
            routeView(LazyRouterDemoRoute.Detail) {
                primaryWithLocation { context ->
                    vbox(10.0) {
                        label("详情页参数：${context.params["itemId"]}")
                        actionBar(alignEnd = false) {
                            button("返回") { onAction { localNavigator.back() } }
                        }
                    }
                }
            }
            routeLazy(LazyRouterDemoRoute.LazyPanel) {
                {
                    val currentInit = panelInitCount.incrementAndGet()
                    vbox(10.0) {
                        label("这段内容只在首次导航到该路由时创建。")
                        label("初始化次数：$currentInit")
                        actionBar(alignEnd = false) {
                            button("返回") { onAction { localNavigator.back() } }
                        }
                    }
                }
            }
        }
    }
}
