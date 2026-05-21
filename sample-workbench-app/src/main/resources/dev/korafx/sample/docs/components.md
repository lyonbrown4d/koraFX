# Components 模块文档

> `components` 不是“一个大组件”，而是把高频 UI 建设工作拆成可独立发布的能力包。

## 模块边界

- `korafx-components`：通用 Surface、标签、导航、状态条、消息横幅
- `korafx-source-editor`：文本/代码编辑与诊断提示
- `korafx-data-grid`：可扩展的数据表
- `korafx-resource-explorer`：文件树与资源详情联动
- `korafx-components`：app shell、workspace layout、tab workspace、surface 与通用业务组件
- `korafx-inspector-panel`：对象属性与动态检查
- `korafx-command-palette`：命令驱动入口
- `korafx-virtual-list` / `korafx-graph-editor`：高性能列表与图结构可视化（已独立模块）

## 页面中可见能力

### Surface 与文案层

- `alertBanner`, `heroBanner`, `metricCard`, `statusBar`, `statusItem`
- `badge`, `chip`, `emptyState`, `pageHeader`
- `card`, `section`, `activityTimeline`

### 表单/控制层

- `button`、`ghostButton`、`toggleButton`
- `splitMenuButton`、`menuButton`、`hyperlink`
- `progressBar`、`slider`、`pagination`

### 容器层

- `appShell`、`workspaceLayout` 与 `tabWorkspace`
- `inspectorPanel` 与 `resourceExplorer` 的布局组合
- `dataGrid` 的操作栏与快照模式

## 推荐组合示例

```kotlin
pageHeader(
  title = "Native And Semantic Controls",
  subtitle = "统一主题语义 + 业务交互事件",
  eyebrow = "Theme / Control Gallery",
)

flowPane {
  button("Primary")
  ghostButton("Ghost")
  toggleButton("Toggle") { isSelected = true }
}
```

## 独立发布策略

### 为什么要拆包

- 避免“全量引入”导致启动体积不可控
- 下游可以只引入 `components` 或 `data-grid` 等子集
- 每个模块都可以单独迭代版本和变更日志

### 对开发者的收益

1. 应用按场景选配
2. 迭代隔离，降低升级冲击
3. 组件可独立回归测试（单模块打包）

## 组件层的演进方向

- 加入更多默认动作面板（文件拖放、筛选抽屉、批量操作 toolbar）
- 提供可替换的空态/加载态模板（默认骨架屏）
- 为图形化组件（graph-editor）补齐统一的编辑器快捷键协议
