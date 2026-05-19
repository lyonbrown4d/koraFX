package dev.korafx.framework

import dev.korafx.framework.navigation.PageInstancePolicy
import dev.korafx.framework.navigation.Route
import dev.korafx.framework.theme.BuiltInThemes
import javafx.scene.layout.Pane
import java.lang.reflect.Modifier
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
                presets(listOf(BuiltInThemes.Nord, BuiltInThemes.Dark))
                default(BuiltInThemes.Nord)
                persistSelection = true
            }
            navigation {
                initialRoute = TestRoute.Home
                routes(TestRoute.entries)
                pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE
            }
            content {
                Pane()
            }
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
        assertEquals(BuiltInThemes.Nord, spec.theme.defaultTheme)
        assertTrue(spec.theme.persistSelection)
        assertSame(TestRoute.Home, spec.navigation.initialRoute)
        assertEquals(TestRoute.entries.toList(), spec.navigation.routes)
        assertEquals(PageInstancePolicy.KEEP_ALIVE, spec.navigation.pageInstancePolicy)
        assertNotNull(spec.contentFactory)
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

    private class TestCloseable : AutoCloseable {
        override fun close() = Unit
    }
}
