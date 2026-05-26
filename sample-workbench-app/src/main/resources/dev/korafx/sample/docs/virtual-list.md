# Virtual List

`korafx-virtual-list` provides scrollable virtualized surfaces for large datasets.

## Surface list

- `virtualList` for lightweight item streams.
- `virtualTable` for column/table-like datasets.
- `virtualTerminal` for continuously appended log lines.

## Example

```kotlin
virtualList(
  dataLoader = { offset, limit -> events.drop(offset.toInt()).take(limit) },
  totalCountEstimate = { events.size },
) {
  item {
    text(item.title)
    text(item.message)
  }
  onSelect { selected -> status = selected.firstOrNull()?.title }
}
```

Virtualized controls reduce rendering overhead and keep tool UIs responsive under high volume.
