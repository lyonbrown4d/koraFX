# KoraFX Workbench

## 总览

`sample-workbench-app` 是 KoraFX 的完整能力演示入口，目标是验证库的使用体验是否适合构建：

- 桌面管理工具（如数据库 GUI）
- Git/运维后台
- 通用开发者工具工作台

它通过三部分能力组织：

1. **KoraFX Framework**：应用启动、DI、导航、主题、DevTools、生命周期。
2. **KoraFX Modules**：`dsl` / `components` / `mvvm` / `theme` / `router` 等可选能力。
3. **Documentation in Demo**：当前路由的右侧会同步显示说明文档，便于快速查阅和复用。

## 当前布局

当前示例采用了 `documentation` 风格的布局：

- **左侧**：路由导航与页面目录（支持快速切换模块）。
- **中间**：模块演示与交互实例（当前页面内容）。
- **右侧**：对应页面的 Markdown 文档（后续将接入富文本解析器）。

## 快速启动能力

- 顶栏支持主题切换、下一主题、命令面板唤起。
- 左侧导航（`NavigationRail`）和右侧文档保持联动。
- 路由示例展示了 `path`、`query`、`hash`、前进后退与历史栈行为。
- 所有模块都支持从 demo 中直接拷贝交互代码片段到实际项目。

## 接下来的扩展方向

- 完成高级组件（source editor、data grid、resource explorer）的独立发布。
- 将主题能力收敛为 Material 体系并提供 light/dark 切换。
- 引入 Markdown 富文本引擎，替换当前纯文本预览。
