# 主题系统文档

> 主题层是 KoraFX 的“外观协议”。组件只关心 token，不应关心颜色字面值。

## 现状能力

- `ThemeManager` 作为单一主题源，内部暴露 `StateFlow<KoraTheme>`
- `BuiltInThemes.MaterialLight` / `MaterialDark` 与 `FluentLight` / `FluentDark` 提供开箱主题
- 框架在启动时绑定 `scene` 主题，不需要各页面单独挂载 stylesheet
- 主题变更通过 `themeManager.setTheme` / `nextTheme` / `toggle` 等动作广播

## 主题 token

### Color

- `colors.primary`
- `colors.surface`
- `colors.surfaceMuted`
- `colors.textPrimary`
- `colors.textSecondary`
- `colors.border`

### Typography / Spacing / Radius / States

- `typography.fontFamily / baseSize / headlineSize`
- `spacing.xs..xxl`
- `radii.small|medium|large|pill`
- `states`（hover、focus、disabled、selected）

右侧源码示例面板也基于当前主题动态渲染，保证示例展示符合 Material 或 Fluent 风格。

## 实现示例

```kotlin
theme {
  presets(BuiltInThemes.all)
  default(BuiltInThemes.MaterialLight)
  persistSelection = true
}
```

## 使用边界

- 当你接入第三方控件时，优先把 `KoraTheme` 的颜色语义映射到控件 style class
- 组件如果提供 `styleClass`，应避免在代码里内联硬编码颜色
- 需要更深主题控制时，新增 `ThemeTokenProvider` 并兼容默认 token 读取方式

## 路线图（本页面对应）

1. 统一 Material 与 Fluent Light / Dark 基础变量和交互状态
2. 覆盖标准 JavaFX 控件的主链路状态（hover、focused、disabled）
3. 在 `korafx-components` 与高级组件模块中统一状态色定义
4. 提供主题迁移工具：从旧 token 到新 token 的兼容映射
5. 增加更细颗粒度的文档演示（按钮组、输入组、代码块）
