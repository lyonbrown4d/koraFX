package dev.korafx.examples.navigationtheme

import dev.korafx.components.ToastHost
import dev.korafx.components.appShell
import dev.korafx.components.toastHost
import dev.korafx.dsl.paddingAll
import dev.korafx.dsl.tabPane
import dev.korafx.framework.theme.SceneThemeController
import dev.korafx.framework.theme.ThemeManager
import dev.korafx.navigation.NavigationDecision
import dev.korafx.navigation.NavigationTransitionProfile
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.RouteTransition
import dev.korafx.navigation.routeHost
import dev.korafx.navigation.scaleDuration
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

fun main(args: Array<String>) {
    Application.launch(NavigationThemeApp::class.java, *args)
}

class NavigationThemeApp : Application() {
    internal val uiScope = MainScope()
    internal val themeManager = ThemeManager()
    private val themeController = SceneThemeController(themeManager)
    internal val notifications = ToastHost()
    internal val blockRouterDemo = MutableStateFlow(true)
    internal val transitionPreset = MutableStateFlow(NavigationTransitionProfile.Adaptive)
    internal val transitionEnabled = MutableStateFlow(true)
    internal val transitionDurationScale = MutableStateFlow(1.0)
    internal val navigator: Navigator<DemoRoute> = Navigator(
        initialRoute = DemoRoute.Overview,
        routes = DemoRoute.all,
        pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
    )
    internal val transitionByState: Flow<RouteTransition> = combine(
        navigator.state,
        transitionPreset,
        transitionEnabled,
        transitionDurationScale,
    ) { state, profile, enabled, duration ->
        if (!enabled) {
            RouteTransition.None
        } else {
            profile.resolve(state.navigationType).scaleDuration(duration)
        }
    }

    init {
        navigator.beforeEach { context ->
            if (context.to.route.id == DemoRoute.RouterHost.id && blockRouterDemo.value) {
                NavigationDecision.Block("Router Host 演示当前被守卫拦截。可在 Guards 中关闭后再进入。")
            } else {
                NavigationDecision.Allow
            }
        }
    }

    override fun start(stage: Stage) {
        val root = appShell {
            topBar { topToolbar() }
            navigation { moduleNavigation() }
            content {
                routeHost(
                    scope = uiScope,
                    navigator = navigator,
                    transition = transitionByState,
                    init = {
                        paddingAll(20.0)
                    },
                ) { route ->
                    routeContent(route)
                }
            }
            details {
                tabPane {
                    tab("文档", closable = false) {
                        documentationPane()
                    }
                    tab("源码", closable = false) {
                        sourcePane()
                    }
                    tab("路由状态", closable = false) {
                        stateSnapshotPane()
                    }
                }
            }
            overlay {
                toastHost(uiScope, notifications)
            }
        }

        val scene = Scene(root, 1320.0, 820.0)
        themeController.bind(scene)

        stage.title = "KoraFX Navigation Theme"
        stage.scene = scene
        stage.show()
    }

    override fun stop() {
        uiScope.cancel()
        themeController.dispose()
    }
}
