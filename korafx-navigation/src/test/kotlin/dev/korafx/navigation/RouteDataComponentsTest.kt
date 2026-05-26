package dev.korafx.navigation

import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.test.Test

class RouteDataComponentsTest {
    @Test
    fun `route data host renders loaded route data`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)

        try {
            val host = routeDataHostFor(scope, navigator) {
                routeDataHost(
                    scope = scope,
                    navigator = navigator,
                    load = { context -> "data:${context.route.id}" },
                    loading = { Label("loading") },
                ) { _, value ->
                    Label(value)
                }
            }

            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "data:home" }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "data:settings" }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route data host can cache data and revalidate on demand`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)
        val controller = RouteDataController()
        var loads = 0

        try {
            val host = routeDataHostFor(scope, navigator) {
                routeDataHost(
                    scope = scope,
                    navigator = navigator,
                    controller = controller,
                    cache = true,
                    load = { context ->
                        loads += 1
                        "data:${context.route.id}:$loads"
                    },
                    loading = { Label("loading") },
                ) { _, value ->
                    Label(value)
                }
            }

            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "data:home:1" }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "data:settings:2" }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "data:home:1" }

            controller.revalidate()
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == "data:home:3" }
        } finally {
            scope.cancel()
        }
    }

    private fun routeDataHostFor(
        scope: CoroutineScope,
        navigator: Navigator<TestRoute>,
        content: () -> StackPane,
    ): StackPane =
        FxTestSupport.run {
            lateinit var result: StackPane
            runOnFxThread {
                result = content()
            }
            result
        }
}
