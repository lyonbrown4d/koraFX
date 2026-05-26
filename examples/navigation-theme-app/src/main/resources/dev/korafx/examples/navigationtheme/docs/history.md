# 历史栈与恢复

`Navigator` 内置 `backStack` / `forwardStack`，支持：

- `navigate`, `replace`, `navigatePath`, `replacePath`
- `back`, `forward`
- `popToRoot`, `clearNavigationHistory`

### 导航行为

1. `navigate` 默认 `PUSH`，会写入 `backStack`。
2. `replace` 不会额外写入 back 历史。
3. `back`/`forward` 触发 `POP` transition。

### 注意

`popToRoot` 会持续调用 `back()`，适合返回工作流入口；  
`clearNavigationHistory` 只清空历史，不改变当前路由。
