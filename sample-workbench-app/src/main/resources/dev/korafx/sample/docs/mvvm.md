# MVVM 文档

## 状态驱动架构

本示例的 `WorkbenchViewModel` 使用：

- `ViewState` 存储页面 UI 状态
- `UiAction` 触发动作（点击、路由、输入变化）
- `UiEvent` 发布反馈事件

核心原则：

- 所有可观察字段都进入 `StateFlow`
- UI 通过 `stateText/stateVisible/stateList` 与状态绑定
- 避免在视图层直接保存长期业务状态

## 与 Koin 的关系

`WorkbenchViewModel` 由 `installKoin` 提供：

1. `ThemeManager` 注入：控制主题与切换状态
2. `Navigator` 注入：处理路由跳转、历史、路径参数等
3. 命令面板等服务通过 host 注入

## 适配真实项目

- 下游应用可按模块选择注入哪些服务
- 将示例 ViewModel 按模块分散到 feature 层，不必让 Workbench 的 action/event 直接穿透所有页面
