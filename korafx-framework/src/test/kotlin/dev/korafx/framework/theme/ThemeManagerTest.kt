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
        assertSame(BuiltInThemes.MaterialLight, BuiltInThemes.requireById("material-light"))
        assertSame(BuiltInThemes.MaterialDark, BuiltInThemes.findById("material-dark"))
        assertFailsWith<IllegalStateException> {
            BuiltInThemes.requireById("missing")
        }
    }

    @Test
    fun `theme manager exposes and updates current theme`() {
        val manager = ThemeManager()

        assertEquals(BuiltInThemes.MaterialLight, manager.currentTheme())
        assertEquals(BuiltInThemes.MaterialLight, manager.theme.value)

        manager.setTheme(BuiltInThemes.MaterialDark)

        assertEquals(BuiltInThemes.MaterialDark, manager.currentTheme())
        assertEquals(BuiltInThemes.MaterialDark, manager.theme.value)
    }

    @Test
    fun `theme manager sets theme by available id`() {
        val manager = ThemeManager()

        manager.setTheme("material-dark")

        assertEquals(BuiltInThemes.MaterialDark, manager.currentTheme())
    }

    @Test
    fun `theme manager cycles through available themes`() {
        val manager = ThemeManager(
            initialTheme = BuiltInThemes.MaterialLight,
            availableThemes = listOf(BuiltInThemes.MaterialLight, BuiltInThemes.MaterialDark),
        )

        manager.nextTheme()
        assertEquals(BuiltInThemes.MaterialDark, manager.currentTheme())

        manager.nextTheme()
        assertEquals(BuiltInThemes.MaterialLight, manager.currentTheme())

        manager.nextTheme()
        assertEquals(BuiltInThemes.MaterialDark, manager.currentTheme())

        manager.previousTheme()
        assertEquals(BuiltInThemes.MaterialLight, manager.currentTheme())
    }

    @Test
    fun `theme manager toggles between Material light and dark themes`() {
        val manager = ThemeManager()

        manager.toggle()
        assertEquals(BuiltInThemes.MaterialDark, manager.currentTheme())

        manager.toggle()
        assertEquals(BuiltInThemes.MaterialLight, manager.currentTheme())
    }

    @Test
    fun `material light and dark themes have distinct token sets`() {
        val light = BuiltInThemes.MaterialLight
        val dark = BuiltInThemes.MaterialDark

        assertTrue(light.tokens.colors.surface != dark.tokens.colors.surface)
        assertTrue(light.tokens.colors.surfaceMuted != dark.tokens.colors.surfaceMuted)
        assertTrue(light.tokens.colors.textPrimary != dark.tokens.colors.textPrimary)
        assertTrue(light.tokens.colors.textSecondary != dark.tokens.colors.textSecondary)
        assertTrue(light.tokens.colors.border != dark.tokens.colors.border)
        assertTrue(light.tokens.states.selected != dark.tokens.states.selected)
        assertEquals("#FFFFFF", light.tokens.states.selectedText)
        assertEquals("#381E72", dark.tokens.states.selectedText)
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
            StateColorTokens.from(BuiltInThemes.MaterialLight.tokens.colors).copy(disabledOpacity = 1.4)
        }
        assertFailsWith<IllegalArgumentException> {
            BuiltInThemes.MaterialLight.tokens.copy(radius = -1)
        }
    }
}
