package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentResourceExplorerStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".resource-explorer") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.md)
    }

    rule(".resource-explorer-search") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        fx("text-fill", colors.textPrimary)
        radius(context.radius)
        padding(spacing.sm, spacing.md)
    }

    rule(".resource-explorer-search:focused") {
        fx("border-color", states.focus)
    }

    rule(".resource-explorer-breadcrumb") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "700")
        padding(0, spacing.xs, spacing.xs, spacing.xs)
    }

    rule(".resource-explorer-empty-state") {
        fx("text-fill", colors.textSecondary)
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.lg)
    }

    rule(".resource-explorer-tree") {
        fx("background-color", colors.surface)
        fx("border-color", "transparent")
        radius(context.radii.medium)
        fx("padding", "0")
    }

    rule(".resource-explorer-row", ".resource-explorer-row-text") {
        fx("background-color", "transparent")
    }

    rule(".resource-explorer-row-primary") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "700")
    }

    rule(".resource-explorer-row-secondary") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".resource-explorer-row-status") {
        fx("text-fill", colors.primary)
        fx("background-color", "derive(${colors.primary}, 88%)")
        fx("font-size", "${typography.baseSize - 2}px")
        fx("font-weight", "800")
        radius(context.radii.pill)
        padding(spacing.xxs, spacing.sm)
    }

    rule(".resource-explorer-row-icon") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".resource-explorer-tree .tree-cell", ".resource-explorer-cell") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("background-radius", "${context.smallRadius}px")
        fx("border-radius", "${context.smallRadius}px")
        padding(spacing.sm, spacing.md)
    }

    rule(".resource-explorer-tree .tree-cell:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".resource-explorer-tree .tree-cell:selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
    }

    rule(
        ".resource-explorer-tree .tree-cell:selected .resource-explorer-row-primary",
        ".resource-explorer-tree .tree-cell:selected .resource-explorer-row-secondary",
        ".resource-explorer-tree .tree-cell:selected .resource-explorer-row-icon",
    ) {
        fx("text-fill", states.selectedText)
    }

    rule(".resource-explorer-tree .tree-cell:selected .resource-explorer-row-status") {
        fx("background-color", states.selectedText)
        fx("text-fill", states.selected)
    }

    rule(".resource-explorer-tree .tree-cell:empty") {
        fx("background-color", "transparent")
    }

    rule(".resource-explorer-tree .tree-cell:selected .tree-disclosure-node .arrow") {
        fx("background-color", states.selectedText)
    }
}
