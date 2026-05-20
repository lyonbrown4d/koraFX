# KoraFX Design

KoraFX is now a Kotlin-first JavaFX framework, not only a thin utility collection.

The framework stays direct and small by exposing four primary runtime modules plus one optional development module:

```text
korafx-dsl
korafx-navigation
korafx-framework
korafx-components
korafx-devtools
```

`korafx-bom`, examples, and the workbench sample support publishing and adoption, but they are not part of the runtime split.

## Module Boundaries

### korafx-dsl

Lowest-level user-facing module.

Responsibilities:

- JavaFX layout and control builders.
- Event, spacing, grow, alignment, style-class, and CSS helpers.
- Flow-to-node binding helpers.
- Lightweight state primitives under `dev.korafx.dsl.state`.
- Stateful rendering helpers such as `stateful`, `bindList`, and `bindRenderState`.
- Form and dialog thin wrappers.

Constraint: keep JavaFX native nodes visible and usable. The DSL should reduce repetitive setup, not replace JavaFX concepts.

### korafx-framework

Default application framework module.

Responsibilities:

- MVVM primitives under `dev.korafx.framework.mvvm`.
- Navigation primitives under `dev.korafx.navigation`.
- Theme services under `dev.korafx.framework.theme`.
- Koin-backed application composition through `koraApplication { installKoin { ... } }`.
- JavaFX coroutine integration.

Koin is the default framework-level composition model. ViewModels should still use constructor injection so they remain easy to test and can be instantiated manually when needed.
Application-level commands are not part of this layer yet; command palette data remains a component/application concern.
Optional features should integrate through `KoraApplicationPlugin` so the framework does not depend on add-on modules.

### korafx-components

Optional component/workbench module.

Responsibilities:

- Reusable JavaFX components for desktop tools.
- Workbench layouts, navigation surfaces, editors, data grids, inspector panels, timelines, command palette, feedback states, and semantic display controls.
- Icon-ready controls backed by Ikonli JavaFX core.
- Stable style classes that can be fully covered by framework themes.

Components remain normal JavaFX nodes. They may accept explicit framework services such as `Navigator`, `ThemeManager`, `CoroutineScope`, callbacks, and state flows.
Concrete Ikonli icon packs are application choices and should not be bundled into `korafx-components`.

### korafx-devtools

Optional development-only module.

Responsibilities:

- Register a `devtools { ... }` extension for `koraApplication`.
- Provide runtime inspection panels for scene graph, node properties, navigation state, and theme tokens.
- Provide Chrome DevTools-like node picking and visual highlighting for JavaFX nodes.
- Open as a resizable bottom dock by default, with left, right, bottom, and independent-window placements.
- Keep inspection in-process: picker events, hit-testing, and highlight overlays target the application scene, not the DevTools scene.
- Reuse KoraFX framework/components as a subapp: internal routes, theme binding, iconography, and localized strings.
- Stay out of production dependencies unless the application explicitly opts in.

Constraint: devtools can depend on `korafx-framework`, but `korafx-framework` must not depend on devtools.

## Package Boundaries

```text
dev.korafx.dsl.*
dev.korafx.framework.*
dev.korafx.components.*
dev.korafx.devtools.*
```

Avoid adding new top-level runtime packages unless a feature clearly cannot fit one of these three areas.

## API Naming Guidelines

- `bindX` means continuous Flow binding from state to JavaFX nodes.
- `bindXBidirectional` means the control writes back to `MutableStateFlow`.
- `stateX` means a DSL node derives one property from screen state.
- `XHost` means a component hosts or switches child content.
- `XState` means a small state model for rendering.
- Keep low-level DSL names close to JavaFX class names.
- Keep framework-owned names in `dev.korafx.framework.*`.

## Iteration Order

1. Stabilize the DSL and state binding surface.
2. Stabilize the framework composition model around Koin, MVVM, navigation, and theme.
3. Expand components for realistic Git/database/workbench applications.
4. Keep sample-workbench-app as the complete reference application.
