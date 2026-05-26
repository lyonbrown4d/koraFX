package dev.korafx.framework.theme

import kotlin.test.assertContains

internal fun assertTokenStyles(css: String, material: KoraTheme) {
    val states = material.tokens.states

    assertContains(css, ".root.${ThemeStyleClass.Root}")
    assertContains(css, "-fx-background-color: #FFFBFE;")
    assertContains(css, "-fx-font-family: \"Roboto\", \"Segoe UI\", \"Microsoft YaHei UI\", sans-serif;")
    assertContains(css, "-fx-font-size: 14px;")
    assertContains(css, "-fx-selection-bar: ${states.selected};")
    assertContains(css, "-fx-selection-bar-text: ${states.selectedText};")
    assertContains(css, "-fx-cell-hover-color: ${states.rowHover};")
    assertContains(css, "-fx-background-radius: 12px;")
    assertContains(css, "-fx-background-radius: 16px;")
    assertContains(css, "-fx-padding: 10 16 10 16;")
    assertContains(css, "-fx-background-color: ${states.controlHover};")
    assertContains(css, "-fx-background-color: ${states.surfaceHover};")
    assertContains(css, "-fx-opacity: 0.56;")
    assertContains(css, "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.06), 10, 0.10, 0, 2);")
    assertContains(css, "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.14), 14, 0.12, 0, 4);")
    assertContains(css, "-fx-text-fill: ${material.tokens.colors.textPrimary};")
    assertContains(css, "-fx-border-color: ${material.tokens.colors.success};")
    assertContains(css, "-fx-border-color: ${material.tokens.colors.danger};")
}

internal fun assertComponentStyles(css: String) {
    listOf(
        ".button.nav-button-active",
        ".button.route-button",
        ".button.route-button-active",
        ".menu-button > .label",
        ".menu-button > .arrow-button",
        ".menu-button > .arrow-button > .arrow",
        ".split-menu-button",
        ".split-menu-button > .label",
        ".split-menu-button > .arrow-button",
        ".split-menu-button:armed > .label",
        ".split-menu-button:showing > .arrow-button",
        ".split-menu-button:focused",
        ".context-menu",
        ".menu-item:selected",
        ".menu-item:disabled",
        ".check-menu-item:selected",
        ".check-menu-item:disabled",
        ".radio-menu-item:selected",
        ".radio-menu-item:disabled",
        ".menu-item:focused .label",
        ".check-menu-item:checked > .left-container > .check",
        ".menu-bar > .container > .menu-button",
        ".menu-bar > .container > .menu-button:focused",
        ".menu-bar > .container > .menu-button > .label",
        ".menu-bar > .container > .menu-button:disabled",
        ".menu-bar > .container > .menu-button:showing > .label",
        ".card",
        ".section-title",
        ".action-bar",
        ".form",
        ".form-item",
        ".form-label",
        ".form-helper",
        ".validation-message",
        ".submit-bar",
        ".route-state-host",
        ".badge",
        ".chip",
        ".chip.chip-selected",
        ".tone-success",
        ".tone-warning",
        ".tone-danger",
        ".metric-card",
        ".metric-value",
        ".alert-banner",
        ".alert-title",
        ".alert-message",
        ".alert-action",
        ".breadcrumb",
        ".breadcrumb-item-link",
        ".breadcrumb-item-current",
        ".page-header",
        ".page-header-title",
        ".page-header-actions",
        ".status-bar",
        ".status-item",
        ".modal-card",
        ".modal-backdrop",
        ".snackbar",
        ".toast-success",
        ".feedback-state",
        ".feedback-graphic",
        ".feedback-actions",
        ".feedback-action",
        ".loading-state-indicator",
    ).forEach { selector ->
        assertContains(css, selector)
    }
}
