package dev.korafx.framework.theme

internal fun StylesheetBuilder.overlayControlStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".tooltip") {
        surface(colors.surfaceMuted, colors.border, context.smallRadius)
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize - 1}px")
        fx("effect", elevation.dropdown)
        padding(spacing.sm, spacing.md)
    }

    rule(".dialog-pane") {
        surface(colors.surface, colors.border, context.radii.large)
        fx("effect", elevation.modal)
        fx("padding", "0")
    }

    rule(".dialog-pane > .header-panel") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent transparent ${colors.border} transparent")
        fx("background-radius", "${context.radii.large}px ${context.radii.large}px 0 0")
        padding(spacing.xl)
    }

    rule(".dialog-pane > .header-panel .label") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".dialog-pane > .content", ".dialog-pane > .content.label") {
        fx("text-fill", colors.textPrimary)
        padding(spacing.xl)
    }

    rule(".dialog-pane > .button-bar", ".dialog-pane > .button-bar > .container", ".button-bar", ".button-bar > .container") {
        fx("background-color", colors.surface)
        fx("border-color", "${colors.border} transparent transparent transparent")
        padding(spacing.lg, spacing.xl)
    }

    rule(".button-bar .button") {
        padding(spacing.md, spacing.xl)
    }

    rule(".dialog-pane .graphic-container") {
        fx("background-color", "transparent")
        padding(spacing.xl, 0, spacing.xl, spacing.xl)
    }

    rule(".dialog-pane:focused") {
        fx("border-color", states.focus)
    }
}
