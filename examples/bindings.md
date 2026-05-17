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

## State Selectors In DSL

For screen state, prefer binding at the property declaration site instead of collecting everything in a separate `bindUi` function:

```kotlin
data class EditorState(
    val title: String,
    val draft: String,
    val notes: List<String>,
)

val scope = MainScope()
val state = MutableStateFlow(
    EditorState(
        title = "Draft",
        draft = "",
        notes = emptyList(),
    ),
)

val view = panel {
    label {
        styleClasses("headline")
    }.stateText(scope, state) { it.title }

    textField {
        promptText = "Write a note"
    }.stateText(
        scope = scope,
        state = state,
        onTextChange = { value ->
            state.value = state.value.copy(draft = value)
        },
    ) { it.draft }

    button("Submit") {
        onAction {
            val next = state.value.draft.trim()
            if (next.isNotEmpty()) {
                state.value = state.value.copy(draft = "", notes = listOf(next) + state.value.notes)
            }
        }
    }.stateDisable(scope, state) { it.draft.isBlank() }

    vbox(spacing = 8.0) {
    }.stateList(
        scope = scope,
        state = state,
        items = { it.notes },
        empty = {
            label("No notes")
        },
    ) { note ->
        label(note)
    }
}
```

Use `stateful` for a small state-aware subtree when repeating `scope` and `state` would be noisy. Plain controls are still allowed inside the block:

```kotlin
panel {
    label("Static header")

    stateful(scope, state) {
        vbox(spacing = 8.0) {
            label(text = { it.title })
            textField(
                text = { it.draft },
                onTextChange = { value ->
                    state.value = state.value.copy(draft = value)
                },
            )
            label("Only visible when there are notes") {
                stateVisible { it.notes.isNotEmpty() }
            }
        }
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
