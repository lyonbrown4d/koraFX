# DSL 文档

## 设计目标

KoraFX DSL 的目标不是替代 JavaFX，而是提供**更简洁的 Kotlin 建图体验**：

- 降低重复布局模板代码
- 通过命名函数表达语义（`vbox`, `gridPane`, `form`）
- 与 JavaFX 原生组件保持一一映射

## 关键能力

- 布局构建：`vbox`, `hbox`, `gridPane`, `borderLayout` 等
- 表单构造：`form`, `item`, `submitBar`, `validationMessage`
- 状态绑定：`bindTextBidirectional`, `bindSelectedItemBidirectional`, `stateVisible`, `stateText`
- 控件增强：`statusDisable`, `stateList` 等状态语义入口

## 使用建议

1. 用 DSL 组装页面骨架，用原生 JavaFX 组件保持灵活性。
2. 把状态放在 `StateFlow`，在 DSL 中用 `state*` 绑定映射。
3. 需要组件复用时，优先抽象成自己的高级组件函数而不是封装在全局。

## 文档化示例

当前路由中你可以看到：

- 带状态校验的项目表单
- 通过 `route*` 系列 API 的导航示例
- 本地原生控件 + DSL 的统一封装模式
