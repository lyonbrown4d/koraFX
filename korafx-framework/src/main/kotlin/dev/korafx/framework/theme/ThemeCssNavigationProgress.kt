package dev.korafx.framework.theme

internal fun StylesheetBuilder.navigationProgressStyles(context: ThemeCssContext) {
    val colors = context.colors
    val states = context.states

    rule(".slider .track") {
        fx("background-color", colors.border)
        fx("background-radius", "${context.radius}px")
    }

    rule(".slider .thumb") {
        fx("background-color", states.selected)
        fx("background-radius", "${context.radius}px")
    }

    rule(".progress-bar .track", ".progress-indicator") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
    }

    rule(".progress-bar .bar", ".progress-indicator .percentage", ".progress-indicator > .determinate-indicator > .progress") {
        fx("background-color", states.selected)
    }

    rule(".progress-indicator > .determinate-indicator > .indicator") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
    }

    rule(".progress-indicator:indeterminate .segment") {
        fx("background-color", states.selected)
    }

    rule(".pagination .pagination-control .button") {
        ghostControl(context)
    }

    rule(".pagination .pagination-control .page-number") {
        fx("background-radius", "${context.radius}px")
        fx("border-radius", "${context.radius}px")
    }

    rule(".pagination .pagination-control .button:hover", ".pagination .pagination-control .page-number:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".pagination .pagination-control .button:pressed") {
        fx("background-color", states.controlPressed)
    }

    rule(".pagination .pagination-control .page-number:pressed") {
        fx("background-color", states.controlPressed)
    }

    rule(".pagination .pagination-control .page-number:selected") {
        fx("background-color", states.selected)
        fx("border-color", states.selected)
        fx("text-fill", states.selectedText)
    }
}
