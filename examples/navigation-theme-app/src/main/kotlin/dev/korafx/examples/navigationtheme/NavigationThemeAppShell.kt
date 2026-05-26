package dev.korafx.examples.navigationtheme

import dev.korafx.components.ToastTone
import dev.korafx.components.section
import dev.korafx.dsl.bindDisable
import dev.korafx.dsl.button
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.label
import dev.korafx.dsl.menuButton
import dev.korafx.dsl.onAction
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.toolbar
import dev.korafx.dsl.vbox
import dev.korafx.framework.theme.BuiltInThemes
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.NavigationTransitionProfile
import dev.korafx.navigation.routeButton
import javafx.scene.Node
import kotlinx.coroutines.flow.map

internal fun NavigationThemeApp.routeContent(route: DemoRoute): Node =
    vbox(spacing = 16.0) {
        section(
            title = route.title,
            description = route.description,
        ) {
            when (route) {
                DemoRoute.Overview -> buildOverview()
                DemoRoute.PathRouting -> buildPathRouting()
                DemoRoute.History -> buildHistory()
                DemoRoute.Guards -> buildGuards()
                DemoRoute.RouterHost -> buildRouterHost()
                DemoRoute.StateRestoration -> buildStateRestoration()
                DemoRoute.RouteData -> buildRouteData()
                DemoRoute.LazyRouter -> buildLazyRouter()
                DemoRoute.RouteResult -> buildRouteResultDemo()
                DemoRoute.RouteTransitionMeta -> buildRouteTransitionMeta()
                DemoRoute.Transitions -> buildTransitions()
            }
        }
    }

internal fun NavigationThemeApp.topToolbar() = toolbar {
    label("KoraFX Navigation + Theme") {
        styleClasses("headline")
    }
    spacer()

    button("返回") {
        onAction { navigator.back() }
        bindDisable(
            uiScope,
            navigator.state.map { state -> state.backStack.isEmpty() },
        )
    }
    button("前进") {
        onAction { navigator.forward() }
        bindDisable(
            uiScope,
            navigator.state.map { state -> state.forwardStack.isEmpty() },
        )
    }
    button("Home") {
        onAction { navigator.navigate(DemoRoute.Overview.id) }
    }

    menuButton(
        text = "Theme",
        content = {
            BuiltInThemes.all.forEach { theme ->
                actionItem(theme.displayName) {
                    setTheme(theme)
                }
            }
        },
    )
    ghostButton("Next Theme") {
        onAction { nextTheme() }
    }

    comboBox(
        items = NavigationTransitionProfile.entries.toList(),
        init = {
            prefWidth = 190.0
        },
    ) {
        render { it.label }
        onSelect { profile ->
            transitionPreset.value = profile ?: NavigationTransitionProfile.Adaptive
        }
        select(transitionPreset.value)
    }
}

internal fun NavigationThemeApp.moduleNavigation() =
    scrollPane(
        init = {
            isFitToWidth = true
            prefWidth = 270.0
            minWidth = 220.0
            maxWidth = 360.0
        },
    ) {
        content {
            sidebar(width = 250.0, spacing = 12.0) {
                label("Navigation Routes") {
                    styleClasses(ThemeStyleClass.Headline)
                }
                RouteSection.entries.forEach { section ->
                    label(section.label) {
                        styleClasses(ThemeStyleClass.Muted)
                    }
                    DemoRoute.bySection(section).forEach { route ->
                        routeButton(
                            scope = uiScope,
                            navigator = navigator,
                            route = route,
                        ) {
                            maxWidth = Double.MAX_VALUE
                        }
                    }
                }

                section("Quick Actions") {
                    button("随机跳转") {
                        onAction {
                            navigator.navigatePath("/routes/${(1000..9999).random()}/files")
                        }
                    }
                    button("清理历史") {
                        onAction {
                            navigator.clearNavigationHistory()
                            notifications.show(
                                message = "导航历史已清空。",
                                tone = ToastTone.INFO,
                            )
                        }
                    }
                }
            }
        }
    }

internal fun NavigationThemeApp.nextTheme() {
    themeManager.nextTheme()
    notifications.show(
        message = "Theme switched to ${themeManager.currentTheme().displayName}.",
        tone = ToastTone.SUCCESS,
    )
}

internal fun NavigationThemeApp.setTheme(theme: KoraTheme) {
    themeManager.setTheme(theme)
    notifications.show(
        message = "Theme switched to ${theme.displayName}.",
        tone = ToastTone.SUCCESS,
    )
}
