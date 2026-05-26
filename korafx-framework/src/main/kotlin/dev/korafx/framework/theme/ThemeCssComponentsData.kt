package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentDataGridStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".editable-table") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
    }

    rule(".editable-table-placeholder") {
        fx("text-fill", colors.textSecondary)
    }

    rule(
        ".editable-table-text-column .label",
        ".editable-table-editable-text-column .label",
        ".editable-table-node-column .label",
        ".editable-table-action-column .label",
    ) {
        fx("font-weight", "700")
    }

    rule(".editable-table-cell-node") {
        fx("background-color", "transparent")
        padding(spacing.xs, spacing.sm)
    }

    rule(".editable-table .table-cell:editing") {
        fx("background-color", colors.surface)
        fx("border-color", states.focus)
        fx("padding", "0")
    }

    rule(".editable-table .table-cell:editing .text-field") {
        fx("background-color", colors.surface)
        fx("border-color", "transparent")
        fx("background-radius", "0")
        fx("border-radius", "0")
        padding(spacing.sm, spacing.md)
    }

    rule(".editable-table-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }

    rule(".data-grid") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.md)
    }

    rule(".data-grid-toolbar") {
        fx("background-color", "transparent")
        padding(0, 0, spacing.sm, 0)
    }

    rule(".data-grid-search") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        fx("text-fill", colors.textPrimary)
        radius(context.radius)
        padding(spacing.sm, spacing.md)
    }

    rule(".data-grid-search:focused") {
        fx("border-color", states.focus)
    }

    rule(".data-grid-toolbar-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }

    rule(".data-grid-toolbar-batch-action", ".data-grid-toolbar-snapshot-action", ".data-grid-column-visibility") {
        fx("border-color", colors.border)
    }

    rule(".data-grid-column-visibility .label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".data-grid-column-visibility-item") {
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".data-grid-table") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
    }

    rule(".data-grid-row-dirty") {
        fx("background-color", "derive(${colors.warning}, 88%)")
    }

    rule(".data-grid-row-dirty .table-cell") {
        fx("border-color", "transparent transparent transparent ${colors.warning}")
        fx("border-width", "0 0 0 3px")
    }

    rule(".data-grid-footer") {
        fx("border-color", "${colors.border} transparent transparent transparent")
        padding(spacing.sm, 0, 0, 0)
    }

    rule(".data-grid-footer-label", ".data-grid-selection-summary", ".data-grid-empty", ".data-grid-loading") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".data-grid-selection-summary") {
        fx("font-weight", "700")
    }
}
