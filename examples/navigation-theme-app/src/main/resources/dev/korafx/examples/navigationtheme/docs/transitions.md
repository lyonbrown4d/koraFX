# 转场动画

`NavigationTransitionProfile` 统一了导航动作到转场的映射：

- `INITIAL`：应用启动
- `PUSH`：新路由入栈
- `REPLACE`：替换当前路由
- `POP`：返回/前进

### 配置方式

1. 通过顶部下拉框切换 Profile（Adaptive / PushSlide / Fade / Scale）
2. `routeHost` 接收 `Flow<RouteTransition>`，在路由变化时自动过渡
3. 可在转场面板中关闭动画，或调节速度倍率（0.5~2.0）
4. 也可直接对单次导航传入自定义 `RouteTransition`
5. 新增：通过路由 `meta` 提供优先级更高的转场配置（`korafx.navigation.transition`）

### 建议

- `Adaptive` 使用不同导航动作映射不同动画，最贴近产品端交互。
- `duration` 通过乘数缩放（0.5~2.0）动态调整，适配不同交互节奏。
- 复杂页面可在页面级别再包一层局部动画，避免大面积重绘。
