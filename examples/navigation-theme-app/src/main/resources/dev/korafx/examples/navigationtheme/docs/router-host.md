# Router Host（布局 + Outlet）

`routeHost` 适合全局单页切换；`routerHost` 适合复杂的布局树。

核心概念：

- 每个 `Route` 可定义 `layoutKey`
- `Router Layout` 复用共享 shell（导航条、工具栏）
- `route` 与 `outlet` 组合形成父子布局关系

### 典型场景

- 仪表盘 + 详情页共享顶部工具栏
- 带侧边栏和正文主区的两列路由
- 在不同模块间通过 `routeMeta` 分发页面能力

### 迁移建议

先用 `routeHost` 打磨页面切换能力，再逐步将关键区域迁移到 `routerHost`。
