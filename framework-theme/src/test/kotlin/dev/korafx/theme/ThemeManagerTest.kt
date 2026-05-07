package dev.korafx.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ThemeManagerTest {
    @Test
    fun `built in themes expose unique selectable presets`() {
        assertEquals(
            BuiltInThemes.all.size,
            BuiltInThemes.all.map(KoraTheme::id).toSet().size,
        )
        assertSame(BuiltInThemes.Light, BuiltInThemes.requireById("light"))
        assertSame(BuiltInThemes.GraphiteDark, BuiltInThemes.findById("graphite-dark"))
        assertFailsWith<IllegalStateException> {
            BuiltInThemes.requireById("missing")
        }
    }

    @Test
    fun `theme manager exposes and updates current theme`() {
        val manager = ThemeManager()

        assertEquals(BuiltInThemes.Light, manager.currentTheme())
        assertEquals(BuiltInThemes.Light, manager.theme.value)

        manager.setTheme(BuiltInThemes.Dark)

        assertEquals(BuiltInThemes.Dark, manager.currentTheme())
        assertEquals(BuiltInThemes.Dark, manager.theme.value)
    }

    @Test
    fun `theme manager sets theme by available id`() {
        val manager = ThemeManager()

        manager.setTheme("nord")

        assertEquals(BuiltInThemes.Nord, manager.currentTheme())
    }

    @Test
    fun `theme manager cycles through available themes`() {
        val manager = ThemeManager(
            initialTheme = BuiltInThemes.Light,
            availableThemes = listOf(BuiltInThemes.Light, BuiltInThemes.Nord, BuiltInThemes.GraphiteDark),
        )

        manager.nextTheme()
        assertEquals(BuiltInThemes.Nord, manager.currentTheme())

        manager.nextTheme()
        assertEquals(BuiltInThemes.GraphiteDark, manager.currentTheme())

        manager.nextTheme()
        assertEquals(BuiltInThemes.Light, manager.currentTheme())

        manager.previousTheme()
        assertEquals(BuiltInThemes.GraphiteDark, manager.currentTheme())
    }

    @Test
    fun `theme manager toggles between built in light and dark themes`() {
        val manager = ThemeManager()

        manager.toggle()
        assertEquals(BuiltInThemes.Dark, manager.currentTheme())

        manager.toggle()
        assertEquals(BuiltInThemes.Light, manager.currentTheme())
    }
}
