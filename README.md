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
korafx-navigation     Route primitives, navigator API, and route-aware UI
korafx-framework      Default framework stack: MVVM, navigation, theme, Koin
korafx-command-palette Optional advanced command palette and command host surfaces
korafx-components     Optional base workbench components and shared JavaFX surfaces
korafx-data-grid      Optional advanced data grid and editable table surfaces
korafx-inspector-panel Optional advanced inspector/property panel surfaces
korafx-graph-editor   Lightweight JavaFX graph editor and directed edge model
korafx-virtual-list   Virtualized async list and selection infrastructure
korafx-resource-explorer Optional advanced resource tree explorer surfaces
korafx-source-editor  Optional advanced source/code/query editor surfaces
korafx-workspace      Optional advanced workspace layout and tabbed workbench surfaces
korafx-test           Optional TestFX-backed JavaFX testing utilities
korafx-devtools       Optional runtime inspector for KoraFX applications
examples/*            Small runnable examples
sample-workbench-app  Runnable sample app
```

Application code should start from the direct dependency path:

```kotlin
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-framework")
implementation("io.github.daiyuang:korafx-navigation") // optional when using navigation without framework
implementation("io.github.daiyuang:korafx-command-palette") // only when command palette surfaces are needed
implementation("io.github.daiyuang:korafx-components")
implementation("io.github.daiyuang:korafx-data-grid") // only when table/grid surfaces are needed
implementation("io.github.daiyuang:korafx-inspector-panel") // only when detail/inspector surfaces are needed
implementation("io.github.daiyuang:korafx-resource-explorer") // only when tree/resource explorer surfaces are needed
implementation("io.github.daiyuang:korafx-source-editor") // only when editor surfaces are needed
implementation("io.github.daiyuang:korafx-workspace") // only when workspace/tab surfaces are needed
implementation("io.github.daiyuang:korafx-virtual-list") // optional virtualized list component
implementation("io.github.daiyuang:korafx-graph-editor") // optional graph editor surfaces
testImplementation("io.github.daiyuang:korafx-test") // only for JavaFX UI/component tests
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
- [Navigation Examples](examples/navigation-components.md)
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
3. Keep route-aware UI in `korafx-navigation`.
4. Keep `korafx-components` as the optional base workbench/component layer.
5. Publish large advanced components as independent modules, starting with `korafx-command-palette`, `korafx-data-grid`, `korafx-inspector-panel`, `korafx-resource-explorer`, `korafx-source-editor`, and `korafx-workspace`.

See [DESIGN.md](DESIGN.md) for the project boundary and non-goals.
See [docs/ROADMAP.md](docs/ROADMAP.md) for the component iteration roadmap.
