# 导航守卫

守卫可挂在全局、单路由前进和单路由离开上：

- `beforeEach`：全局前置守卫
- `beforeEnter(route)`：仅进入目标路由时
- `beforeLeave(route)`：离开当前路由时
- `_Async` 变体支持异步校验（权限、远端状态、二次确认）

返回值为 `NavigationDecision`：

- `Allow`：放行
- `Block(reason)`：阻断并返回原因
- `Redirect(routeId/path, replace)`：重定向到指定目标

### 典型用途

1. 登录态校验
2. 未保存变更弹窗确认
3. 远端能力检测与灰度路由
