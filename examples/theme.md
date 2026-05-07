# Theme Examples

## Manual Wiring

```kotlin
val themeManager = ThemeManager()
val sceneThemeController = SceneThemeController(themeManager)

val scene = Scene(root, 960.0, 640.0)
sceneThemeController.bind(scene)

themeManager.toggle()
```

## Built-In Presets

```kotlin
BuiltInThemes.all.forEach { theme ->
    println("${theme.id}: ${theme.displayName}")
}

themeManager.setTheme("nord")
themeManager.nextTheme()
themeManager.previousTheme()
```

Use `ThemeManager(availableThemes = listOf(...))` when an application should expose only part of the built-in theme catalog:

```kotlin
val themeManager = ThemeManager(
    initialTheme = BuiltInThemes.Light,
    availableThemes = listOf(
        BuiltInThemes.Light,
        BuiltInThemes.Dark,
        BuiltInThemes.Nord,
    ),
)
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
