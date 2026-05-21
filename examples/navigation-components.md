# Navigation Examples

Navigation core and route-aware UI live in `dev.korafx.navigation`.

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

## RouterHost With Layouts

Use `routerHost` when routes should share reusable layout shells. Shells can be nested, and pages render into the nearest layout outlet.

```kotlin
import dev.korafx.components.workspaceLayout

data object ProjectRoute : PathRoute {
    override val id = "project"
    override val title = "Project"
    override val path = "/projects/:projectId/:tab?"
}

enum class AppLayout {
    Workbench,
    Project,
}

val root = routerHost(scope, navigator) {
    layout(AppLayout.Workbench) {
        shellWithOutlets { outlets ->
            workspaceLayout {
                topBar {
                    toolbar {
                        label("KoraFX")
                    }
                }
                navigation {
                    navigationRail(scope, navigator)
                }
                content(outlets.primary)
                details(outlets.outlet("details"))
            }
        }

        route(AppRoute.Overview) {
            overviewPage()
        }

        routeLazy(AppRoute.Settings) {
            val screen = SettingsScreen(settingsRepository)
            { _ -> screen.build() }
        }

        layout(AppLayout.Project) {
            shell { outlet ->
                borderLayout {
                    top(projectToolbar())
                    center(outlet)
                }
            }

            routeView(ProjectRoute) {
                primaryWithLocation { context ->
                    projectPage(context.params["projectId"].orEmpty())
                }
                outlet("details") { route ->
                    projectDetails(route.title)
                }
            }
        }
    }
}
```

## Path Routes History And Guards

```kotlin
data object ProjectRoute : PathRoute {
    override val id = "project"
    override val title = "Project"
    override val path = "/projects/:projectId/:tab?"
    override val meta = routeMeta("requiresAuth" to true)
}

data object LoginRoute : PathRoute {
    override val id = "login"
    override val title = "Login"
    override val path = "/login"
}

val navigator = Navigator.fromPath(
    initialPath = preferences.lastRoutePath ?: "/",
    routes = listOf(AppRoute.Overview, ProjectRoute, LoginRoute),
    fallbackRoute = AppRoute.Overview,
    pageInstancePolicy = PageInstancePolicy.KEEP_ALIVE,
)

navigator.beforeEach { context ->
    if (context.to.meta.boolean("requiresAuth") && !session.isAuthenticated) {
        NavigationDecision.Redirect(path = "/login")
    } else {
        NavigationDecision.Allow
    }
}

navigator.beforeLeave(ProjectRoute) { context ->
    if (projectEditor.hasUnsavedChanges(context.from.requiredParam("projectId"))) {
        NavigationDecision.Block("Project has unsaved changes.")
    } else {
        NavigationDecision.Allow
    }
}

navigator.beforeEnter(ProjectRoute) { context ->
    if (projectRepository.exists(context.to.requiredParam("projectId"))) {
        NavigationDecision.Allow
    } else {
        NavigationDecision.Redirect(path = "/")
    }
}

navigator.navigatePath("/projects/42/files?mode=review#diff")
navigator.navigatePath(
    ProjectRoute.location(
        params = mapOf("projectId" to 42, "tab" to "files"),
        query = mapOf("mode" to "review"),
        hash = "diff",
    ),
)
navigator.back()
navigator.forward()
```

Use route-aware buttons and links when a control should navigate and maintain active state automatically:

```kotlin
hbox(spacing = 8.0) {
    add(routeButton(scope, navigator, AppRoute.Overview))
    add(pathButton(scope, navigator, "/projects/42/files?mode=review", text = "Project files"))
    add(routeLink(scope, navigator, LoginRoute))
    add(pathLink(scope, navigator, navigator.currentLocation.withQuery("tab" to "history"), text = "History"))
}
```

Derive path strings from the current location when only query or hash changes:

```kotlin
val nextPath = navigator.currentLocation
    .withQuery("filter" to "open", "page" to 1)

scope.launch {
    navigator.navigatePathAsync(nextPath)
}
```

Use async navigation when guards need suspend work:

```kotlin
navigator.beforeEachAsync { context ->
    if (context.to.meta.boolean("requiresAuth") && !sessionStore.isAuthenticated()) {
        NavigationDecision.Redirect(path = "/login")
    } else {
        NavigationDecision.Allow
    }
}

scope.launch {
    navigator.navigatePathAsync("/projects/42")
}
```

Typed result keys are useful for selector/detail flows where a route returns a value to the caller:

```kotlin
val selectedProjectKey = navigationResultKey<String>("selected-project")

scope.launch {
    navigator.navigatePathAsync("/projects")
    val projectId = navigator.awaitResult(selectedProjectKey)
    navigator.navigatePathAsync(ProjectRoute.location(params = mapOf("projectId" to projectId)))
}

// Inside the project picker page:
navigator.setResult(selectedProjectKey, selectedProject.id)
```

## Route Data Host

```kotlin
val dataController = RouteDataController()

val content = routeDataHost(
    scope = scope,
    navigator = navigator,
    controller = dataController,
    cache = true,
    load = { context ->
        repository.loadProject(context.params["projectId"].orEmpty())
    },
) { context, project ->
    projectPage(project, selectedTab = context.params["tab"])
}

dataController.revalidate()
```

## Route Restoration

Use explicit restoration helpers for desktop state that belongs to a specific route location. The state is keyed by `NavigationLocation.fullPath`, so `/projects/42/files` and `/projects/43/files` restore independently.

```kotlin
routeView(ProjectRoute) {
    primaryWithLocation { context ->
        scrollPane(
            init = {
                routeScrollRestoration(scope, navigator, key = "project-files")
            },
        ) {
            vbox(spacing = 8.0) {
                listView(
                    items = projectFiles,
                    init = {
                        routeSelectionRestoration(
                            scope = scope,
                            navigator = navigator,
                            key = "selected-file",
                            keyOf = { file -> file.path },
                        )
                        routeFocusRestoration(scope, navigator, key = "file-list-focus")
                    },
                )
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
        appToolbar(
            title = "KoraFX",
            subtitle = "Workbench",
            actions = {
                ghostButton("Toggle Theme") {
                    onAction {
                        notifications.show(
                            message = "Theme switched.",
                            tone = ToastTone.SUCCESS,
                        )
                    }
                }
            },
        )
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
val header = pageHeader(
    title = "Repository",
    subtitle = "main branch / working tree",
    eyebrow = "Workspace",
) {
    breadcrumb(
        items = listOf(
            breadcrumbItem("workspace", "Workspace"),
            breadcrumbItem("repository", "Repository"),
            breadcrumbItem("source", "src/Main.kt", current = true),
        ),
        onSelect = { id -> println("Open $id") },
    )
}

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

val status = statusBar {
    statusItem("Ready", ComponentTone.SUCCESS)
    spacer()
    statusItem("main", ComponentTone.INFO)
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

Advanced editor APIs live in the independent `korafx-source-editor` artifact under `dev.korafx.sourceeditor`, so source editing can grow independently from the base component package.

```kotlin
import dev.korafx.sourceeditor.codeEditor

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

## Source And Query Editor

```kotlin
import dev.korafx.datagrid.dataGrid
import dev.korafx.sourceeditor.SourceDiagnostic
import dev.korafx.sourceeditor.queryEditor
import dev.korafx.sourceeditor.sourceEditor

val source = sourceEditor(
    title = "RepositoryConfig.kt",
    text = "data class RepositoryConfig(val branch: String)",
    language = "kotlin",
    readOnly = true,
    diagnostics = listOf(
        SourceDiagnostic(1, 12, "Read-only source preview.", ComponentTone.INFO),
    ),
) {
    action("Open File") {
        println("Open source file")
    }
}

data class QueryRow(
    val id: Int,
    val name: String,
)

val query = queryEditor(
    text = "select id, name from users;",
    onRun = { sql ->
        println("Run query: $sql")
    },
    onStop = {
        println("Stop query")
    },
) {
    diagnostics(
        listOf(
            SourceDiagnostic(1, 8, "Demo warning from SQL parser.", ComponentTone.WARNING),
        ),
    )
    result(
        title = "Query Result",
        node = dataGrid(
            items = listOf(QueryRow(1, "Ada"), QueryRow(2, "Linus")),
            showSearch = false,
        ) {
            constrainedResize()
            readOnlyTextColumn("ID") { it.id }
            readOnlyTextColumn("Name") { it.name }
        },
    )
}
```

## Tab Workspace

```kotlin
import dev.korafx.components.tabWorkspace

val workspace = tabWorkspace(
    emptyText = "Open a file or query...",
) {
    onSelect { id ->
        println("Selected tab: $id")
    }
    onClose { id ->
        println("Closed tab: $id")
    }
    tab(
        id = "readme",
        title = "README.md",
        closable = false,
    ) {
        sourceEditor(
            title = "README.md",
            text = "# KoraFX",
            language = "markdown",
            readOnly = true,
        )
    }
    tab(
        id = "query:users",
        title = "Users Query",
        dirty = true,
    ) {
        queryEditor(
            text = "select * from users;",
            onRun = { sql -> println(sql) },
        )
    }
}

workspace.openTab("src/Main.kt", "Main.kt") {
    sourceEditor(
        title = "Main.kt",
        text = "fun main() {}",
        language = "kotlin",
        readOnly = true,
    )
}
```

## Activity Timeline

```kotlin
data class ActivityEvent(
    val title: String,
    val message: String,
    val time: String,
    val group: String,
    val tone: ComponentTone,
)

val timeline = activityTimeline(
    events = listOf(
        ActivityEvent("Commit 4a18c2", "Theme coverage refined.", "09:12", "Git", ComponentTone.SUCCESS),
        ActivityEvent("Query finished", "24 rows returned.", "09:20", "Database", ComponentTone.INFO),
        ActivityEvent("Migration warning", "Missing index.", "09:32", "Database", ComponentTone.WARNING),
    ),
    emptyText = "No activity yet",
) {
    groupBy { it.group }
    timeOf { it.time }
    titleOf { it.title }
    messageOf { it.message }
    toneOf { it.tone }
    action("Open") { event ->
        println("Open ${event.title}")
    }
}
```

## Command Palette

```kotlin
import dev.korafx.commandpalette.CommandPaletteCommand
import dev.korafx.commandpalette.CommandPaletteHost
import dev.korafx.commandpalette.commandPalette

val paletteHost = CommandPaletteHost(
    listOf(
        CommandPaletteCommand(
            id = "open-file",
            title = "Open File",
            description = "Open a repository file in the workspace.",
            group = "Navigation",
        ) {
            println("Open file")
        },
        CommandPaletteCommand(
            id = "theme.next",
            title = "Next Theme",
            description = "Switch to the next built-in theme preset.",
            group = "Theme",
        ) {
            println("Next theme")
        },
    ),
)

val root = stackPane {
    add(workbenchLayout {
        topBar {
            toolbar {
                ghostButton("Commands") {
                    onAction {
                        paletteHost.show()
                    }
                }
            }
        }
        content {
            label("Workbench content")
        }
    })
    commandPalette(paletteHost) {
        emptyState("No commands match the current search.")
    }
}
```

## Layout And Data Grid

Data grid APIs live in the independent `korafx-data-grid` artifact under `dev.korafx.datagrid`.

```kotlin
import dev.korafx.datagrid.dataGrid

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

For a full tool-style workbench, use `workspaceLayout` from `korafx-components` when the UI needs navigation, details, status, and overlay slots:

```kotlin
import dev.korafx.components.workspaceLayout

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
import dev.korafx.inspector.inspectorPanel

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

Resource explorer APIs live in the independent `korafx-resource-explorer` artifact under `dev.korafx.resourceexplorer`.

```kotlin
import dev.korafx.resourceexplorer.resourceExplorer

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
