# DSL 模块文档

> 该页面用于说明 `korafx-dsl` 在工程里的“代码组织价值”。不是所有逻辑都放在 DSL，但重复结构、状态绑定和布局可读性必须统一。

## 设计原则

1. **保留 JavaFX 原生能力**
   DSL 只封装重复结构，不改写底层控件行为。
2. **约定可读性优先于炫技**
   `vbox -> hbox -> form` 的分层，强调“可阅读、可扫描、可复用”。
3. **状态绑定靠显式声明**
   文本、选择、显示状态在 DSL 节点层直接绑定，不依赖反射。

## 构建面向页面的能力

### 布局

- `vbox` / `hbox` / `gridPane` / `borderLayout`
  用于表达页面骨架和可复用区域。
- `panel` / `card` / `section`
  在示例中分别用于“标准容器 / 可选边距 / 文档区块”。
- `workbenchLayout`
  预定义了 Top / Nav / Content / Details / Footer 布局槽位。

### 表单

- `form` + `item` + `validationMessage`
  在同一入口把校验、布局和提交行为串起来。
- `submitBar`
  可复用于通用提交动作区。

### 状态绑定

- `stateText`, `stateVisible`, `stateList`
  由 `StateFlow` 渲染文本、显示状态和列表内容。
- `bindTextBidirectional`, `bindSelectedItemBidirectional`, `bindValueBidirectional`
  将页面控件与 `StateFlow` 双向同步。
- `stateDisable`
  用状态控制控件的可操作性。

## 路由示例

在本页演示区域中你可以看到：

- 表单项联动校验（项目名 + 并发数）
- `gridPane` + `comboBox` + `choiceBox` 的组合
- 路由展示区使用 `routeScrollRestoration` / `routeSelectionRestoration`

## 典型写法（推荐）

```kotlin
section(
  title = "State Binding",
  description = "绑定是显式的，副作用可追踪。",
) {
  form {
    item("Project") {
      textField {
        bindTextBidirectional(uiScope, dslProjectName)
      }
      validationMessage(uiScope, dslProjectNameError)
    }
    item("Parallel") {
      intSpinner {
        bindValueBidirectional(uiScope, dslParallelism)
      }
    }
  }
}
```

## API 收口建议

### 推荐边界

- 如果你只需要**布局和行为脚手架**：直接使用 `korafx-dsl`。
- 如果你需要**复杂交互模型**（编辑器、图表、文件树）：建议封装成高级组件模块，不要反向塞回 DSL。

### 反模式（避免）

- 把状态管理完全藏在控件扩展函数里（可测试性变差）
- 将业务验证从 `StateFlow` 转移到控件本地属性里（难以复用）
- 让 DSL 组件直接承担路由权限或导航策略（应交给导航层）

## 下一步计划

- 增加更多“高级组件入口”的 DSL 包装（非破坏性）
- 增加更多参数型路由布局片段（避免重复创建）
- 补齐可供复制的“页面模板脚手架”片段（登录页、列表页、详情页）
