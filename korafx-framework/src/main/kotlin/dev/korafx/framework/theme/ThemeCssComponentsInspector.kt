package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentInspectorStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val elevation = context.elevation

    rule(".inspector-panel") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.lg)
    }

    rule(".inspector-panel-header") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(0, 0, spacing.md, 0)
    }

    rule(".inspector-panel-title-row", ".inspector-panel-metadata", ".inspector-panel-actions") {
        fx("background-color", "transparent")
    }

    rule(".inspector-panel-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize + 2}px")
        fx("font-weight", "800")
    }

    rule(".inspector-panel-subtitle") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".inspector-panel-metadata-item") {
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".inspector-panel-body") {
        fx("background-color", "transparent")
    }

    rule(".inspector-panel-section") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".inspector-panel-section-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "700")
    }

    rule(".inspector-panel-property") {
        fx("background-color", "transparent")
        padding(spacing.xs, 0)
    }

    rule(".inspector-panel-property-name") {
        fx("text-fill", colors.textSecondary)
        fx("font-weight", "700")
        fx("min-width", "96px")
        fx("pref-width", "96px")
    }

    rule(".inspector-panel-property-value", ".inspector-panel-content", ".inspector-panel-section-content") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".inspector-panel-actions") {
        fx("border-color", "${colors.border} transparent transparent transparent")
        padding(spacing.md, 0, 0, 0)
    }

    rule(".inspector-panel-action") {
        fx("cursor", "hand")
    }

    rule(".button.inspector-panel-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }

    rule(".inspector-panel-empty") {
        fx("text-fill", colors.textSecondary)
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.lg)
    }
}
