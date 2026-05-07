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
        assertContains(css, ".feedback-state")
        assertContains(css, ".loading-state-indicator")
    }
}
