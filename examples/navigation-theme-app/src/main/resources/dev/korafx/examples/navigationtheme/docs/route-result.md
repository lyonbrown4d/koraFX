# 路由结果回传（navigationResult）

KoraFX 的路由结果模型借鉴了页面返回值模式，可用于页面 A 打开页面 B 后让 B 回传一个结果给 A。

核心 API：

- `navigationResultKey<T>("name")`：定义强类型结果 key
- `localNavigator.setResult(key, value)`：在离开页前设置返回值
- `navigator.results(key)`：监听该 key 的结果流
- `navigator.awaitResult(key)`：挂起等待下一次结果返回

## 使用场景

- 表单选择弹窗/子页面返回选择项
- 多步向导的中间结果回传
- 在主页面中等待弹窗或子页面完成任务

## 示例行为

1. 当前路由页点击 **Picker**，进入选择页
2. 选择任意一项后调用 `setResult` 并返回
3. 上一页 `results` 与 `awaitResult` 会接收到 `alpha / beta / gamma`
