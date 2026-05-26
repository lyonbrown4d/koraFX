package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentTimelineStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val elevation = context.elevation

    rule(".activity-timeline") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.lg)
    }

    rule(".activity-timeline-content") {
        fx("background-color", "transparent")
    }

    rule(".activity-timeline-empty") {
        fx("text-fill", colors.textSecondary)
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.lg)
    }

    rule(".activity-timeline-group") {
        fx("text-fill", colors.textSecondary)
        fx("font-weight", "800")
        padding(spacing.md, 0, spacing.sm, 0)
    }

    rule(".activity-timeline-row") {
        fx("background-color", "transparent")
        padding(spacing.sm, 0)
    }

    rule(".activity-timeline-marker-column") {
        fx("min-width", "18px")
        fx("pref-width", "18px")
    }

    rule(".activity-timeline-marker") {
        fx("background-color", colors.textSecondary)
        fx("min-width", "10px")
        fx("min-height", "10px")
        fx("pref-width", "10px")
        fx("pref-height", "10px")
        fx("background-radius", "999px")
    }

    rule(".activity-timeline-connector") {
        fx("background-color", colors.border)
        fx("min-width", "2px")
        fx("pref-width", "2px")
        fx("min-height", "42px")
    }

    rule(".activity-timeline-event") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".activity-timeline-meta") {
        fx("background-color", "transparent")
    }

    rule(".activity-timeline-time", ".activity-timeline-message") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".activity-timeline-time") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "700")
    }

    rule(".activity-timeline-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".activity-timeline-action") {
        ghostControl(context)
        padding(spacing.xs, spacing.md)
    }

    rule(".activity-timeline-row.tone-success .activity-timeline-marker") {
        fx("background-color", colors.success)
    }

    rule(".activity-timeline-row.tone-warning .activity-timeline-marker") {
        fx("background-color", colors.warning)
    }

    rule(".activity-timeline-row.tone-danger .activity-timeline-marker") {
        fx("background-color", colors.danger)
    }

    rule(".activity-timeline-row.tone-info .activity-timeline-marker") {
        fx("background-color", colors.info)
    }
}
