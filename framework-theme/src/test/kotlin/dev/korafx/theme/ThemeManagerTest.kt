package dev.korafx.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeManagerTest {
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
    fun `theme manager toggles between built in light and dark themes`() {
        val manager = ThemeManager()

        manager.toggle()
        assertEquals(BuiltInThemes.Dark, manager.currentTheme())

        manager.toggle()
        assertEquals(BuiltInThemes.Light, manager.currentTheme())
    }
}
