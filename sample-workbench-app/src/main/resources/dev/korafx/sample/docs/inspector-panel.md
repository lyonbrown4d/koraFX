# Inspector Panel

`korafx-inspector-panel` renders structured metadata for selected objects.

## Use cases

- Commit/file detail metadata.
- Graph node/edge attributes.
- Runtime process and schema detail sidebars.

## Example

```kotlin
inspectorPanel(title = "Selected module", subtitle = "Metadata") {
  property("Artifact", "korafx-inspector-panel")
  property("Status", "Ready")
  actions {
    action("Open") { openModule(source) }
  }
}
```

## Behavior

Panel slots support section grouping and fallback empty state when no item is selected.
