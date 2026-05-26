package dev.korafx.examples.navigationtheme

import dev.korafx.components.actionBar
import dev.korafx.components.section
import dev.korafx.dsl.bindDisable
import dev.korafx.dsl.button
import dev.korafx.dsl.checkBox
import dev.korafx.dsl.hbox
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.slider
import dev.korafx.dsl.stateText
import dev.korafx.dsl.vbox
import dev.korafx.navigation.NavigationTransitionProfile
import dev.korafx.navigation.ROUTE_TRANSITION_META_KEY
import kotlinx.coroutines.flow.map

internal fun NavigationThemeApp.buildTransitions() {
    section("转场策略") {
        label("NavigationType 说明：")
        vbox(spacing = 6.0) {
            label("- INITIAL：首次加载 / create")
            label("- PUSH：新路由入栈")
            label("- POP：后退/前进")
            label("- REPLACE：同级替换")
        }
        actionBar(alignEnd = false) {
            button("Adaptive") {
                onAction { transitionPreset.value = NavigationTransitionProfile.Adaptive }
            }
            button("Push Slide") {
                onAction { transitionPreset.value = NavigationTransitionProfile.PushSlide }
            }
            button("Fade") {
                onAction { transitionPreset.value = NavigationTransitionProfile.Fade }
            }
            button("Scale") {
                onAction { transitionPreset.value = NavigationTransitionProfile.Scale }
            }
            button("None") {
                onAction { transitionPreset.value = NavigationTransitionProfile.None }
            }
        }

        section("转场参数") {
            hbox(spacing = 12.0) {
                checkBox("启用转场") {
                    isSelected = transitionEnabled.value
                    selectedProperty().addListener { _, _, selected ->
                        transitionEnabled.value = selected
                    }
                }
                slider(
                    min = 0.5,
                    max = 2.0,
                    value = transitionDurationScale.value,
                    init = {
                        prefWidth = 210.0
                        valueProperty().addListener { _, _, value ->
                            transitionDurationScale.value = value.toDouble()
                        }
                    },
                )
                label("x1.0")
                label("当前倍率：").stateText(uiScope, transitionDurationScale) {
                    "x${"%.2f".format(it)}"
                }
            }
        }
        actionBar(alignEnd = false) {
            button("Push") {
                onAction { navigator.navigatePath("/routes/${(1000..2000).random()}/overview") }
            }
            button("Replace") {
                onAction { navigator.replace(DemoRoute.Overview) }
            }
            button("Pop") {
                onAction { navigator.back() }
                bindDisable(
                    uiScope,
                    navigator.state.map { state -> state.backStack.isEmpty() },
                )
            }
        }

        actionBar(alignEnd = false) {
            button("Route Meta 转场示例") {
                onAction { navigator.navigate(DemoRoute.RouteTransitionMeta) }
            }
        }
    }
}

internal fun NavigationThemeApp.buildRouteTransitionMeta() {
    section("路由级转场覆盖") {
        label("本页面的 `PathRoute.meta` 中配置了：")
        label("${ROUTE_TRANSITION_META_KEY} = \"fade\"")
        label("即使在顶部全局选择了其他 profile，进入此路由时也会优先采用 Fade 转场。")
        actionBar(alignEnd = false) {
            button("返回动画示例页") {
                onAction { navigator.navigate(DemoRoute.Transitions.id) }
            }
            button("再次返回 Home") {
                onAction { navigator.navigate(DemoRoute.Overview.id) }
            }
        }
    }
}
