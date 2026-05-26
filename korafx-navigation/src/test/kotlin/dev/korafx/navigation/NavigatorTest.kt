package dev.korafx.navigation

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigatorTest {
    @Test
    fun `navigator exposes initial state`() {
        val navigator = Navigator(
            initialRoute = NavigatorTestRoute.Home,
            routes = NavigatorTestRoute.all,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )

        assertEquals(NavigatorTestRoute.Home, navigator.currentRoute)
        assertEquals(NavigatorTestRoute.all, navigator.routes)
        assertEquals(PageInstancePolicy.KEEP_ALIVE, navigator.state.value.pageInstancePolicy)
    }

    @Test
    fun `navigate by route id updates current route`() {
        val navigator = Navigator(initialRoute = NavigatorTestRoute.Home, routes = NavigatorTestRoute.all)

        val changed = navigator.navigate(NavigatorTestRoute.Settings.id)

        assertTrue(changed)
        assertEquals(NavigatorTestRoute.Settings, navigator.currentRoute)
        assertEquals(NavigatorTestRoute.Settings, navigator.state.value.currentRoute)
    }

    @Test
    fun `navigate by route object resolves registered route instance`() {
        val navigator = Navigator(initialRoute = NavigatorTestRoute.Home, routes = NavigatorTestRoute.all)

        navigator.navigate(NavigatorTestRoute.Settings.copy(title = "External settings"))

        assertEquals(NavigatorTestRoute.Settings, navigator.currentRoute)
    }

    @Test
    fun `navigate returns false for unknown route id`() {
        val navigator = Navigator(initialRoute = NavigatorTestRoute.Home, routes = NavigatorTestRoute.all)

        val changed = navigator.navigate("missing")

        assertFalse(changed)
        assertEquals(NavigatorTestRoute.Home, navigator.currentRoute)
    }

    @Test
    fun `navigator rejects invalid route lists`() {
        assertFailsWith<IllegalArgumentException> {
            Navigator(initialRoute = NavigatorTestRoute.Home, routes = emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            Navigator(initialRoute = NavigatorTestRoute.Home, routes = listOf(NavigatorTestRoute.Settings))
        }
        assertFailsWith<IllegalArgumentException> {
            Navigator(
                initialRoute = NavigatorTestRoute.Home,
                routes = listOf(NavigatorTestRoute.Home, NavigatorTestRoute.Home.copy(title = "Duplicate")),
            )
        }
    }

    @Test
    fun `navigator supports back forward and replace history`() {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

        assertTrue(navigator.navigatePath("/projects/42"))
        assertTrue(navigator.navigatePath("/settings"))
        assertTrue(navigator.canGoBack)
        assertEquals(PathTestRoute.Settings, navigator.currentRoute)

        assertTrue(navigator.back())
        assertEquals(PathTestRoute.Project, navigator.currentRoute)
        assertTrue(navigator.canGoForward)

        assertTrue(navigator.forward())
        assertEquals(PathTestRoute.Settings, navigator.currentRoute)

        assertTrue(navigator.replacePath("/login"))
        assertEquals(PathTestRoute.Login, navigator.currentRoute)
        assertTrue(navigator.back())
        assertEquals(PathTestRoute.Project, navigator.currentRoute)
    }

    @Test
    fun `navigation guards can block or redirect navigation`() {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)
        navigator.beforeEach { context ->
            when {
                context.to.route == PathTestRoute.Settings -> NavigationDecision.Block("Settings disabled")
                context.to.meta.boolean("requiresAuth") -> NavigationDecision.Redirect(path = "/login")
                else -> NavigationDecision.Allow
            }
        }

        assertFalse(navigator.navigatePath("/settings"))
        assertEquals(PathTestRoute.Home, navigator.currentRoute)

        assertTrue(navigator.navigatePath("/admin"))
        assertEquals(PathTestRoute.Login, navigator.currentRoute)
    }

    @Test
    fun `navigation can precheck target route by id or path`() {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

        assertEquals("allow", when (navigator.canNavigate(PathTestRoute.Project.id)) {
            is NavigationDecision.Allow -> "allow"
            is NavigationDecision.Redirect -> "redirect"
            is NavigationDecision.Block -> "block"
        })

        when (val decision = navigator.canNavigate("missing-route")) {
            is NavigationDecision.Block -> assertEquals("Unknown route 'missing-route'.", decision.reason)
            else -> throw AssertionError("Expected block decision.")
        }

        assertTrue(navigator.canNavigatePath("/projects/99") is NavigationDecision.Allow)
        assertEquals(null, navigator.canNavigatePath("/i/definitely/do/not/exist"))
    }

    @Test
    fun `route enter and leave guards can block or redirect navigation`() {
        val navigator = Navigator(initialRoute = PathTestRoute.Project, routes = PathTestRoute.all)
        val leaveGuard = navigator.beforeLeave(PathTestRoute.Project) {
            NavigationDecision.Block("Project has unsaved changes")
        }

        assertFalse(navigator.navigatePath("/settings"))
        assertEquals(PathTestRoute.Project, navigator.currentRoute)

        leaveGuard.close()
        navigator.beforeEnter(PathTestRoute.Admin) {
            NavigationDecision.Redirect(path = "/login")
        }

        assertTrue(navigator.navigatePath("/admin"))
        assertEquals(PathTestRoute.Login, navigator.currentRoute)
    }

    @Test
    fun `async navigation guards run for suspend navigation`() = runTest {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)
        navigator.beforeEachAsync { context ->
            if (context.to.route == PathTestRoute.Settings) {
                NavigationDecision.Block("Settings disabled")
            } else {
                NavigationDecision.Allow
            }
        }

        assertFalse(navigator.navigatePathAsync("/settings"))
        assertEquals(PathTestRoute.Home, navigator.currentRoute)

        assertTrue(navigator.navigatePathAsync("/projects/42"))
        assertEquals(PathTestRoute.Project, navigator.currentRoute)
    }

    @Test
    fun `async route guards run for suspend navigation`() = runTest {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)
        navigator.beforeEnterAsync(PathTestRoute.Settings) {
            NavigationDecision.Block("Settings disabled")
        }

        assertFalse(navigator.navigatePathAsync("/settings"))
        assertEquals(PathTestRoute.Home, navigator.currentRoute)
    }

    @Test
    fun `navigator exposes typed result streams`() = runTest {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)
        val pending = async {
            navigator.results<String>("selected-project").first()
        }

        navigator.setResult("selected-project", "42")

        assertEquals("42", pending.await())
    }

    @Test
    fun `navigator supports typed result keys and awaiting results`() = runTest {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)
        val selectedProject = navigationResultKey<String>("selected-project")
        val pending = async {
            navigator.awaitResult(selectedProject)
        }

        navigator.setResult(selectedProject, "42")

        assertEquals("42", pending.await())
    }
}
