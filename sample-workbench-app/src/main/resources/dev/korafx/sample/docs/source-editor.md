# Source Editor

`korafx-source-editor` is the default editor surface for code-like content.

## Entry components

- `sourceEditor` for general source + result slots.
- `queryEditor` for SQL-like workflows.
- `codeEditor` for plain code blocks and lightweight text operations.
- `diagnostics` blocks for marker style feedback.

## Example

```kotlin
sourceEditor(
  title = "Query.sql",
  text = "select id, name from users;",
  language = "sql",
) {
  action("Run") { /* execute query */ }
  action("Format") { /* format text */ }
  markIdle("Ready")
}
```

## Query editor workflow

```kotlin
val editor = queryEditor(
    text = "select * from modules",
    onRun = { sql ->
        editor.markRunning("Running: $sql")
        editor.setResult(
            vbox(spacing = 8.0) {
                label("Demo result")
                label("Rows: 3")
            },
            title = "Execution result",
        )
        editor.markSuccess("Query completed")
    },
)
```

## Notes

Editor surfaces are intentionally opinionated for layout and toolbar slots, while keeping text handling explicit through callbacks.
