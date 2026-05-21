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

themeManager.setTheme("material-light")
themeManager.setTheme("material-dark")
themeManager.setTheme("fluent-light")
themeManager.setTheme("fluent-dark")
themeManager.toggle()
themeManager.nextTheme()
themeManager.previousTheme()
```

Expose presets in UI with the control DSL:

```kotlin
comboBox<KoraTheme>(
    items = themeManager.availableThemes,
    init = {
        prefWidth = 180.0
    },
) {
    render { it.displayName }
    onSelect { theme ->
        if (theme != null) {
            themeManager.setTheme(theme)
        }
    }
}.bindSelectedItem(scope, themeManager.theme)
```

Use `ThemeManager(availableThemes = listOf(...))` when an application should expose only part of the built-in theme catalog:

```kotlin
val themeManager = ThemeManager(
    initialTheme = BuiltInThemes.MaterialLight,
    availableThemes = listOf(
        BuiltInThemes.MaterialLight,
        BuiltInThemes.MaterialDark,
        BuiltInThemes.FluentLight,
        BuiltInThemes.FluentDark,
    ),
)
```

## Custom Tokens

```kotlin
val brandColors = ColorTokens(
    primary = "#0F766E",
    surface = "#F3FAF8",
    surfaceMuted = "#FFFFFF",
    textPrimary = "#10201D",
    textSecondary = "#58706A",
    border = "#C9E2DC",
    success = "#0F9F6E",
    warning = "#B7791F",
    danger = "#C2410C",
    info = "#2563EB",
)

val brandTheme = KoraTheme(
    id = "brand",
    displayName = "Brand",
    tokens = ThemeTokens(
        colors = brandColors,
        typography = TypographyTokens(
            fontFamily = "\"Segoe UI\", \"Microsoft YaHei UI\", sans-serif",
            baseSize = 14,
            headlineSize = 28,
        ),
        radius = 12,
        spacing = SpacingTokens.compact(),
        radii = RadiusTokens.fromBase(12),
        states = StateColorTokens.from(brandColors),
    ),
)

themeManager.setTheme(brandTheme)
```

You can omit `spacing`, `radii`, `states`, and `elevation`; defaults are derived from `radius` and `colors`. Override them when an application needs compact density, custom hover/pressed colors, or different shadow depth.

### Components covered by built-in theme stylesheets

The built-in themes currently style the following DSL and component class groups:
- core surfaces: `panel`, `card`, `form`, `status-bar`, `app-toolbar`, `kora-window-titlebar`, `app-shell`, `modal-card`, etc.
- controls and groups: `nav-rail`, `action-bar`, `toast`, `feedback`, `submit-bar`, `route-state-host`
- component labels and helper texts: `headline`, `muted`, `section-title`, `form-label`, `validation-message`

When adding custom controls, prefer these same style-class hooks to keep visual consistency across all themes.
