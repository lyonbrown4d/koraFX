# Theme Examples

## Manual Wiring

```kotlin
val themeManager = ThemeManager()
val sceneThemeController = SceneThemeController(themeManager)

val scene = Scene(root, 960.0, 640.0)
sceneThemeController.bind(scene)

themeManager.toggle()
```

## Custom Tokens

```kotlin
val brandTheme = KoraTheme(
    id = "brand",
    displayName = "Brand",
    tokens = ThemeTokens(
        colors = ColorTokens(
            primary = "#0F766E",
            surface = "#F3FAF8",
            surfaceMuted = "#FFFFFF",
            textPrimary = "#10201D",
            textSecondary = "#58706A",
            border = "#C9E2DC",
        ),
        typography = TypographyTokens(
            fontFamily = "\"Segoe UI\", \"Microsoft YaHei UI\", sans-serif",
            baseSize = 14,
            headlineSize = 28,
        ),
        radius = 12,
    ),
)

themeManager.setTheme(brandTheme)
```
