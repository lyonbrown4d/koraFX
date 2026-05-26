package dev.korafx.framework.theme

internal fun StylesheetBuilder.buttonStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states

    rule(".button", ".toggle-button", ".menu-button") {
        primaryControl(context)
    }

    rule(".button:hover", ".toggle-button:hover", ".menu-button:hover") {
        fx("background-color", states.controlHover)
    }

    rule(".button:armed", ".toggle-button:selected", ".menu-button:showing") {
        fx("background-color", states.controlPressed)
    }

    rule(".menu-button > .label") {
        fx("background-color", "transparent")
        fx("text-fill", states.selectedText)
        fx("padding", "0")
        fx("alignment", "center-left")
    }

    rule(".menu-button > .arrow-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("padding", "0 0 0 ${spacing.md}")
    }

    rule(".split-menu-button") {
        primaryControl(context)
        fx("padding", "0")
        fx("cursor", "hand")
    }

    rule(".split-menu-button > .label", ".split-menu-button > .arrow-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("text-fill", states.selectedText)
        fx("cursor", "hand")
    }

    rule(".split-menu-button > .label") {
        padding(spacing.md, spacing.xl)
        fx("alignment", "center-left")
        fx("background-radius", "${context.radius}px 0 0 ${context.radius}px")
        fx("border-radius", "${context.radius}px 0 0 ${context.radius}px")
    }

    rule(".split-menu-button > .arrow-button") {
        padding(spacing.md, spacing.lg)
        fx("border-color", "transparent transparent transparent derive(${states.selectedText}, -24%)")
        fx("border-width", "0 0 0 1px")
        fx("background-radius", "0 ${context.radius}px ${context.radius}px 0")
        fx("border-radius", "0 ${context.radius}px ${context.radius}px 0")
    }

    rule(
        ".menu-button > .arrow-button > .arrow",
        ".split-menu-button > .arrow-button > .arrow",
    ) {
        fx("background-color", states.selectedText)
        fx("background-insets", "0")
    }

    rule(".split-menu-button > .label:hover", ".split-menu-button > .arrow-button:hover") {
        fx("background-color", states.controlHover)
    }

    rule(
        ".split-menu-button:armed > .label",
        ".split-menu-button > .arrow-button:pressed",
        ".split-menu-button:showing > .arrow-button",
    ) {
        fx("background-color", states.controlPressed)
        fx("border-color", "transparent")
    }

    rule(".split-menu-button:focused") {
        fx("effect", "dropshadow(gaussian, derive(${states.focus}, 55%), 8, 0.18, 0, 0)")
    }

    rule(".menu-button > .context-menu", ".split-menu-button > .context-menu") {
        surface(colors.surface, colors.border, context.radius)
        fx("effect", context.elevation.dropdown)
    }

    rule(".menu-bar > .container > .menu-button") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("border-color", "transparent")
        radius(context.smallRadius)
        padding(spacing.sm, spacing.lg)
        fx("cursor", "hand")
    }

    rule(".menu-bar > .container > .menu-button:hover", ".menu-bar > .container > .menu-button:showing") {
        fx("background-color", states.surfaceHover)
    }

    rule(".menu-bar > .container > .menu-button:focused") {
        fx("background-color", states.surfaceHover)
        fx("border-color", states.focus)
    }

    rule(".menu-bar > .container > .menu-button:focused > .label", ".menu-bar > .container > .menu-button:showing > .label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".menu-bar > .container > .menu-button:disabled") {
        fx("opacity", states.disabledOpacity.toString())
    }

    rule(".menu-bar > .container > .menu-button > .label") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("padding", "0")
    }

    rule(".menu-bar > .container > .menu-button > .arrow-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("padding", "0")
    }

    rule(
        ".button:focused",
        ".toggle-button:focused",
        ".menu-button:focused",
        ".split-menu-button:focused",
        ".text-field:focused",
        ".password-field:focused",
        ".text-area:focused",
        ".combo-box-base:focused",
        ".choice-box:focused",
        ".date-picker:focused",
        ".color-picker:focused",
        ".spinner:focused",
        ".list-view:focused",
        ".table-view:focused",
        ".tree-view:focused",
        ".tree-table-view:focused",
    ) {
        fx("border-color", states.focus)
        fx("effect", "dropshadow(gaussian, derive(${states.focus}, 55%), 8, 0.18, 0, 0)")
    }

    rule(
        ".button:disabled",
        ".toggle-button:disabled",
        ".menu-button:disabled",
        ".split-menu-button:disabled",
        ".text-input:disabled",
        ".combo-box-base:disabled",
        ".choice-box:disabled",
        ".date-picker:disabled",
        ".color-picker:disabled",
        ".spinner:disabled",
        ".list-view:disabled",
        ".table-view:disabled",
        ".tree-view:disabled",
        ".tree-table-view:disabled",
        ".tab-pane:disabled",
        ".pagination:disabled",
        ".scroll-bar:disabled",
    ) {
        fx("opacity", states.disabledOpacity.toString())
    }

    rule(".button.ghost-button", ".button.nav-button", ".button.route-button", ".toggle-button") {
        ghostControl(context)
    }

    rule(".button.icon-only-button") {
        padding(spacing.md)
        fx("min-width", "34px")
        fx("pref-width", "34px")
    }

    rule(".button.ghost-button:hover", ".button.nav-button:hover", ".button.route-button:hover", ".toggle-button:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".button.nav-button", ".button.route-button") {
        fx("alignment", "center-left")
    }

    rule(".button.nav-button") {
        fx("max-width", "Infinity")
    }

    rule(
        ".button.nav-button-active",
        ".button.nav-button-active:hover",
        ".button.route-button-active",
        ".button.route-button-active:hover",
        ".toggle-button:selected",
    ) {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
        fx("border-color", "transparent")
    }

    rule(
        ".menu-button .label",
        ".split-menu-button .label",
        ".button.nav-button-active .label",
        ".button.route-button-active .label",
    ) {
        fx("text-fill", states.selectedText)
    }

    rule(
        ".button .ikonli-font-icon",
        ".menu-button .ikonli-font-icon",
        ".split-menu-button .ikonli-font-icon",
    ) {
        fx("icon-color", states.selectedText)
    }

    rule(
        ".button.ghost-button .ikonli-font-icon",
        ".button.nav-button .ikonli-font-icon",
        ".button.route-button .ikonli-font-icon",
        ".toggle-button .ikonli-font-icon",
    ) {
        fx("icon-color", colors.textPrimary)
    }

    rule(
        ".button.nav-button-active .ikonli-font-icon",
        ".button.route-button-active .ikonli-font-icon",
        ".toggle-button:selected .ikonli-font-icon",
    ) {
        fx("icon-color", states.selectedText)
    }
}
