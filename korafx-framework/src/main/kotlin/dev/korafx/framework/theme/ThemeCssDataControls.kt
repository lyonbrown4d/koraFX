package dev.korafx.framework.theme

internal fun StylesheetBuilder.dataControlStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states

    rule(".list-view", ".table-view", ".tree-view", ".tree-table-view") {
        fx("padding", "0")
        fx("border-width", "1px")
        fx("background-insets", "0")
        fx("border-insets", "0")
    }

    rule(".list-cell", ".tree-cell", ".table-row-cell", ".tree-table-row-cell") {
        fx("background-color", colors.surfaceMuted)
        fx("text-fill", colors.textPrimary)
        fx("border-color", "transparent")
    }

    rule(".list-cell", ".tree-cell") {
        padding(spacing.sm, spacing.md)
    }

    rule(".table-cell", ".tree-table-cell") {
        fx("text-fill", colors.textPrimary)
        fx("border-color", "transparent")
        padding(spacing.sm, spacing.md)
    }

    rule(".list-cell:odd", ".tree-cell:odd", ".table-row-cell:odd", ".tree-table-row-cell:odd") {
        fx("background-color", states.rowAlternate)
    }

    rule(".list-cell:hover", ".tree-cell:hover", ".table-row-cell:hover", ".tree-table-row-cell:hover") {
        fx("background-color", states.rowHover)
    }

    rule(".list-cell:selected", ".tree-cell:selected", ".table-row-cell:selected", ".tree-table-row-cell:selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
        fx("border-color", states.selected)
    }

    rule(".list-cell:empty", ".tree-cell:empty") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
    }

    rule(".table-row-cell:selected .table-cell", ".tree-table-row-cell:selected .tree-table-cell") {
        fx("text-fill", states.selectedText)
    }

    rule(".table-row-cell:empty", ".tree-table-row-cell:empty") {
        fx("background-color", "transparent")
    }

    rule(".table-row-cell:empty .table-cell", ".tree-table-row-cell:empty .tree-table-cell") {
        fx("border-color", "transparent")
    }

    rule(
        ".list-view .placeholder .label",
        ".table-view .placeholder .label",
        ".tree-view .placeholder .label",
        ".tree-table-view .placeholder .label",
    ) {
        fx("text-fill", colors.textSecondary)
    }

    rule(
        ".table-view .column-header-background",
        ".table-view .column-header",
        ".table-view .filler",
        ".tree-table-view .column-header-background",
        ".tree-table-view .column-header",
        ".tree-table-view .filler",
    ) {
        fx("background-color", states.surfaceHover)
        fx("border-color", colors.border)
    }

    rule(".table-view .column-header .label", ".tree-table-view .column-header .label") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "700")
    }

    rule(".table-view .column-header:hover", ".tree-table-view .column-header:hover") {
        fx("background-color", states.rowHover)
    }

    rule(".table-view .column-resize-line", ".tree-table-view .column-resize-line") {
        fx("background-color", states.focus)
    }

    rule(".tree-view .tree-cell .tree-disclosure-node .arrow", ".tree-table-view .tree-table-row-cell .tree-disclosure-node .arrow") {
        fx("background-color", colors.textSecondary)
    }

    rule(".tree-cell:selected .tree-disclosure-node .arrow", ".tree-table-row-cell:selected .tree-disclosure-node .arrow") {
        fx("background-color", states.selectedText)
    }
}
