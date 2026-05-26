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
