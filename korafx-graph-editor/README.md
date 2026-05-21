# korafx-graph-editor

Lightweight JavaFX graph editor module with a Kotlin DSL API.

## Quick usage

```kotlin
import dev.korafx.grapheditor.graphEditor
import dev.korafx.grapheditor.graphEditorDemo
import dev.korafx.dsl.vbox

val editor = graphEditor {
    val source = node("source", "Source", x = 80.0, y = 100.0)
    val sink = node("sink", "Sink", x = 320.0, y = 100.0)

    edge(source, sink) { created ->
        created.label = "flow"
    }

    onNodeSelected { node ->
        // react to selection
    }
}

val demo = graphEditorDemo() // ready-to-run three-node sample
```

## Features

- Render nodes and directed edges with handles for basic linking.
- Add nodes and connect nodes through handles or click-select chaining.
- Move nodes by dragging, select nodes/edges, delete selected with Delete/Backspace.
- Optional pan + zoom (Ctrl + mouse wheel, middle button drag).

## Styling

- Built-in style is loaded automatically from `dev/korafx/grapheditor/graph-editor.css`.
- You can still overlay or replace styles from application-level CSS via JavaFX stylesheet APIs.

```kotlin
val editor = graphEditor()
val theme = javaClass.getResource("/app/styles/graph-editor-theme.css")
theme?.let { editor.stylesheets.add(it.toExternalForm()) }
```

## Style classes

- `graph-editor`
- `graph-editor-node`
- `graph-editor-node-container`
- `graph-editor-node-group`
- `graph-editor-node-label`
- `graph-editor-handle`
- `graph-editor-handle-selected`
- `graph-editor-node-selected`
- `graph-editor-edge`
- `graph-editor-edge-arrow`
- `graph-editor-edge-label`
- `graph-editor-edge-selected`
- `graph-editor-connection-preview`
