package dev.korafx.framework.theme

internal fun StylesheetBuilder.baseStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".root.${ThemeStyleClass.Root}") {
        fx("base", colors.surfaceMuted)
        fx("accent", colors.primary)
        fx("focus-color", states.focus)
        fx("faint-focus-color", "transparent")
        fx("control-inner-background", colors.surfaceMuted)
        fx("background-color", colors.surface)
        fx("font-family", typography.fontFamily)
        fx("font-size", "${typography.baseSize}px")
        fx("selection-bar", states.selected)
        fx("selection-bar-text", states.selectedText)
        fx("cell-hover-color", states.rowHover)
        fx("text-background-color", colors.textPrimary)
    }

    rule(".label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".ikonli-font-icon", ".korafx-icon") {
        fx("icon-color", colors.textSecondary)
    }

    rule(".label.${ThemeStyleClass.Headline}") {
        fx("font-size", "${typography.headlineSize}px")
        fx("font-weight", "700")
    }

    rule(".${ThemeStyleClass.Muted}") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".tool-bar", ".menu-bar", ".status-strip", ".context-menu") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
    }

    rule(".tool-bar", ".menu-bar") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".tool-bar") {
        padding(spacing.lg, spacing.xl)
    }

    rule(".context-menu") {
        surface(colors.surface, colors.border, context.radius)
        radius(context.smallRadius)
        padding(spacing.xs)
        fx("effect", elevation.dropdown)
        fx("background-insets", "0")
        fx("border-insets", "0")
    }

    rule(".menu", ".menu-item", ".check-menu-item", ".radio-menu-item") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("background-radius", "${context.smallRadius}px")
        fx("border-radius", "${context.smallRadius}px")
    }

    rule(".menu .label", ".menu-item .label", ".check-menu-item .label", ".radio-menu-item .label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".menu:hover", ".menu:showing", ".menu-item:focused", ".check-menu-item:focused", ".radio-menu-item:focused") {
        fx("background-color", states.surfaceHover)
    }

    rule(".menu-item:focused .label", ".check-menu-item:focused .label", ".radio-menu-item:focused .label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".menu-item:selected", ".check-menu-item:selected", ".radio-menu-item:selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
    }

    rule(".menu-item:selected .label", ".check-menu-item:selected .label", ".radio-menu-item:selected .label") {
        fx("text-fill", states.selectedText)
    }

    rule(".check-menu-item:checked > .left-container > .check", ".radio-menu-item:checked > .left-container > .radio") {
        fx("background-color", states.selected)
        fx("background-insets", "0")
    }

    rule(".menu-item:disabled", ".check-menu-item:disabled", ".radio-menu-item:disabled") {
        fx("opacity", states.disabledOpacity.toString())
    }
}
