package dev.korafx.framework.theme

object ThemeStylesheetFactory {
    fun render(theme: KoraTheme): String {
        val context = ThemeCssContext(theme)
        return cssStylesheet {
            baseStyles(context)
            buttonStyles(context)
            inputStyles(context)
            dataControlStyles(context)
            navigationControlStyles(context)
            overlayControlStyles(context)
            componentStyles(context)
        }
    }
}

private data class ThemeCssContext(
    val theme: KoraTheme,
) {
    val colors: ColorTokens = theme.tokens.colors
    val typography: TypographyTokens = theme.tokens.typography
    val spacing: SpacingTokens = theme.tokens.spacing
    val radii: RadiusTokens = theme.tokens.radii
    val states: StateColorTokens = theme.tokens.states
    val elevation: ElevationTokens = theme.tokens.elevation
    val radius: Int = radii.medium
    val smallRadius: Int = radii.small
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

    fun padding(value: Int) {
        fx("padding", "$value $value $value $value")
    }

    fun padding(
        vertical: Int,
        horizontal: Int,
    ) {
        fx("padding", "$vertical $horizontal $vertical $horizontal")
    }

    fun padding(
        top: Int,
        right: Int,
        bottom: Int,
        left: Int,
    ) {
        fx("padding", "$top $right $bottom $left")
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
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".root.${ThemeStyleClass.Root}") {
        fx("base", colors.surfaceMuted)
        fx("accent", colors.primary)
        fx("focus-color", states.focus)
        fx("faint-focus-color", "transparent")
        fx("control-inner-background", colors.surfaceMuted)
        fx("background-color", colors.surface)
        fx("font-family", typography.fontFamily)
        fx("font-size", "${typography.baseSize}px")
    }

    rule(".label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".label.${ThemeStyleClass.Headline}") {
        fx("font-size", "${typography.headlineSize}px")
        fx("font-weight", "700")
    }

    rule(".${ThemeStyleClass.Muted}") {
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
        padding(spacing.lg, spacing.xl)
    }

    rule(".context-menu") {
        surface(colors.surface, colors.border, context.radius)
        radius(context.smallRadius)
        padding(spacing.xs)
        fx("effect", elevation.dropdown)
        fx("background-insets", "0")
        fx("border-insets", "0")
    }

    rule(".menu", ".menu-item", ".check-menu-item", ".radio-menu-item") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("background-radius", "${context.smallRadius}px")
        fx("border-radius", "${context.smallRadius}px")
    }

    rule(".menu .label", ".menu-item .label", ".check-menu-item .label", ".radio-menu-item .label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".menu:hover", ".menu:showing", ".menu-item:focused", ".check-menu-item:focused", ".radio-menu-item:focused") {
        fx("background-color", states.surfaceHover)
    }

    rule(".menu-item:focused .label", ".check-menu-item:focused .label", ".radio-menu-item:focused .label") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".check-menu-item:checked > .left-container > .check", ".radio-menu-item:checked > .left-container > .radio") {
        fx("background-color", states.selected)
        fx("background-insets", "0")
    }
}

private fun StylesheetBuilder.buttonStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states

    rule(".button", ".toggle-button", ".menu-button") {
        primaryControl(context)
    }

    rule(".button:hover", ".toggle-button:hover", ".menu-button:hover") {
        fx("background-color", states.controlHover)
    }

    rule(".button:armed", ".toggle-button:selected", ".menu-button:showing") {
        fx("background-color", states.controlPressed)
    }

    rule(".menu-button > .label") {
        fx("background-color", "transparent")
        fx("text-fill", states.selectedText)
        fx("padding", "0")
        fx("alignment", "center-left")
    }

    rule(".menu-button > .arrow-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("padding", "0 0 0 ${spacing.md}")
    }

    rule(".split-menu-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("padding", "0")
        fx("cursor", "hand")
    }

    rule(".split-menu-button > .label", ".split-menu-button > .arrow-button") {
        fx("background-color", states.selected)
        fx("border-color", "transparent")
        fx("background-insets", "0")
        fx("border-insets", "0")
        fx("text-fill", states.selectedText)
        fx("cursor", "hand")
    }

    rule(".split-menu-button > .label") {
        padding(spacing.md, spacing.xl)
        fx("alignment", "center-left")
        fx("background-radius", "${context.radius}px 0 0 ${context.radius}px")
        fx("border-radius", "${context.radius}px 0 0 ${context.radius}px")
    }

    rule(".split-menu-button > .arrow-button") {
        padding(spacing.md, spacing.lg)
        fx("border-color", "transparent transparent transparent rgba(255, 255, 255, 0.28)")
        fx("border-width", "0 0 0 1px")
        fx("background-radius", "0 ${context.radius}px ${context.radius}px 0")
        fx("border-radius", "0 ${context.radius}px ${context.radius}px 0")
    }

    rule(
        ".menu-button > .arrow-button > .arrow",
        ".split-menu-button > .arrow-button > .arrow",
    ) {
        fx("background-color", states.selectedText)
        fx("background-insets", "0")
    }

    rule(".split-menu-button > .label:hover", ".split-menu-button > .arrow-button:hover") {
        fx("background-color", states.controlHover)
    }

    rule(
        ".split-menu-button:armed > .label",
        ".split-menu-button > .arrow-button:pressed",
        ".split-menu-button:showing > .arrow-button",
    ) {
        fx("background-color", states.controlPressed)
    }

    rule(".split-menu-button:focused") {
        fx("effect", "dropshadow(gaussian, derive(${states.focus}, 55%), 8, 0.18, 0, 0)")
    }

    rule(".menu-button > .context-menu", ".split-menu-button > .context-menu") {
        surface(colors.surface, colors.border, context.radius)
        fx("effect", context.elevation.dropdown)
    }

    rule(".menu-bar > .container > .menu-button") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("border-color", "transparent")
        radius(context.smallRadius)
        padding(spacing.sm, spacing.lg)
        fx("cursor", "hand")
    }

    rule(".menu-bar > .container > .menu-button:hover", ".menu-bar > .container > .menu-button:showing") {
        fx("background-color", states.surfaceHover)
    }

    rule(".menu-bar > .container > .menu-button > .label") {
        fx("background-color", "transparent")
        fx("text-fill", colors.textPrimary)
        fx("padding", "0")
    }

    rule(".menu-bar > .container > .menu-button > .arrow-button") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("padding", "0")
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
        fx("border-color", states.focus)
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
        fx("opacity", states.disabledOpacity.toString())
    }

    rule(".button.ghost-button", ".button.nav-button", ".toggle-button") {
        ghostControl(context)
    }

    rule(".button.ghost-button:hover", ".button.nav-button:hover", ".toggle-button:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".button.nav-button") {
        fx("alignment", "center-left")
        fx("max-width", "Infinity")
    }

    rule(".button.nav-button-active", ".button.nav-button-active:hover", ".toggle-button:selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
        fx("border-color", "transparent")
    }

    rule(".menu-button .label", ".split-menu-button .label", ".button.nav-button-active .label") {
        fx("text-fill", states.selectedText)
    }
}

private fun StylesheetBuilder.inputStyles(context: ThemeCssContext) {
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

private fun StylesheetBuilder.dataControlStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states

    rule(".list-view", ".table-view", ".tree-view", ".tree-table-view") {
        fx("padding", "0")
    }

    rule(".list-cell", ".tree-cell", ".table-row-cell", ".tree-table-row-cell") {
        fx("background-color", colors.surfaceMuted)
        fx("text-fill", colors.textPrimary)
        fx("border-color", "transparent")
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

    rule(".table-view .column-resize-line", ".tree-table-view .column-resize-line") {
        fx("background-color", states.focus)
    }

    rule(".tree-view .tree-cell .tree-disclosure-node .arrow", ".tree-table-view .tree-table-row-cell .tree-disclosure-node .arrow") {
        fx("background-color", colors.textSecondary)
    }
}

private fun StylesheetBuilder.navigationControlStyles(context: ThemeCssContext) {
    val colors = context.colors
    val spacing = context.spacing
    val states = context.states

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
        padding(spacing.sm, spacing.xl)
    }

    rule(".tab-pane .tab:selected") {
        fx("background-color", states.selected)
    }

    rule(".tab-pane .tab:selected .tab-label") {
        fx("text-fill", states.selectedText)
    }

    rule(".tab-pane .tab:focused") {
        fx("border-color", states.focus)
    }

    rule(".tab-pane .tab-close-button") {
        fx("background-color", colors.textSecondary)
    }

    rule(".tab-workspace") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.large)
    }

    rule(".tab-workspace .tab-content-area") {
        fx("background-color", colors.surface)
    }

    rule(".tab-workspace .tab-workspace-empty-tab") {
        fx("opacity", "1")
    }

    rule(".tab-workspace .tab-workspace-empty-tab .tab-label") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".tab-workspace-empty-pane") {
        fx("background-color", colors.surface)
        padding(spacing.xl)
    }

    rule(".tab-workspace-empty") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".tab-workspace .tab-workspace-tab-dirty .tab-label") {
        fx("font-weight", "800")
    }

    rule(".tab-workspace-dirty-marker") {
        fx("text-fill", colors.warning)
        fx("font-weight", "900")
    }

    rule(".split-pane") {
        fx("background-color", colors.surface)
    }

    rule(".split-pane-divider", ".split-pane:horizontal > .split-pane-divider", ".split-pane:vertical > .split-pane-divider") {
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

    rule(".accordion > .titled-pane > .title") {
        fx("background-color", colors.surfaceMuted)
    }

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

    rule(".pagination .pagination-control .page-number:selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
    }

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
    }

    rule(".scroll-bar .thumb:hover") {
        fx("background-color", colors.textSecondary)
    }

    rule(".hyperlink") {
        fx("text-fill", colors.primary)
        fx("border-color", "transparent")
        fx("padding", "0")
    }

    rule(".tool-bar:horizontal .separator .line", ".tool-bar:vertical .separator .line") {
        fx("border-color", colors.border)
    }
}

private fun StylesheetBuilder.overlayControlStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".tooltip") {
        surface(colors.surfaceMuted, colors.border, context.smallRadius)
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize - 1}px")
        fx("effect", elevation.dropdown)
        padding(spacing.sm, spacing.md)
    }

    rule(".dialog-pane") {
        surface(colors.surface, colors.border, context.radii.large)
        fx("effect", elevation.modal)
        fx("padding", "0")
    }

    rule(".dialog-pane > .header-panel") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent transparent ${colors.border} transparent")
        fx("background-radius", "${context.radii.large}px ${context.radii.large}px 0 0")
        padding(spacing.xl)
    }

    rule(".dialog-pane > .header-panel .label") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".dialog-pane > .content", ".dialog-pane > .content.label") {
        fx("text-fill", colors.textPrimary)
        padding(spacing.xl)
    }

    rule(".dialog-pane > .button-bar", ".dialog-pane > .button-bar > .container", ".button-bar", ".button-bar > .container") {
        fx("background-color", colors.surface)
        fx("border-color", "${colors.border} transparent transparent transparent")
        padding(spacing.lg, spacing.xl)
    }

    rule(".button-bar .button") {
        padding(spacing.md, spacing.xl)
    }

    rule(".dialog-pane .graphic-container") {
        fx("background-color", "transparent")
        padding(spacing.xl, 0, spacing.xl, spacing.xl)
    }

    rule(".dialog-pane:focused") {
        fx("border-color", states.focus)
    }
}

private fun StylesheetBuilder.componentStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".panel", ".card", ".feedback-state", ".modal-card", ".snackbar", ".text-flow") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
    }

    rule(".panel", ".card") {
        fx("effect", elevation.card)
    }

    rule(".border-layout") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        fx("padding", "0")
    }

    rule(".border-layout-top", ".border-layout-bottom", ".border-layout-left", ".border-layout-right") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.lg, spacing.xl)
    }

    rule(".border-layout-top") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
        fx("background-radius", "${context.radii.large}px ${context.radii.large}px 0 0")
    }

    rule(".border-layout-bottom") {
        fx("border-color", "${colors.border} transparent transparent transparent")
        fx("background-radius", "0 0 ${context.radii.large}px ${context.radii.large}px")
    }

    rule(".border-layout-left") {
        fx("border-color", "transparent ${colors.border} transparent transparent")
    }

    rule(".border-layout-right") {
        fx("border-color", "transparent transparent transparent ${colors.border}")
    }

    rule(".border-layout-center") {
        fx("background-color", colors.surface)
        padding(spacing.xl)
    }

    rule(".workspace-layout") {
        fx("background-color", colors.surface)
    }

    rule(".workspace-layout-frame", ".workspace-layout-body") {
        fx("background-color", colors.surface)
    }

    rule(".workspace-layout-top-bar", ".workspace-layout-status") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.lg, spacing.xl)
    }

    rule(".workspace-layout-top-bar") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
    }

    rule(".workspace-layout-status") {
        fx("border-color", "${colors.border} transparent transparent transparent")
    }

    rule(".workspace-layout-navigation", ".workspace-layout-details") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.xl)
    }

    rule(".workspace-layout-navigation") {
        fx("border-color", "transparent ${colors.border} transparent transparent")
    }

    rule(".workspace-layout-details") {
        fx("border-color", "transparent transparent transparent ${colors.border}")
    }

    rule(".workspace-layout-content") {
        fx("background-color", colors.surface)
        padding(spacing.xl)
    }

    rule(".workspace-layout-overlay") {
        fx("background-color", "transparent")
    }

    rule(".workspace-layout-overlay-item") {
        fx("effect", elevation.dropdown)
    }

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

    rule(".resource-explorer-tree") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("padding", "0")
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

    rule(".resource-explorer-tree .tree-cell:selected .tree-disclosure-node .arrow") {
        fx("background-color", states.selectedText)
    }

    rule(".editable-table") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
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

    rule(".data-grid-footer-label", ".data-grid-empty", ".data-grid-loading") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".inspector-panel") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.lg)
    }

    rule(".inspector-panel-header") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
        padding(0, 0, spacing.md, 0)
    }

    rule(".inspector-panel-title-row", ".inspector-panel-metadata", ".inspector-panel-actions") {
        fx("background-color", "transparent")
    }

    rule(".inspector-panel-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.baseSize + 2}px")
        fx("font-weight", "800")
    }

    rule(".inspector-panel-subtitle") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".inspector-panel-metadata-item") {
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".inspector-panel-body") {
        fx("background-color", "transparent")
    }

    rule(".inspector-panel-section") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".inspector-panel-section-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "700")
    }

    rule(".inspector-panel-property") {
        fx("background-color", "transparent")
        padding(spacing.xs, 0)
    }

    rule(".inspector-panel-property-name") {
        fx("text-fill", colors.textSecondary)
        fx("font-weight", "700")
        fx("min-width", "96px")
        fx("pref-width", "96px")
    }

    rule(".inspector-panel-property-value", ".inspector-panel-content", ".inspector-panel-section-content") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".inspector-panel-actions") {
        fx("border-color", "${colors.border} transparent transparent transparent")
        padding(spacing.md, 0, 0, 0)
    }

    rule(".inspector-panel-action") {
        fx("cursor", "hand")
    }

    rule(".button.inspector-panel-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }

    rule(".inspector-panel-empty") {
        fx("text-fill", colors.textSecondary)
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.lg)
    }

    rule(".activity-timeline") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.lg)
    }

    rule(".activity-timeline-content") {
        fx("background-color", "transparent")
    }

    rule(".activity-timeline-empty") {
        fx("text-fill", colors.textSecondary)
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.lg)
    }

    rule(".activity-timeline-group") {
        fx("text-fill", colors.textSecondary)
        fx("font-weight", "800")
        padding(spacing.md, 0, spacing.sm, 0)
    }

    rule(".activity-timeline-row") {
        fx("background-color", "transparent")
        padding(spacing.sm, 0)
    }

    rule(".activity-timeline-marker-column") {
        fx("min-width", "18px")
        fx("pref-width", "18px")
    }

    rule(".activity-timeline-marker") {
        fx("background-color", colors.textSecondary)
        fx("min-width", "10px")
        fx("min-height", "10px")
        fx("pref-width", "10px")
        fx("pref-height", "10px")
        fx("background-radius", "999px")
    }

    rule(".activity-timeline-connector") {
        fx("background-color", colors.border)
        fx("min-width", "2px")
        fx("pref-width", "2px")
        fx("min-height", "42px")
    }

    rule(".activity-timeline-event") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".activity-timeline-meta") {
        fx("background-color", "transparent")
    }

    rule(".activity-timeline-time", ".activity-timeline-message") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".activity-timeline-time") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "700")
    }

    rule(".activity-timeline-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".activity-timeline-action") {
        ghostControl(context)
        padding(spacing.xs, spacing.md)
    }

    rule(".activity-timeline-row.tone-success .activity-timeline-marker") {
        fx("background-color", colors.success)
    }

    rule(".activity-timeline-row.tone-warning .activity-timeline-marker") {
        fx("background-color", colors.warning)
    }

    rule(".activity-timeline-row.tone-danger .activity-timeline-marker") {
        fx("background-color", colors.danger)
    }

    rule(".activity-timeline-row.tone-info .activity-timeline-marker") {
        fx("background-color", colors.info)
    }

    rule(".command-palette") {
        fx("background-color", "transparent")
    }

    rule(".command-palette-scrim") {
        fx("background-color", "rgba(15, 23, 42, 0.38)")
    }

    rule(".command-palette-card") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.dropdown)
        padding(spacing.lg)
    }

    rule(".command-palette-search") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        fx("text-fill", colors.textPrimary)
        radius(context.radius)
        padding(spacing.md, spacing.lg)
    }

    rule(".command-palette-search:focused") {
        fx("border-color", states.focus)
    }

    rule(".command-palette-results", ".command-palette-row-content", ".command-palette-row-text") {
        fx("background-color", "transparent")
    }

    rule(".command-palette-group") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "800")
        padding(spacing.md, spacing.xs, spacing.xs, spacing.xs)
    }

    rule(".command-palette-row") {
        fx("background-color", "transparent")
        fx("border-color", "transparent")
        fx("alignment", "center-left")
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".command-palette-row:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".command-palette-row-selected", ".command-palette-row-selected:hover") {
        fx("background-color", states.selected)
        fx("border-color", states.selected)
    }

    rule(".command-palette-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".command-palette-description", ".command-palette-id", ".command-palette-empty") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".command-palette-row-selected .command-palette-title", ".command-palette-row-selected .command-palette-description", ".command-palette-row-selected .command-palette-id") {
        fx("text-fill", states.selectedText)
    }

    rule(".command-palette-id") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".command-palette-empty") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.lg)
    }

    rule(".badge", ".chip") {
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "700")
        fx("border-width", "1px")
        radius(context.radii.pill)
    }

    rule(".badge") {
        padding(spacing.xs, spacing.md)
    }

    rule(".chip") {
        padding(spacing.sm, spacing.lg)
        fx("cursor", "hand")
    }

    rule(".chip:hover") {
        fx("background-color", states.surfaceHover)
    }

    rule(".chip.chip-selected") {
        fx("background-color", states.selected)
        fx("text-fill", states.selectedText)
        fx("border-color", states.selected)
    }

    semanticTone("neutral", colors.textSecondary, colors.surfaceMuted, colors.border)
    semanticTone("primary", colors.primary, "derive(${colors.primary}, 88%)", colors.primary)
    semanticTone("success", colors.success, "derive(${colors.success}, 88%)", colors.success)
    semanticTone("warning", colors.warning, "derive(${colors.warning}, 88%)", colors.warning)
    semanticTone("danger", colors.danger, "derive(${colors.danger}, 88%)", colors.danger)
    semanticTone("info", colors.info, "derive(${colors.info}, 88%)", colors.info)

    rule(".metric-card") {
        fx("border-width", "1px 1px 1px 4px")
    }

    rule(".metric-label", ".metric-helper") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".metric-label") {
        fx("font-weight", "700")
    }

    rule(".metric-value") {
        fx("font-size", "${typography.headlineSize}px")
        fx("font-weight", "800")
        fx("text-fill", colors.textPrimary)
    }

    rule(".alert-banner") {
        fx("border-width", "1px 1px 1px 4px")
    }

    rule(".alert-title") {
        fx("font-weight", "800")
        fx("text-fill", colors.textPrimary)
    }

    rule(".alert-message") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".alert-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }

    rule(".code-editor") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        fx("padding", "0")
    }

    rule(".code-editor-toolbar", ".code-editor-status") {
        fx("background-color", colors.surfaceMuted)
        padding(spacing.sm, spacing.md)
    }

    rule(".code-editor-toolbar") {
        fx("border-color", "transparent transparent ${colors.border} transparent")
        fx("background-radius", "${context.radii.large}px ${context.radii.large}px 0 0")
    }

    rule(".code-editor-status") {
        fx("border-color", "${colors.border} transparent transparent transparent")
        fx("background-radius", "0 0 ${context.radii.large}px ${context.radii.large}px")
    }

    rule(".code-editor-title") {
        fx("font-weight", "800")
        fx("text-fill", colors.textPrimary)
    }

    rule(".code-editor-area") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("font-size", "${typography.baseSize}px")
        fx("text-fill", colors.textPrimary)
        fx("highlight-fill", states.selected)
        fx("highlight-text-fill", states.selectedText)
        fx("background-color", colors.surface)
        fx("border-color", "transparent")
        fx("background-radius", "0")
        fx("border-radius", "0")
    }

    rule(".code-editor-area .content") {
        fx("background-color", colors.surface)
        fx("background-radius", "0")
    }

    rule(".code-editor-status-text") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".source-editor") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.md)
    }

    rule(".source-editor-toolbar") {
        fx("background-color", "transparent")
        padding(0, 0, spacing.sm, 0)
    }

    rule(".source-editor-action") {
        ghostControl(context)
        padding(spacing.sm, spacing.lg)
    }

    rule(".source-editor-code") {
        fx("effect", "none")
    }

    rule(".source-editor-diagnostics", ".source-editor-result") {
        fx("background-color", colors.surface)
        fx("border-color", colors.border)
        radius(context.radii.medium)
        padding(spacing.md)
    }

    rule(".source-editor-diagnostics-title", ".source-editor-result-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
    }

    rule(".source-editor-diagnostic") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", colors.border)
        radius(context.smallRadius)
        padding(spacing.sm, spacing.md)
    }

    rule(".source-editor-diagnostic-location") {
        fx("font-family", "\"Cascadia Mono\", \"JetBrains Mono\", Consolas, monospace")
        fx("text-fill", colors.textSecondary)
        fx("font-weight", "700")
    }

    rule(".source-editor-diagnostic-message", ".source-editor-result-content", ".source-editor-result-node") {
        fx("text-fill", colors.textPrimary)
    }

    rule(".source-editor-diagnostic.tone-warning") {
        fx("border-color", colors.warning)
    }

    rule(".source-editor-diagnostic.tone-danger") {
        fx("border-color", colors.danger)
    }

    rule(".source-editor-diagnostic.tone-info") {
        fx("border-color", colors.info)
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
        padding(spacing.sm, 0, 0, 0)
    }

    rule(".form") {
        surface(colors.surfaceMuted, colors.border, context.radius)
        padding(spacing.md)
    }

    rule(".form-item") {
        padding(spacing.sm, 0, 0, 0)
    }

    rule(".form-label") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "600")
        fx("font-size", "${typography.baseSize + 1}px")
    }

    rule(".form-helper", ".validation-message") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".validation-message") {
        fx("text-fill", states.invalid)
    }

    rule(".submit-bar") {
        fx("padding", "${spacing.md}px 0 0 0")
    }

    rule(".nav-rail") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "transparent ${colors.border} transparent transparent")
        padding(spacing.xxl, spacing.xl)
    }

    rule(".route-state-host") {
        surface(colors.surfaceMuted, colors.border, context.radius)
        fx("padding", "${spacing.sm}px")
    }

    rule(".status-strip") {
        fx("background-color", colors.surfaceMuted)
        fx("border-color", "${colors.border} transparent transparent transparent")
        padding(spacing.lg, spacing.xl)
    }

    rule(".app-shell", ".app-shell-layout") {
        fx("background-color", colors.surface)
    }

    rule(".app-shell-overlay", ".modal-host") {
        fx("background-color", "transparent")
    }

    rule(".modal-backdrop") {
        fx("background-color", "rgba(17, 24, 39, 0.42)")
        padding(spacing.xxxl)
    }

    rule(".modal-card") {
        fx("effect", elevation.modal)
    }

    rule(".modal-title") {
        fx("font-size", "${typography.baseSize + 4}px")
        fx("font-weight", "700")
    }

    rule(".modal-content") {
        padding(spacing.xxs, 0)
    }

    rule(".modal-actions") {
        padding(spacing.xs, 0, 0, 0)
    }

    rule(".modal-secondary-action") {
        ghostControl(context)
    }

    rule(".modal-destructive-action") {
        fx("background-color", colors.danger)
        fx("text-fill", states.selectedText)
    }

    rule(".toast-host") {
        padding(spacing.xl)
    }

    rule(".snackbar") {
        fx("effect", elevation.snackbar)
        fx("min-width", "320px")
        fx("max-width", "460px")
    }

    rule(".snackbar-title", ".form-label") {
        fx("font-weight", "700")
    }

    rule(".snackbar-action", ".snackbar-dismiss") {
        padding(spacing.sm, spacing.lg)
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
        fx("progress-color", states.selected)
    }

    rule(".validation-message") {
        fx("text-fill", states.invalid)
    }
}

private fun RuleBuilder.primaryControl(context: ThemeCssContext) {
    val spacing = context.spacing
    val states = context.states
    fx("background-color", states.selected)
    fx("text-fill", states.selectedText)
    fx("border-color", "transparent")
    radius(context.radius)
    padding(spacing.md, spacing.xl)
    fx("cursor", "hand")
}

private fun RuleBuilder.ghostControl(context: ThemeCssContext) {
    val colors = context.colors
    fx("background-color", colors.surfaceMuted)
    fx("text-fill", colors.textPrimary)
    fx("border-color", colors.border)
    radius(context.radius)
}

private fun StylesheetBuilder.semanticTone(
    name: String,
    text: String,
    background: String,
    border: String,
) {
    rule(".tone-$name") {
        fx("background-color", background)
        fx("text-fill", text)
        fx("border-color", border)
    }

    rule(".metric-card.tone-$name") {
        fx("border-color", border)
    }

    rule(".alert-banner.tone-$name") {
        fx("border-color", border)
    }
}
