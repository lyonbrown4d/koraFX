// 通过 setResult/results/awaitResult 实现路由结果回传
private fun buildRouteResultDemo() {
    val localNavigator = Navigator(
        initialRoute = RouteResultDemoRoute.Entry,
        routes = RouteResultDemoRoute.all,
        pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
    )
    val resultKey = navigationResultKey<String>("sample-route-result")
    val resultState = MutableStateFlow("未选择")

    uiScope.launch {
        localNavigator.results(resultKey).collect { value ->
            resultState.value = value
        }
    }

    section("路由返回值") {
        label("路由结果约定：下游页面通过 setResult 写回上游。")
        label("当前结果：").stateText(uiScope, resultState) { "当前结果：$it" }
        actionBar(alignEnd = false) {
            button("打开 Picker 页面") {
                onAction { localNavigator.navigate(RouteResultDemoRoute.Picker) }
            }
            button("等待下一次 Picker 返回") {
                onAction {
                    uiScope.launch {
                        val picked = localNavigator.awaitResult(resultKey)
                        notifications.show(
                            message = "awaitResult 接收: $picked",
                            tone = ToastTone.INFO,
                        )
                    }
                }
            }
        }

        routerHost(
            scope = uiScope,
            navigator = localNavigator,
            transition = RouteTransition.Fade(),
        ) {
            route(RouteResultDemoRoute.Entry) {
                vbox(10.0) {
                    label("当前路由：${localNavigator.currentLocation.fullPath}")
                    button("Picker") { onAction { localNavigator.navigate(RouteResultDemoRoute.Picker) } }
                }
            }
            route(RouteResultDemoRoute.Picker) {
                vbox(10.0) {
                    label("选择后会返回上一页并携带结果。")
                    button("选择 Alpha") {
                        onAction {
                            localNavigator.setResult(resultKey, "alpha")
                            localNavigator.back()
                        }
                    }
                    button("选择 Beta") {
                        onAction {
                            localNavigator.setResult(resultKey, "beta")
                            localNavigator.back()
                        }
                    }
                    button("选择 Gamma") {
                        onAction {
                            localNavigator.setResult(resultKey, "gamma")
                            localNavigator.back()
                        }
                    }
                }
            }
        }
    }
}
