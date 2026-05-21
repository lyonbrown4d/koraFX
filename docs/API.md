# KoraFX API Overview

KoraFX is now oriented as a Kotlin-first JavaFX application framework. The default path should be direct:

```kotlin
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-framework")
implementation("io.github.daiyuang:korafx-navigation") // optional direct navigation core + UI
implementation("io.github.daiyuang:korafx-command-palette") // optional advanced command surfaces
implementation("io.github.daiyuang:korafx-components")
implementation("io.github.daiyuang:korafx-data-grid") // optional advanced table/grid surfaces
implementation("io.github.daiyuang:korafx-inspector-panel") // optional advanced inspector/detail surfaces
implementation("io.github.daiyuang:korafx-resource-explorer") // optional advanced resource tree surfaces
implementation("io.github.daiyuang:korafx-source-editor") // optional advanced editor surfaces
implementation("io.github.daiyuang:korafx-workspace") // optional advanced workspace/tab surfaces
testImplementation("io.github.daiyuang:korafx-test") // optional TestFX-backed JavaFX testing utilities
```

Runtime code is intentionally split into focused publishable modules: `korafx-dsl`, `korafx-navigation`, `korafx-framework`, `korafx-components`, and optional advanced modules such as `korafx-command-palette`, `korafx-data-grid`, `korafx-inspector-panel`, `korafx-resource-explorer`, `korafx-source-editor`, and `korafx-workspace`.
`korafx-test` is a test-scope module for JavaFX component tests and TestFX integration.
`korafx-navigation` and `korafx-components` expose Ikonli JavaFX core for icon-ready APIs, but applications should choose their own icon pack dependency.
`korafx-devtools` is an optional debug-only module and should not be required by production applications.
`korafx-macos` is an optional platform module for macOS native titlebar integration.

## korafx-framework

`korafx-framework` is the default application layer. It exports:

- DSL builders from `korafx-dsl`
- State primitives from `dev.korafx.dsl.state`
- MVVM primitives from `dev.korafx.framework.mvvm`
- Navigation from `dev.korafx.navigation` (exported by framework for convenience)
- Theme services from `dev.korafx.framework.theme`
- Koin core as the default DI runtime
- Coroutines JavaFX integration

Main API:

- `koraApplication { ... }`
- `KoraApplication`
- `KoraApplicationBuilder`
- `KoraWindowChromeMode`
- `KoraWindowControlSide`
- `window { title / width / height / size(...) / minSize(...) / resizable / titleBar { ... } }`
- `installKoin { modules(...) }`
- `theme { presets(...); default(...); persistSelection = true }`
- `navigation { initialRoute = ...; initialPath = ...; routes(...); persistLocation = true }`
- `content { ... }`
- `lifecycle { close<T>(); cancel<T>(); onStop { ... } }`
- `install(plugin)`
- `KoraApplicationPlugin.modules(app)` for plugin-owned Koin modules
- `KoraApplication.loadModules(...)`, `unloadModules(...)`
- `ViewModel<S, A, E>`
- `ViewState`, `UiAction`, `UiEvent`
- `Route`, `PathRoute`, `RouteMeta`, `NavigationLocation<R>`, `Navigator<R>`, `NavigationState<R>` (from `dev.korafx.navigation`)
- `Navigator.fromPath(...)` for deep-link or persisted path startup
- `Navigator.navigatePath(...)`, `replacePath(...)`, `back()`, `forward()`, `beforeEach(...)`, `beforeEnter(...)`, `beforeLeave(...)`
- `Navigator.navigatePathAsync(...)`, `replacePathAsync(...)`, `backAsync()`, `forwardAsync()`, `beforeEachAsync(...)`, `beforeEnterAsync(...)`, `beforeLeaveAsync(...)`
- `Navigator.saveState(...)`, `restoredState<T>(...)`, `setResult(...)`, `results<T>(...)`, `awaitResult(...)`, `navigationResultKey<T>(...)`
- `NavigationLocation.withQuery(...)`, `withoutQuery(...)`, `withHash(...)`, `withoutHash()`
- `ThemeManager`, `SceneThemeController`, `BuiltInThemes`

Example:

```kotlin
fun main(args: Array<String>) = koraApplication(args) {
    window {
        title = "KoraFX Workbench"
        width = 1280.0
        height = 820.0
        minSize(860.0, 560.0)
        titleBar {
            subtitle = "Framework + Components"
            chromeMode = KoraWindowChromeMode.NATIVE_OVERLAY
            controlSide = KoraWindowControlSide.AUTO
            cornerRadius = 14.0
            transparentBackground = true
            dragOpacity = 0.92
            macos {
                preserveTrafficLights = true
                fullSizeContentView = true
                transparentTitlebar = true
                trafficLightInset(14.0, 12.0)
            }
            content {
                statusItem("Custom titlebar slot", ComponentTone.INFO)
            }
        }
    }

    installKoin {
        modules(appModule, repositoryModule)
    }

    theme {
        presets(BuiltInThemes.all)
        default(BuiltInThemes.MaterialLight)
        persistSelection = true
    }

    navigation {
        initialRoute = WorkbenchRoute.Overview
        initialPath = "/"
        routes(WorkbenchRoute.all)
        persistLocation = true
    }

    devtools {
        enabled = true
    }

    content {
        AppRoot(this).buildRoot()
    }

    lifecycle {
        close<AppViewModel>()
    }
}
```

Guidelines:

- Prefer constructor injection and register application services in Koin.
- Keep application structure in `koraApplication`; only drop to JavaFX `Application` directly for advanced platform integration.
- Use `Navigator` as the single source of route state.
- Use `ThemeManager` as the single source of theme state.
- Use `KoraApplication.uiScope` for JavaFX UI bindings; the framework cancels it during shutdown.
- Use `window { titleBar { ... } }` for cross-platform window chrome; `KoraWindowChromeMode.AUTO` preserves native macOS traffic lights and uses custom chrome on Windows/Linux.
- Use `KoraWindowChromeMode.CUSTOM` to force self-drawn controls on every OS, `NATIVE` to keep the platform titlebar, or `NATIVE_OVERLAY` for optional platform modules such as `korafx-macos`.
- In pure JavaFX, Electron-style macOS traffic-light overlay with scene content inside the native titlebar requires native NSWindow integration. Add `korafx-macos` and `installMacosChrome()` before using `titleBar { macos { ... } }`.
- Use `cornerRadius`, `transparentBackground`, and `dragOpacity` when the application wants rounded transparent chrome or a translucent drag feedback.
- Keep screen state in ViewModels and render it through `stateText`, `stateList`, `stateVisible`, and related DSL bindings.
- Register closeable app services with `lifecycle { close<T>() }` instead of writing raw shutdown handlers when possible.
- Let optional plugins return their own Koin modules from `KoraApplicationPlugin.modules(app)` when they need app-scoped services.
- Keep command palette commands in application/component modules for now; do not put command registration in the framework entry.
- Use `install(plugin)` for optional framework add-ons; core framework should not directly depend on optional modules.

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
- Window/shape helpers: `scene`, `stage`, `popup`, `rectangle`

Guidelines:

- Keep factory names close to JavaFX class names.
- Expose `init: Node.() -> Unit` so callers can still use native JavaFX APIs.
- Bind state at the component property layer; avoid reflection-driven UI binding.
- Use `stateful(scope, state) { ... }` only for a local state-aware subtree.

## korafx-navigation

`korafx-navigation` owns both route state and navigation-specific UI. Use it directly for apps that need routing without the full framework, or through `korafx-framework` for the default application stack.

Package layout:

- `dev.korafx.navigation`: route contracts, navigator, path matching, guards, history, route data hosts, router outlets, and navigation UI.

Main API:

- Core: `Route`, `PathRoute`, `RouteMeta`, `RouteQuery`, `NavigationLocation<R>`, `NavigationState<R>`, `Navigator<R>`
- Navigation actions: `navigate(...)`, `replace(...)`, `navigatePath(...)`, `replacePath(...)`, `back()`, `forward()`
- Guards and data: `beforeEach(...)`, `beforeEnter(...)`, `beforeLeave(...)`, `routeDataHost`, `routeStateHost`, `RouteDataController`
- UI: `navigationRail`, `routeButton`, `pathButton`, `routeLink`, `pathLink`, `routeHost`, `routerHost`, `RouterModule`, `routeLazy`
- Restoration: `routeScrollRestoration`, `routeSelectionRestoration`, `routeFocusRestoration`

Example:

```kotlin
navigationRail(
    scope = uiScope,
    navigator = navigator,
    icon = { route -> routeIcons[route.id] },
)
```

Guidelines:

- Keep all route-aware UI in `korafx-navigation`, not `korafx-components`.
- Use `Navigator` as the single source of route, path, history, route meta, and route-local restored state.
- Keep application-level DI and lifecycle wiring in `korafx-framework`; navigation should stay reusable outside the framework entry.

## korafx-components

`korafx-components` is the optional base component layer for real desktop tools and workbench-style applications.

Package layout:

- `dev.korafx.components`: small and medium reusable workbench components.

Main API:

- Shell: `appShell`, `appToolbar`, `toolbarGroup`
- Overlays: `ModalHost`, `modalHost`, `ModalAction`
- Layout: `borderLayout`
- Activity: `activityTimeline`, `ActivityTimeline`, `ActivityTimelineBuilder`
- Feedback: `feedbackState`, `emptyState`, `loadingState`, `errorState`, `ToastHost`, `toastHost`, `snackbar`
- Surfaces: `card`, `section`, `actionBar`, `breadcrumb`, `pageHeader`, `heroBanner`, `statusBar`, `statusItem`
- Icons: `koraIcon`, `iconButton`, `setKoraIcon`, `clearKoraIcon`
- Semantic display: `badge`, `chip`, `metricCard`, `alertBanner`, `ComponentTone`

Icon usage:

```kotlin
dependencies {
    implementation("org.kordamp.ikonli:ikonli-bootstrapicons-pack:<ikonli-version>")
}

chip(
    text = "Connected",
    tone = ComponentTone.SUCCESS,
    icon = BootstrapIcons.CHECK_CIRCLE,
)
```

Guidelines:

- Components are still JavaFX nodes and should remain composable.
- Keep large advanced components in independent publishable modules instead of growing `korafx-components`.
- Components may accept explicit framework services such as `CoroutineScope`, `ThemeManager`, and command hosts.
- Components should have stable style classes so `korafx-framework` theme services can fully cover them.
- Keep concrete Ikonli icon packs in application/sample modules; `korafx-components` should only require Ikonli JavaFX core.

## korafx-command-palette

`korafx-command-palette` is an advanced component module for keyboard-first command surfaces. It is published independently so applications can opt into command host and palette UI APIs only when needed.

Main API:

- `dev.korafx.commandpalette.CommandPaletteHost`
- `dev.korafx.commandpalette.CommandPaletteCommand`
- `dev.korafx.commandpalette.commandPalette`
- `CommandPalette`
- `CommandPaletteBuilder`

Guidelines:

- Use this module for global command menus, navigation launchers, quick actions, and keyboard-driven desktop tools.
- Keep command discovery, shortcut metadata, scoped commands, and DI/module registration work here instead of adding command APIs to `korafx-components` or `korafx-framework`.

## korafx-inspector-panel

`korafx-inspector-panel` is an advanced component module for detail/property panels. It is published independently so applications can opt into inspector UI APIs only when needed.

Main API:

- `dev.korafx.inspector.inspectorPanel`
- `InspectorPanel`
- `InspectorPanelBuilder`
- `InspectorSectionBuilder`
- `InspectorMetadataBuilder`
- `InspectorActionsBuilder`

Guidelines:

- Use this module for selected-resource details, property sheets, metadata panels, object inspectors, and side-detail panes.
- Keep inspector-specific editing, validation, grouping, schema-driven rendering, and property adapters here instead of adding detail-panel APIs to `korafx-components`.

## korafx-resource-explorer

`korafx-resource-explorer` is an advanced component module for tree-heavy desktop tools. It is published independently so applications can opt into repository/schema/resource browsing APIs only when needed.

Main API:

- `dev.korafx.resourceexplorer.resourceExplorer`
- `ResourceExplorer`
- `ResourceExplorerBuilder`
- `ResourceContextMenuBuilder`
- `ResourceExplorer.breadcrumb(...)`, `hideBreadcrumb()`
- `ResourceExplorer.selectedItem()`, `selectedPath()`, `selectedPathText(...)`, `selectPath(...)`
- `ResourceExplorer.expandSelected()`, `collapseSelected()`, `collapseAll()`
- `ResourceExplorerBuilder.secondaryText { ... }`, `status { ... }`, `emptyState(...)`

Guidelines:

- Use this module for Git trees, file/project navigators, database schema browsers, and nested resource pickers.
- Keep explorer-specific search, lazy loading, context menus, selection, drag/drop, and virtualization work here instead of adding tree APIs to `korafx-components`.

## korafx-data-grid

`korafx-data-grid` is an advanced component module for table-heavy desktop tools. It is published independently so applications can opt into grid/editing APIs only when needed.

Main API:

- `dev.korafx.datagrid.dataGrid`
- `dev.korafx.datagrid.editableTable`
- `DataGrid`
- `DataGridSelectionSummary`
- `DataGridBuilder`
- `DataGridBuilder.selectionSummary(...)`
- `DataGridBuilder.toolbarBatchAction(...)`
- `DataGridBuilder.columnVisibility(...)`, `toolbarSnapshotAction(...)`, `dataSnapshot(...)`, `copyText(...)`
- `DataGridToolbarBuilder.batchAction(...)`
- `DataGridToolbarBuilder.columnVisibility(...)`, `snapshotAction(...)`
- `DataGrid.selectedItems()`
- `DataGridDataSnapshot`
- `EditableTableBuilder`

Guidelines:

- Use this module for query results, admin tables, Git status lists, and editable tabular forms.
- Keep grid-specific filtering, sorting, validation, selection, and virtualization work here instead of adding table APIs to `korafx-components`.

## korafx-source-editor

`korafx-source-editor` is an advanced component module. It is published independently so applications can opt into editor surfaces without pulling them into the base component artifact.

Main API:

- `dev.korafx.sourceeditor.codeEditor`
- `dev.korafx.sourceeditor.sourceEditor`
- `dev.korafx.sourceeditor.queryEditor`
- `CodeEditor`
- `CodeEditorPosition`
- `SourceEditor`
- `SourceDiagnostic`
- `CodeEditor.goTo(line, column)`, `selectLine(line)`, `find(...)`, `findNext(...)`, `findPrevious(...)`
- `CodeEditor.showSearchBar(...)`, `showReplaceBar(...)`, `replaceNext(...)`, `replaceAll(...)`, `hideSearchBar()`
- `CodeEditor.selectionLength`, `selectedLineCount`, `setLineNumbersVisible(...)`, `setWrapText(...)`
- `SourceEditor.jumpToDiagnostic(...)`, `showReplace(...)`, `replaceNext(...)`, `replaceAll(...)`, `onDiagnosticSelected { ... }`

Example:

```kotlin
sourceEditor(
    title = "Main.kt",
    text = sourceText,
    language = "kotlin",
    showSearch = true,
) {
    diagnostic(3, 12, "Unused value", ComponentTone.WARNING)
    showReplace("value", "result")
    onDiagnosticSelected { diagnostic ->
        println("Jumped to ${diagnostic.line}:${diagnostic.column}")
    }
}
```

Guidelines:

- Use this module for source previews, simple text/code editing, SQL query panels, diagnostics, find/navigation, and editor result slots.
- Keep heavyweight editor evolution here instead of adding editor-specific APIs to `korafx-components`.

## korafx-workspace

`korafx-workspace` is an advanced component module for workbench-style applications. It is published independently so applications can opt into workspace shells and tabbed document surfaces only when needed.

Main API:

- `dev.korafx.workspace.workspaceLayout`
- `dev.korafx.workspace.tabWorkspace`
- `WorkspaceLayout`
- `WorkspaceLayoutBuilder`
- `TabWorkspace`
- `TabWorkspaceBuilder`

Guidelines:

- Use this module for IDE-like layouts, Git/database workbenches, multi-document tools, and tabbed editors.
- Keep workspace-specific docking, split panes, tab persistence, and document lifecycle work here instead of adding workbench APIs to `korafx-components`.

## korafx-test

`korafx-test` is a test-scope helper module for JavaFX component tests. It wraps the repeated JavaFX toolkit startup helpers and exposes TestFX JUnit 5 dependencies for robot-based UI tests.

Main API:

- `dev.korafx.test.FxTestSupport.start()`
- `FxTestSupport.runOnFxThread { ... }`
- `FxTestSupport.callOnFxThread { ... }`
- `FxTestSupport.waitForFxCondition { ... }`
- `FxTestSupport.showStage(root, width, height, title)`
- `FxRobot.runOnFxThread { ... }`
- `FxRobot.waitForFxCondition { ... }`
- `Stage.setTestScene(root, width, height)`

Example:

```kotlin
class SourceEditorTest {
    @Test
    fun `renders editor`() {
        FxTestSupport.runOnFxThread {
            val editor = sourceEditor(title = "Query.sql")
            assertEquals("Query.sql", editor.editor.titleLabel.text)
        }
    }
}
```

Guidelines:

- Depend on `korafx-test` with `testImplementation`, never from production code.
- Prefer normal DSL factories in component tests and mount only when Stage/Scene behavior matters.
- Use TestFX `ApplicationExtension` and `FxRobot` for interaction tests that need real click, key, or focus behavior.

## korafx-devtools

`korafx-devtools` is the optional runtime inspector for debugging KoraFX applications. Add it only to development builds when possible.

Main API:

- `devtools { ... }`
- `KoraDevtoolsBuilder`
- `KoraDevtoolsLanguage`
- `KoraDevtoolsPlacement`
- `KoraDevtoolsPanel`

Example:

```kotlin
dependencies {
    implementation("io.github.daiyuang:korafx-devtools")
}

fun main(args: Array<String>) = koraApplication(args) {
    devtools {
        enabled = System.getProperty("korafx.devtools") == "true"
        shortcut = "Ctrl+Shift+I"
        pickerShortcut = "Ctrl+Shift+C"
        highlightSelection = true
        language = KoraDevtoolsLanguage.SYSTEM
        placement = KoraDevtoolsPlacement.BOTTOM
        dockWidth = 420.0
        dockHeight = 360.0
        panels {
            sceneGraph()
            inspector()
            navigation()
            theme()
        }
    }
}
```

Initial panels:

- Scene Graph: inspect the live JavaFX node tree.
- Inspector: view selected node properties, bounds, pseudo classes, and CSS metadata.
- Navigation: view registered routes and jump to a route.
- Navigation also shows the current full path, params/query/hash, back/forward stacks, and can navigate by route path.
- Theme: view theme tokens and switch the active theme at runtime.
- Pick Node: press `Ctrl+Shift+C` or the `Pick Node` button, then click an application node to select and highlight it.
- DevTools opens as a resizable bottom dock by default. Use `LEFT`, `RIGHT`, `BOTTOM`, or `WINDOW`, or switch placement from the DevTools header at runtime.
- Node picking is in-process. DevTools hit-tests the application scene by screen coordinates and renders highlights in an application overlay, so docked and window placements share the same picker behavior.
- The DevTools surface is implemented as an internal KoraFX subapp using `dev.korafx.workspace.workspaceLayout`, `navigationRail`, `routeHost`, framework theme binding, localized text, and Ikonli icons.
- DevTools plugin services are loaded into the host `KoraApplication` Koin graph and unloaded during application shutdown.
- Built-in text supports `SYSTEM`, `ENGLISH`, and `CHINESE`.

## korafx-macos

`korafx-macos` is an optional platform module for macOS native window chrome. The current JVM API is stable, while the Objective-C++/JNI bridge can be filled in behind `KoraMacosNativeBridge`.

Main API:

- `installMacosChrome()`
- `titleBar { macos { ... } }`
- `KoraMacosChromeSpec`
- `KoraMacosChromeBuilder`

Example:

```kotlin
dependencies {
    implementation("io.github.daiyuang:korafx-macos")
}

fun main(args: Array<String>) = koraApplication(args) {
    installMacosChrome {
        preserveTrafficLights = true
    }

    window {
        titleBar {
            chromeMode = KoraWindowChromeMode.NATIVE_OVERLAY
            macos {
                fullSizeContentView = true
                transparentTitlebar = true
                trafficLightInset(14.0, 12.0)
            }
        }
    }
}
```

On non-macOS platforms, `NATIVE_OVERLAY` falls back to the framework custom chrome path.

## Naming Checklist

When adding API:

- Use `bindX` for continuous Flow binding.
- Use `bindXBidirectional` only when control changes write back to `MutableStateFlow`.
- Use `stateX` when a normal DSL node derives one property from screen state.
- Use `XHost` for components that render hosted content.
- Use `XState` for small sealed/data state types.
- Prefer framework-owned names only in `korafx-framework`; keep low-level DSL names close to JavaFX.
