# 转场动画

`NavigationTransitionProfile` 统一了导航动作到转场的映射：

- `INITIAL`：应用启动
- `PUSH`：新路由入栈
- `REPLACE`：替换当前路由
- `POP`：返回/前进

### 配置方式

1. 通过顶部下拉框切换 Profile（Adaptive / PushSlide / Fade / Scale）
2. `routeHost` 接收 `Flow<RouteTransition>`，在路由变化时自动过渡
3. 也可直接对单次导航传入自定义 `RouteTransition`

### 建议

- `Adaptive` 使用不同导航动作映射不同动画，最贴近产品端交互。
- 复杂页面可在页面级别再包一层局部动画，避免大面积重绘。
