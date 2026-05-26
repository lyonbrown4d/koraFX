package dev.korafx.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigatorPathTest {
    @Test
    fun `navigate path resolves dynamic params query and hash`() {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

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
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

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
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

        assertTrue(navigator.navigatePath("/workspace/files"))
        assertEquals(PathTestRoute.LocalizedWorkspace, navigator.currentRoute)
        assertNull(navigator.currentLocation.params["locale"])
        assertEquals("files", navigator.currentLocation.params["section"])

        assertTrue(navigator.navigatePath("/zh/workspace/settings"))
        assertEquals("zh", navigator.currentLocation.params["locale"])
        assertEquals("settings", navigator.currentLocation.params["section"])
    }

    @Test
    fun `nested path routes inherit parent path segments`() {
        val navigator = Navigator(initialRoute = NestedPathRouteSpec.Root, routes = NestedPathRouteSpec.all)

        assertTrue(navigator.navigatePath("/workspace"))
        assertEquals(NestedPathRouteSpec.Workspace, navigator.currentRoute)

        assertTrue(navigator.navigatePath("/workspace/users/42"))
        assertEquals(NestedPathRouteSpec.WorkspaceSection, navigator.currentRoute)
        assertEquals("42", navigator.currentLocation.params["userId"])
        assertEquals("/workspace/users/42", navigator.currentLocation.path)
    }

    @Test
    fun `path matcher prefers more specific routes over declaration order`() {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

        assertTrue(navigator.navigatePath("/projects/settings"))
        assertEquals(PathTestRoute.ProjectSettings, navigator.currentRoute)
        assertTrue(navigator.navigatePath("/projects/42"))
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
    fun `path routes can generate locations and typed params can be read`() {
        val path = PathTestRoute.Project.location(
            params = mapOf("projectId" to 42, "tab" to "source view"),
            query = mapOf("page" to 2, "enabled" to true, "filter" to listOf("open", "assigned")),
            hash = "row 4",
        )
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

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
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

        assertTrue(navigator.navigatePath("/projects/42/files?mode=review&tag=a#diff"))

        val location = navigator.currentLocation
        assertEquals("/projects/42/files?mode=edit&tag=a&page=2#diff", location.withQuery("mode" to "edit", "page" to 2))
        assertEquals("/projects/42/files?page=2#diff", location.withQuery(mapOf("page" to 2), preserveExisting = false))
        assertEquals("/projects/42/files?tag=a#diff", location.withoutQuery("mode"))
        assertEquals("/projects/42/files?mode=review&tag=a#row%204", location.withHash("row 4"))
        assertEquals("/projects/42/files?mode=review&tag=a", location.withoutHash())
    }

    @Test
    fun `navigator stores route restoration state by full path`() {
        val navigator = Navigator(initialRoute = PathTestRoute.Home, routes = PathTestRoute.all)

        assertTrue(navigator.navigatePath("/projects/42?tab=files"))
        navigator.saveState("scrollY", 320.0)
        assertEquals(320.0, navigator.restoredState<Double>("scrollY"))

        assertTrue(navigator.navigatePath("/projects/43?tab=files"))
        assertNull(navigator.restoredState<Double>("scrollY"))

        assertTrue(navigator.back())
        assertEquals(320.0, navigator.restoredState<Double>("scrollY"))
    }
}
