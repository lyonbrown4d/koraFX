package dev.korafx.navigation

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigatorTest {
    @Test
    fun `navigator exposes initial state`() {
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )

        assertEquals(TestRoute.Home, navigator.currentRoute)
        assertEquals(TestRoute.all, navigator.routes)
        assertEquals(PageInstancePolicy.KEEP_ALIVE, navigator.state.value.pageInstancePolicy)
    }

    @Test
    fun `navigate by route id updates current route`() {
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        val changed = navigator.navigate(TestRoute.Settings.id)

        assertTrue(changed)
        assertEquals(TestRoute.Settings, navigator.currentRoute)
        assertEquals(TestRoute.Settings, navigator.state.value.currentRoute)
    }

    @Test
    fun `navigate by route object resolves registered route instance`() {
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        navigator.navigate(TestRoute.Settings.copy(title = "External settings"))

        assertEquals(TestRoute.Settings, navigator.currentRoute)
    }

    @Test
    fun `navigate returns false for unknown route id`() {
        val navigator = Navigator(
            initialRoute = TestRoute.Home,
            routes = TestRoute.all,
        )

        val changed = navigator.navigate("missing")

        assertFalse(changed)
        assertEquals(TestRoute.Home, navigator.currentRoute)
    }

    @Test
    fun `navigator rejects invalid route lists`() {
        assertFailsWith<IllegalArgumentException> {
            Navigator(initialRoute = TestRoute.Home, routes = emptyList())
        }

        assertFailsWith<IllegalArgumentException> {
            Navigator(initialRoute = TestRoute.Home, routes = listOf(TestRoute.Settings))
        }

        assertFailsWith<IllegalArgumentException> {
            Navigator(
                initialRoute = TestRoute.Home,
                routes = listOf(TestRoute.Home, TestRoute.Home.copy(title = "Duplicate")),
            )
        }
    }

    @Test
    fun `navigate path resolves dynamic params query and hash`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

        val changed = navigator.navigatePath("/projects/42/activity?mode=edit&tag=a&tag=b#row-2")

        assertTrue(changed)
        assertEquals(PathTestRoute.Project, navigator.currentRoute)
        assertEquals("42", navigator.currentLocation.params["projectId"])
        assertEquals("activity", navigator.currentLocation.params["tab"])
        assertEquals("edit", navigator.currentLocation.query["mode"])
        assertEquals(listOf("a", "b"), navigator.currentLocation.query.all("tag"))
        assertEquals("row-2", navigator.currentLocation.hash)
        assertEquals(true, navigator.currentLocation.meta["project"])
    }

    @Test
    fun `path matcher supports optional segments and catch all splats`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

        assertTrue(navigator.navigatePath("/projects/42"))
        assertEquals("42", navigator.currentLocation.params["projectId"])
        assertNull(navigator.currentLocation.params["tab"])

        assertTrue(navigator.navigatePath("/files/src/main/App.kt"))
        assertEquals(PathTestRoute.Files, navigator.currentRoute)
        assertEquals("src/main/App.kt", navigator.currentLocation.params["*"])

        assertTrue(navigator.navigatePath("/files/src/main/My%20File.kt"))
        assertEquals("src/main/My File.kt", navigator.currentLocation.params["*"])
    }

    @Test
    fun `path matcher supports optional middle segments`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

        assertTrue(navigator.navigatePath("/workspace/files"))
        assertEquals(PathTestRoute.LocalizedWorkspace, navigator.currentRoute)
        assertNull(navigator.currentLocation.params["locale"])
        assertEquals("files", navigator.currentLocation.params["section"])

        assertTrue(navigator.navigatePath("/zh/workspace/settings"))
        assertEquals("zh", navigator.currentLocation.params["locale"])
        assertEquals("settings", navigator.currentLocation.params["section"])
    }

    @Test
    fun `path matcher prefers more specific routes over declaration order`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

        assertTrue(navigator.navigatePath("/projects/settings"))

        assertEquals(PathTestRoute.ProjectSettings, navigator.currentRoute)
        assertTrue(navigator.navigatePath("/projects/42"))
        assertEquals(PathTestRoute.Project, navigator.currentRoute)
    }

    @Test
    fun `navigator supports back forward and replace history`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

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
    fun `navigator can start from an initial path`() {
        val navigator = Navigator.fromPath(
            initialPath = "/projects/42/files",
            routes = PathTestRoute.all,
            fallbackRoute = PathTestRoute.Home,
            pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
        )

        assertEquals(PathTestRoute.Project, navigator.currentRoute)
        assertEquals("42", navigator.currentLocation.params["projectId"])
        assertEquals("files", navigator.currentLocation.params["tab"])
        assertEquals(PageInstancePolicy.KEEP_ALIVE, navigator.state.value.pageInstancePolicy)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `navigation guards can block or redirect navigation`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )
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
    fun `route enter and leave guards can block or redirect navigation`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Project,
            routes = PathTestRoute.all,
        )
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
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )
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
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )
        navigator.beforeEnterAsync(PathTestRoute.Settings) {
            NavigationDecision.Block("Settings disabled")
        }

        assertFalse(navigator.navigatePathAsync("/settings"))
        assertEquals(PathTestRoute.Home, navigator.currentRoute)
    }

    @Test
    fun `path routes can generate locations and typed params can be read`() {
        val path = PathTestRoute.Project.location(
            params = mapOf("projectId" to 42, "tab" to "source view"),
            query = mapOf("page" to 2, "enabled" to true, "filter" to listOf("open", "assigned")),
            hash = "row 4",
        )
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

        assertEquals("/projects/42/source%20view?page=2&enabled=true&filter=open&filter=assigned#row%204", path)
        assertTrue(navigator.navigatePath(path))
        assertEquals(42, navigator.currentLocation.intParam("projectId"))
        assertEquals("source view", navigator.currentLocation.requiredParam("tab"))
        assertEquals(2, navigator.currentLocation.query.int("page"))
        assertEquals(true, navigator.currentLocation.query.boolean("enabled"))
        assertEquals(listOf("open", "assigned"), navigator.currentLocation.query.all("filter"))
        assertEquals("row 4", navigator.currentLocation.hash)
    }

    @Test
    fun `navigation locations can derive query and hash paths`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

        assertTrue(navigator.navigatePath("/projects/42/files?mode=review&tag=a#diff"))

        val location = navigator.currentLocation
        assertEquals(
            "/projects/42/files?mode=edit&tag=a&page=2#diff",
            location.withQuery("mode" to "edit", "page" to 2),
        )
        assertEquals(
            "/projects/42/files?page=2#diff",
            location.withQuery(mapOf("page" to 2), preserveExisting = false),
        )
        assertEquals("/projects/42/files?tag=a#diff", location.withoutQuery("mode"))
        assertEquals("/projects/42/files?mode=review&tag=a#row%204", location.withHash("row 4"))
        assertEquals("/projects/42/files?mode=review&tag=a", location.withoutHash())
    }

    @Test
    fun `navigator stores route restoration state by full path`() {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )

        assertTrue(navigator.navigatePath("/projects/42?tab=files"))
        navigator.saveState("scrollY", 320.0)
        assertEquals(320.0, navigator.restoredState<Double>("scrollY"))

        assertTrue(navigator.navigatePath("/projects/43?tab=files"))
        assertNull(navigator.restoredState<Double>("scrollY"))

        assertTrue(navigator.back())
        assertEquals(320.0, navigator.restoredState<Double>("scrollY"))
    }

    @Test
    fun `navigator exposes typed result streams`() = runTest {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )
        val pending = async {
            navigator.results<String>("selected-project").first()
        }

        navigator.setResult("selected-project", "42")

        assertEquals("42", pending.await())
    }

    @Test
    fun `navigator supports typed result keys and awaiting results`() = runTest {
        val navigator = Navigator(
            initialRoute = PathTestRoute.Home,
            routes = PathTestRoute.all,
        )
        val selectedProject = navigationResultKey<String>("selected-project")
        val pending = async {
            navigator.awaitResult(selectedProject)
        }

        navigator.setResult(selectedProject, "42")

        assertEquals("42", pending.await())
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

    private data class PathTestRoute(
        override val id: String,
        override val title: String,
        override val path: String,
        override val meta: RouteMeta = RouteMeta.Empty,
    ) : PathRoute {
        companion object {
            val Home = PathTestRoute("home", "Home", "/")
            val Project = PathTestRoute(
                id = "project",
                title = "Project",
                path = "/projects/:projectId/:tab?",
                meta = routeMeta("project" to true),
            )
            val ProjectSettings = PathTestRoute("project-settings", "Project Settings", "/projects/settings")
            val Files = PathTestRoute("files", "Files", "/files/*")
            val LocalizedWorkspace = PathTestRoute(
                "localized-workspace",
                "Localized Workspace",
                "/:locale?/workspace/:section",
            )
            val Settings = PathTestRoute("settings", "Settings", "/settings")
            val Admin = PathTestRoute(
                id = "admin",
                title = "Admin",
                path = "/admin",
                meta = routeMeta("requiresAuth" to true),
            )
            val Login = PathTestRoute("login", "Login", "/login")

            val all: List<PathTestRoute>
                get() = listOf(Home, Project, ProjectSettings, Files, LocalizedWorkspace, Settings, Admin, Login)
        }
    }
}
