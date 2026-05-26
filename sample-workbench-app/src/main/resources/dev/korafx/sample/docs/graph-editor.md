# Graph Editor

`korafx-graph-editor` targets node/edge interaction workflows for domain graphs.

## Basic model

- Nodes with ids and labels.
- Directed edges with optional relation labels.
- Drag/selection/edit callbacks.
- View model integration for persistence and command integration.

## Example

```kotlin
graphEditor {
  val catalog = node(id = "catalog", label = "Catalog", x = 80.0, y = 80.0)
  val model = node(id = "model", label = "ViewModel", x = 320.0, y = 80.0)
  edge(catalog, model, "feeds")
}
```

Use this module for flow-based tools, dependency graph viewers, and visual diagnostics.
