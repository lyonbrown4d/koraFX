# KoraFX Workbench Sample

This module is the full demo application for KoraFX. It is intentionally structured like a real desktop tool instead of a single-file showcase.

The sample depends on the direct framework path:

```kotlin
implementation(project(":korafx-framework"))
implementation(project(":korafx-components"))
implementation(project(":korafx-devtools"))
implementation(libs.ikonli.bootstrapicons.pack)
```

## Package Layout

- `dev.korafx.sample.MainKt`: `koraApplication` entry. It configures window, Koin modules, theme, navigation, content, and lifecycle cleanup.
- `dev.korafx.sample.di`: Koin composition module plus `WorkbenchAppGraph`, a small typed access layer over framework services.
- `dev.korafx.sample.data`: In-memory demo data sources used by the UI.
- `dev.korafx.sample.domain`: Plain domain models for tables, explorer resources, and activity events.
- `dev.korafx.sample.navigation`: `PathRoute` definitions consumed by `Navigator`.
- `dev.korafx.sample.viewmodel`: StateFlow-backed MVVM state, actions, and events.
- `dev.korafx.sample.ui`: JavaFX UI built with KoraFX DSL and component APIs.

## DI Approach

The sample uses `korafx-framework` as the application layer:

- `koraApplication` creates the JavaFX application, scene, theme binding, Koin context, and framework services.
- `installKoin` registers demo catalog, `WorkbenchViewModel`, and command palette host.
- `devtools` enables the optional runtime inspector subapp. Press `Ctrl+Shift+I` to open the resizable dock or `Ctrl+Shift+C` to pick a node from the app window. Use `LEFT`, `RIGHT`, `BOTTOM`, or `WINDOW`, or switch placement from the DevTools header. Picking stays in-process and targets the application scene in every placement.
- `KoraApplication.uiScope` drives JavaFX bindings and is cancelled by the framework on shutdown.
- Commands remain a component/application concern; they are not registered through a framework-level command API.

## What It Demonstrates

- Kotlin-first JavaFX DSL: layout, control, form, menu, table, tree, and binding helpers.
- MVVM: `ViewModel`, `ViewState`, `UiAction`, `UiEvent`, and StateFlow rendering.
- Navigation: `Navigator`, `PathRoute`, persisted location, active route controls, path/query/hash navigation, history, and explicit scroll/selection restoration.
- Theme: token-driven built-in presets and `SceneThemeController`.
- Components: workbench layout, border layout, breadcrumbs, page headers, status bars, resource explorer, tab workspace, source/query editors, data grid, inspector panel, activity timeline, command palette, Ikonli-ready controls, badges, chips, metrics, alerts, and feedback states.
- Icons: the app chooses `ikonli-bootstrapicons-pack`; the reusable KoraFX component module only depends on Ikonli JavaFX core.
- DevTools: localized subapp shell, Ikonli icons, scene graph, node picker/highlighter, node inspector, navigation state, and theme token panels.
