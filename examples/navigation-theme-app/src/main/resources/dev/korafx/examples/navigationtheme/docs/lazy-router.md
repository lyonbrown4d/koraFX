# 路由懒加载（routeLazy）

`routeLazy` 用于把某个路由页面的构建函数延迟到首次访问时才执行，适用于体积较大或仅在特定场景出现的页面。

与 `route` 不同，`routeLazy` 接受一个“工厂函数”，第一次需要渲染该路由时才创建真实 `Node`，后续可配合 `PageInstancePolicy` 控制复用策略。

## 关键点

- `routeLazy(route) { ... }`：延迟构建路由视图
- `PageInstancePolicy.KEEP_ALIVE`：页面实例保留，切回来时不重新创建
- `localNavigator`：示例内独立子导航器，避免影响主演示导航栈
- 动态参数：`/detail/:itemId` 在跳转时按路径参数提供上下文

## 示例行为

1. 进入 `/lazy-router` 主页面后，先打开参数详情页，确认可拿到 `itemId`
2. 再打开 `Lazy Panel`，观察只在首次访问时才显示初始化次数从 `1` 开始
3. 再次返回并重新进入 `Lazy Panel`，在 KEEP_ALIVE 模式下不应再次增长
