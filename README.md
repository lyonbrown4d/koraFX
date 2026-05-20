# KoraFX

KoraFX is a Kotlin-first JavaFX application framework.

The default stack is:

- Kotlin-friendly JavaFX DSL.
- Koin-backed application composition.
- MVVM helpers based on `StateFlow` and `SharedFlow`.
- Route navigation for desktop screens.
- Token-driven theme presets.
- Optional workbench components for real desktop tools.
- Ikonli-ready component APIs without forcing a concrete icon pack.

## Modules

```text
korafx-bom            Maven BOM for aligning KoraFX module versions
korafx-dsl            Low-level JavaFX DSL and Flow state binding primitives
korafx-navigation     Route primitives and navigator API (`Route`, `Navigator`, `NavigationState`)
korafx-framework      Default framework stack: MVVM, navigation, theme, Koin
korafx-components     Optional workbench components and higher-level JavaFX surfaces
korafx-devtools       Optional runtime inspector for KoraFX applications
examples/*            Small runnable examples
sample-workbench-app  Runnable sample app
```

Application code should start from the direct dependency path:

```kotlin
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-framework")
implementation("io.github.daiyuang:korafx-components")
implementation("io.github.daiyuang:korafx-devtools") // keep this in development builds when possible
// Pick any Ikonli pack in the application when icons are needed.
implementation("org.kordamp.ikonli:ikonli-bootstrapicons-pack:<ikonli-version>")
```

Minimal framework entry:

```kotlin
fun main(args: Array<String>) = koraApplication(args) {
    window {
        title = "KoraFX Workbench"
        width = 1280.0
        height = 820.0
    }

    installKoin {
        modules(appModule)
    }

    theme {
        presets(BuiltInThemes.all)
        default(BuiltInThemes.MaterialLight)
        persistSelection = true
    }

    navigation {
        initialRoute = WorkbenchRoute.Overview
        routes(WorkbenchRoute.all)
    }

    devtools {
        enabled = true
        shortcut = "Ctrl+Shift+I"
        pickerShortcut = "Ctrl+Shift+C"
        language = KoraDevtoolsLanguage.SYSTEM
        placement = KoraDevtoolsPlacement.BOTTOM
        dockWidth = 420.0
        dockHeight = 360.0
    }

    content {
        AppRoot(this).buildRoot()
    }

    lifecycle {
        close<AppViewModel>()
    }
}
```

## Build

Use the default Gradle user home:

```powershell
.\gradlew.bat build --no-configuration-cache --console=plain
```

## Run The Sample

```powershell
.\gradlew.bat :sample-workbench-app:run
```

Runnable examples:

```powershell
.\gradlew.bat :examples:dsl-basic-app:run
.\gradlew.bat :examples:mvvm-counter-app:run
.\gradlew.bat :examples:navigation-theme-app:run
```

## Examples

See [examples/README.md](examples/README.md) for the examples index, or jump directly to:

- [DSL Examples](examples/dsl.md)
- [Binding Examples](examples/bindings.md)
- [MVVM Examples](examples/mvvm.md)
- [Navigation And Components](examples/navigation-components.md)
- [Theme Examples](examples/theme.md)
- [Sample Apps](examples/sample.md)

## API Overview

See [docs/API.md](docs/API.md) for the current module capability map and public API naming conventions.

## Release

See [docs/PUBLISHING.md](docs/PUBLISHING.md) for Maven Central publishing setup and required credentials.

## Design Notes

The current iteration order is:

1. Make `korafx-framework` the default application entry.
2. Keep `korafx-dsl` available for low-level builder-only usage.
3. Keep `korafx-components` as the optional workbench/component layer.

See [DESIGN.md](DESIGN.md) for the project boundary and non-goals.
See [docs/ROADMAP.md](docs/ROADMAP.md) for the component iteration roadmap.
