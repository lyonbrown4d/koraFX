# KoraFX Roadmap

This roadmap tracks framework and component work for real desktop applications, especially Git source tools and database GUI tools. KoraFX now uses `korafx-framework` as the direct application entry, `korafx-navigation` for route-aware UI, `korafx-components` as the base workbench/component layer, and independently published modules for large advanced components.

## Principles

- Components return normal JavaFX nodes and keep native JavaFX APIs accessible.
- Components provide stable style classes so `korafx-framework` theme services can cover them consistently.
- `korafx-framework` provides the default Koin-backed application model.
- Route-aware UI belongs in `korafx-navigation`; generic surfaces belong in `korafx-components`.
- Shared JavaFX test helpers and TestFX integration belong in `korafx-test`, not duplicated in every module.
- Components accept explicit callbacks and state; framework services can be injected by the application layer.
- DSL should remove repetitive JavaFX setup, not hide JavaFX concepts.
- Each component should land with tests, theme selectors, documentation, and sample usage.

## Iteration Order

### 1. Workspace Layout

Status: implemented as the current component iteration.

Target apps: Git source clients, database admin tools, internal desktop consoles.

Goal: provide a reusable application workspace structure above `borderLayout`, with named slots for navigation, toolbar, content, details, status, and overlays.

Module boundary: workspace layout APIs live in `korafx-components` under `dev.korafx.components`; `tabWorkspace` also lives in components.

First version:

- `workspaceLayout { topBar { ... } navigation { ... } content { ... } details { ... } status { ... } }`
- Optional collapsed details/sidebar state.
- Stable style classes for each slot.
- Fit common desktop workbench layouts without requiring `appShell` routing.

Acceptance:

- Sample shows a Git/database-like workbench with sidebar, main content, right inspector, and status strip.
- Theme covers all workspace slot classes.
- Tests assert slot assignment and style classes.

### 2. Resource Explorer

Status: implemented as the current component iteration.

Target apps: Git repository browser, database schema/table explorer, file/project navigator.

Goal: provide a styled tree/list explorer with search, selection, row actions, and context menu hooks.

Module boundary: resource explorer APIs live in `korafx-resource-explorer` under `dev.korafx.resourceexplorer`; keep `korafx-components` for smaller shared building blocks.

First version:

- `resourceExplorer(items, childrenOf, textOf) { ... }`
- Tree model adapter for nested resources.
- Optional filter text field.
- Selection callback and row action callback.
- Context menu builder per item.
- Empty/loading/error states can reuse feedback components.

Acceptance:

- Sample shows repository-like folders/files and database-like schemas/tables.
- Supports icon/graphic per item without forcing an icon system.
- Tests cover filtering, selection, expansion, and context menu creation.

### 3. Data Grid

Status: implemented as the current component iteration.

Target apps: database table viewer, query result grid, Git file/status list, admin tables.

Goal: evolve beyond `editableTable` into a higher-level grid with filtering, sorting, row state, and bulk actions.

Module boundary: data grid APIs live in `korafx-data-grid` under `dev.korafx.datagrid`; keep `korafx-components` for smaller shared building blocks.

First version:

- `dataGrid(items) { textColumn(...) editableTextColumn(...) actionColumn(...) }`
- Built-in toolbar slot for search/filter/actions.
- Row dirty marker support.
- Optional footer/status text.
- Empty state and loading state hooks.

Acceptance:

- Builds on `editableTable` rather than duplicating table edit logic.
- Sample shows a query result table and a Git status table.
- Tests cover editable commits, dirty row styling, search filtering, and action column callbacks.

### 4. Inspector Panel

Status: implemented as the current component iteration.

Target apps: Git commit/file inspector, database table/column detail panel, settings/detail sidebars.

Goal: provide a consistent details panel for key/value properties, sections, actions, and metadata.

Module boundary: inspector panel APIs live in `korafx-inspector-panel` under `dev.korafx.inspector`; keep `korafx-components` for smaller shared display primitives.

First version:

- `inspectorPanel(title, subtitle) { property("Branch", "...") section("Details") { ... } actions { ... } }`
- Support badges/chips for status metadata.
- Optional empty state when no item is selected.
- Stable style classes for title, section, property row, value, and action area.

Acceptance:

- Sample connects explorer selection to inspector content.
- Tests cover property rendering, empty state, and action slot.
- Theme covers inspector structure.

### 5. Query / Source Editor

Status: implemented as the current component iteration.

Target apps: SQL query editor, Git diff/source viewer, configuration editors.

Goal: extend `codeEditor` into a task-oriented editor surface with toolbar actions, status, diagnostics, and result/output slot.

Module boundary: editor APIs live in `korafx-source-editor` under `dev.korafx.sourceeditor`; keep `korafx-components` for smaller shared building blocks.

First version:

- `sourceEditor(...)` as a general editor surface.
- `queryEditor(...)` convenience wrapper for run/stop actions and result area.
- Dirty state and `markClean()` remain explicit.
- Diagnostics list with line/column/message.
- Optional read-only source viewer mode.

Acceptance:

- Sample shows SQL editor with Run action and a result grid.
- Sample shows source viewer with diagnostics.
- Tests cover action callbacks, dirty state, diagnostics rendering, and result slot.

### 6. Tab Workspace

Status: implemented as the current component iteration.

Target apps: multi-file Git tools, multi-query database clients, multi-document admin tools.

Goal: provide a themed multi-tab workspace that manages close/dirty indicators and typed tab metadata while still returning JavaFX `TabPane`.

Module boundary: tab workspace APIs live in `korafx-components` under `dev.korafx.components`.

First version:

- `tabWorkspace { tab(id, title, dirty = false) { ... } }`
- Close callback and select callback.
- Dirty marker style class.
- Empty placeholder when no tabs exist.

Acceptance:

- Sample opens explorer items into tabs.
- Tests cover add/select/close callbacks and dirty marker styling.
- Theme covers tab workspace dirty/empty states.

### 7. Activity Timeline

Status: implemented as the current component iteration.

Target apps: Git log, database execution history, background task logs, audit trails.

Goal: provide a vertical timeline/list for timestamped events with status tones and actions.

First version:

- `activityTimeline(events) { titleOf ... messageOf ... toneOf ... }`
- Grouping by date or section.
- Optional action per event.
- Works with static lists first; Flow binding can be layered later.

Acceptance:

- Sample shows Git commits and SQL execution history.
- Tests cover grouping, tone classes, empty state, and event action callbacks.
- Theme covers timeline marker, connector, row, and tone classes.

### 8. Command Palette

Status: implemented as the current component iteration.

Target apps: complex desktop tools with many actions, Git commands, database actions, navigation shortcuts.

Goal: provide a keyboard-friendly command picker that can be shown in an overlay and dispatch explicit commands.

Module boundary: command palette APIs live in `korafx-command-palette` under `dev.korafx.commandpalette`; keep `korafx-components` and `korafx-framework` free of command registration concerns.

First version:

- `CommandPaletteHost` owns visibility and command list.
- `commandPalette(host) { ... }` renders search, filtered commands, keyboard-friendly selection, and empty state.
- Command model includes id, title, description, group, and action.
- No global shortcut registration in v1; applications wire their own accelerator.

Acceptance:

- Sample opens the palette from a toolbar button.
- Tests cover filtering, command selection, empty state, and close behavior.
- Theme covers overlay, search field, command row, selected row, and group labels.

## Near-Term Focus

The next two implementation iterations should be:

1. Component integration polish, because the major workbench surfaces now need consistent cross-component examples.
2. Release readiness, because the roadmap core components need API review, docs cleanup, and versioned publishing checks.
