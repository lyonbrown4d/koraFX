package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentBadgeAndAlertStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states

    rule(".badge", ".chip") {
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "700")
        fx("border-width", "1px")
        radius(context.radii.pill)
    }

    rule(".badge") {
        padding(spacing.xs, spacing.md)
    }

    rule(".chip") {
        padding(spacing.sm, spacing.lg)
        fx("cursor", "hand")
    }

    rule(".chip:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".chip.chip-selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
        fx("border-color", states.selected)
    }

    rule(".chip.chip-selected .ikonli-font-icon") {
        fx("icon-color", states.selectedText)
    }

    semanticTone("neutral", colors.textSecondary, colors.surfaceMuted, colors.border)
    semanticTone("primary", colors.primary, "derive(${colors.primary}, 88%)", colors.primary)
    semanticTone("success", colors.success, "derive(${colors.success}, 88%)", colors.success)
    semanticTone("warning", colors.warning, "derive(${colors.warning}, 88%)", colors.warning)
    semanticTone("danger", colors.danger, "derive(${colors.danger}, 88%)", colors.danger)
    semanticTone("info", colors.info, "derive(${colors.info}, 88%)", colors.info)

    rule(".metric-card") {
        fx("border-width", "1px 1px 1px 4px")
    }

    rule(".metric-label", ".metric-helper") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".metric-label") {
        fx("font-weight", "700")
    }

    rule(".metric-value") {
        fx("font-size", "${typography.headlineSize}px")
        fx("font-weight", "800")
        fx("text-fill", colors.textPrimary)
    }

    rule(".alert-banner") {
        fx("border-width", "1px 1px 1px 4px")
    }

    rule(".alert-title") {
        fx("font-weight", "800")
        fx("text-fill", colors.textPrimary)
    }

    rule(".alert-message") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".alert-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }
}
