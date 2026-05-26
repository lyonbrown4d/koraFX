package dev.korafx.framework.theme

internal fun StylesheetBuilder.navigationScrollAndLinkStyles(context: ThemeCssContext) {
    val colors = context.colors
    val states = context.states

    rule(".separator .line", ".separator:horizontal .line", ".separator:vertical .line") {
        fx("border-color", colors.border)
    }

    rule(".scroll-pane", ".scroll-pane > .viewport", ".scroll-pane > .corner") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
    }

    rule(".scroll-pane > .viewport") {
        fx("border-color", "transparent")
    }

    rule(".scroll-bar", ".scroll-bar .track") {
        fx("background-color", "transparent")
    }

    rule(".scroll-bar .thumb") {
        fx("background-color", states.scrollbarThumb)
        fx("background-radius", "${context.radius}px")
        fx("min-width", "8px")
        fx("min-height", "8px")
    }

    rule(".scroll-bar .thumb:hover") {
        fx("background-color", colors.textSecondary)
    }

    rule(".scroll-bar .thumb:pressed") {
        fx("background-color", states.selected)
    }

    rule(".scroll-bar .increment-button", ".scroll-bar .decrement-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("padding", "0")
    }

    rule(".scroll-bar .increment-button:hover", ".scroll-bar .decrement-button:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".hyperlink") {
        fx("text-fill", colors.primary)
        fx("border-color", "transparent")
        fx("padding", "0")
    }

    rule(".hyperlink:hover") {
        fx("text-fill", states.focus)
    }

    rule(".hyperlink.route-link-active", ".hyperlink.route-link-active:hover") {
        fx("text-fill", states.focus)
        fx("underline", "true")
    }

    rule(".hyperlink:armed", ".hyperlink:visited") {
        fx("text-fill", states.focus)
    }

    rule(".hyperlink:focused") {
        fx("border-color", states.focus)
    }

    rule(".hyperlink:disabled") {
        fx("opacity", states.disabledOpacity.toString())
    }

    rule(".tool-bar:horizontal .separator .line", ".tool-bar:vertical .separator .line") {
        fx("border-color", colors.border)
    }
}
