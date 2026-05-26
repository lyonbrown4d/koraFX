# Resource Explorer

`korafx-resource-explorer` turns hierarchical resources into selectable explorer tables.

## Features

- Tree model adapter via child/text/secondary providers.
- Search entry for file or object discovery.
- Row action hooks.
- Selection state callback for opening details/workspace content.

```kotlin
resourceExplorer(resources) {
  children { it.children }
  text { it.name }
  secondaryText { if (it.children.isEmpty()) "file" else "folder" }
  onSelect { selected -> openInWorkspace(selected) }
}
```

## Integration

Use together with `tabWorkspace` for file-like workflows and `inspectorPanel` for side-detail rendering.
