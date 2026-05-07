# KoraFX API Overview

KoraFX 的 API 目标是薄封装 JavaFX，而不是替换 JavaFX。命名规则尽量保持可预测：

- `lowerCamelCase` top-level factory 创建 JavaFX 节点，例如 `vbox`、`button`、`tableView`。
- `NodeContainerBuilder` 扩展用于在 DSL block 内追加节点，例如 `panel { button("Save") }`。
- `bindX` 表示从 `Flow` 或 `MutableStateFlow` 绑定到 JavaFX 节点。
- `renderX` 表示一次性根据当前值渲染节点。
- `routeX` 表示基于 `Navigator` 的路由组件。
- `XState` 表示轻量状态模型，例如 `RenderState`、`NavigationState`。

## framework-dsl

`framework-dsl` 是核心模块，保持 Kotlin-friendly，但不隐藏原生 JavaFX API。

主要能力：

- Layout factories: `borderPane`、`vbox`、`hbox`、`stackPane`、`gridPane`、`flowPane`、`tilePane`、`anchorPane`、`scrollPane`、`splitPane`、`tabPane`。
- Control factories: `label`、`button`、`checkBox`、`textField`、`textArea`、`listView`、`comboBox`、`choiceBox`、`slider`、`datePicker`、`colorPicker`、`radioButton`、`toggleButton`。
- Advanced controls: `treeView`、`tableView`、`accordion`、`titledPane`、`spinner`、`progressBar`、`pagination`。
- Layout helpers: `insets`、`padding`、`margin`、`growHorizontal`、`growVertical`、`align`、`gridAlign`。
- Styling helpers: `styleClass`、`styleClasses`、`toggleStyleClass`、`pseudoClass`、`invalidWhen`。
- Event helpers: `onAction` for `ButtonBase` and `MenuItem`.
- Binding helpers: `bindText`、`bindVisible`、`bindDisable`、`bindItems`、`bindSelectedItem`、`bindValue`、`bindProgress`、`bindInvalid`、`bindValidation`。
- Bidirectional bindings: text input、toggle selected、selection controls、slider、spinner、date picker。
- Render helpers: `fragment`、`renderIf`、`renderUnless`、`renderEach`、`bindContent`、`bindChildren`、`bindEach`、`bindList`、`bindRenderState`。
- Form/dialog helpers: `form`、`submitBar`、`validationMessage`、`alert`、`confirmation`、`textInputDialog`、`customDialog`。

Guidelines:

- Prefer adding tiny JavaFX extension helpers over wrapping whole concepts.
- Keep factory names close to JavaFX class names.
- Expose `init: Node.() -> Unit` so callers can still use native JavaFX properties.
- Use `Flow` bindings for dynamic state; do not introduce a DSL-owned state model.
- Prefer `bindList` when list items need multiple nodes or an empty state; use `bindChildren` for one node per item.
- Prefer dialog builder names that mirror JavaFX properties, such as `buttonTypes`; shorter aliases like `buttons` may exist for readability.

Binding selection guide:

| API | Use when | Effect |
| --- | --- | --- |
| `bindItems` | Target is `ListView`、`ComboBox`、`ChoiceBox`、`TableView`. | Replaces the control's `items` from `Flow<List<T>>`. |
| `bindChildren` | Target is a `Pane` and each item maps to exactly one `Node`. | Replaces `children`; the node factory runs on the JavaFX Application Thread. |
| `bindList` / `bindEach` | Target is a `Pane` and each item may render a fragment or the list needs an empty state. | Replaces `children` using `FragmentBuilder`. |
| `bindRenderState` | UI must represent loading, empty, failed, and content states explicitly. | Replaces `children` from a `RenderState<T>`. |

## framework-state

Small primitives shared by MVVM and components.

Main API:

- `MutableStateStore<S>`
- `UiEventStream<E>`
- `Flow<T>.collectLatestIn(scope, collector)`

Guidelines:

- Keep this module dependency-light.
- Use it for primitives only; application state shape belongs to user code.

## framework-mvvm

MVVM stays independent from DI containers.

Main API:

- `ViewState`
- `UiAction`
- `UiEvent`
- `ViewModel<S, A, E>`
- `ViewModelFactory<VM>`
- `viewModelFactory { ... }`
- `isClosed`
- `closeAll()`
- `awaitState { ... }`
- `awaitEvent { ... }`

Guidelines:

- Prefer constructor injection.
- Applications decide whether to instantiate manually, via custom factories, or via external DI.
- Inject `coroutineContext` in tests for deterministic async actions.
- Always call `close()` from the owning lifecycle.
- `close()` is idempotent; after close, `dispatch` and the protected state/event helpers become no-ops.

## framework-navigation

Navigation is intentionally small and state-driven.

Main API:

- `Route`
- `PageInstancePolicy`
- `NavigationState<R>`
- `Navigator<R>`

Guidelines:

- Routes are plain values with `id` and `title`.
- `Navigator` owns current route state only.
- Page construction belongs to DSL/components or application code.

## framework-theme

Theme support generates JavaFX stylesheets from typed tokens.

Main API:

- `ColorTokens`
- `TypographyTokens`
- `ThemeTokens`
- `KoraTheme`
- `BuiltInThemes`
- `ThemeManager`
- `SceneThemeController`
- `ThemeStylesheetFactory`

`BuiltInThemes.all` exposes selectable JavaFX stylesheet presets. Use `BuiltInThemes.findById` or `ThemeManager.setTheme(id)` when theme choice is stored in user settings.
`ThemeManager.nextTheme()` and `previousTheme()` cycle through the configured `availableThemes`; `toggle()` remains a Light/Dark convenience.

Guidelines:

- Theme tokens should remain small and CSS-oriented.
- Components should style by stable style classes, not direct colors.

## framework-components

Components are optional conveniences built on DSL, navigation, and state flows.

Main API:

- Shell: `appShell`
- Overlays: `ModalHost`, `modalHost`, `ModalAction`
- Navigation: `navigationRail`、`routeHost`、`routeStateHost`
- Feedback: `feedbackState`、`emptyState`、`loadingState`、`errorState`、`ToastHost`、`toastHost`、`snackbar`
- Surfaces: `card`、`section`、`actionBar`

`routeStateHost` 按路由执行渲染取消：路由切换后只会渲染当前路由的 `stateFor` 流，不会被旧路由更新覆盖。
`appShell` provides stable top/navigation/content/footer/overlay slots for common desktop layouts.
`ModalHost` owns one in-scene modal request; `modalHost` renders it in an overlay slot.
`ToastHost` owns transient feedback messages; `toastHost` renders them as JavaFX nodes.

Guidelines:

- Components accept explicit dependencies such as `CoroutineScope` and `Navigator`.
- Components should not own application lifecycle.
- Components should remain composable JavaFX nodes.

## Naming Checklist

When adding API:

- Use `bindX` for continuous Flow binding.
- Use `bindXBidirectional` only when control changes write back to `MutableStateFlow`.
- Use `renderX` for immediate in-block rendering.
- Use `XHost` for container components that switch child content.
- Use `XState` for small sealed/data state types.
- Avoid names that imply framework ownership, such as `Application`, `Module`, `Container`, or `Command`.
