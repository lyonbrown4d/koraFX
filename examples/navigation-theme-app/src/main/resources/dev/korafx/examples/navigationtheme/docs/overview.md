# Navigation Theme 示例 · 概览

这个页面汇总了 KoraFX 的路由核心能力：

- 声明式路由（`Route` / `PathRoute`）
- 动态参数、可选参数、Query、Hash
- 历史栈与回退/前进（`back` / `forward`）
- Guard、重定向与预检能力
- `routeHost` 与 `routerHost` 的切换模式
- 路由级状态持久化（按 location key）
- 转场动画（`RouteTransition`）

建议：

1. 在 `Navigator` 初始化时传入全部路由与初始路由。
2. 在 `routeHost` 中绑定当前路由内容渲染。
3. 通过 `navigation-state` 订阅导航栈信息与返回值。
