package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentShellAndFeedbackStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".app-shell", ".app-shell-frame", ".app-shell-body") {
        fx("background-color", colors.surface)
    }

    rule(".app-shell-top-bar", ".app-shell-footer") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.lg, spacing.xl)
    }

    rule(".app-shell-top-bar") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".app-shell-footer") {
        fx("border-color", "${colors.border} transparent transparent transparent")
    }

    rule(".app-shell-navigation", ".app-shell-details") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.xl)
    }

    rule(".app-shell-navigation") {
        fx("border-color", "transparent ${colors.border} transparent transparent")
    }

    rule(".app-shell-details") {
        fx("border-color", "transparent transparent transparent ${colors.border}")
    }

    rule(".app-shell-content") {
        fx("background-color", colors.surface)
    }

    rule(".app-shell-overlay", ".modal-host") {
        fx("background-color", "transparent")
    }

    rule(".app-shell-overlay-item") {
        fx("effect", elevation.dropdown)
    }

    rule(".modal-backdrop") {
        fx("background-color", "rgba(17, 24, 39, 0.42)")
        padding(spacing.xxxl)
    }

    rule(".modal-card") {
        fx("effect", elevation.modal)
    }

    rule(".modal-title") {
        fx("font-size", "${typography.baseSize + 4}px")
        fx("font-weight", "700")
    }

    rule(".modal-content") {
        padding(spacing.xxs, 0)
    }

    rule(".modal-actions") {
        padding(spacing.xs, 0, 0, 0)
    }

    rule(".modal-secondary-action") {
        ghostControl(context)
    }

    rule(".modal-destructive-action") {
        fx("background-color", colors.danger)
        fx("text-fill", states.selectedText)
    }

    rule(".toast-host") {
        padding(spacing.xl)
    }

    rule(".snackbar") {
        fx("effect", elevation.snackbar)
        fx("min-width", "320px")
        fx("max-width", "460px")
    }

    rule(".snackbar-title", ".form-label") {
        fx("font-weight", "700")
    }

    rule(".snackbar-action", ".snackbar-dismiss") {
        padding(spacing.sm, spacing.lg)
    }

    rule(".toast-info") {
        fx("border-color", colors.info)
    }

    rule(".toast-success") {
        fx("border-color", colors.success)
    }

    rule(".toast-warning") {
        fx("border-color", colors.warning)
    }

    rule(".toast-error", ".error-state") {
        fx("border-color", colors.danger)
    }

    rule(".feedback-title", ".feedback-message") {
        fx("alignment", "center")
        fx("text-alignment", "center")
    }

    rule(".loading-state-indicator") {
        fx("progress-color", states.selected)
    }
}
