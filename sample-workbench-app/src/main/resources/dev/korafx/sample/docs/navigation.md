# Navigation

`korafx-navigation` provides typed path routes, history, lazy modules and route guards.

## Route concepts

- `PathRoute` routes expose `id` and `path`.
- `Navigator` owns current route + location state.
- `navigate`, `navigatePath` and `navigateAsync` for imperative jumps.
- `back`, `forward`, `replace`, guards and restoration helpers.

## Example

```kotlin
val state = Navigator(WorkbenchRoute.Framework, WorkbenchRoute.moduleRoutes)
state.navigatePath("/components/data-grid")
state.back()   // return to previous entry
state.forward()
```

## Advanced behavior

- Nested layouts are available through route layout trees.
- Per-route `params`, `query`, and `hash` are parsed from paths.
- Guards (`beforeEach`, `beforeEnter`, `beforeLeave`) can block or redirect navigation.
- Route state restoration API supports scroll/index persistence for complex pages.

## Integrating with controls

Use `routeButton` for nav buttons and `routeHost`/`routerHost` for layout-aware route rendering.
