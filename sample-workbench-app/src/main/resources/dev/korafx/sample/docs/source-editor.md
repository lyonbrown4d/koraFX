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
}
```

## Notes

Editor surfaces are intentionally opinionated for layout and toolbar slots, while keeping text handling explicit through callbacks.
