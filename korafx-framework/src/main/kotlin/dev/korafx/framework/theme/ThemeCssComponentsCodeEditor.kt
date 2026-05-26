package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentCodeEditorStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".code-editor") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        fx("padding", "0")
    }

    rule(".code-editor-toolbar", ".code-editor-status") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.sm, spacing.md)
    }

    rule(".code-editor-search") {
        fx("background-color", colors.surface)
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(spacing.sm, spacing.md)
    }

    rule(".code-editor-search-label", ".code-editor-replace-label", ".code-editor-search-result") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".code-editor-search-field", ".code-editor-replace-field") {
        surface(colors.surfaceMuted, colors.border, context.smallRadius)
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize}px")
        fx("text-fill", colors.textPrimary)
        fx("highlight-fill", states.selected)
        fx("highlight-text-fill", states.selectedText)
    }

    rule(".code-editor-search-button") {
        ghostControl(context)
        padding(spacing.xs, spacing.md)
    }

    rule(".code-editor-toolbar") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
        fx("background-radius", "${context.radii.large}px ${context.radii.large}px 0 0")
    }

    rule(".code-editor-status") {
        fx("border-color", "${colors.border} transparent transparent transparent")
        fx("background-radius", "0 0 ${context.radii.large}px ${context.radii.large}px")
    }

    rule(".code-editor-title") {
        fx("font-weight", "800")
        fx("text-fill", colors.textPrimary)
    }

    rule(".code-editor-area") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize}px")
        fx("text-fill", colors.textPrimary)
        fx("highlight-fill", states.selected)
        fx("highlight-text-fill", states.selectedText)
        fx("background-color", colors.surface)
        fx("border-color", "transparent")
        fx("background-radius", "0")
        fx("border-radius", "0")
    }

    rule(".code-editor-frame") {
        fx("background-color", colors.surface)
    }

    rule(".code-editor-line-numbers") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent ${colors.border} transparent transparent")
        padding(spacing.sm, spacing.sm)
    }

    rule(".code-editor-line-number") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize}px")
        fx("text-fill", colors.textSecondary)
        fx("alignment", "center-right")
        fx("min-width", "32px")
    }

    rule(".code-editor-line-number-active") {
        fx("text-fill", colors.primary)
        fx("font-weight", "800")
    }

    rule(".code-editor-area .content") {
        fx("background-color", colors.surface)
        fx("background-radius", "0")
    }

    rule(".code-editor-status-text") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
    }
}
