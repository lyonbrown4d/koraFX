# KoraFX API Overview

KoraFX is now oriented as a Kotlin-first JavaFX application framework. The default path should be direct:

```kotlin
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-framework")
implementation("io.github.daiyuang:korafx-components")
```

Runtime code is intentionally split into four primary publishable modules: `korafx-dsl`, `korafx-navigation`, `korafx-framework`, and `korafx-components`.
`korafx-components` exposes Ikonli JavaFX core for icon-ready components, but applications should choose their own icon pack dependency.
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

## korafx-components

`korafx-components` is the optional component layer for real desktop tools and workbench-style applications.

Main API:

- Shell: `appShell`, `appToolbar`, `toolbarGroup`
- Overlays: `ModalHost`, `modalHost`, `ModalAction`
- Layout: `borderLayout`, `workspaceLayout`, `WorkspaceLayout`
- Resource browsing: `resourceExplorer`, `ResourceExplorer`, `ResourceExplorerBuilder`
- Data grids: `dataGrid`, `DataGrid`, `DataGridBuilder`, `editableTable`, `EditableTableBuilder`
- Details: `inspectorPanel`, `InspectorPanel`, `InspectorPanelBuilder`
- Editor surfaces: `codeEditor`, `sourceEditor`, `queryEditor`, `SourceEditor`, `SourceDiagnostic`
- Workspaces: `tabWorkspace`, `TabWorkspace`, `TabWorkspaceBuilder`
- Activity: `activityTimeline`, `ActivityTimeline`, `ActivityTimelineBuilder`
- Commands: `CommandPaletteHost`, `CommandPaletteCommand`, `commandPalette`, `CommandPalette`
- Navigation: `navigationRail`, `routeButton`, `pathButton`, `routeLink`, `pathLink`, `routeHost`, `routerHost`, `routeDataHost`, `routeStateHost`, `RouterModule`, `RouteDataController`, `routeLazy`, `routeScrollRestoration`, `routeSelectionRestoration`, `routeFocusRestoration`
- Feedback: `feedbackState`, `emptyState`, `loadingState`, `errorState`, `ToastHost`, `toastHost`, `snackbar`
- Surfaces: `card`, `section`, `actionBar`, `breadcrumb`, `pageHeader`, `statusBar`, `statusItem`
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

navigationRail(
    scope = uiScope,
    navigator = navigator,
    icon = { route -> routeIcons[route.id] },
)
```

Guidelines:

- Components are still JavaFX nodes and should remain composable.
- Components may accept explicit framework services such as `CoroutineScope`, `Navigator`, `ThemeManager`, and command hosts.
- Components should have stable style classes so `korafx-framework` theme services can fully cover them.
- Keep concrete Ikonli icon packs in application/sample modules; `korafx-components` should only require Ikonli JavaFX core.

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
- The DevTools surface is implemented as an internal KoraFX subapp using `workspaceLayout`, `navigationRail`, `routeHost`, framework theme binding, localized text, and Ikonli icons.
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
