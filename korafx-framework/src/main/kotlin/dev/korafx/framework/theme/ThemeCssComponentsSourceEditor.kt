package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentSourceEditorStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val elevation = context.elevation

    rule(".source-editor") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.md)
    }

    rule(".source-editor-toolbar") {
        fx("background-color", "transparent")
        padding(0, 0, spacing.sm, 0)
    }

    rule(".source-editor-status") {
        fx("background-color", "derive(${colors.surface}, 8%)")
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(spacing.sm, spacing.md)
    }

    rule(".source-editor-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }

    rule(".source-editor-status-message") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".source-editor-status-badge") {
        fx("font-weight", "700")
    }

    rule(".source-editor-code") {
        fx("effect", "none")
    }

    rule(".source-editor-diagnostics", ".source-editor-result") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".source-editor-diagnostics-title", ".source-editor-result-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".source-editor-diagnostic") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
        radius(context.smallRadius)
        padding(spacing.sm, spacing.md)
    }

    rule(".source-editor-diagnostic-location") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("text-fill", colors.textSecondary)
        fx("font-weight", "700")
    }

    rule(".source-editor-diagnostic-message", ".source-editor-result-content", ".source-editor-result-node") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".source-editor-diagnostic.tone-warning") {
        fx("border-color", colors.warning)
    }

    rule(".source-editor-diagnostic.tone-danger") {
        fx("border-color", colors.danger)
    }

    rule(".source-editor-diagnostic.tone-info") {
        fx("border-color", colors.info)
    }
}
