# Navigation And Components Examples

## State Driven Sidebar

```kotlin
sealed class AppRoute(
    override val id: String,
    override val title: String,
) : Route {
    data object Overview : AppRoute("overview", "Overview")
    data object Settings : AppRoute("settings", "Settings")

    companion object {
        val all = listOf(Overview, Settings)
    }
}

val navigator = Navigator(
    initialRoute = AppRoute.Overview,
    routes = AppRoute.all,
)

val scope = MainScope()

val sidebar = sidebar {
    bindEach(scope, navigator.state.map { state -> state.routes }) { route ->
        navButton(route.title, active = route.id == navigator.currentRoute.id) {
            maxWidth = Double.MAX_VALUE
            onAction {
                navigator.navigate(route.id)
            }
        }
    }
}
```

For MVVM, prefer deriving navigation items inside the ViewModel and binding the view to plain state:

```kotlin
data class NavigationItem(
    val title: String,
    val routeId: String,
    val active: Boolean,
)
```

## NavigationRail And RouteHost

```kotlin
val navigator = Navigator(
    initialRoute = AppRoute.Overview,
    routes = AppRoute.all,
    pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
)

val scope = MainScope()

val root = workbenchLayout {
    navigation {
        navigationRail(scope, navigator)
    }

    content {
        routeHost(scope, navigator) { route ->
            when (route) {
                AppRoute.Overview -> panel {
                    label("Overview") {
                        styleClasses("headline")
                    }
                }
                AppRoute.Settings -> panel {
                    label("Settings") {
                        styleClasses("headline")
                    }
                }
            }
        }
    }
}
```

## AppShell With Toasts

```kotlin
val notifications = ToastHost()

val root = appShell {
    topBar {
        toolbar {
            label("KoraFX") {
                styleClasses("headline")
            }
            spacer()
            ghostButton("Toggle Theme") {
                onAction {
                    notifications.show(
                        message = "Theme switched.",
                        tone = ToastTone.SUCCESS,
                    )
                }
            }
        }
    }

    navigation {
        navigationRail(scope, navigator)
    }

    content {
        routeHost(scope, navigator) { route ->
            label(route.title)
        }
    }

    overlay {
        toastHost(scope, notifications)
    }
}
```

## ModalHost

```kotlin
val modals = ModalHost()

val root = appShell {
    content {
        button("Open Settings") {
            onAction {
                modals.show(
                    title = "Workspace settings",
                    message = "Use an in-scene modal for local editing flows.",
                    actions = listOf(
                        ModalAction("Cancel"),
                        ModalAction(
                            text = "Apply",
                            role = ModalActionRole.PRIMARY,
                            onAction = { println("Apply settings") },
                        ),
                    ),
                ) {
                    label("Settings content")
                }
            }
        }
    }

    overlay(alignment = Pos.CENTER, margin = Insets.EMPTY) {
        modalHost(scope, modals)
    }
}
```

## Route State Host

```kotlin
val overviewState = MutableStateFlow<RenderState<List<DocumentRow>>>(RenderState.Loading)
val settingsState = MutableStateFlow<RenderState<List<DocumentRow>>>(RenderState.Empty)

val root = workbenchLayout {
    navigation {
        navigationRail(scope, navigator)
    }

    content {
        routeStateHost(
            scope = scope,
            navigator = navigator,
            stateFor = { route ->
                when (route) {
                    AppRoute.Overview -> overviewState
                    AppRoute.Settings -> settingsState
                }
            },
            loading = { route ->
                loadingState("Loading ${route.title}...")
            },
            empty = { route ->
                emptyState(
                    title = route.title,
                    message = "No content for this route yet.",
                )
            },
            failed = { route, failure ->
                errorState(
                    title = "${route.title} failed",
                    message = failure.message,
                )
            },
        ) { route, rows ->
            label(route.title) {
                styleClasses("headline")
            }
            renderEach(rows) { row ->
                label(row.title)
            }
        }
    }
}
```

## Feedback States

```kotlin
val empty = emptyState(
    title = "No documents",
    message = "Create a document to start editing.",
    actionText = "Create",
    onAction = {
        println("Create document")
    },
)

val loading = loadingState("Loading workspace...")

val error = errorState(
    title = "Workspace failed to load",
    message = "Check the project path and retry.",
    actionText = "Retry",
    onAction = {
        println("Retry")
    },
)
```

Feedback components are plain JavaFX nodes, so they can be composed inside normal DSL blocks:

```kotlin
val content = panel {
    when {
        state.loading -> loadingState("Loading routes...")
        state.errorMessage != null -> errorState(
            title = "Load failed",
            message = state.errorMessage,
            actionText = "Retry",
            onAction = { viewModel.dispatch(AppAction.Retry) },
        )
        state.items.isEmpty() -> emptyState(
            title = "No routes",
            message = "Add a route to populate the navigation rail.",
        )
        else -> listView(state.items) {
            render { it.title }
        }
    }
}
```

## Surfaces

```kotlin
val settings = section(
    title = "General",
    description = "Basic workspace preferences.",
) {
    gridPane(hgap = 12.0, vgap = 10.0) {
        column(prefWidth = 120.0, alignment = HPos.RIGHT)
        column(grow = Priority.ALWAYS, fillWidth = true)

        label(0, 0, "Workspace")
        textField(1, 0, "KoraFX") {
            maxWidth = Double.MAX_VALUE
        }
    }

    actionBar {
        ghostButton("Cancel")
        button("Save") {
            onAction {
                println("Save settings")
            }
        }
    }
}

val dashboard = card {
    label("Build status") {
        styleClasses("headline")
    }
    label("All modules are passing.") {
        styleClasses("muted")
    }
}
```

## Semantic Display

```kotlin
val summary = section(
    title = "Release readiness",
    description = "Small semantic components share the same theme tone classes.",
) {
    hbox(spacing = 8.0) {
        badge("Stable", ComponentTone.SUCCESS)
        badge("Docs", ComponentTone.INFO)
        chip("Theme", tone = ComponentTone.PRIMARY, selected = true)
        chip("Samples", tone = ComponentTone.NEUTRAL)
    }

    metricCard(
        label = "Styled controls",
        value = "24",
        helper = "Covered by generated JavaFX CSS",
        tone = ComponentTone.INFO,
    ) {
        badge("Theme ready", ComponentTone.SUCCESS)
    }

    alertBanner(
        title = "Theme coverage changed",
        message = "Review the generated CSS before publishing a new preset.",
        tone = ComponentTone.WARNING,
        actionText = "Inspect",
        onAction = {
            println("Inspect theme coverage")
        },
    )
}
```

## Code Editor

```kotlin
val editor = codeEditor(
    title = "Kotlin Scratch",
    text = "fun main() {\n    println(\"KoraFX\")\n}",
    language = "kotlin",
    placeholder = "Start typing...",
    onTextChange = { text ->
        println("Document changed: ${text.length} chars")
    },
) {
    prefHeight = 260.0
    tabSize(4)
}

editor.markClean()
```

## Layout And Data Grid

```kotlin
data class TaskRow(
    var title: String,
    var owner: String,
    val status: String,
)

val layout = borderLayout {
    header {
        label("Workspace")
    }
    sidebar {
        vbox(spacing = 8.0) {
            label("Inbox")
            label("Roadmap")
            label("Settings")
        }
    }
    content {
        dataGrid(
            items = listOf(
                TaskRow("Document DSL", "Core", "Draft"),
                TaskRow("Theme coverage", "Design", "Review"),
            ),
            searchPrompt = "Search tasks...",
        ) {
            search(textOf = { "${it.title} ${it.owner} ${it.status}" })
            dirtyRows { it.status == "Draft" }
            emptyState("No tasks match the current search.")
            footer("2 tasks")
            toolbar {
                action("Refresh") {
                    println("Refresh tasks")
                }
            }
            constrainedResize()
            editableTextColumn("Title", valueOf = { it.title }) { row, value ->
                row.title = value
            }
            editableTextColumn("Owner", valueOf = { it.owner }) { row, value ->
                row.owner = value
            }
            readOnlyTextColumn("Status") { it.status }
        }
    }
    footer {
        label("DataGrid keeps TableView editable commits explicit.")
    }
}
```

For a full tool-style workbench, use `workspaceLayout` when the UI needs navigation, details, status, and overlay slots:

```kotlin
val workspace = workspaceLayout {
    topBar {
        label("Git / Database Workspace")
    }
    navigation {
        vbox(spacing = 8.0) {
            label("Repository")
            label("Branches")
            label("Schemas")
        }
    }
    content {
        codeEditor(
            title = "Query.sql",
            text = "select * from users;",
            language = "sql",
        )
    }
    details {
        section(title = "Inspector") {
            label("Connection: local")
            label("Branch: main")
        }
    }
    status {
        label("Ready")
    }
    overlay {
        badge("Saved", ComponentTone.SUCCESS)
    }
}
```

## Inspector Panel

```kotlin
val inspector = inspectorPanel(
    title = "users",
    subtitle = "public.users table",
) {
    badge("Selected", ComponentTone.INFO)
    property("Rows", "128")
    property("Owner", "analytics")
    section("Columns") {
        property("id", "uuid")
        property("email", "varchar")
        badge("indexed", ComponentTone.SUCCESS)
    }
    actions {
        action("Open") {
            println("Open selected resource")
        }
    }
}

val emptyInspector = inspectorPanel(emptyText = "Select a resource to inspect.")
```

## Resource Explorer

```kotlin
data class Resource(
    val name: String,
    val children: List<Resource> = emptyList(),
)

val roots = listOf(
    Resource(
        "Repository",
        listOf(
            Resource("src", listOf(Resource("Main.kt"), Resource("Theme.kt"))),
            Resource("README.md"),
        ),
    ),
    Resource(
        "Database",
        listOf(Resource("public", listOf(Resource("users"), Resource("orders")))),
    ),
)

val explorer = resourceExplorer(
    items = roots,
    childrenOf = { it.children },
    textOf = { it.name },
) {
    search(prompt = "Search resources...")
    onSelect { resource ->
        println("Selected: ${resource?.name}")
    }
    rowAction { resource ->
        println("Open: ${resource.name}")
    }
    contextMenu { resource ->
        actionItem("Open") {
            println("Open ${resource.name}")
        }
        actionItem("Inspect") {
            println("Inspect ${resource.name}")
        }
    }
}
```
