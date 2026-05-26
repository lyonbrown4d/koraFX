# 路由级转场覆盖

在 `navigation-theme-app` 的路由能力里，全局 `NavigationTransitionProfile` 决定了默认转场策略。
`routeTransition(profile)` 现在会优先读取 `NavigationLocation.meta` 的配置进行覆盖。

### 支持的 meta key

- `korafx.navigation.transition`
  - 类型可为 `RouteTransition`、`NavigationTransitionProfile` 或字符串
  - 字符串支持：
    - `fade`
    - `slide`
    - `scale`
    - `none`
    - profile 名称，如 `adaptive`, `pushSlide`, `fade`, `scale`, `none`
    - `profile:<profileName>` 显式声明 profile
- `korafx.navigation.transitionProfile`
  - 与上面 `transition` 的 profile 模式等价，类型可为 `NavigationTransitionProfile` 或字符串

### 示例

```kotlin
meta = routeMeta(
    ROUTE_TRANSITION_META_KEY to "fade",
)
```

当导航到该路由时，转场会优先走 `fade`，不受顶部全局 `transitionPreset` 的影响。
