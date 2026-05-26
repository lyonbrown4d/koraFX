package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentMarkdownStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states

    rule(".markdown-document") {
        fx("background-color", colors.surfaceMuted)
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize}px")
        fx("line-height", "1.4")
        fx("padding", "${spacing.md}px")
    }

    rule(".markdown-h1", ".markdown-h2", ".markdown-h3", ".markdown-h4", ".markdown-h5", ".markdown-h6") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "700")
    }

    rule(".markdown-h1") {
        fx("font-size", "${typography.headlineSize}px")
        fx("padding", "6px 0 2px 0")
    }

    rule(".markdown-h2") {
        fx("font-size", "${typography.baseSize + 4}px")
        fx("padding", "4px 0 2px 0")
    }

    rule(".markdown-h3") {
        fx("font-size", "${typography.baseSize + 2}px")
        fx("padding", "4px 0 2px 0")
    }

    rule(".markdown-h4", ".markdown-h5", ".markdown-h6") {
        fx("font-size", "${typography.baseSize}px")
        fx("padding", "4px 0 2px 0")
    }

    rule(".markdown-paragraph") {
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize}px")
        fx("line-spacing", "3px")
    }

    rule(".markdown-inline-code") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("text-fill", colors.textSecondary)
        fx("background-color", colors.surface)
        fx("padding", "2px 6px")
    }

    rule(".markdown-inline") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".markdown-code-block") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        fx("border-width", "1px")
        radius(context.smallRadius)
        fx("padding", "${spacing.md}px")
    }

    rule(".markdown-code-line") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("text-fill", colors.textPrimary)
    }

    rule(".markdown-quote") {
        fx("background-color", "derive(${colors.surface}, 96%)")
        fx("text-fill", colors.textSecondary)
        fx("padding", "${spacing.md}px")
        fx("border-color", colors.primary)
        fx("border-width", "0 0 0 4px")
    }

    rule(".markdown-list") {
        fx("padding", "4px 0")
    }

    rule(".markdown-list-item") {
        fx("padding", "0 0 4px 0")
    }

    rule(".markdown-list-marker") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".markdown-list-item-text") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".validation-message") {
        fx("text-fill", states.invalid)
    }
}
