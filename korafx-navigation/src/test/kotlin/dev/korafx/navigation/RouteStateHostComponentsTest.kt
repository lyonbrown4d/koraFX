package dev.korafx.navigation

import dev.korafx.dsl.RenderState
import dev.korafx.dsl.label
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteStateHostComponentsTest {
    @Test
    fun `route state host renders state for current route`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)
        val home = MutableStateFlow<RenderState<List<String>>>(RenderState.Loading)
        val settings = MutableStateFlow<RenderState<List<String>>>(RenderState.Empty)

        try {
            val host = routeStateHostFor(scope, navigator, home, settings)

            FxTestSupport.waitForFxCondition { host.labels() == listOf("loading:home") }
            home.value = RenderState.Content(listOf("alpha", "beta"))
            FxTestSupport.waitForFxCondition { host.labels() == listOf("home:alpha", "home:beta") }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { host.labels() == listOf("empty:settings") }

            settings.value = RenderState.Failed("Offline")
            FxTestSupport.waitForFxCondition { host.labels() == listOf("failed:settings:Offline") }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route state host ignores stale route state updates after navigation`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all)
        val home = MutableStateFlow<RenderState<List<String>>>(RenderState.Content(listOf("home-initial")))
        val settings = MutableStateFlow<RenderState<List<String>>>(RenderState.Content(listOf("settings-initial")))

        try {
            val host = routeStateHostFor(scope, navigator, home, settings)

            FxTestSupport.waitForFxCondition { host.labels() == listOf("home:home-initial") }
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { host.labels() == listOf("settings:settings-initial") }
            home.value = RenderState.Content(listOf("home-stale"))
            FxTestSupport.waitForFxCondition { host.labels() == listOf("settings:settings-initial") }
        } finally {
            scope.cancel()
        }
    }

    private fun routeStateHostFor(
        scope: CoroutineScope,
        navigator: Navigator<TestRoute>,
        home: MutableStateFlow<RenderState<List<String>>>,
        settings: MutableStateFlow<RenderState<List<String>>>,
    ): VBox =
        FxTestSupport.run {
            lateinit var result: VBox
            runOnFxThread {
                result = routeStateHost(
                    scope = scope,
                    navigator = navigator,
                    stateFor = { route ->
                        when (route) {
                            TestRoute.Home -> home
                            TestRoute.Settings -> settings
                            else -> error("Unexpected route: ${route.id}")
                        }
                    },
                    loading = { route -> label("loading:${route.id}") },
                    empty = { route -> label("empty:${route.id}") },
                    failed = { route, failure -> label("failed:${route.id}:${failure.message}") },
                ) { route, rows ->
                    rows.forEach { row -> label("${route.id}:$row") }
                }
            }
            result
        }
}
