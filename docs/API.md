# KoraFX API Overview

KoraFX is now oriented as a Kotlin-first JavaFX application framework. The default path should be direct:

```kotlin
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-framework")
implementation("io.github.daiyuang:korafx-components")
```

Runtime code is intentionally split into three publishable modules: `korafx-dsl`, `korafx-framework`, and `korafx-components`.

## korafx-framework

`korafx-framework` is the default application layer. It exports:

- DSL builders from `korafx-dsl`
- State primitives from `dev.korafx.dsl.state`
- MVVM primitives from `dev.korafx.framework.mvvm`
- Navigation from `dev.korafx.framework.navigation`
- Theme services from `dev.korafx.framework.theme`
- Koin core as the default DI runtime
- Coroutines JavaFX integration

Main API:

- `KoraWindowConfig`
- `KoraAppServices<R>`
- `koraFrameworkModule(initialRoute, routes)`
- `ViewModel<S, A, E>`
- `ViewState`, `UiAction`, `UiEvent`
- `Route`, `Navigator<R>`, `NavigationState<R>`
- `ThemeManager`, `SceneThemeController`, `BuiltInThemes`

Guidelines:

- Prefer constructor injection and register application services in Koin.
- Keep JavaFX lifecycle in the `Application` class and put app services in Koin modules.
- Use `Navigator` as the single source of route state.
- Use `ThemeManager` as the single source of theme state.
- Keep screen state in ViewModels and render it through `stateText`, `stateList`, `stateVisible`, and related DSL bindings.

## korafx-dsl

`korafx-dsl` remains available for users who only want Kotlin builders around JavaFX and do not want the framework stack.

Main API:

- Layout factories: `borderPane`, `vbox`, `hbox`, `stackPane`, `gridPane`, `flowPane`, `tilePane`, `anchorPane`, `scrollPane`, `splitPane`, `tabPane`
- Control factories: `label`, `button`, `checkBox`, `textField`, `textArea`, `listView`, `comboBox`, `choiceBox`, `slider`, `datePicker`, `colorPicker`, `radioButton`, `toggleButton`
- Advanced controls: `treeView`, `tableView`, `accordion`, `titledPane`, `spinner`, `progressBar`, `pagination`
- Binding helpers: `bindText`, `bindVisible`, `bindDisable`, `bindItems`, `bindSelectedItem`, `bindValue`, `bindProgress`, `bindInvalid`, `bindValidation`
- State helpers: `stateText`, `stateVisible`, `stateDisable`, `stateStyleClass`, `stateValidation`, `stateList`, `stateful`
- Render helpers: `fragment`, `renderIf`, `renderUnless`, `renderEach`, `bindContent`, `bindChildren`, `bindEach`, `bindList`, `bindRenderState`
- Styling helpers: `styleClass`, `styleClasses`, `toggleStyleClass`, `pseudoClass`, `cssStyle`, `cssStyleOf`, `cssAppend`, `styleRaw`
- Form/dialog helpers: `form`, `submitBar`, `validationMessage`, `alert`, `confirmation`, `textInputDialog`, `customDialog`

Guidelines:

- Keep factory names close to JavaFX class names.
- Expose `init: Node.() -> Unit` so callers can still use native JavaFX APIs.
- Bind state at the component property layer; avoid reflection-driven UI binding.
- Use `stateful(scope, state) { ... }` only for a local state-aware subtree.

## korafx-components

`korafx-components` is the optional component layer for real desktop tools and workbench-style applications.

Main API:

- Shell: `appShell`
- Overlays: `ModalHost`, `modalHost`, `ModalAction`
- Layout: `borderLayout`, `workspaceLayout`, `WorkspaceLayout`
- Resource browsing: `resourceExplorer`, `ResourceExplorer`, `ResourceExplorerBuilder`
- Data grids: `dataGrid`, `DataGrid`, `DataGridBuilder`, `editableTable`, `EditableTableBuilder`
- Details: `inspectorPanel`, `InspectorPanel`, `InspectorPanelBuilder`
- Editor surfaces: `codeEditor`, `sourceEditor`, `queryEditor`, `SourceEditor`, `SourceDiagnostic`
- Workspaces: `tabWorkspace`, `TabWorkspace`, `TabWorkspaceBuilder`
- Activity: `activityTimeline`, `ActivityTimeline`, `ActivityTimelineBuilder`
- Commands: `CommandPaletteHost`, `CommandPaletteCommand`, `commandPalette`, `CommandPalette`
- Navigation: `navigationRail`, `routeHost`, `routeStateHost`
- Feedback: `feedbackState`, `emptyState`, `loadingState`, `errorState`, `ToastHost`, `toastHost`, `snackbar`
- Surfaces: `card`, `section`, `actionBar`
- Semantic display: `badge`, `chip`, `metricCard`, `alertBanner`, `ComponentTone`

Guidelines:

- Components are still JavaFX nodes and should remain composable.
- Components may accept explicit framework services such as `CoroutineScope`, `Navigator`, `ThemeManager`, and command hosts.
- Components should have stable style classes so `korafx-framework` theme services can fully cover them.

## Naming Checklist

When adding API:

- Use `bindX` for continuous Flow binding.
- Use `bindXBidirectional` only when control changes write back to `MutableStateFlow`.
- Use `stateX` when a normal DSL node derives one property from screen state.
- Use `XHost` for components that render hosted content.
- Use `XState` for small sealed/data state types.
- Prefer framework-owned names only in `korafx-framework`; keep low-level DSL names close to JavaFX.
