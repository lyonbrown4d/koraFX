# KoraFX Design

KoraFX 的目标不是做一个完整 JavaFX App Framework，而是提供一套更适合 Kotlin 使用的 JavaFX 开发辅助库。

当前边界收敛为三层：

1. DSL：封装 JavaFX API，让布局、控件、事件、样式和绑定写法更 Kotlin-friendly。
2. MVVM：提供轻量 ViewModel、StateFlow 状态、Action/Event 模型和生命周期释放。
3. Components：基于 DSL 和 MVVM 封装少量常用组件，例如路由、导航、主题、表单、对话框。

## Non-goals

第一阶段不做这些内容：

- 不绑定任何 DI 框架。
- 不封装完整应用启动器。
- 不做命令系统框架。
- 不做窗口管理平台。
- 不隐藏 JavaFX 原生 API。
- 不替用户决定工程架构。

## Module Boundaries

```text
framework-dsl
framework-state
framework-mvvm
framework-navigation
framework-theme
framework-components
sample-workbench-app
```

### framework-dsl

最高优先级模块。

目标：

- 覆盖常用 JavaFX layout。
- 覆盖常用 JavaFX controls。
- 提供事件、样式、padding、margin、grow、alignment 等小型扩展。
- 提供 Flow 到 JavaFX 节点的绑定工具。
- 提供 `RenderState`、`bindList`、`bindRenderState` 等轻量渲染工具。
- 提供 form/dialog 的薄封装。
- 保持薄封装，允许随时回到原生 JavaFX API。

### framework-state

为 MVVM 和组件提供最小状态原语。

目标：

- `MutableStateStore`
- `UiEventStream`
- Flow collection helpers

### framework-mvvm

不依赖 DI。

ViewModel 的创建方式由应用层决定，可以手写构造，也可以用 Koin、Dagger、Spring 或自定义 factory。

目标：

- `ViewState`
- `UiAction`
- `UiEvent`
- `ViewModel<S, A, E>`
- `ViewModelFactory`
- `awaitState`
- `awaitEvent`
- ViewModel lifecycle cleanup

生命周期规则：

- `close()` 必须由拥有者调用，例如 `Application.stop` 或页面销毁逻辑。
- `close()` 是幂等的。
- `isClosed` 可用于调试或断言生命周期。
- 关闭后，`dispatch`、状态更新和事件发送 helper 都是 no-op。

### framework-navigation

作为常用组件存在，不作为核心框架。

目标：

- `Route`
- `Navigator`
- 当前路由状态
- 简单页面切换模型

### framework-theme

作为常用组件存在。

目标：

- theme tokens
- runtime theme switching
- JavaFX stylesheet generation

### framework-components

基于 `framework-dsl`、`framework-navigation` 和后续 MVVM 能力提供可选 UI 组件。

目标：

- 保持组件轻量。
- 不引入应用框架生命周期。
- 不绑定 DI。
- 组件接受显式依赖，例如 `CoroutineScope`、`Navigator`、`ThemeManager`。

当前组件：

- `navigationRail`
- `routeHost`
- `routeStateHost`
- `emptyState`
- `loadingState`
- `errorState`
- `card`
- `section`
- `actionBar`

## Iteration Order

1. 完整完善 DSL。
2. 稳定 MVVM 设计。
3. 基于 DSL + MVVM 抽常用组件。
4. 最后再考虑测试、文档、发布。

## API Naming Guidelines

当前 API 命名约定见 [docs/API.md](docs/API.md)。

简要规则：

- `bindX` 表示从 `Flow` 到 JavaFX 节点的持续绑定。
- `bindXBidirectional` 表示控件会写回 `MutableStateFlow`。
- `renderX` 表示基于当前值的一次性渲染。
- `XHost` 表示负责切换子节点内容的容器组件。
- `XState` 表示小型状态类型。
- 避免 `Application`、`Module`、`Container`、`Command` 等容易把库推向完整框架的命名。

## MVVM And DI

MVVM 不应该直接耦合任何依赖注入库。

原因：

- DI 是应用组装方式，不是 MVVM 模型本身。
- 一旦耦合 Koin，用户就必须接受 Koin 的生命周期和模块模型。
- 轻量库更适合保持 constructor injection，由应用层选择是否接入 DI。

推荐方式：

```kotlin
val themeManager = ThemeManager()
val navigator = Navigator(initialRoute, routes)
val viewModel = WorkbenchViewModel(themeManager, navigator)
```

如果用户想用 DI，可以在应用层这样做：

```kotlin
single { ThemeManager() }
single { Navigator(initialRoute = Home, routes = AppRoute.all) }
single { HomeViewModel(get(), get()) }
```

KoraFX 只需要保证 ViewModel 易于构造、易于释放、易于测试。
