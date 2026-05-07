# Binding Examples

## StateFlow To JavaFX

```kotlin
val scope = MainScope()
val title = MutableStateFlow("Overview")
val enabled = MutableStateFlow(true)

val view = panel {
    label {
        bindText(scope, title)
    }

    toggleButton("Enabled") {
        bindSelectedBidirectional(scope, enabled)
    }
}
```

## List Bindings

Use `bindItems` for native JavaFX item controls:

```kotlin
data class DocumentRow(
    val title: String,
)

val scope = MainScope()
val documents = MutableStateFlow(emptyList<DocumentRow>())

val documentList = listView<DocumentRow>(init = {
    bindItems(scope, documents)
})
```

Use `bindChildren` when each item maps to one node in a pane:

```kotlin
val documentTitles = vbox(spacing = 8.0) {}

documentTitles.bindChildren(scope, documents) { document ->
    label(document.title)
}
```

Use `bindList` when an item renders multiple nodes or the list needs an empty state:

```kotlin
val listPane = vbox(spacing = 8.0) {}

listPane.bindList(
    scope = scope,
    flow = documents,
    empty = {
        label("No documents")
    },
) { document ->
    hbox(spacing = 8.0) {
        label(document.title) {
            growHorizontal()
        }
        button("Open")
    }
}
```

## Render State

For explicit loading, empty, content, and failure states:

```kotlin
val workspace = MutableStateFlow<RenderState<List<DocumentRow>>>(RenderState.Loading)
val contentPane = vbox(spacing = 8.0) {}

contentPane.bindRenderState(
    scope = scope,
    flow = workspace,
    loading = {
        label("Loading workspace...")
    },
    empty = {
        label("Workspace is empty")
    },
    failed = { failure ->
        label("Load failed: ${failure.message}")
    },
) { rows ->
    renderEach(rows) { row ->
        label(row.title)
    }
}
```
