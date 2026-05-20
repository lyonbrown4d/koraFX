package dev.korafx.framework

import dev.korafx.navigation.PageInstancePolicy
import dev.korafx.navigation.PathRoute
import dev.korafx.navigation.Route
import dev.korafx.framework.theme.BuiltInThemes
import javafx.scene.layout.Pane
import java.lang.reflect.Modifier
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KoraApplicationBuilderTest {
    @Test
    fun `builder captures application structure`() {
        val module = org.koin.dsl.module {}

        val spec = KoraApplicationBuilder().apply {
            window {
                title = "Workbench"
                size(1280.0, 820.0)
            }
            installKoin {
                modules(module)
            }
            theme {
                presets(listOf(BuiltInThemes.MaterialLight))
                default(BuiltInThemes.MaterialLight)
                persistSelection = true
            }
            navigation {
                initialRoute = TestRoute.Home
                initialPath = "/settings"
                routes(TestRoute.entries)
                pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE
                persistLocation = true
                preferencesNode = "dev.korafx.test.navigation"
                preferencesKey = "location"
            }
            content {
                Pane()
            }
            install(object : KoraApplicationPlugin {})
            lifecycle {
                close<TestCloseable>()
                onStop {
                    // Captured by the spec; actual invocation is covered by application shutdown.
                }
            }
        }.build()

        assertEquals("Workbench", spec.window.title)
        assertEquals(1280.0, spec.window.width)
        assertEquals(820.0, spec.window.height)
        assertEquals(listOf(module), spec.modules)
        assertEquals(BuiltInThemes.MaterialLight, spec.theme.defaultTheme)
        assertTrue(spec.theme.persistSelection)
        assertSame(TestRoute.Home, spec.navigation.initialRoute)
        assertEquals("/settings", spec.navigation.initialPath)
        assertEquals(TestRoute.entries.toList(), spec.navigation.routes)
        assertEquals(PageInstancePolicy.KEEP_ALIVE, spec.navigation.pageInstancePolicy)
        assertTrue(spec.navigation.persistLocation)
        assertEquals("dev.korafx.test.navigation", spec.navigation.preferencesNode)
        assertEquals("location", spec.navigation.preferencesKey)
        assertNotNull(spec.contentFactory)
        assertEquals(1, spec.plugins.size)
        assertEquals(2, spec.stopHandlers.size)
    }

    @Test
    fun `navigation defaults to a root route when not configured`() {
        val spec = KoraApplicationBuilder().build()

        assertEquals("root", spec.navigation.initialRoute.id)
        assertEquals("Root", spec.navigation.initialRoute.title)
        assertEquals(listOf(spec.navigation.initialRoute), spec.navigation.routes)
    }

    @Test
    fun `navigation resolves saved persisted path before configured initial path`() {
        val nodeName = "dev.korafx.test.navigation.${System.nanoTime()}"
        val preferences = Preferences.userRoot().node(nodeName)
        preferences.put("location", "/settings")

        try {
            val spec = KoraApplicationBuilder().apply {
                navigation {
                    initialRoute = PathTestRoute.Home
                    initialPath = "/home"
                    routes(PathTestRoute.all)
                    persistLocation = true
                    preferencesNode = nodeName
                    preferencesKey = "location"
                }
            }.build()

            assertEquals("/settings", spec.navigation.resolveInitialPath())
        } finally {
            preferences.removeNode()
        }
    }

    @Test
    fun `javafx launcher application is public for reflective launch`() {
        val launcherClass = KoraFxApplication::class.java

        assertTrue(Modifier.isPublic(launcherClass.modifiers))
        assertTrue(Modifier.isPublic(launcherClass.getDeclaredConstructor().modifiers))
    }

    private enum class TestRoute(
        override val id: String,
        override val title: String,
    ) : Route {
        Home("home", "Home"),
        Settings("settings", "Settings"),
    }

    private data class PathTestRoute(
        override val id: String,
        override val title: String,
        override val path: String,
    ) : PathRoute {
        companion object {
            val Home = PathTestRoute("home", "Home", "/home")
            val Settings = PathTestRoute("settings", "Settings", "/settings")

            val all: List<PathTestRoute>
                get() = listOf(Home, Settings)
        }
    }

    private class TestCloseable : AutoCloseable {
        override fun close() = Unit
    }
}
