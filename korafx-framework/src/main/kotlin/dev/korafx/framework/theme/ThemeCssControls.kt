package dev.korafx.framework.theme

internal fun RuleBuilder.primaryControl(context: ThemeCssContext) {
    val spacing = context.spacing
    val states = context.states
    fx("background-color", states.selected)
    fx("text-fill", states.selectedText)
    fx("border-color", "transparent")
    radius(context.radius)
    padding(spacing.md, spacing.xl)
    fx("cursor", "hand")
}

internal fun RuleBuilder.ghostControl(context: ThemeCssContext) {
    val colors = context.colors
    fx("background-color", colors.surfaceMuted)
    fx("text-fill", colors.textPrimary)
    fx("border-color", colors.border)
    radius(context.radius)
}

internal fun StylesheetBuilder.semanticTone(
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

    rule(".tone-$name .ikonli-font-icon") {
        fx("icon-color", text)
    }

    rule(".metric-card.tone-$name") {
        fx("border-color", border)
    }

    rule(".alert-banner.tone-$name") {
        fx("border-color", border)
    }
}
