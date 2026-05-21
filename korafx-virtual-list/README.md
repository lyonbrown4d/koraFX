# korafx-virtual-list

分页异步加载的 JavaFX 虚拟列表组件（`VirtualList`），面向大量数据场景，提供按需分页、选择模型与占位态。

## Quick usage

```kotlin
import dev.korafx.virtuallist.VirtualListDataLoader
import dev.korafx.virtuallist.VirtualSelectionMode
import dev.korafx.virtuallist.virtualList

val loader: VirtualListDataLoader<String> = { offset, limit ->
    (offset until (offset + limit)).map { "row-$it" }
}

val list = virtualList(
    dataLoader = loader,
    pageSize = 50,
    selectionMode = VirtualSelectionMode.SINGLE,
) {
    itemOf { value -> Label("Item: $value") }
}
```

## Features

- 可配置固定高度 / 动态高度列表项；
- 基于 offset/limit 的异步分页加载；
- 手动触发 `loadMore()`，以及滚动到底部自动触发加载；
- 支持 `SINGLE` / `MULTIPLE` 选择模式；
- 可设置加载中、空态、错误占位符与错误处理回调。


