package dev.korafx.framework.theme

internal fun StylesheetBuilder.navigationTabStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states

    rule(".tab-pane") {
        fx("background-color", colors.surface)
    }

    rule(".tab-pane .tab-header-area .tab-header-background") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".tab-pane .tab") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-radius", "${context.radius}px ${context.radius}px 0 0")
        fx("border-radius", "${context.radius}px ${context.radius}px 0 0")
        padding(spacing.sm, spacing.xl)
    }

    rule(".tab-pane .tab:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".tab-pane .tab:pressed") {
        fx("background-color", states.controlPressed)
    }

    rule(".tab-pane .tab:selected") {
        fx("background-color", states.selected)
    }

    rule(".tab-pane .tab:selected .tab-label") {
        fx("text-fill", states.selectedText)
    }

    rule(".tab-pane .tab:focused") {
        fx("border-color", states.focus)
    }

    rule(".tab-pane .tab:focused .tab-label", ".tab-pane .tab:selected .tab-close-button") {
        fx("text-fill", states.selectedText)
    }

    rule(".tab-pane .tab:disabled") {
        fx("opacity", states.disabledOpacity.toString())
    }

    rule(".tab-pane .tab:selected:disabled .tab-label") {
        fx("text-fill", "derive(${states.selectedText}, -20%)")
    }

    rule(".tab-pane .tab-close-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("text-fill", colors.textSecondary)
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("padding", "0")
    }

    rule(".tab-pane .tab-close-button:hover") {
        fx("background-color", states.surfaceHover)
        fx("text-fill", colors.textPrimary)
    }

    rule(".tab-workspace") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.large)
    }

    rule(".tab-workspace .tab-content-area") {
        fx("background-color", colors.surface)
    }

    rule(".tab-workspace .tab-workspace-empty-tab") {
        fx("opacity", "1")
    }

    rule(".tab-workspace .tab-workspace-empty-tab .tab-label") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".tab-workspace-empty-pane") {
        fx("background-color", colors.surface)
        padding(spacing.xl)
    }

    rule(".tab-workspace-empty") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".tab-workspace .tab-workspace-tab-dirty .tab-label") {
        fx("font-weight", "800")
    }

    rule(".tab-workspace-dirty-marker") {
        fx("text-fill", colors.warning)
        fx("font-weight", "900")
    }
}
