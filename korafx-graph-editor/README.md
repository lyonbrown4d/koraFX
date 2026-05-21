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
