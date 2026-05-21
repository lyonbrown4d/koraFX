# Components 文档

## 组件化方向

KoraFX 组件层提供的是**可选且可独立替换的高级控件**，不是单体框架绑定点。

当前 sample 覆盖了：

- `korafx-components`：Surface、通知、状态条、徽章、卡片等
- `korafx-source-editor`：`sourceEditor`, `queryEditor`, `codeEditor`
- `korafx-data-grid`：带扩展工具栏的表格能力
- `korafx-resource-explorer`：树形资源浏览与右侧详情联动
- `korafx-inspector-panel`：对象属性检视
- `korafx-workspace`：多 Tab 布局与主细节面板
- `korafx-command-palette`：命令覆盖式入口

## 开发建议

- 每个高级组件保持独立模块并遵循一致 theme token。
- 组件应支持状态：空态、加载态、错误态和无障碍可访问性。
- 复杂组件（如树编辑、表格编辑）优先做最小可用版本，再通过版本迭代扩展能力。

## Demo 对应点

在本页“Component Gallery”中，你会看到：

- Surface 与表单组件组合
- Source editor 与 diagnostics 行为
- Workspace / TabWorkspace 与 Inspector 组合
- DataGrid 的列、快照、批量动作
