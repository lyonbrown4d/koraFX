package dev.korafx.framework.theme

import kotlin.test.Test
import kotlin.test.assertContains

class ThemeStylesheetFactoryTest {
    @Test
    fun `stylesheet contains token values`() {
        val material = BuiltInThemes.MaterialLight
        val css = ThemeStylesheetFactory.render(material)

        assertTokenStyles(css, material)
        assertComponentStyles(css)
    }

    @Test
    fun `stylesheet covers common JavaFX control skins`() {
        val css = ThemeStylesheetFactory.render(BuiltInThemes.MaterialLight)

        assertCommonControlStyles(css)
        assertWorkbenchControlStyles(css)
        assertAdvancedComponentStyles(css)
    }

    @Test
    fun `fluent themes render windows typography and shell surfaces`() {
        listOf(BuiltInThemes.FluentLight, BuiltInThemes.FluentDark).forEach { theme ->
            val css = ThemeStylesheetFactory.render(theme)

            assertContains(css, "-fx-font-family: \"Segoe UI\", \"Microsoft YaHei UI\", sans-serif;")
            assertContains(css, "-fx-accent: ${theme.tokens.colors.primary};")
            assertContains(css, "-fx-background-color: ${theme.tokens.colors.surface};")
            assertContains(css, "-fx-background-radius: 4px;")
            assertContains(css, ".app-shell")
            assertContains(css, ".app-shell-frame")
            assertContains(css, ".app-shell-navigation")
            assertContains(css, ".workspace-layout")
            assertContains(css, ".workspace-layout-content")
        }
    }

    @Test
    fun `all built in themes render stylesheet from tokens`() {
        BuiltInThemes.all.forEach { theme ->
            val css = ThemeStylesheetFactory.render(theme)

            assertContains(css, "-fx-background-color: ${theme.tokens.colors.surface};")
            assertContains(css, "-fx-font-family: ${theme.tokens.typography.fontFamily};")
            assertContains(css, "-fx-font-size: ${theme.tokens.typography.baseSize}px;")
            assertContains(css, "-fx-background-radius: ${theme.tokens.radius}px;")
            assertContains(css, "-fx-background-color: ${theme.tokens.states.controlHover};")
            assertContains(css, "-fx-background-color: ${theme.tokens.states.surfaceHover};")
            assertContains(css, "-fx-selection-bar-text: ${theme.tokens.states.selectedText};")
            assertContains(css, "-fx-opacity: ${theme.tokens.states.disabledOpacity};")
            assertContains(css, "-fx-effect: ${theme.tokens.elevation.card};")
            assertContains(css, "-fx-effect: ${theme.tokens.elevation.dropdown};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.success};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.warning};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.danger};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.info};")
        }
    }
}
