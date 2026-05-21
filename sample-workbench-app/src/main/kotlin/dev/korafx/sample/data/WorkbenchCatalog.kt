package dev.korafx.sample.data

import dev.korafx.components.ComponentTone
import dev.korafx.sample.domain.ActivityEvent
import dev.korafx.sample.domain.EditableModule
import dev.korafx.sample.domain.ExplorerResource
import dev.korafx.sample.domain.ModuleSummary
import dev.korafx.sample.domain.SourceSnippet
import java.time.LocalDate

interface WorkbenchCatalog {
    val moduleSummaries: List<ModuleSummary>
    val editableModules: List<EditableModule>
    val explorerResources: List<ExplorerResource>
    val activityEvents: List<ActivityEvent>
    val sourceSnippets: List<SourceSnippet>
    val dslModeOptions: List<String>
    val dslRuntimeOptions: List<String>
    val initialProjectName: String
    val initialDslMode: String
    val initialDslRuntime: String
    val initialDslParallelism: Int
    val initialDslTargetDate: LocalDate
}

class InMemoryWorkbenchCatalog : WorkbenchCatalog {
    override val moduleSummaries = listOf(
        ModuleSummary("korafx-dsl", "Kotlin-first JavaFX construction API"),
        ModuleSummary("korafx-framework", "Koin-backed MVVM, navigation and theme services"),
        ModuleSummary("korafx-navigation", "Path route navigation, history and restoration helpers"),
        ModuleSummary("korafx-command-palette", "Independent advanced command palette and command host surfaces"),
        ModuleSummary("korafx-components", "Optional base JavaFX workbench components, workspace layout and tab workspace"),
        ModuleSummary("korafx-data-grid", "Independent advanced data grid and editable table surfaces"),
        ModuleSummary("korafx-graph-editor", "Independent advanced graph editing surfaces"),
        ModuleSummary("korafx-inspector-panel", "Independent advanced inspector and property panel surfaces"),
        ModuleSummary("korafx-resource-explorer", "Independent advanced resource tree explorer surfaces"),
        ModuleSummary("korafx-source-editor", "Independent advanced source/code/query editor surfaces"),
        ModuleSummary("korafx-virtual-list", "Independent advanced virtualized list, table and terminal surfaces"),
    )

    override val editableModules = listOf(
        EditableModule("DSL", "Core", "Ready"),
        EditableModule("Theme", "Design", "Review"),
        EditableModule("Components", "Product", "Draft"),
    )

    override val explorerResources = listOf(
        ExplorerResource(
            "Repository",
            listOf(
                ExplorerResource(
                    "src",
                    listOf(
                        ExplorerResource("Main.kt"),
                        ExplorerResource("Theme.kt"),
                    ),
                ),
                ExplorerResource("README.md"),
            ),
        ),
        ExplorerResource(
            "Database",
            listOf(
                ExplorerResource(
                    "public",
                    listOf(
                        ExplorerResource("users"),
                        ExplorerResource("orders"),
                    ),
                ),
                ExplorerResource("analytics"),
            ),
        ),
    )

    override val activityEvents = listOf(
        ActivityEvent(
            title = "Commit 4a18c2",
            message = "Refined JavaFX theme coverage for selection controls.",
            time = "09:12",
            group = "Git",
            tone = ComponentTone.SUCCESS,
        ),
        ActivityEvent(
            title = "Query finished",
            message = "select name, owner, status from modules returned 3 rows.",
            time = "09:20",
            group = "Database",
            tone = ComponentTone.INFO,
        ),
        ActivityEvent(
            title = "Migration warning",
            message = "Index modules_owner_idx is missing in the sample schema.",
            time = "09:32",
            group = "Database",
            tone = ComponentTone.WARNING,
        ),
    )

    override val sourceSnippets = listOf(
        SourceSnippet(
            id = "framework-entry",
            module = "Framework",
            title = "Application entry",
            description = "The framework entry owns window configuration, DI modules, theme presets and app content.",
            language = "kotlin",
            routeIds = setOf("overview", "framework"),
            code = """
                fun main(args: Array<String>) = koraApplication(args) {
                    window {
                        title = "KoraFX Workbench"
                        width = 1120.0
                        height = 720.0
                    }

                    installKoin {
                        modules(workbenchModule())
                    }

                    theme {
                        presets(BuiltInThemes.all)
                        default(BuiltInThemes.MaterialLight)
                    }

                    navigation {
                        initialRoute = WorkbenchRoute.Overview
                        routes(WorkbenchRoute.all)
                    }

                    content {
                        WorkbenchRootView(WorkbenchAppGraph.from(this)).buildRoot()
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "dsl-stateful-layout",
            module = "DSL",
            title = "State-bound JavaFX layout",
            description = "Compose normal controls and bind their text/value directly from StateFlow.",
            language = "kotlin",
            routeIds = setOf("overview", "dsl"),
            code = """
                val projectName = MutableStateFlow("KoraFX")

                vbox(spacing = 12.0) {
                    label("Project")
                    textField(projectName.value) {
                        textProperty().addListener { _, _, value ->
                            projectName.value = value
                        }
                    }
                    label().stateText(uiScope, projectName) { "Editing ${'$'}it" }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "mvvm-action-state",
            module = "MVVM",
            title = "ViewModel action and state",
            description = "Keep UI events as actions and expose renderable state through the ViewModel.",
            language = "kotlin",
            routeIds = setOf("overview", "mvvm"),
            code = """
                data class CounterState(val count: Int) : ViewState

                sealed interface CounterAction : UiAction {
                    data object Increment : CounterAction
                }

                class CounterViewModel : ViewModel<CounterState, CounterAction, UiEvent>(
                    CounterState(count = 0),
                ) {
                    override fun onAction(action: CounterAction) {
                        if (action is CounterAction.Increment) {
                            updateState { it.copy(count = it.count + 1) }
                        }
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "theme-scene-controller",
            module = "Theme",
            title = "Scene theme wiring",
            description = "Bind generated theme stylesheets once and drive changes through ThemeManager.",
            language = "kotlin",
            routeIds = setOf("overview", "theme"),
            code = """
                val themeManager = ThemeManager()
                val themeController = SceneThemeController(themeManager)

                stage.scene = Scene(root)
                themeController.bind(stage.scene)

                ghostButton("Next Theme") {
                    onAction { themeManager.nextTheme() }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "navigation-route-host",
            module = "Navigation",
            title = "Route navigation",
            description = "Use typed routes for tabs, rails, command palette actions and deep links.",
            language = "kotlin",
            routeIds = setOf("overview", "navigation", "components"),
            code = """
                sealed class AppRoute(
                    override val id: String,
                    override val path: String,
                ) : PathRoute {
                    data object Home : AppRoute("home", "/")
                    data object Settings : AppRoute("settings", "/settings")
                }

                val navigator = Navigator(AppRoute.Home, listOf(AppRoute.Home, AppRoute.Settings))
                navigator.navigatePath("/settings")
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "components-surface",
            module = "Components",
            title = "Surface composition",
            description = "Base components provide consistent page headers, cards, actions and semantic badges.",
            language = "kotlin",
            routeIds = setOf("components"),
            code = """
                pageHeader(
                    title = "Modules",
                    subtitle = "Reusable surface components for desktop tools.",
                    eyebrow = "korafx-components",
                )

                section("Actions") {
                    actionBar(alignEnd = false) {
                        button("Run")
                        ghostButton("Cancel")
                        badge("Ready", ComponentTone.SUCCESS)
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "source-editor-diagnostics",
            module = "SourceEditor",
            title = "Editor diagnostics",
            description = "Attach diagnostics and editor actions to a source preview without browser embedding.",
            language = "kotlin",
            routeIds = setOf("components", "source-editor"),
            code = """
                sourceEditor(
                    text = "fun main() = println(\"KoraFX\")",
                    language = "kotlin",
                ) {
                    diagnostics(
                        SourceDiagnostic(1, 14, "Replace preview target.", ComponentTone.INFO),
                    )
                    action("Format") {
                        println("Format current source")
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "data-grid-actions",
            module = "DataGrid",
            title = "Grid selection and snapshots",
            description = "Configure typed columns, selection summaries and snapshot actions.",
            language = "kotlin",
            routeIds = setOf("components", "data-grid"),
            code = """
                dataGrid(modules) {
                    constrainedResize()
                    readOnlyTextColumn("Name") { it.name }
                    readOnlyTextColumn("Owner") { it.owner }
                    readOnlyTextColumn("Status") { it.status }

                    selectionSummary()
                    toolbarSnapshotAction("Copy Selected", selectedOnly = true) { snapshot ->
                        val text = snapshot.toDelimitedText()
                        clipboard.putString(text)
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "resource-explorer-open",
            module = "ResourceExplorer",
            title = "Resource tree open handler",
            description = "Expose project resources as a typed tree and open selected files into tabs.",
            language = "kotlin",
            routeIds = setOf("components", "resource-explorer"),
            code = """
                resourceExplorer(resources) {
                    children { resource -> resource.children }
                    text { resource -> resource.name }
                    secondaryText { resource ->
                        if (resource.children.isEmpty()) "file" else "${'$'}{resource.children.size} items"
                    }
                    rowAction { resource ->
                        tabs.open(resource.name, dirty = false)
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "workspace-tabs",
            module = "Workspace",
            title = "Tabbed workspace",
            description = "Use TabWorkspace for files, query tabs, diff views and resource previews.",
            language = "kotlin",
            routeIds = setOf("workspace"),
            code = """
                tabWorkspace(emptyText = "Open a file...") {
                    tab("readme", "README.md", closable = false, select = true) {
                        sourceEditor(
                            title = "README.md",
                            text = "# KoraFX",
                            language = "markdown",
                            readOnly = true,
                        )
                    }

                    tab("query", "Query.sql", dirty = true) {
                        queryEditor(text = "select * from modules;")
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "inspector-properties",
            module = "InspectorPanel",
            title = "Inspector metadata",
            description = "InspectorPanel is designed for selected rows, graph nodes and resource metadata.",
            language = "kotlin",
            routeIds = setOf("inspector-panel"),
            code = """
                inspectorPanel(
                    title = "Selected module",
                    subtitle = "Metadata",
                ) {
                    badge("Advanced", ComponentTone.INFO)
                    property("Artifact", "korafx-inspector-panel")
                    property("Route", "/components/inspector-panel")

                    actions {
                        action("Open") {
                            navigator.navigatePath("/components/inspector-panel")
                        }
                    }
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "command-palette-host",
            module = "CommandPalette",
            title = "Command palette host",
            description = "Register command objects once and show the host from toolbar shortcuts or key bindings.",
            language = "kotlin",
            routeIds = setOf("command-palette"),
            code = """
                val host = CommandPaletteHost(
                    listOf(
                        CommandPaletteCommand(
                            id = "theme.next",
                            title = "Next Theme",
                            group = "Theme",
                        ) {
                            themeManager.nextTheme()
                        },
                    ),
                )

                button("Commands") {
                    onAction { host.show() }
                }
                commandPalette(host)
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "graph-editor-model",
            module = "GraphEditor",
            title = "Graph editor model",
            description = "Build nodes and edges declaratively, then let the editor manage interaction.",
            language = "kotlin",
            routeIds = setOf("components", "graph-editor"),
            code = """
                graphEditor {
                    val viewModel = node("view-model", "ViewModel", x = 80.0, y = 80.0)
                    val catalog = node("catalog", "Catalog", x = 320.0, y = 80.0)
                    val ui = node("ui", "JavaFX UI", x = 200.0, y = 220.0)

                    edge(catalog, viewModel, "feeds")
                    edge(viewModel, ui, "state")
                }
            """.trimIndent(),
        ),
        SourceSnippet(
            id = "virtualized-surfaces",
            module = "Virtualization",
            title = "Virtualized list, table and terminal",
            description = "Render large feeds, paged tables and append-only terminal output without loading everything into custom nodes.",
            language = "kotlin",
            routeIds = setOf("components", "virtual-list"),
            code = """
                virtualList(
                    dataLoader = { offset, limit ->
                        events.drop(offset.toInt()).take(limit)
                    },
                    totalCountEstimate = { events.size },
                ) {
                    item {
                        text(item.title)
                        text(item.message)
                    }
                    onSelect { selected ->
                        status.text = selected.firstOrNull()?.title ?: "Nothing selected"
                    }
                }

                virtualTable<ProcessRow>(
                    dataLoader = { offset, limit -> repository.page(offset, limit) },
                    totalCountEstimate = { repository.estimateCount() },
                    pageSize = 100,
                ) {
                    constrainedResize()
                    textColumn("PID", valueOf = { it.pid })
                    textColumn("Name", valueOf = { it.name })
                    textColumn("CPU", valueOf = { it.cpu })
                }

                val terminal = virtualTerminal(maxLines = 2_000, autoScroll = true) {
                    line("[00:00:01] connected", "terminal-success")
                }
                terminal.appendLine("[00:00:02] streamed log row")
            """.trimIndent(),
        ),
    )

    override val dslModeOptions = listOf("DSL First", "MVVM Ready", "Component Polish")
    override val dslRuntimeOptions = listOf("Manual JavaFX", "Custom Factory", "External DI")
    override val initialProjectName = "KoraFX"
    override val initialDslMode = "DSL First"
    override val initialDslRuntime = "Manual JavaFX"
    override val initialDslParallelism = 2
    override val initialDslTargetDate: LocalDate = LocalDate.now().plusWeeks(1)
}
