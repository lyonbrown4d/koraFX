package dev.korafx.framework.theme

internal fun StylesheetBuilder.navigationPaneStyles(context: ThemeCssContext) {
    val colors = context.colors
    val states = context.states

    rule(".split-pane") {
        fx("background-color", colors.surface)
    }

    rule(".split-pane-divider", ".split-pane:horizontal > .split-pane-divider", ".split-pane:vertical > .split-pane-divider") {
        fx("background-color", colors.border)
        fx("padding", "0 1 0 1")
    }

    rule(".split-pane-divider:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".split-pane:focused > .split-pane-divider") {
        fx("background-color", states.focus)
    }

    rule(".titled-pane > .title", ".titled-pane > .content") {
        surface(colors.surfaceMuted, colors.border, context.radius)
    }

    rule(".titled-pane:hover > .title") {
        fx("background-color", states.surfaceHover)
    }

    rule(".titled-pane:focused > .title", ".titled-pane:focused > .title > .text") {
        fx("border-color", states.focus)
        fx("fill", states.focus)
    }

    rule(".titled-pane > .title > .text") {
        fx("fill", colors.textPrimary)
    }

    rule(".titled-pane:expanded > .title") {
        fx("background-color", states.selected)
    }

    rule(".titled-pane:expanded > .title > .text", ".accordion > .titled-pane:expanded > .title > .text") {
        fx("fill", states.selectedText)
    }

    rule(".titled-pane > .title > .arrow-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
    }

    rule(".titled-pane > .title > .arrow-button > .arrow") {
        fx("background-color", colors.textSecondary)
    }

    rule(".titled-pane:expanded > .title > .arrow-button > .arrow") {
        fx("background-color", states.selectedText)
    }

    rule(".titled-pane > .content") {
        fx("background-radius", "0 0 ${context.radius}px ${context.radius}px")
        fx("border-radius", "0 0 ${context.radius}px ${context.radius}px")
    }

    rule(".accordion") {
        fx("background-color", "transparent")
    }

    rule(".accordion > .titled-pane > .title") {
        fx("background-color", colors.surfaceMuted)
    }

    rule(".accordion > .titled-pane") {
        fx("border-color", "transparent")
    }

    rule(".accordion > .titled-pane:expanded > .title") {
        fx("background-color", states.selected)
        fx("border-color", states.focus)
    }

    rule(".accordion > .titled-pane:expanded > .content") {
        surface(colors.surface, colors.border, context.radius)
    }
}
