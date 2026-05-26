# Route Data（路由数据加载）

`routeDataHost` 是 `korafx-navigation` 提供的高级路由能力，用于把“加载 + 成功 + 失败”状态和路由状态绑定到同一个页面。

它提供了三个关键能力：

- 在异步 `load(context)` 中返回当前路由上下文的页面数据。
- `loading` 与 `failed` 插槽分别承接加载态和异常态。
- 通过 `RouteDataController` 提供 `revalidate()`，可触发当前路由的重拉取。

### 使用方式

```kotlin
val controller = RouteDataController()

val host = routeDataHost(
    scope = uiScope,
    navigator = navigator,
    controller = controller,
    cache = true,
    load = { context ->
        val delayMs = context.query.int("delay") ?: 300
        delay(delayMs.toLong())
        "data:${context.location.fullPath}"
    },
    loading = { context ->
        loadingState("Loading ${context.route.title}...")
    },
    failed = { context, error ->
        errorState(
            title = "${context.route.title} failed",
            message = error.message.orEmpty(),
        )
    },
) { context, value ->
    label("Result: $value")
}
```

### 示例推荐

- 结合 `restore` 与 `state`，可把数据加载状态也按 `location` 维度保存（配合 `saveState`）。
- 路由参数变化（比如 `query`）会触发 `routeDataHost` 重新加载。
- 搭配路由守卫可在进入某些页面前执行权限或参数校验。
