package dev.korafx.framework.theme

internal data class ThemeCssContext(
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
internal annotation class ThemeCssDsl

internal fun cssStylesheet(content: StylesheetBuilder.() -> Unit): String =
    StylesheetBuilder().apply(content).build()

@ThemeCssDsl
internal class StylesheetBuilder {
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
internal class RuleBuilder {
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
