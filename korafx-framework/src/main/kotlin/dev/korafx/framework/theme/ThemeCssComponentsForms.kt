package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentFormAndStatusStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states

    rule(".section") {
        fx("background-color", colors.surfaceMuted)
    }

    rule(".section-title") {
        fx("font-size", "${typography.baseSize + 4}px")
        fx("font-weight", "700")
    }

    rule(".section-description", ".modal-message", ".snackbar-message", ".form-helper") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".feedback-graphic", ".feedback-actions") {
        fx("background-color", "transparent")
    }

    rule(".feedback-action") {
        padding(spacing.sm, spacing.xl)
    }

    rule(".action-bar") {
        padding(spacing.sm, 0, 0, 0)
    }

    rule(".status-bar") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
        fx("border-width", "1px 0 0 0")
        padding(spacing.sm, spacing.lg)
    }

    rule(".status-item") {
        fx("font-size", "${typography.baseSize - 1}px")
        radius(context.smallRadius)
        padding(spacing.xs, spacing.sm)
    }

    rule(".status-item .ikonli-font-icon") {
        fx("icon-color", colors.textSecondary)
    }

    rule(".form") {
        surface(colors.surfaceMuted, colors.border, context.radius)
        padding(spacing.md)
    }

    rule(".form-item") {
        padding(spacing.sm, 0, 0, 0)
    }

    rule(".form-label") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "600")
        fx("font-size", "${typography.baseSize + 1}px")
    }

    rule(".form-helper", ".validation-message") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".validation-message") {
        fx("text-fill", states.invalid)
    }

    rule(".submit-bar") {
        fx("padding", "${spacing.md}px 0 0 0")
    }

    rule(".nav-rail") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent ${colors.border} transparent transparent")
        padding(spacing.xxl, spacing.xl)
    }

    rule(".route-state-host") {
        surface(colors.surfaceMuted, colors.border, context.radius)
        fx("padding", "${spacing.sm}px")
    }

    rule(".status-strip") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "${colors.border} transparent transparent transparent")
        padding(spacing.lg, spacing.xl)
    }
}
