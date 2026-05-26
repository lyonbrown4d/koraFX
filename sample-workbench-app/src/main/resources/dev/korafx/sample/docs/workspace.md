# Workspace

`korafx-components` provides `tabWorkspace` and related workbench primitives for document-centric applications.

## Typical layout

- Sidebar: route explorer or project navigation.
- Main content: editor/table/overview pages.
- Details: inspector or property panel.
- Status strip: command feedback and process state.

## Tab patterns

```kotlin
tabWorkspace(emptyText = "Open a file...") {
  tab("readme", "README.md", closable = false, select = true) {
    sourceEditor(title = "README.md", text = "# KoraFX", readOnly = true)
  }
  tab("query", "Query.sql", dirty = true) {
    queryEditor(text = "select * from modules;")
  }
}
```

Use this module as a reusable workbench layout for git tools, DB clients, and internal consoles.
