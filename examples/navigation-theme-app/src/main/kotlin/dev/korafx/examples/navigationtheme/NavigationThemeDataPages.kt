package dev.korafx.examples.navigationtheme

import dev.korafx.components.ToastTone
import dev.korafx.components.actionBar
import dev.korafx.components.errorState
import dev.korafx.components.loadingState
import dev.korafx.components.section
import dev.korafx.dsl.button
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.stateText
import dev.korafx.dsl.vbox
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.RouteDataController
import dev.korafx.navigation.RouteTransition
import dev.korafx.navigation.navigationResultKey
import dev.korafx.navigation.routeDataHost
import dev.korafx.navigation.routerHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

internal fun NavigationThemeApp.buildRouteData() {
    val controller = RouteDataController()

    section("routeDataHost 场景化演示") {
        actionBar(alignEnd = false) {
            button("正常加载") {
                onAction { navigator.navigatePath("/route-data") }
            }
            button("延迟 1.5 秒") {
                onAction { navigator.navigatePath("/route-data?delay=1500") }
            }
            button("模拟错误") {
                onAction { navigator.navigatePath("/route-data?mode=error") }
            }
            button("Revalidate") {
                onAction { controller.revalidate() }
            }
        }

        routeDataHost(
            scope = uiScope,
            navigator = navigator,
            controller = controller,
            cache = true,
            init = {
                paddingAll(4.0)
            },
            load = { context ->
                val delayMs = context.query.int("delay") ?: 300
                delay(delayMs.coerceIn(80, 3000).toLong())
                if (context.query["mode"] == "error") {
                    throw IllegalStateException("mock loader error")
                }
                "load-ok:${context.location.fullPath}:${System.currentTimeMillis()}"
            },
            loading = { context ->
                loadingState("Loading ${context.route.title}...")
            },
            failed = { context, error ->
                errorState(
                    title = "${context.route.title} 加载失败",
                    message = error.message.orEmpty(),
                )
            },
        ) { _, value ->
            vbox(10.0) {
                label(value)
            }
        }
    }
}

internal fun NavigationThemeApp.buildLazyRouter() {
    val localNavigator = Navigator(
        initialRoute = LazyRouterDemoRoute.Home,
        routes = LazyRouterDemoRoute.all,
        pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
    )
    val panelInitCount = AtomicInteger(0)

    section("路由按需加载（routeLazy）") {
        label("本示例使用独立 navigator 演示 routeLazy 何时触发构建。")
        label("进入 Lazy Panel 前，相关内容不会创建。")
        label("离开后再次进入也不会重复初始化。")
        routerHost(
            scope = uiScope,
            navigator = localNavigator,
            transition = RouteTransition.Fade(),
            init = {
                styleClasses("lazy-route-host")
            },
        ) {
            route(LazyRouterDemoRoute.Home) {
                vbox(10.0) {
                    label("当前路由：${localNavigator.currentLocation.fullPath}")
                    actionBar(alignEnd = false) {
                        button("打开详情（参数路由）") {
                            onAction {
                                localNavigator.navigatePath("/detail/007")
                            }
                        }
                        button("打开懒加载面板") {
                            onAction {
                                localNavigator.navigate(LazyRouterDemoRoute.LazyPanel)
                            }
                        }
                    }
                }
            }
            route(LazyRouterDemoRoute.Detail) {
                vbox(10.0) {
                    label("详情页参数：${localNavigator.currentLocation.params["itemId"]}")
                    actionBar(alignEnd = false) {
                        button("返回") {
                            onAction { localNavigator.back() }
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
                            button("返回") {
                                onAction { localNavigator.back() }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun NavigationThemeApp.buildRouteResultDemo() {
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
            init = {
                styleClasses("route-result-host")
            },
        ) {
            route(RouteResultDemoRoute.Entry) {
                vbox(10.0) {
                    label("当前路由：${localNavigator.currentLocation.fullPath}")
                    label("选择一个项目继续：")
                    button("Picker") {
                        onAction { localNavigator.navigate(RouteResultDemoRoute.Picker) }
                    }
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
