package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentLayoutStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".panel", ".card", ".feedback-state", ".modal-card", ".snackbar", ".text-flow") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
    }

    rule(".panel", ".card") {
        fx("effect", elevation.card)
    }

    rule(".border-layout") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        fx("padding", "0")
    }

    rule(".border-layout-top", ".border-layout-bottom", ".border-layout-left", ".border-layout-right") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.lg, spacing.xl)
    }

    rule(".border-layout-top") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
        fx("background-radius", "${context.radii.large}px ${context.radii.large}px 0 0")
    }

    rule(".border-layout-bottom") {
        fx("border-color", "${colors.border} transparent transparent transparent")
        fx("background-radius", "0 0 ${context.radii.large}px ${context.radii.large}px")
    }

    rule(".border-layout-left") {
        fx("border-color", "transparent ${colors.border} transparent transparent")
    }

    rule(".border-layout-right") {
        fx("border-color", "transparent transparent transparent ${colors.border}")
    }

    rule(".border-layout-center") {
        fx("background-color", colors.surface)
        padding(spacing.xl)
    }

    rule(".workspace-layout") {
        fx("background-color", colors.surface)
    }

    rule(".workspace-layout-frame", ".workspace-layout-body") {
        fx("background-color", colors.surface)
    }

    rule(".workspace-layout-top-bar", ".workspace-layout-status") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.lg, spacing.xl)
    }

    rule(".workspace-layout-top-bar") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".workspace-layout-status") {
        fx("border-color", "${colors.border} transparent transparent transparent")
    }

    rule(".workspace-layout-navigation", ".workspace-layout-details") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.xl)
    }

    rule(".workspace-layout-navigation") {
        fx("border-color", "transparent ${colors.border} transparent transparent")
    }

    rule(".workspace-layout-details") {
        fx("border-color", "transparent transparent transparent ${colors.border}")
    }

    rule(".workspace-layout-content") {
        fx("background-color", colors.surface)
        padding(spacing.xl)
    }

    rule(".workspace-layout-overlay") {
        fx("background-color", "transparent")
    }

    rule(".workspace-layout-overlay-item") {
        fx("effect", elevation.dropdown)
    }

    rule(".kora-window-frame") {
        fx("background-color", colors.surface)
    }

    rule(".kora-window-frame.kora-window-transparent") {
        fx("background-color", colors.surface)
    }

    rule(".kora-window-frame.kora-window-rounded") {
        radius(context.radii.large)
    }

    rule(".kora-window-titlebar") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(0, spacing.sm)
    }

    rule(".kora-window-title-stack", ".kora-window-titlebar-content", ".kora-window-controls") {
        fx("background-color", "transparent")
    }

    rule(".kora-window-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize}px")
        fx("font-weight", "800")
    }

    rule(".kora-window-subtitle") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 2}px")
    }

    rule(".button.kora-window-button") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textSecondary)
        fx("border-color", "transparent")
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "800")
        radius(context.smallRadius)
        padding(spacing.xs, spacing.sm)
    }

    rule(".button.kora-window-button:hover") {
        fx("background-color", states.surfaceHover)
        fx("text-fill", colors.textPrimary)
    }

    rule(".button.kora-window-button:pressed") {
        fx("background-color", states.controlPressed)
        fx("text-fill", states.selectedText)
    }

    rule(".button.kora-window-close-button:hover") {
        fx("background-color", colors.danger)
        fx("text-fill", "white")
    }

    rule(".app-toolbar") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(spacing.md, spacing.xl)
    }

    rule(".toolbar-group", ".app-toolbar-navigation", ".app-toolbar-content", ".app-toolbar-actions") {
        fx("background-color", "transparent")
    }

    rule(".app-toolbar-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize + 3}px")
        fx("font-weight", "900")
    }

    rule(".app-toolbar-title .ikonli-font-icon") {
        fx("icon-color", colors.primary)
    }

    rule(".app-toolbar-subtitle") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".korafx-devtools-dock-host") {
        fx("background-color", colors.surface)
    }

    rule(".korafx-devtools-docked") {
        fx("background-color", colors.surface)
        fx("border-color", "${colors.border} transparent transparent transparent")
        fx("border-width", "1px 0 0 0")
        fx("effect", elevation.dropdown)
    }

    rule(".korafx-devtools") {
        fx("background-color", colors.surface)
    }

    rule(".korafx-devtools-header", ".korafx-devtools-status") {
        fx("background-color", colors.surfaceMuted)
    }

    rule(".korafx-devtools-header") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".korafx-devtools-status") {
        fx("border-color", "${colors.border} transparent transparent transparent")
    }

    rule(".korafx-devtools-nav") {
        fx("background-color", colors.surfaceMuted)
    }

    rule(".korafx-devtools-fps-overlay") {
        fx("background-color", "rgba(15, 23, 42, 0.86)")
        fx("background-radius", "${context.radii.large}px")
        fx("border-color", "rgba(148, 163, 184, 0.36)")
        fx("border-radius", "${context.radii.large}px")
        fx("effect", elevation.dropdown)
        padding(spacing.xs, spacing.sm)
    }

    rule(".korafx-devtools-fps-overlay-fps") {
        fx("text-fill", "#ffffff")
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "800")
    }

    rule(".korafx-devtools-fps-overlay-frame-time") {
        fx("text-fill", "rgba(226, 232, 240, 0.84)")
        fx("font-size", "${typography.baseSize - 3}px")
    }
}
