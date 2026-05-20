package dev.korafx.components

import dev.korafx.dsl.RenderState
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.Route
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NavigationComponentsTest {
    @Test
    fun `navigation rail renders routes and updates active button`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        try {
            val rail = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.VBox
                runOnFxThread {
                    result = navigationRail(
                        scope = scope,
                        navigator = navigator,
                        icon = { BootstrapIcons.ALARM },
                    )
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                rail.children.size == 2 &&
                    (rail.children[0] as Button).styleClass.contains("nav-button-active")
            }
            assertEquals(
                BootstrapIcons.ALARM,
                assertIs<FontIcon>((rail.children[0] as Button).graphic).iconCode,
            )

            navigator.navigate(TestRoute.Settings.id)

            FxTestSupport.waitForFxCondition {
                !(rail.children[0] as Button).styleClass.contains("nav-button-active") &&
                    (rail.children[1] as Button).styleClass.contains("nav-button-active")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route host recreates nodes for recreate policy`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
            pageInstancePolicy = PageInstancePolicy.RECREATE,
        )
        var created = 0

        try {
            val host = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.StackPane
                runOnFxThread {
                    result = routeHost(scope, navigator) { route ->
                        created += 1
                        Label(route.title)
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.children.singleOrNull() is Label
            }

            val firstHome = host.children.single()
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == TestRoute.Settings.title
            }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition {
                host.children.singleOrNull() !== firstHome &&
                    (host.children.singleOrNull() as? Label)?.text == TestRoute.Home.title
            }

            assertEquals(3, created)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route host reuses nodes for keep alive policy`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )

        try {
            val host = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.StackPane
                runOnFxThread {
                    result = routeHost(scope, navigator) { route ->
                        Label(route.title)
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == TestRoute.Home.title
            }
            val firstHome = host.children.single()

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                (host.children.singleOrNull() as? Label)?.text == TestRoute.Settings.title
            }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition {
                host.children.singleOrNull() === firstHome
            }

            assertSame(firstHome, host.children.single())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route state host renders state for current route`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        val home = MutableStateFlow<RenderState<List<String>>>(RenderState.Loading)
        val settings = MutableStateFlow<RenderState<List<String>>>(RenderState.Empty)

        try {
            val host = FxTestSupport.run {
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
                        loading = { route ->
                            label("loading:${route.id}")
                        },
                        empty = { route ->
                            label("empty:${route.id}")
                        },
                        failed = { route, failure ->
                            label("failed:${route.id}:${failure.message}")
                        },
                    ) { route, rows ->
                        rows.forEach { row ->
                            label("${route.id}:$row")
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("loading:home")
            }

            home.value = RenderState.Content(listOf("alpha", "beta"))
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("home:alpha", "home:beta")
            }

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("empty:settings")
            }

            settings.value = RenderState.Failed("Offline")
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("failed:settings:Offline")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route state host ignores stale route state updates after navigation`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )
        val home = MutableStateFlow<RenderState<List<String>>>(
            RenderState.Content(listOf("home-initial")),
        )
        val settings = MutableStateFlow<RenderState<List<String>>>(
            RenderState.Content(listOf("settings-initial")),
        )

        try {
            val host = FxTestSupport.run {
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
                    ) { route, rows ->
                        rows.forEach { row ->
                            label("${route.id}:$row")
                        }
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("home:home-initial")
            }

            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("settings:settings-initial")
            }

            home.value = RenderState.Content(listOf("home-stale"))

            FxTestSupport.waitForFxCondition {
                host.labels() == listOf("settings:settings-initial")
            }
        } finally {
            scope.cancel()
        }
    }

    private data class TestRoute(
        override val id: String,
        override val title: String,
    ) : Route {
        companion object {
            val Home = TestRoute(id = "home", title = "Home")
            val Settings = TestRoute(id = "settings", title = "Settings")

            val all: List<TestRoute>
                get() = listOf(Home, Settings)
        }
    }

    private fun VBox.labels(): List<String> =
        children.map { node -> (node as Label).text }
}
