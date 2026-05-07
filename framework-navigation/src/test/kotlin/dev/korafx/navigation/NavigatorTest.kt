package dev.korafx.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
}
