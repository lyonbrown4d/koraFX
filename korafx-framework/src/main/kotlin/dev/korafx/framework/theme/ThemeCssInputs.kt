package dev.korafx.framework.theme

internal fun StylesheetBuilder.inputStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".check-box", ".radio-button") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".check-box .box", ".radio-button .radio") {
        surface(colors.surfaceMuted, colors.border, context.smallRadius)
    }

    rule(".check-box:selected .box", ".radio-button:selected .radio") {
        fx("background-color", states.selected)
        fx("border-color", states.selected)
    }

    rule(".check-box:selected .mark", ".radio-button:selected .dot") {
        fx("background-color", states.selectedText)
    }

    rule(
        ".text-field",
        ".password-field",
        ".text-area",
        ".combo-box-base",
        ".choice-box",
        ".date-picker",
        ".color-picker",
        ".spinner",
        ".list-view",
        ".table-view",
        ".tree-view",
    ) {
        surface(colors.surfaceMuted, colors.border, context.radius)
        fx("text-fill", colors.textPrimary)
    }

    rule(".text-field", ".password-field", ".combo-box-base", ".choice-box", ".date-picker", ".color-picker", ".spinner") {
        padding(spacing.sm, spacing.md)
    }

    rule(".text-field:hover", ".password-field:hover", ".text-area:hover") {
        fx("background-color", colors.surface)
        fx("border-color", colors.textSecondary)
    }

    rule(".text-area") {
        fx("control-inner-background", colors.surfaceMuted)
        fx("highlight-fill", states.selected)
        fx("highlight-text-fill", states.selectedText)
        fx("padding", "${spacing.sm}px")
    }

    rule(".text-area .content", ".text-area .scroll-pane", ".text-area .scroll-pane .viewport") {
        fx("background-color", colors.surfaceMuted)
        fx("background-radius", "${context.radius}px")
    }

    rule(
        ".combo-box-base",
        ".choice-box",
        ".date-picker",
        ".color-picker",
        ".spinner",
    ) {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        fx("border-width", "1px")
        fx("background-insets", "0")
        fx("border-insets", "0")
    }

    rule(
        ".combo-box-base:hover",
        ".choice-box:hover",
        ".date-picker:hover",
        ".color-picker:hover",
        ".spinner:hover",
    ) {
        fx("background-color", states.surfaceHover)
        fx("border-color", colors.textSecondary)
    }

    rule(
        ".combo-box-base:focused",
        ".combo-box-base:showing",
        ".choice-box:focused",
        ".choice-box:showing",
        ".date-picker:focused",
        ".date-picker:showing",
        ".color-picker:focused",
        ".color-picker:showing",
        ".spinner:focused",
    ) {
        fx("background-color", colors.surface)
        fx("border-color", states.focus)
        fx("effect", "dropshadow(gaussian, derive(${states.focus}, 55%), 8, 0.18, 0, 0)")
    }

    rule(
        ".combo-box-base .text-field",
        ".combo-box-base .list-cell",
        ".choice-box .label",
        ".date-picker .text-field",
        ".color-picker .color-picker-label",
        ".color-picker.split-button > .color-picker-label",
        ".spinner .text-field",
    ) {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("text-fill", colors.textPrimary)
        fx("padding", "0")
    }

    rule(".choice-box > .label") {
        fx("padding", "0")
    }

    rule(
        ".combo-box-base .text-field:focused",
        ".date-picker .text-field:focused",
        ".spinner .text-field:focused",
    ) {
        fx("border-color", "transparent")
    }

    rule(
        ".combo-box-base .arrow-button",
        ".combo-box-base:editable > .arrow-button",
        ".choice-box .open-button",
        ".date-picker .arrow-button",
        ".date-picker > .arrow-button",
        ".color-picker .arrow-button",
        ".color-picker.split-button > .arrow-button",
        ".spinner .increment-arrow-button",
        ".spinner .decrement-arrow-button",
    ) {
        fx("background-color", "transparent")
        fx("background-radius", "${context.radius}px")
        fx("border-color", "transparent")
        fx("padding", "0 ${spacing.sm} 0 ${spacing.sm}")
    }

    rule(
        ".combo-box-base .arrow-button:hover",
        ".combo-box-base:editable > .arrow-button:hover",
        ".choice-box .open-button:hover",
        ".date-picker .arrow-button:hover",
        ".date-picker > .arrow-button:hover",
        ".color-picker .arrow-button:hover",
        ".color-picker.split-button > .arrow-button:hover",
        ".spinner .increment-arrow-button:hover",
        ".spinner .decrement-arrow-button:hover",
    ) {
        fx("background-color", states.surfaceHover)
    }

    rule(
        ".combo-box-base:pressed",
        ".choice-box:pressed",
        ".date-picker:pressed",
        ".color-picker:pressed",
        ".spinner .increment-arrow-button:pressed",
        ".spinner .decrement-arrow-button:pressed",
    ) {
        fx("background-color", states.controlPressed)
    }

    rule(
        ".combo-box-base .arrow",
        ".choice-box .open-button .arrow",
        ".date-picker .arrow",
        ".color-picker .arrow",
        ".color-picker.split-button > .arrow-button > .arrow",
        ".spinner .increment-arrow",
        ".spinner .decrement-arrow",
    ) {
        fx("background-color", colors.textSecondary)
    }

    rule(
        ".combo-box-popup .list-view",
        ".choice-box .context-menu",
        ".date-picker-popup",
        ".color-palette",
    ) {
        surface(colors.surface, colors.border, context.radius)
        fx("padding", "${spacing.xs}px")
        fx("effect", elevation.dropdown)
    }

    rule(".date-picker-popup .month-year-pane") {
        fx("background-color", colors.surface)
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(spacing.sm)
    }

    rule(".combo-box-popup .list-view") {
        fx("background-insets", "0")
        fx("border-insets", "0")
    }

    rule(
        ".combo-box-popup .list-cell",
        ".choice-box .menu-item",
        ".date-picker-popup .day-cell",
        ".date-picker-popup .day-name-cell",
        ".date-picker-popup .week-number-cell",
        ".color-palette .color-picker-grid",
    ) {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("background-radius", "${context.smallRadius}px")
        fx("border-radius", "${context.smallRadius}px")
    }

    rule(
        ".combo-box-popup .list-cell:hover",
        ".combo-box-popup .list-cell:filled:hover",
        ".choice-box .menu-item:focused",
        ".date-picker-popup .day-cell:hover",
        ".date-picker-popup .today",
    ) {
        fx("background-color", states.surfaceHover)
    }

    rule(
        ".combo-box-popup .list-cell:selected",
        ".choice-box .menu-item:focused .label",
        ".choice-box .menu-item:selected",
        ".date-picker-popup .day-cell:selected",
    ) {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
    }

    rule(".date-picker-popup .previous-month", ".date-picker-popup .next-month") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".color-palette .color-square") {
        fx("background-radius", "${context.smallRadius}px")
        fx("border-radius", "${context.smallRadius}px")
        fx("border-color", colors.border)
    }

    rule(".choice-box .menu-item:focused .label") {
        fx("text-fill", states.selectedText)
    }

    rule(
        ".text-field.invalid",
        ".text-area.invalid",
        ".password-field.invalid",
        ".combo-box-base.invalid",
        ".choice-box.invalid",
        ".date-picker.invalid",
        ".spinner.invalid",
    ) {
        fx("border-color", states.invalid)
    }
}
