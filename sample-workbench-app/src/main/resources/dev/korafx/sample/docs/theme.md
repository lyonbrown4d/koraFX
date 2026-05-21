# 主题系统

## 设计目标

KoraFX 的主题系统需要保证：

- JavaFX 标准控件行为一致（Button、TextField、Table 等）
- 可预测的 token 继承关系
- 多主题运行时切换与持久化

## 当前实现

- `ThemeManager` 管理 `KoraTheme`，通过 `StateFlow` 通知全局。
- `BuiltInThemes` 提供 `Material Light` 与 `Material Dark`。
- `scene` 级主题注入由框架控制，不需要每个页面重复设置 stylesheet。

## 实施路线

1. 统一视觉 token：色彩、排版、圆角、间距、状态色。
2. 逐步补齐 JavaFX 标准组件样式覆盖。
3. 提供可继承的主题预设，允许下游覆盖部分 token。
4. 集成 markdown 富文本预览后，支持文档内实时主题色块和代码高亮。
