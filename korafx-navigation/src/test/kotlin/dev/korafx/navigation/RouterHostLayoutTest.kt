package dev.korafx.navigation

import dev.korafx.components.borderLayout
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class RouterHostLayoutTest {
    @Test
    fun `router host renders routes inside a shared layout outlet`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)
        var layoutCreated = 0

        try {
            val host = routerHostBuilderFor(scope, navigator) {
                layout(TestLayout.Workbench) {
                    shell { outlet ->
                        layoutCreated += 1
                        borderLayout {
                            top(Label("Workbench"))
                            center(outlet)
                        }
                    }
                    route(TestRoute.Home) { route -> Label("page:${route.id}") }
                    route(TestRoute.Settings) { route -> Label("page:${route.id}") }
                }
            }

            FxTestSupport.waitForFxCondition { host.currentRouterPageText() == "page:home" }
            val firstLayout = host.children.single()
            navigator.navigate(TestRoute.Settings.id)

            FxTestSupport.waitForFxCondition {
                host.children.single() === firstLayout && host.currentRouterPageText() == "page:settings"
            }
            assertEquals(1, layoutCreated)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host can switch between layout routes and direct routes`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)

        try {
            val host = routerHostBuilderFor(scope, navigator) {
                layout(TestLayout.Workbench) {
                    shell { outlet -> borderLayout { center(outlet) } }
                    route(TestRoute.Home) { route -> Label("layout:${route.id}") }
                }
                route(TestRoute.Settings) { route -> Label("direct:${route.id}") }
            }

            FxTestSupport.waitForFxCondition { host.currentRouterPageText() == "layout:home" }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == "direct:settings"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host keeps route pages alive inside layouts`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all, PageInstancePolicy.KEEP_ALIVE)

        try {
            val host = routerHostBuilderFor(scope, navigator) {
                layout(TestLayout.Workbench) {
                    shell { outlet -> borderLayout { center(outlet) } }
                    route(TestRoute.Home) { route -> Label(route.title) }
                    route(TestRoute.Settings) { route -> Label(route.title) }
                }
            }

            FxTestSupport.waitForFxCondition { host.currentRouterPageText() == TestRoute.Home.title }
            val firstHome = host.currentRouterPage()
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { host.currentRouterPageText() == TestRoute.Settings.title }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition { host.currentRouterPage() === firstHome }
            assertSame(firstHome, host.currentRouterPage())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `router host renders nested layout chains`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Settings, TestRoute.all)

        try {
            val host = routerHostBuilderFor(scope, navigator) {
                layout(TestLayout.Workbench) {
                    shell { outlet -> borderLayout { top(Label("root-layout")); center(outlet) } }
                    layout(TestLayout.Details) {
                        shell { outlet -> borderLayout { top(Label("child-layout")); center(outlet) } }
                        route(TestRoute.Settings) { route -> Label("page:${route.id}") }
                    }
                }
            }

            FxTestSupport.waitForFxCondition {
                val root = host.children.singleOrNull() as? BorderPane
                val child = (root?.center as? StackPane)?.children?.singleOrNull() as? BorderPane
                val page = (child?.center as? StackPane)?.children?.singleOrNull() as? Label
                (root?.top as? Label)?.text == "root-layout" &&
                    (child?.top as? Label)?.text == "child-layout" &&
                    page?.text == "page:settings"
            }
        } finally {
            scope.cancel()
        }
    }

    private fun <R : Route> routerHostBuilderFor(
        scope: CoroutineScope,
        navigator: Navigator<R>,
        block: RouterHostBuilder<R>.() -> Unit,
    ): StackPane = FxTestSupport.run {
        lateinit var result: StackPane
        runOnFxThread {
            result = routerHost(scope, navigator, content = block)
        }
        result
    }
}
