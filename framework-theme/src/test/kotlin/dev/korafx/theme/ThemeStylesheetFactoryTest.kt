package dev.korafx.theme

import kotlin.test.Test
import kotlin.test.assertContains

class ThemeStylesheetFactoryTest {
    @Test
    fun `stylesheet contains token values`() {
        val css = ThemeStylesheetFactory.render(BuiltInThemes.Light)

        assertContains(css, ".root.korafx-root")
        assertContains(css, "-fx-background-color: #F6F7FB;")
        assertContains(css, "-fx-font-family: \"Segoe UI\", \"Microsoft YaHei UI\", sans-serif;")
        assertContains(css, "-fx-font-size: 14px;")
        assertContains(css, "-fx-background-radius: 14px;")
        assertContains(css, ".button.nav-button-active")
        assertContains(css, ".card")
        assertContains(css, ".section-title")
        assertContains(css, ".action-bar")
        assertContains(css, ".app-shell")
        assertContains(css, ".modal-card")
        assertContains(css, ".modal-backdrop")
        assertContains(css, ".snackbar")
        assertContains(css, ".toast-success")
        assertContains(css, ".feedback-state")
        assertContains(css, ".loading-state-indicator")
    }

    @Test
    fun `all built in themes render stylesheet from tokens`() {
        BuiltInThemes.all.forEach { theme ->
            val css = ThemeStylesheetFactory.render(theme)

            assertContains(css, "-fx-background-color: ${theme.tokens.colors.surface};")
            assertContains(css, "-fx-font-size: ${theme.tokens.typography.baseSize}px;")
            assertContains(css, "-fx-background-radius: ${theme.tokens.radius}px;")
        }
    }
}
