package dev.korafx.theme

data class ColorTokens(
    val primary: String,
    val surface: String,
    val surfaceMuted: String,
    val textPrimary: String,
    val textSecondary: String,
    val border: String,
    val success: String = "#16A34A",
    val warning: String = "#D97706",
    val danger: String = "#DC2626",
    val info: String = "#2563EB",
)

data class TypographyTokens(
    val fontFamily: String,
    val baseSize: Int,
    val headlineSize: Int,
)

data class SpacingTokens(
    val xxs: Int = 4,
    val xs: Int = 6,
    val sm: Int = 8,
    val md: Int = 10,
    val lg: Int = 12,
    val xl: Int = 16,
    val xxl: Int = 18,
    val xxxl: Int = 24,
) {
    init {
        require(listOf(xxs, xs, sm, md, lg, xl, xxl, xxxl).all { it >= 0 }) {
            "Spacing token values must be non-negative."
        }
    }

    companion object {
        fun comfortable(): SpacingTokens = SpacingTokens()

        fun compact(): SpacingTokens =
            SpacingTokens(
                xxs = 2,
                xs = 4,
                sm = 6,
                md = 8,
                lg = 10,
                xl = 12,
                xxl = 14,
                xxxl = 18,
            )
    }
}

data class RadiusTokens(
    val small: Int,
    val medium: Int,
    val large: Int,
    val pill: Int = 999,
) {
    init {
        require(listOf(small, medium, large, pill).all { it >= 0 }) {
            "Radius token values must be non-negative."
        }
    }

    companion object {
        fun fromBase(base: Int): RadiusTokens =
            RadiusTokens(
                small = (base - 4).coerceAtLeast(4),
                medium = base,
                large = base + 4,
            )
    }
}

data class StateColorTokens(
    val controlHover: String,
    val controlPressed: String,
    val surfaceHover: String,
    val rowHover: String,
    val rowAlternate: String,
    val selected: String,
    val selectedText: String = "white",
    val focus: String,
    val invalid: String,
    val scrollbarThumb: String,
    val disabledOpacity: Double = 0.56,
) {
    init {
        require(disabledOpacity in 0.0..1.0) {
            "Disabled opacity must be between 0.0 and 1.0."
        }
    }

    companion object {
        fun from(colors: ColorTokens): StateColorTokens =
            StateColorTokens(
                controlHover = "derive(${colors.primary}, -8%)",
                controlPressed = "derive(${colors.primary}, -14%)",
                surfaceHover = "derive(${colors.surfaceMuted}, -4%)",
                rowHover = "derive(${colors.primary}, 88%)",
                rowAlternate = "derive(${colors.surfaceMuted}, -2%)",
                selected = colors.primary,
                focus = colors.primary,
                invalid = colors.danger,
                scrollbarThumb = "derive(${colors.border}, -8%)",
            )
    }
}

data class ElevationTokens(
    val card: String = "dropshadow(gaussian, rgba(0, 0, 0, 0.06), 10, 0.10, 0, 2)",
    val dropdown: String = "dropshadow(gaussian, rgba(0, 0, 0, 0.14), 14, 0.12, 0, 4)",
    val modal: String = "dropshadow(gaussian, rgba(0, 0, 0, 0.22), 18, 0.16, 0, 8)",
    val snackbar: String = "dropshadow(gaussian, rgba(0, 0, 0, 0.18), 12, 0.12, 0, 4)",
)

data class ThemeTokens(
    val colors: ColorTokens,
    val typography: TypographyTokens,
    val radius: Int,
    val spacing: SpacingTokens = SpacingTokens.comfortable(),
    val radii: RadiusTokens = RadiusTokens.fromBase(radius),
    val states: StateColorTokens = StateColorTokens.from(colors),
    val elevation: ElevationTokens = ElevationTokens(),
) {
    init {
        require(radius >= 0) {
            "Theme radius must be non-negative."
        }
    }
}

data class KoraTheme(
    val id: String,
    val displayName: String,
    val tokens: ThemeTokens,
)
