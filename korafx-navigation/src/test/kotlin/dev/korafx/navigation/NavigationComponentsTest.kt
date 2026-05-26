package dev.korafx.navigation

import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NavigationComponentsTest {
    @Test
    fun `navigation rail renders routes and updates active button`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(initialRoute = TestRoute.Home, routes = TestRoute.all)

        try {
            val rail = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.VBox
                runOnFxThread {
                    result = navigationRail(scope = scope, navigator = navigator, icon = { BootstrapIcons.ALARM })
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                rail.children.size == 2 &&
                    (rail.children[0] as Button).styleClass.contains("nav-button-active")
            }
            assertEquals(BootstrapIcons.ALARM, assertIs<FontIcon>((rail.children[0] as Button).graphic).iconCode)

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
    fun `navigation rail uses async navigation guards`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(initialRoute = TestRoute.Home, routes = TestRoute.all)
        val guardCalls = AtomicInteger(0)
        navigator.beforeEnterAsync(TestRoute.Settings) {
            guardCalls.incrementAndGet()
            NavigationDecision.Block("Settings disabled")
        }

        try {
            val rail = FxTestSupport.run {
                lateinit var result: javafx.scene.layout.VBox
                runOnFxThread {
                    result = navigationRail(scope = scope, navigator = navigator)
                }
                result
            }

            FxTestSupport.waitForFxCondition { rail.children.size == 2 }
            FxTestSupport.runOnFxThread {
                (rail.children[1] as Button).fire()
            }

            FxTestSupport.waitForFxCondition { guardCalls.get() == 1 }
            assertEquals(TestRoute.Home, navigator.currentRoute)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `path buttons navigate by path and update active state`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(initialRoute = PathComponentRoute.Home, routes = PathComponentRoute.all)

        try {
            val button = FxTestSupport.run {
                lateinit var result: Button
                runOnFxThread {
                    result = pathButton(
                        scope = scope,
                        navigator = navigator,
                        path = "/projects/42?tab=files",
                        text = "Project",
                    )
                }
                result
            }

            FxTestSupport.waitForFxCondition { button.styleClass.contains("route-button-inactive") }
            FxTestSupport.runOnFxThread { button.fire() }

            FxTestSupport.waitForFxCondition {
                navigator.currentRoute == PathComponentRoute.Project &&
                    navigator.currentLocation.params["projectId"] == "42" &&
                    button.styleClass.contains("route-button-active")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route links navigate by route and expose route link state`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(initialRoute = TestRoute.Home, routes = TestRoute.all)
        var activeState: Boolean? = null

        try {
            val link = FxTestSupport.run {
                lateinit var result: Hyperlink
                runOnFxThread {
                    result = routeLink(scope = scope, navigator = navigator, route = TestRoute.Settings) { state ->
                        activeState = state.active
                    }
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                activeState == false && link.styleClass.contains("route-link-inactive")
            }
            FxTestSupport.runOnFxThread { link.fire() }

            FxTestSupport.waitForFxCondition {
                navigator.currentRoute == TestRoute.Settings &&
                    activeState == true &&
                    link.styleClass.contains("route-link-active")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route host recreates nodes for recreate policy`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(TestRoute.Home, TestRoute.all, PageInstancePolicy.RECREATE)
        var created = 0

        try {
            val host = routeHostFor(scope, navigator) { route ->
                created += 1
                Label(route.title)
            }

            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == TestRoute.Home.title }
            val firstHome = host.children.single()
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == TestRoute.Settings.title }
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
        val navigator = Navigator(TestRoute.Home, TestRoute.all, PageInstancePolicy.KEEP_ALIVE)

        try {
            val host = routeHostFor(scope, navigator) { route -> Label(route.title) }

            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == TestRoute.Home.title }
            val firstHome = host.children.single()
            navigator.navigate(TestRoute.Settings.id)
            FxTestSupport.waitForFxCondition { (host.children.singleOrNull() as? Label)?.text == TestRoute.Settings.title }
            navigator.navigate(TestRoute.Home.id)
            FxTestSupport.waitForFxCondition { host.children.singleOrNull() === firstHome }
            assertSame(firstHome, host.children.single())
        } finally {
            scope.cancel()
        }
    }
}
