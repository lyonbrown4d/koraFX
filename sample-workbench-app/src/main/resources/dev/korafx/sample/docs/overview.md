# KoraFX Workbench 总览

> 这是 KoraFX 示例工程里的“文档+能力联合展示页”。每一个路由页面都对应一个独立模块文档，目标是把框架能力讲成可复用的“工程模板”。

## 你在这里看到什么

- **框架层**（`korafx-framework` + `korafx-navigation`）：应用生命周期、DI、路由、主题、DevTools、窗口配置。
- **中间件层**（`korafx-components`）：通用 Surface、Layout、按钮/列表/状态条等基元。
- **高级组件层**（独立子模块）：`source-editor`、`data-grid`、`resource-explorer`、`inspector-panel`、`workspace` 等。
- **文档与教学层**：右侧 Source Code 面板展示每个模块的调用示例，避免引入 WebView。

## 默认布局

`sample-workbench-app` 使用 app shell + 可拖拽 split pane 组织模块目录、实时 demo 和源码示例：

- **TopBar**：命令区 + 命令面板入口
- **Navigation**：`navigationRail` 与 `routeButton`
- **Content**：当前路由的展示区域
- **Source**：当前路由的 Kotlin 调用示例和说明
- **Footer**：状态和日志输出

## 设计目标（独立模块文档化）

1. **每个路由就是一个独立页面文档**。
   `overview`、`dsl`、`components`、`mvvm`、`theme` 五个独立条目可被单独复制到生产项目作为骨架。
2. **每个文档可独立演进**。
   路由资源是独立的 `*.md`，新增模块只需增加 `WorkbenchRoute` 条目和文档文件。
3. **文档与交互一致**。
   示例中的按钮、表单、列表和主题变化，都直接反映到右侧文档的说明和状态。

## 路由能力速览

- `overview`：工作台能力地图、路线和目标场景
- `dsl`：原子化布局与控件 DSL、绑定语义、可读性优先策略
- `components`：高级组件组合策略与模块发布边界
- `mvvm`：StateFlow 驱动的单向数据流 + Koin 注入
- `theme`：Material / Fluent token、亮暗主题切换、样式统一策略

## 启动与运行

```kotlin
koraApplication {
  window {
    title = "KoraFX Workbench"
    width = 1120.0
    height = 720.0
  }
  theme {
    presets(BuiltInThemes.all)
    default(BuiltInThemes.MaterialLight)
  }
  navigation {
    initialRoute = WorkbenchRoute.Overview
    routes(WorkbenchRoute.all)
  }
}
```

## 典型应用场景

该模式适用于你需要“同一个工程即是代码库，又是文档库”的场景：

- 数据库管理台、运维控制台
- Git 可视化桌面客户端
- 日志/任务系统的多功能工作台
- 任何想把组件能力通过“实例 + 文档 + 导航”一次交付的内网工具

## 与示例联调建议

当你在新项目落地时，建议先按以下顺序接入：

1. `korafx-framework`（入口+窗口+生命周期）
2. `korafx-navigation`（路径、历史、query/hash）
3. `korafx-components`（基础视觉和布局）
4. 按需添加高级组件模块
5. 把文档页面作为 `WorkbenchRoute` 的资源驱动入口保留
