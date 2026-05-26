package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentCommandPaletteStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".command-palette") {
        fx("background-color", "transparent")
    }

    rule(".command-palette-scrim") {
        fx("background-color", "rgba(15, 23, 42, 0.38)")
    }

    rule(".command-palette-card") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.dropdown)
        padding(spacing.lg)
    }

    rule(".command-palette-search") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        fx("text-fill", colors.textPrimary)
        radius(context.radius)
        padding(spacing.md, spacing.lg)
    }

    rule(".command-palette-search:focused") {
        fx("border-color", states.focus)
    }

    rule(".command-palette-results", ".command-palette-row-content", ".command-palette-row-text") {
        fx("background-color", "transparent")
    }

    rule(".command-palette-group") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "800")
        padding(spacing.md, spacing.xs, spacing.xs, spacing.xs)
    }

    rule(".command-palette-row") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("alignment", "center-left")
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".command-palette-row:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".command-palette-row-selected", ".command-palette-row-selected:hover") {
        fx("background-color", states.selected)
        fx("border-color", states.selected)
    }

    rule(".command-palette-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".command-palette-description", ".command-palette-id", ".command-palette-empty") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".command-palette-row-selected .command-palette-title", ".command-palette-row-selected .command-palette-description", ".command-palette-row-selected .command-palette-id") {
        fx("text-fill", states.selectedText)
    }

    rule(".command-palette-row-selected .ikonli-font-icon") {
        fx("icon-color", states.selectedText)
    }

    rule(".command-palette-id") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".command-palette-empty") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.lg)
    }
}
