package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentHeaderStyles(context: ThemeCssContext) {
    val colors = context.colors
    val typography = context.typography
    val spacing = context.spacing
    val states = context.states
    val elevation = context.elevation

    rule(".breadcrumb") {
        fx("background-color", "transparent")
        padding(spacing.xs, 0)
    }

    rule(".breadcrumb-item") {
        fx("font-size", "${typography.baseSize - 1}px")
    }

    rule(".breadcrumb-item-link") {
        fx("text-fill", colors.primary)
        fx("border-color", "transparent")
        fx("background-color", "transparent")
        radius(context.smallRadius)
        padding(spacing.xs, spacing.sm)
    }

    rule(".breadcrumb-item-link:hover") {
        fx("background-color", states.surfaceHover)
        fx("text-fill", states.focus)
    }

    rule(".breadcrumb-item-current") {
        fx("text-fill", colors.textPrimary)
        fx("font-weight", "800")
        padding(spacing.xs, spacing.sm)
    }

    rule(".breadcrumb-item-disabled", ".breadcrumb-separator") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".breadcrumb-item .ikonli-font-icon") {
        fx("icon-color", colors.textSecondary)
    }

    rule(".page-header") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.lg)
    }

    rule(".page-header-title-row", ".page-header-actions", ".page-header-content") {
        fx("background-color", "transparent")
    }

    rule(".page-header-eyebrow") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "800")
    }

    rule(".page-header-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.headlineSize}px")
        fx("font-weight", "900")
    }

    rule(".page-header-title .ikonli-font-icon") {
        fx("icon-color", colors.primary)
    }

    rule(".page-header-subtitle") {
        fx("text-fill", colors.textSecondary)
    }

    rule(".hero-banner") {
        surface(colors.surfaceMuted, colors.border, context.radii.large)
        fx("effect", elevation.card)
        padding(spacing.lg)
    }

    rule(".hero-banner-header", ".hero-banner-actions", ".hero-banner-content") {
        fx("background-color", "transparent")
    }

    rule(".hero-banner-icon") {
        fx("background-color", "derive(${colors.primary}, 88%)")
        radius(context.radii.large)
        padding(spacing.md)
    }

    rule(".hero-banner-icon-glyph") {
        fx("icon-color", colors.primary)
    }

    rule(".hero-banner-eyebrow") {
        fx("text-fill", colors.textSecondary)
        fx("font-size", "${typography.baseSize - 1}px")
        fx("font-weight", "800")
    }

    rule(".hero-banner-title") {
        fx("text-fill", colors.textPrimary)
        fx("font-size", "${typography.headlineSize + 2}px")
        fx("font-weight", "900")
    }

    rule(".hero-banner-subtitle") {
        fx("text-fill", colors.textSecondary)
    }
}
