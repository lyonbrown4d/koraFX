package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentVirtualizedStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".virtual-list", ".virtual-table", ".virtual-terminal") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.md)
    }

    rule(".virtual-list-list-view", ".virtual-table-table-view", ".virtual-terminal-list-view") {
        surface(colors.surface, colors.border, context.radii.medium)
    }

    rule(".virtual-list-list-view .list-cell", ".virtual-terminal-list-view .list-cell") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(spacing.sm, spacing.md)
    }

    rule(".virtual-list-list-view .list-cell:hover", ".virtual-terminal-list-view .list-cell:hover") {
        fx("background-color", states.rowHover)
    }

    rule(".virtual-list-list-view .list-cell:selected", ".virtual-terminal-list-view .list-cell:selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
    }

    rule(".virtual-table-table-view .column-header-background", ".virtual-table-table-view .column-header") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
    }

    rule(".virtual-table-table-view .table-row-cell") {
        fx("background-color", colors.surface)
    }

    rule(".virtual-table-table-view .table-row-cell:odd") {
        fx("background-color", states.rowAlternate)
    }

    rule(".virtual-table-table-view .table-row-cell:hover") {
        fx("background-color", states.rowHover)
    }

    rule(".virtual-table-table-view .table-row-cell:selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
    }

    rule(".virtual-table-cell-node") {
        fx("background-color", "transparent")
    }

    rule(".virtual-terminal-line-label") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("text-fill", colors.textPrimary)
    }

    rule(".terminal-muted .virtual-terminal-line-label", ".virtual-terminal-list-view .terminal-muted") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".terminal-info .virtual-terminal-line-label", ".virtual-terminal-list-view .terminal-info") {
        fx("text-fill", colors.info)
    }

    rule(".terminal-success .virtual-terminal-line-label", ".virtual-terminal-list-view .terminal-success") {
        fx("text-fill", colors.success)
    }

    rule(".terminal-warning .virtual-terminal-line-label", ".virtual-terminal-list-view .terminal-warning") {
        fx("text-fill", colors.warning)
    }

    rule(".terminal-danger .virtual-terminal-line-label", ".virtual-terminal-list-view .terminal-danger") {
        fx("text-fill", colors.danger)
    }
}
