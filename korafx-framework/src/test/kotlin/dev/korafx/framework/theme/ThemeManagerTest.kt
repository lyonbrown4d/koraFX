package dev.korafx.framework.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ThemeManagerTest {
    @Test
    fun `built in themes expose unique selectable presets`() {
        assertEquals(
            BuiltInThemes.all.size,
            BuiltInThemes.all.map(KoraTheme::id).toSet().size,
        )
        BuiltInThemes.all.forEach { theme ->
            assertTrue(theme.tokens.colors.success.isNotBlank())
            assertTrue(theme.tokens.colors.warning.isNotBlank())
            assertTrue(theme.tokens.colors.danger.isNotBlank())
            assertTrue(theme.tokens.colors.info.isNotBlank())
            assertTrue(theme.tokens.spacing.sm > 0)
            assertEquals(theme.tokens.radius, theme.tokens.radii.medium)
            assertTrue(theme.tokens.radii.small <= theme.tokens.radii.medium)
            assertTrue(theme.tokens.radii.medium <= theme.tokens.radii.large)
            assertTrue(theme.tokens.states.focus.isNotBlank())
            assertTrue(theme.tokens.states.invalid.isNotBlank())
            assertTrue(theme.tokens.states.disabledOpacity in 0.0..1.0)
            assertTrue(theme.tokens.elevation.card.isNotBlank())
        }
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

    @Test
    fun `theme tokens reject invalid dimensions and opacity`() {
        assertFailsWith<IllegalArgumentException> {
            SpacingTokens(sm = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            RadiusTokens(small = 4, medium = -1, large = 12)
        }
        assertFailsWith<IllegalArgumentException> {
            StateColorTokens.from(BuiltInThemes.Light.tokens.colors).copy(disabledOpacity = 1.4)
        }
        assertFailsWith<IllegalArgumentException> {
            BuiltInThemes.Light.tokens.copy(radius = -1)
        }
    }
}
