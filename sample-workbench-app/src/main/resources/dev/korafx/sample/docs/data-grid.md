# Data Grid

`korafx-data-grid` is the advanced table module for browsing and editing large structured data.

## What is included

- Strongly typed builders for read/write columns.
- Search and filtering actions.
- Selection + snapshots.
- Optional dirty-row metadata patterns.

## Example

```kotlin
dataGrid(modules) {
  constrainedResize()
  readOnlyTextColumn("Name") { it.name }
  textColumn("Owner") { it.owner }
  actionColumn("Open") { row ->
    openRow(row.id)
  }
}
```

## Recommendation

Split large data views into paged queries at the data source layer to keep UI updates predictable.
