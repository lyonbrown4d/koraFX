# KoraFX Workbench Sample

This module is the full demo application for KoraFX. It is intentionally structured like a real desktop tool instead of a single-file showcase.

The sample depends on the direct framework path:

```kotlin
implementation(project(":korafx-framework"))
implementation(project(":korafx-components"))
```

## Package Layout

- `dev.korafx.sample.SampleWorkbenchApp`: JavaFX lifecycle only. It creates the DI graph, binds the scene theme, and closes resources.
- `dev.korafx.sample.di`: Koin composition root. `WorkbenchAppGraph` owns application-scoped dependencies.
- `dev.korafx.sample.data`: In-memory demo data sources used by the UI.
- `dev.korafx.sample.domain`: Plain domain models for tables, explorer resources, and activity events.
- `dev.korafx.sample.navigation`: Route definitions consumed by `Navigator`.
- `dev.korafx.sample.viewmodel`: StateFlow-backed MVVM state, actions, and events.
- `dev.korafx.sample.ui`: JavaFX UI built with KoraFX DSL and component APIs.

## DI Approach

The sample uses Koin in the application layer:

- `ThemeManager`, `Navigator`, `WorkbenchViewModel`, `SceneThemeController`, demo catalog, and command palette are registered in a Koin module.
- `korafx-framework` provides Koin as the default application composition path.
- `WorkbenchAppGraph` acts as the boundary between JavaFX lifecycle and injected application services.

## What It Demonstrates

- Kotlin-first JavaFX DSL: layout, control, form, menu, table, tree, and binding helpers.
- MVVM: `ViewModel`, `ViewState`, `UiAction`, `UiEvent`, and StateFlow rendering.
- Navigation: `Navigator`, `Route`, and route-driven page visibility.
- Theme: token-driven built-in presets and `SceneThemeController`.
- Components: workbench layout, border layout, resource explorer, tab workspace, source/query editors, data grid, inspector panel, activity timeline, command palette, badges, chips, metrics, alerts, and feedback states.
