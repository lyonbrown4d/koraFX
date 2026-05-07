# KoraFX

KoraFX is a lightweight Kotlin helper library for JavaFX.

The project is intentionally scoped to:

- Kotlin-friendly JavaFX DSL.
- Lightweight MVVM helpers based on `StateFlow` and `SharedFlow`.
- Optional components such as navigation and theme support.

KoraFX is not a full application framework. It does not bind to a DI container, hide native JavaFX APIs, or prescribe how an application must be assembled.

## Modules

```text
korafx-bom             Maven BOM for aligning KoraFX module versions
framework-dsl          JavaFX DSL, bindings, render state, forms, dialogs
framework-state        Small state and event primitives
framework-mvvm         ViewModel, Action/Event model, test helpers, factories
framework-navigation   Route, Navigator, page instance policy
framework-theme        Theme tokens, theme manager, stylesheet generation
framework-components   App shell, navigation, route host, toast, feedback, surface components
examples/*             Small runnable examples
sample-workbench-app   Runnable sample app
```

Published Maven artifacts use the same capability split with a project prefix:
`korafx-bom`, `korafx-dsl`, `korafx-state`, `korafx-mvvm`, `korafx-navigation`, `korafx-theme`, and `korafx-components`.

```kotlin
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-dsl")
implementation("io.github.daiyuang:korafx-mvvm")
```

## Build

On this repository, use the project-local Gradle user home to avoid writing wrapper files into `D:\.gradle`:

```powershell
$env:GRADLE_USER_HOME="D:\Projects\KoraFX\.gradle-user-home"
.\gradlew.bat build --no-configuration-cache --console=plain
```

## Run The Sample

```powershell
$env:GRADLE_USER_HOME="D:\Projects\KoraFX\.gradle-user-home"
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

1. Make `framework-dsl` pleasant and stable.
2. Stabilize `framework-mvvm`.
3. Extract common components on top of DSL + MVVM.

See [DESIGN.md](DESIGN.md) for the project boundary and non-goals.
