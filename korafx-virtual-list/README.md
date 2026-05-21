# korafx-virtual-list

JavaFX 虚拟化控件集合。模块名暂时保持 `korafx-virtual-list`，但能力已经扩展为 `VirtualList`、`VirtualTable` 和 `VirtualTerminal`，面向大量列表、分页表格与日志/终端行展示场景。

## Quick usage

### VirtualList

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

### VirtualTable

```kotlin
data class ProcessRow(val pid: Int, val name: String, val cpu: Double)

val table = virtualTable<ProcessRow>(
    dataLoader = { offset, limit -> processStore.page(offset, limit) },
    totalCountEstimate = { processStore.estimatedCount },
    pageSize = 100,
    selectionMode = VirtualSelectionMode.SINGLE,
) {
    textColumn("PID", ProcessRow::pid)
    textColumn("Name", ProcessRow::name)
    columnText("CPU", ProcessRow::cpu, render = { "%.1f%%".format(it) })
    onSelect { rows -> println(rows.firstOrNull()) }
}
```

### VirtualTerminal

```kotlin
val terminal = virtualTerminal(maxLines = 2_000, autoScroll = true) {
    line("booting", "terminal-info")
    lineRenderer { line -> Label(line.text).apply { styleClass += line.styleClasses } }
}

terminal.appendLine("connected", "terminal-success")
terminal.clear()
```

## Features

- `VirtualList`：可配置固定高度 / 动态高度列表项，基于 offset/limit 的异步分页加载，支持选择模型与占位态；
- `VirtualTable`：基于 `TableView` 的虚拟表格初版，支持 async/page `dataLoader`、`totalCountEstimate`、columns DSL、选择回调与手动/滚动加载；
- `VirtualTerminal`：基于虚拟化 `ListView` 的日志/终端行控件，支持 `appendLine`、`clear`、`maxLines`、`autoScroll`、行级 style classes 与自定义行渲染；
- 三个控件均提供 `NodeContainerBuilder` 扩展，可在 KoraFX DSL 容器内直接声明。


