package dev.korafx.theme

object ThemeStylesheetFactory {
    fun render(theme: KoraTheme): String {
        val colors = theme.tokens.colors
        val typography = theme.tokens.typography

        return """
            .root.korafx-root {
                -fx-background-color: ${colors.surface};
                -fx-font-family: ${typography.fontFamily};
                -fx-font-size: ${typography.baseSize}px;
            }

            .tool-bar {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: transparent transparent ${colors.border} transparent;
                -fx-padding: 12 16 12 16;
            }

            .menu-bar {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: transparent transparent ${colors.border} transparent;
            }

            .panel {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: ${colors.border};
                -fx-background-radius: ${theme.tokens.radius}px;
                -fx-border-radius: ${theme.tokens.radius}px;
            }

            .card {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: ${colors.border};
                -fx-background-radius: ${theme.tokens.radius}px;
                -fx-border-radius: ${theme.tokens.radius}px;
            }

            .section {
                -fx-background-color: ${colors.surfaceMuted};
            }

            .section-title {
                -fx-font-size: ${typography.baseSize + 4}px;
                -fx-font-weight: 700;
            }

            .section-description {
                -fx-text-fill: ${colors.textSecondary};
            }

            .action-bar {
                -fx-padding: 8 0 0 0;
            }

            .nav-rail {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: transparent ${colors.border} transparent transparent;
                -fx-padding: 18 14 18 14;
            }

            .label {
                -fx-text-fill: ${colors.textPrimary};
            }

            .label.headline {
                -fx-font-size: ${typography.headlineSize}px;
                -fx-font-weight: 700;
            }

            .button {
                -fx-background-color: ${colors.primary};
                -fx-text-fill: white;
                -fx-background-radius: ${theme.tokens.radius}px;
                -fx-padding: 10 16 10 16;
                -fx-cursor: hand;
            }

            .menu-button,
            .split-menu-button {
                -fx-background-color: ${colors.primary};
                -fx-text-fill: white;
                -fx-background-radius: ${theme.tokens.radius}px;
                -fx-padding: 10 16 10 16;
            }

            .button.ghost-button {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-text-fill: ${colors.textPrimary};
                -fx-border-color: ${colors.border};
            }

            .button.nav-button {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-text-fill: ${colors.textPrimary};
                -fx-border-color: transparent;
                -fx-alignment: center-left;
                -fx-max-width: Infinity;
            }

            .button.nav-button-active {
                -fx-background-color: ${colors.primary};
                -fx-text-fill: white;
            }

            .text-area {
                -fx-control-inner-background: ${colors.surfaceMuted};
                -fx-background-color: ${colors.surfaceMuted};
                -fx-text-fill: ${colors.textPrimary};
                -fx-highlight-fill: ${colors.primary};
                -fx-highlight-text-fill: white;
                -fx-border-color: ${colors.border};
                -fx-background-radius: ${theme.tokens.radius}px;
                -fx-border-radius: ${theme.tokens.radius}px;
                -fx-padding: 8px;
            }

            .text-field,
            .password-field,
            .combo-box,
            .choice-box,
            .date-picker,
            .color-picker,
            .spinner,
            .list-view,
            .table-view,
            .tree-view {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: ${colors.border};
                -fx-text-fill: ${colors.textPrimary};
            }

            .text-field.invalid,
            .text-area.invalid,
            .password-field.invalid,
            .combo-box.invalid,
            .choice-box.invalid,
            .date-picker.invalid,
            .spinner.invalid {
                -fx-border-color: ${colors.primary};
            }

            .hyperlink {
                -fx-text-fill: ${colors.primary};
                -fx-border-color: transparent;
                -fx-padding: 0;
            }

            .text-flow {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: ${colors.border};
                -fx-background-radius: ${theme.tokens.radius}px;
                -fx-border-radius: ${theme.tokens.radius}px;
                -fx-padding: 10 12 10 12;
            }

            .status-strip {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: ${colors.border} transparent transparent transparent;
                -fx-padding: 12 16 12 16;
            }

            .feedback-state {
                -fx-background-color: ${colors.surfaceMuted};
                -fx-border-color: ${colors.border};
                -fx-background-radius: ${theme.tokens.radius}px;
                -fx-border-radius: ${theme.tokens.radius}px;
            }

            .feedback-title {
                -fx-alignment: center;
                -fx-text-alignment: center;
            }

            .feedback-message {
                -fx-alignment: center;
                -fx-text-alignment: center;
            }

            .loading-state-indicator {
                -fx-progress-color: ${colors.primary};
            }

            .error-state {
                -fx-border-color: ${colors.primary};
            }

            .form-label {
                -fx-font-weight: 600;
            }

            .form-helper {
                -fx-text-fill: ${colors.textSecondary};
            }

            .validation-message {
                -fx-text-fill: ${colors.primary};
            }

            .muted {
                -fx-text-fill: ${colors.textSecondary};
            }
        """.trimIndent()
    }
}
