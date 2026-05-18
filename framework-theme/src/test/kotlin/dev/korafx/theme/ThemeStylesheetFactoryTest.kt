package dev.korafx.theme

import kotlin.test.Test
import kotlin.test.assertContains

class ThemeStylesheetFactoryTest {
    @Test
    fun `stylesheet contains token values`() {
        val css = ThemeStylesheetFactory.render(BuiltInThemes.Light)

        assertContains(css, ".root.${ThemeStyleClass.Root}")
        assertContains(css, "-fx-background-color: #F6F7FB;")
        assertContains(css, "-fx-font-family: \"Segoe UI\", \"Microsoft YaHei UI\", sans-serif;")
        assertContains(css, "-fx-font-size: 14px;")
        assertContains(css, "-fx-background-radius: 14px;")
        assertContains(css, "-fx-background-radius: 18px;")
        assertContains(css, "-fx-padding: 10 16 10 16;")
        assertContains(css, "-fx-background-color: derive(#246BFD, -8%);")
        assertContains(css, "-fx-background-color: derive(#FFFFFF, -4%);")
        assertContains(css, "-fx-opacity: 0.56;")
        assertContains(css, "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.06), 10, 0.10, 0, 2);")
        assertContains(css, ".button.nav-button-active")
        assertContains(css, ".card")
        assertContains(css, ".section-title")
        assertContains(css, ".action-bar")
        assertContains(css, ".form")
        assertContains(css, ".form-item")
        assertContains(css, ".form-label")
        assertContains(css, ".form-helper")
        assertContains(css, ".validation-message")
        assertContains(css, ".submit-bar")
        assertContains(css, ".route-state-host")
        assertContains(css, ".app-shell")
        assertContains(css, ".modal-card")
        assertContains(css, ".modal-backdrop")
        assertContains(css, ".snackbar")
        assertContains(css, ".toast-success")
        assertContains(css, "-fx-border-color: #16A34A;")
        assertContains(css, "-fx-border-color: #DC2626;")
        assertContains(css, ".feedback-state")
        assertContains(css, ".loading-state-indicator")
    }

    @Test
    fun `stylesheet covers common JavaFX control skins`() {
        val css = ThemeStylesheetFactory.render(BuiltInThemes.Light)

        assertContains(css, ".combo-box-base")
        assertContains(css, ".choice-box")
        assertContains(css, ".date-picker")
        assertContains(css, ".spinner")
        assertContains(css, ".list-cell:selected")
        assertContains(css, ".table-view .column-header")
        assertContains(css, ".tree-view .tree-cell .tree-disclosure-node .arrow")
        assertContains(css, ".tab-pane .tab:selected")
        assertContains(css, ".titled-pane > .title")
        assertContains(css, ".slider .thumb")
        assertContains(css, ".pagination .pagination-control .page-number:selected")
        assertContains(css, ".scroll-bar .thumb")
    }

    @Test
    fun `all built in themes render stylesheet from tokens`() {
        BuiltInThemes.all.forEach { theme ->
            val css = ThemeStylesheetFactory.render(theme)

            assertContains(css, "-fx-background-color: ${theme.tokens.colors.surface};")
            assertContains(css, "-fx-font-size: ${theme.tokens.typography.baseSize}px;")
            assertContains(css, "-fx-background-radius: ${theme.tokens.radius}px;")
            assertContains(css, "-fx-background-color: ${theme.tokens.states.controlHover};")
            assertContains(css, "-fx-background-color: ${theme.tokens.states.surfaceHover};")
            assertContains(css, "-fx-opacity: ${theme.tokens.states.disabledOpacity};")
            assertContains(css, "-fx-effect: ${theme.tokens.elevation.card};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.success};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.warning};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.danger};")
            assertContains(css, "-fx-border-color: ${theme.tokens.colors.info};")
        }
    }
}
