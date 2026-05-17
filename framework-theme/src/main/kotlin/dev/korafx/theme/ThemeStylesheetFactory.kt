package dev.korafx.theme

object ThemeStylesheetFactory {
    fun render(theme: KoraTheme): String {
        val context = ThemeCssContext(theme)
        return cssStylesheet {
            baseStyles(context)
            buttonStyles(context)
            inputStyles(context)
            dataControlStyles(context)
            navigationControlStyles(context)
            componentStyles(context)
        }
    }
}

private data class ThemeCssContext(
    val theme: KoraTheme,
) {
    val colors: ColorTokens = theme.tokens.colors
    val typography: TypographyTokens = theme.tokens.typography
    val radius: Int = theme.tokens.radius
    val smallRadius: Int = (radius - 4).coerceAtLeast(4)
}

@DslMarker
private annotation class ThemeCssDsl

private fun cssStylesheet(content: StylesheetBuilder.() -> Unit): String =
    StylesheetBuilder().apply(content).build()

@ThemeCssDsl
private class StylesheetBuilder {
    private val rules = mutableListOf<String>()

    fun rule(
        vararg selectors: String,
        content: RuleBuilder.() -> Unit,
    ) {
        require(selectors.isNotEmpty()) {
            "CSS rule requires at least one selector."
        }
        val declarations = RuleBuilder().apply(content).build()
        if (declarations.isEmpty()) {
            return
        }

        rules += buildString {
            append(selectors.joinToString(",\n"))
            append(" {\n")
            declarations.forEach { declaration ->
                append("  ")
                append(declaration)
                append('\n')
            }
            append("}")
        }
    }

    fun build(): String = rules.joinToString("\n\n")
}

@ThemeCssDsl
private class RuleBuilder {
    private val declarations = mutableListOf<String>()

    fun fx(
        name: String,
        value: String,
    ) {
        declarations += "-fx-$name: $value;"
    }

    fun radius(value: Int) {
        fx("background-radius", "${value}px")
        fx("border-radius", "${value}px")
        fx("background-insets", "0")
    }

    fun surface(
        background: String,
        border: String,
        radius: Int,
    ) {
        fx("background-color", background)
        fx("border-color", border)
        radius(radius)
    }

    fun build(): List<String> = declarations.toList()
}

private fun StylesheetBuilder.baseStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography

    rule(".root.korafx-root") {
        fx("base", colors.surfaceMuted)
        fx("accent", colors.primary)
        fx("focus-color", colors.primary)
        fx("faint-focus-color", "transparent")
        fx("control-inner-background", colors.surfaceMuted)
        fx("background-color", colors.surface)
        fx("font-family", typography.fontFamily)
        fx("font-size", "${typography.baseSize}px")
    }

    rule(".label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".label.headline") {
        fx("font-size", "${typography.headlineSize}px")
        fx("font-weight", "700")
    }

    rule(".muted") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".tool-bar", ".menu-bar", ".status-strip", ".context-menu") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
    }

    rule(".tool-bar", ".menu-bar") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".tool-bar") {
        fx("padding", "12 16 12 16")
    }

    rule(".context-menu") {
        radius(context.smallRadius)
        fx("padding", "6 6 6 6")
    }

    rule(".menu", ".menu-item", ".check-menu-item", ".radio-menu-item") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
    }

    rule(".menu .label", ".menu-item .label", ".check-menu-item .label", ".radio-menu-item .label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".menu:hover", ".menu:showing", ".menu-item:focused", ".check-menu-item:focused", ".radio-menu-item:focused") {
        fx("background-color", "derive(${colors.primary}, 82%)")
    }
}

private fun StylesheetBuilder.buttonStyles(context: ThemeCssContext) {
    val colors = context.colors

    rule(".button", ".toggle-button", ".menu-button", ".split-menu-button") {
        primaryControl(context)
    }

    rule(".button:hover", ".toggle-button:hover", ".menu-button:hover", ".split-menu-button:hover") {
        fx("background-color", "derive(${colors.primary}, -8%)")
    }

    rule(".button:armed", ".toggle-button:selected", ".menu-button:showing", ".split-menu-button:showing") {
        fx("background-color", "derive(${colors.primary}, -14%)")
    }

    rule(
        ".button:focused",
        ".toggle-button:focused",
        ".menu-button:focused",
        ".split-menu-button:focused",
        ".text-field:focused",
        ".password-field:focused",
        ".text-area:focused",
        ".combo-box-base:focused",
        ".choice-box:focused",
        ".date-picker:focused",
        ".color-picker:focused",
        ".spinner:focused",
        ".list-view:focused",
        ".table-view:focused",
        ".tree-view:focused",
    ) {
        fx("border-color", colors.primary)
    }

    rule(
        ".button:disabled",
        ".toggle-button:disabled",
        ".menu-button:disabled",
        ".split-menu-button:disabled",
        ".text-input:disabled",
        ".combo-box-base:disabled",
        ".choice-box:disabled",
        ".date-picker:disabled",
        ".color-picker:disabled",
        ".spinner:disabled",
    ) {
        fx("opacity", "0.56")
    }

    rule(".button.ghost-button", ".button.nav-button", ".toggle-button") {
        ghostControl(context)
    }

    rule(".button.ghost-button:hover", ".button.nav-button:hover", ".toggle-button:hover") {
        fx("background-color", "derive(${colors.surfaceMuted}, -4%)")
    }

    rule(".button.nav-button") {
        fx("alignment", "center-left")
        fx("max-width", "Infinity")
    }

    rule(".button.nav-button-active", ".button.nav-button-active:hover", ".toggle-button:selected") {
        fx("background-color", colors.primary)
        fx("text-fill", "white")
        fx("border-color", "transparent")
    }

    rule(".menu-button .label", ".split-menu-button .label", ".button.nav-button-active .label") {
        fx("text-fill", "white")
    }
}

private fun StylesheetBuilder.inputStyles(context: ThemeCssContext) {
    val colors = context.colors

    rule(".check-box", ".radio-button") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".check-box .box", ".radio-button .radio") {
        surface(colors.surfaceMuted, colors.border, context.smallRadius)
    }

    rule(".check-box:selected .box", ".radio-button:selected .radio") {
        fx("background-color", colors.primary)
        fx("border-color", colors.primary)
    }

    rule(".check-box:selected .mark", ".radio-button:selected .dot") {
        fx("background-color", "white")
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
        fx("padding", "8 10 8 10")
    }

    rule(".text-area") {
        fx("control-inner-background", colors.surfaceMuted)
        fx("highlight-fill", colors.primary)
        fx("highlight-text-fill", "white")
        fx("padding", "8px")
    }

    rule(".text-area .content", ".scroll-pane", ".scroll-pane .viewport") {
        fx("background-color", colors.surfaceMuted)
        fx("background-radius", "${context.radius}px")
    }

    rule(
        ".combo-box-base .arrow-button",
        ".date-picker .arrow-button",
        ".color-picker .arrow-button",
        ".spinner .increment-arrow-button",
        ".spinner .decrement-arrow-button",
    ) {
        fx("background-color", "transparent")
        fx("background-radius", "${context.radius}px")
    }

    rule(
        ".combo-box-base .arrow",
        ".choice-box .open-button .arrow",
        ".date-picker .arrow",
        ".color-picker .arrow",
        ".spinner .increment-arrow",
        ".spinner .decrement-arrow",
    ) {
        fx("background-color", colors.textSecondary)
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
        fx("border-color", colors.danger)
    }
}

private fun StylesheetBuilder.dataControlStyles(context: ThemeCssContext) {
    val colors = context.colors

    rule(".list-view", ".table-view", ".tree-view") {
        fx("padding", "0")
    }

    rule(".list-cell", ".tree-cell", ".table-row-cell") {
        fx("background-color", colors.surfaceMuted)
        fx("text-fill", colors.textPrimary)
        fx("border-color", "transparent")
    }

    rule(".list-cell:odd", ".tree-cell:odd", ".table-row-cell:odd") {
        fx("background-color", "derive(${colors.surfaceMuted}, -2%)")
    }

    rule(".list-cell:hover", ".tree-cell:hover", ".table-row-cell:hover") {
        fx("background-color", "derive(${colors.primary}, 88%)")
    }

    rule(".list-cell:selected", ".tree-cell:selected", ".table-row-cell:selected") {
        fx("background-color", colors.primary)
        fx("text-fill", "white")
    }

    rule(".table-view .column-header-background", ".table-view .column-header", ".table-view .filler") {
        fx("background-color", "derive(${colors.surfaceMuted}, -4%)")
        fx("border-color", colors.border)
    }

    rule(".table-view .column-header .label") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "700")
    }

    rule(".tree-view .tree-cell .tree-disclosure-node .arrow") {
        fx("background-color", colors.textSecondary)
    }
}

private fun StylesheetBuilder.navigationControlStyles(context: ThemeCssContext) {
    val colors = context.colors

    rule(".tab-pane") {
        fx("background-color", colors.surface)
    }

    rule(".tab-pane .tab-header-area .tab-header-background") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".tab-pane .tab") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-radius", "${context.radius}px ${context.radius}px 0 0")
        fx("border-radius", "${context.radius}px ${context.radius}px 0 0")
        fx("padding", "8 14 8 14")
    }

    rule(".tab-pane .tab:selected") {
        fx("background-color", colors.primary)
    }

    rule(".tab-pane .tab:selected .tab-label") {
        fx("text-fill", "white")
    }

    rule(".split-pane") {
        fx("background-color", colors.surface)
    }

    rule(".split-pane-divider") {
        fx("background-color", colors.border)
        fx("padding", "0 1 0 1")
    }

    rule(".titled-pane > .title", ".titled-pane > .content") {
        surface(colors.surfaceMuted, colors.border, context.radius)
    }

    rule(".titled-pane > .title > .text") {
        fx("fill", colors.textPrimary)
    }

    rule(".titled-pane > .content") {
        fx("background-radius", "0 0 ${context.radius}px ${context.radius}px")
        fx("border-radius", "0 0 ${context.radius}px ${context.radius}px")
    }

    rule(".accordion") {
        fx("background-color", "transparent")
    }

    rule(".slider .track") {
        fx("background-color", colors.border)
        fx("background-radius", "${context.radius}px")
    }

    rule(".slider .thumb") {
        fx("background-color", colors.primary)
        fx("background-radius", "${context.radius}px")
    }

    rule(".progress-bar .track", ".progress-indicator") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
    }

    rule(".progress-bar .bar", ".progress-indicator .percentage") {
        fx("background-color", colors.primary)
    }

    rule(".pagination .pagination-control .button") {
        ghostControl(context)
    }

    rule(".pagination .pagination-control .page-number") {
        fx("background-radius", "${context.radius}px")
        fx("border-radius", "${context.radius}px")
    }

    rule(".pagination .pagination-control .page-number:selected") {
        fx("background-color", colors.primary)
        fx("text-fill", "white")
    }

    rule(".separator .line") {
        fx("border-color", colors.border)
    }

    rule(".scroll-bar", ".scroll-bar .track") {
        fx("background-color", "transparent")
    }

    rule(".scroll-bar .thumb") {
        fx("background-color", "derive(${colors.border}, -8%)")
        fx("background-radius", "${context.radius}px")
    }

    rule(".scroll-bar .thumb:hover") {
        fx("background-color", colors.textSecondary)
    }

    rule(".hyperlink") {
        fx("text-fill", colors.primary)
        fx("border-color", "transparent")
        fx("padding", "0")
    }
}

private fun StylesheetBuilder.componentStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography

    rule(".panel", ".card", ".feedback-state", ".modal-card", ".snackbar", ".text-flow") {
        surface(colors.surfaceMuted, colors.border, context.radius)
    }

    rule(".panel", ".card") {
        fx("effect", "dropshadow(gaussian, rgba(0, 0, 0, 0.06), 10, 0.10, 0, 2)")
    }

    rule(".section") {
        fx("background-color", colors.surfaceMuted)
    }

    rule(".section-title") {
        fx("font-size", "${typography.baseSize + 4}px")
        fx("font-weight", "700")
    }

    rule(".section-description", ".modal-message", ".snackbar-message", ".form-helper") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".action-bar") {
        fx("padding", "8 0 0 0")
    }

    rule(".nav-rail") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent ${colors.border} transparent transparent")
        fx("padding", "18 14 18 14")
    }

    rule(".status-strip") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "${colors.border} transparent transparent transparent")
        fx("padding", "12 16 12 16")
    }

    rule(".app-shell", ".app-shell-layout") {
        fx("background-color", colors.surface)
    }

    rule(".app-shell-overlay", ".modal-host") {
        fx("background-color", "transparent")
    }

    rule(".modal-backdrop") {
        fx("background-color", "rgba(17, 24, 39, 0.42)")
        fx("padding", "24 24 24 24")
    }

    rule(".modal-card") {
        fx("effect", "dropshadow(gaussian, rgba(0, 0, 0, 0.22), 18, 0.16, 0, 8)")
    }

    rule(".modal-title") {
        fx("font-size", "${typography.baseSize + 4}px")
        fx("font-weight", "700")
    }

    rule(".modal-content") {
        fx("padding", "4 0 4 0")
    }

    rule(".modal-actions") {
        fx("padding", "6 0 0 0")
    }

    rule(".modal-secondary-action") {
        ghostControl(context)
    }

    rule(".modal-destructive-action") {
        fx("background-color", colors.danger)
        fx("text-fill", "white")
    }

    rule(".toast-host") {
        fx("padding", "16 16 16 16")
    }

    rule(".snackbar") {
        fx("effect", "dropshadow(gaussian, rgba(0, 0, 0, 0.18), 12, 0.12, 0, 4)")
        fx("min-width", "320px")
        fx("max-width", "460px")
    }

    rule(".snackbar-title", ".form-label") {
        fx("font-weight", "700")
    }

    rule(".snackbar-action", ".snackbar-dismiss") {
        fx("padding", "8 12 8 12")
    }

    rule(".toast-info") {
        fx("border-color", colors.info)
    }

    rule(".toast-success") {
        fx("border-color", colors.success)
    }

    rule(".toast-warning") {
        fx("border-color", colors.warning)
    }

    rule(".toast-error", ".error-state") {
        fx("border-color", colors.danger)
    }

    rule(".feedback-title", ".feedback-message") {
        fx("alignment", "center")
        fx("text-alignment", "center")
    }

    rule(".loading-state-indicator") {
        fx("progress-color", colors.primary)
    }

    rule(".validation-message") {
        fx("text-fill", colors.danger)
    }
}

private fun RuleBuilder.primaryControl(context: ThemeCssContext) {
    val colors = context.colors
    fx("background-color", colors.primary)
    fx("text-fill", "white")
    fx("border-color", "transparent")
    radius(context.radius)
    fx("padding", "10 16 10 16")
    fx("cursor", "hand")
}

private fun RuleBuilder.ghostControl(context: ThemeCssContext) {
    val colors = context.colors
    fx("background-color", colors.surfaceMuted)
    fx("text-fill", colors.textPrimary)
    fx("border-color", colors.border)
    radius(context.radius)
}
