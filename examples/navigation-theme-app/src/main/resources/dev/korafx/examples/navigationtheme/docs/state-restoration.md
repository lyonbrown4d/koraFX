# 路由级状态

`Navigator` 提供 `restoredState` / `saveState`：

```kotlin
navigator.saveState("note", value, location = navigator.currentLocation)
val note = navigator.restoredState<String>("note", navigator.currentLocation)
```

不同 route 的状态会按 `fullPath`（含 params/query/hash）隔离存储，不同同一路由不同参数不会互相污染。

### 适用场景

- 编辑器内输入草稿暂存
- 列表分页状态（页码、筛选条件）
- 表单分页返回时恢复滚动位置

### 注意事项

`clearRestoredState(location)` 可清空指定 location 的缓存；  
也可切换路由并在页面销毁时统一重置。
