# MVVM 模块文档

> 本页面定义 sample 里的状态管理范式：`StateFlow` + `ViewModel` + `Koin` 注入，适用于复杂桌面页面。

## 核心职责

- **数据源**：`WorkbenchViewModel` 只维护 `ViewState`，外部事件映射为 `UiAction`
- **副作用**：`UiEvent` 仅表示反馈（toast、日志、弹窗触发）
- **渲染层**：`stateText/stateVisible/stateList` 与流式状态绑定

## 本示例的约束

### 1）动作（Action）

- `Navigate`, `NavigatePath`：切换路由
- `ToggleTheme`, `NextTheme`, `SelectTheme`：主题操作
- `Increment/Decrement/Reset`：MVVM 计数器示例
- `SubmitDraft`：演示纯 UI 输入提交

### 2）事件（Event）

- `Feedback`：统一进入状态栏的反馈事件
- 便于将来转接通知系统（toast/snackbar）

### 3）状态（State）

`WorkbenchState` 中包括当前路由、文档、主题名、反馈消息、计数器、草稿与列表等字段。

## 与 Koin 集成

`installKoin { modules(workbenchModule()) }` 提供 `WorkbenchViewModel`，其依赖包括：

- `ThemeManager`
- `Navigator<WorkbenchRoute>`

这样做有两个好处：

1. 业务对象可在应用生命周期里替换（测试/预发布/生产）
2. ViewModel 与框架基础服务解耦，便于单元测试替身注入

## 推荐模式

```kotlin
private fun onAction(action: WorkbenchAction) {
    when (action) {
        is WorkbenchAction.NavigatePath -> launch { navigator.navigatePathAsync(action.path) }
        is WorkbenchAction.SubmitDraft -> submitDraft()
        is WorkbenchAction.SelectTheme -> selectTheme(action.themeId)
        is WorkbenchAction.UpdateDraft -> updateDraft(action.value)
    }
}
```

建议保持：

- 所有可重放状态都放在 `State` 里
- 只用 `launch` 处理可取消/异步流程
- 不在 View 层直接管理长期业务字段

## 迁移建议（给真实项目）

- 把 `WorkbenchViewModel` 细分为 Feature 范围的 ViewModel（例如 `GitTreeViewModel`、`QueryPanelViewModel`）
- 每个 Feature 只暴露自己的 Action/Event 集合
- Workbench 层仅做“路由 + 组合”职责，不直接参与业务计算

## 质量指标

在 sample 里，你可以用这个页面验证：

- 主题切换是否实时写回 UI（反馈状态）
- 路由变化是否有明确的 Action 入口
- 文档区更新是否跟随路由变化而无副作用重复渲染
