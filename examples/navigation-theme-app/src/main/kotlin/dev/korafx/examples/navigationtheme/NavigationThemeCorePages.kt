package dev.korafx.examples.navigationtheme

import dev.korafx.components.ToastTone
import dev.korafx.components.actionBar
import dev.korafx.components.section
import dev.korafx.dsl.bindDisable
import dev.korafx.dsl.button
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.stateText
import dev.korafx.dsl.textField
import dev.korafx.navigation.NavigationDecision
import dev.korafx.navigation.RoutePattern
import javafx.scene.control.Label
import javafx.scene.control.TextField
import kotlinx.coroutines.flow.map

internal fun NavigationThemeApp.buildOverview() {
    section("功能速览") {
        actionBar(alignEnd = false) {
            button("Path Routing") { onAction { navigator.navigate(DemoRoute.PathRouting.id) } }
            button("History") { onAction { navigator.navigate(DemoRoute.History.id) } }
            button("Guards") { onAction { navigator.navigate(DemoRoute.Guards.id) } }
            button("Lazy Router") { onAction { navigator.navigate(DemoRoute.LazyRouter.id) } }
            button("Route Result") { onAction { navigator.navigate(DemoRoute.RouteResult.id) } }
            button("Route Data") { onAction { navigator.navigate(DemoRoute.RouteData.id) } }
            button("Transitions") { onAction { navigator.navigate(DemoRoute.Transitions.id) } }
        }
    }

    section("运行时能力") {
        label("1) 支持 routeId 与 pathRoute")
        label("2) Back/Forward 历史栈")
        label("3) Guard 与 async guard")
        label("4) routerHost layouts/outlets")
        label("5) 路由级状态持久化与文档化示例")
        label("6) routeLazy 与延迟初始化")
        label("7) 路由结果流(setResult / results / awaitResult)")
    }
}

internal fun NavigationThemeApp.buildPathRouting() {
    lateinit var projectInput: TextField
    lateinit var sectionInput: TextField
    lateinit var tabInput: TextField

    section("路径导航") {
        hbox(spacing = 8.0) {
            projectInput = textField("101")
            sectionInput = textField("overview")
            tabInput = textField("files")
            button("Go by path") {
                onAction {
                    val project = projectInput.text.trim().takeIf(String::isNotEmpty) ?: "101"
                    val section = sectionInput.text.trim().takeIf(String::isNotEmpty) ?: "overview"
                    val tab = tabInput.text.trim().takeIf(String::isNotEmpty)
                    val fullPath =
                        if (tab == null) {
                            RoutePattern.build("/routes/$project/$section")
                        } else {
                            RoutePattern.build("/routes/$project/$section", query = mapOf("tab" to tab))
                        }
                    navigator.navigatePath(fullPath)
                }
            }
        }
        section("当前匹配") {
            label("fullPath:").stateText(uiScope, navigator.state) { it.currentLocation.fullPath }
            label("params:").stateText(uiScope, navigator.state) {
                it.currentLocation.params.entries.joinToString {
                    "${it.key}=${it.value}"
                }.ifBlank { "无" }
            }
            label("query:").stateText(uiScope, navigator.state) {
                if (it.currentLocation.query.values.isEmpty()) {
                    "无"
                } else {
                    it.currentLocation.query.asQueryMap()
                        .entries.joinToString { (key, values) -> "$key=${values.joinToString(",")}" }
                }
            }
            label("hash:").stateText(uiScope, navigator.state) {
                it.currentLocation.hash ?: "无"
            }
        }
        actionBar(alignEnd = false) {
            button("加 query") {
                onAction {
                    val next = navigator.currentLocation.withQuery("tab" to "overview", "sort" to "name")
                    navigator.navigatePath(next)
                }
            }
            button("加 hash") {
                onAction {
                    val next = navigator.currentLocation.withHash("line-12")
                    navigator.navigatePath(next)
                }
            }
        }
    }
}

internal fun NavigationThemeApp.buildHistory() {
    section("Back/Forward + Replace") {
        actionBar(alignEnd = false) {
            button("Push: /routes/${(10..99).random()}/history") {
                onAction {
                    navigator.navigatePath("/routes/${(10..99).random()}/history")
                }
            }
            button("Push: /state") {
                onAction {
                    navigator.navigatePath("/state")
                }
            }
            button("Replace: /history") {
                onAction {
                    navigator.replacePath("/history")
                }
            }
            button("Back") {
                onAction { navigator.back() }
                bindDisable(
                    uiScope,
                    navigator.state.map { state -> state.backStack.isEmpty() },
                )
            }
            button("Forward") {
                onAction { navigator.forward() }
                bindDisable(
                    uiScope,
                    navigator.state.map { state -> state.forwardStack.isEmpty() },
                )
            }
        }
    }

    section("历史面板") {
        label("Back stack").stateText(uiScope, navigator.state) { state ->
            state.backStack.joinToString(" -> ") { it.route.id }
        }
        label("Forward stack").stateText(uiScope, navigator.state) { state ->
            state.forwardStack.joinToString(" -> ") { it.route.id }
        }
    }
    section("高级动作") {
        actionBar(alignEnd = false) {
            button("PopToRoot") {
                onAction {
                    if (navigator.popToRoot()) {
                        notifications.show(message = "已返回起始页", tone = ToastTone.INFO)
                    } else {
                        notifications.show(message = "当前已是起始页", tone = ToastTone.INFO)
                    }
                }
            }
            button("Clear history") {
                onAction {
                    navigator.clearNavigationHistory()
                    notifications.show(message = "历史栈已清空", tone = ToastTone.INFO)
                }
            }
        }
    }
}

internal fun NavigationThemeApp.buildGuards() {
    section("Guard 示例") {
        val statusLabel: Label =
            label("Guard 状态：Router Host 已放行")
        statusLabel.stateText(uiScope, blockRouterDemo) {
            if (it) "Guard 状态：Router Host 被拦截" else "Guard 状态：Router Host 已放行"
        }
        actionBar(alignEnd = false) {
            button("开启 Guard") {
                onAction { blockRouterDemo.value = true }
            }
            button("关闭 Guard") {
                onAction { blockRouterDemo.value = false }
            }
            button("尝试去 RouterHost") {
                onAction {
                    when (val decision = navigator.canNavigate(DemoRoute.RouterHost.id)) {
                        is NavigationDecision.Allow -> {
                            navigator.navigate(DemoRoute.RouterHost)
                        }
                        is NavigationDecision.Redirect -> {
                            notifications.show(
                                message = "重定向：${decision.routeId ?: decision.path}",
                                tone = ToastTone.INFO,
                            )
                        }
                        is NavigationDecision.Block -> {
                            notifications.show(
                                message = decision.reason ?: "navigation blocked",
                                tone = ToastTone.WARNING,
                            )
                        }
                    }
                }
            }
        }
        statusLabel
    }

    section("守卫思路") {
        label("Guard 在同步/异步路径上都可返回 Allow / Block / Redirect。")
        label("建议在权限判断/未保存检查/登录态恢复上集中挂在 navigator.beforeEach。")
    }
}

internal fun NavigationThemeApp.buildRouterHost() {
    section("如何理解 RouterHost") {
        hbox(spacing = 8.0) {
            button("打开 Router Host 示例") {
                onAction { navigator.navigatePath("/router/project") }
            }
            button("打开子布局示例") {
                onAction { navigator.navigatePath("/router/dashboard") }
            }
        }
        label("当前示例中 routeHost 已用于主内容切换。")
        label("RouterHost 适合需要 layout + shared shell + 多 outlet 的场景。")
    }
}

internal fun NavigationThemeApp.buildStateRestoration() {
    lateinit var note: TextField
    section("按 location 保存任意状态") {
        note = textField {
            promptText = "当前路由 path 维度存储一段文本"
        }
        actionBar(alignEnd = false) {
            button("Save") {
                onAction {
                    navigator.saveState("demo-note", note.text.trim().ifEmpty { "" })
                    notifications.show(
                        message = "已保存到 location = ${navigator.currentLocation.fullPath}",
                        tone = ToastTone.SUCCESS,
                    )
                }
            }
            button("Load") {
                onAction {
                    note.text = navigator.restoredState<String>("demo-note") ?: ""
                }
            }
        }
        label("当前 location:").stateText(uiScope, navigator.state) {
            it.currentLocation.fullPath
        }
        label("恢复值:").stateText(uiScope, navigator.state) {
            navigator.restoredState<String>("demo-note", it.currentLocation) ?: "空"
        }
    }
}
