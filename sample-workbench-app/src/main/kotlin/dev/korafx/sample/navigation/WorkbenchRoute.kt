package dev.korafx.sample.navigation

import dev.korafx.navigation.PathRoute
import dev.korafx.sample.domain.ModuleCategory
import dev.korafx.sample.domain.ModuleShowcase
import dev.korafx.sample.domain.SourceSnippet

data class WorkbenchRoute(
    override val id: String,
    override val title: String,
    override val path: String,
    val summary: String,
    val documentResource: String,
    val moduleId: String? = id.takeUnless { it == "overview" },
) : PathRoute {
    companion object {
        val moduleDirectory: List<ModuleShowcase> =
            listOf(
                ModuleShowcase(
                    id = "framework",
                    title = "Framework",
                    category = ModuleCategory.CORE,
                    summary = "Application bootstrapping, lifecycle, DI integration and workbench-level service wiring.",
                    routePath = "/framework",
                    documentResource = "dev/korafx/sample/docs/framework.md",
                    artifactName = "korafx-framework",
                    tags = listOf("application", "lifecycle", "koin"),
                    sourceSnippets = listOf(
                        SourceSnippet(
                            id = "framework-application-entry",
                            module = "Framework",
                            title = "Application entry",
                            description = "Install application services and modules from the framework entry point.",
                            language = "kotlin",
                            routeIds = setOf("framework"),
                            code = "koraApplication(args) { installKoin { modules(appModule()) } }",
                        ),
                    ),
                ),
                ModuleShowcase(
                    id = "dsl",
                    title = "DSL",
                    category = ModuleCategory.CORE,
                    summary = "Kotlin-first builders for JavaFX layouts, controls and Flow-backed bindings.",
                    routePath = "/dsl",
                    documentResource = "dev/korafx/sample/docs/dsl.md",
                    artifactName = "korafx-dsl",
                    tags = listOf("layout", "controls", "binding"),
                ),
                ModuleShowcase(
                    id = "mvvm",
                    title = "MVVM",
                    category = ModuleCategory.CORE,
                    summary = "StateFlow-driven ViewModel primitives for deterministic UI state, actions and events.",
                    routePath = "/mvvm",
                    documentResource = "dev/korafx/sample/docs/mvvm.md",
                    artifactName = "korafx-framework",
                    tags = listOf("stateflow", "actions", "events"),
                ),
                ModuleShowcase(
                    id = "theme",
                    title = "Theme",
                    category = ModuleCategory.CORE,
                    summary = "Typed design tokens and selectable JavaFX stylesheet presets.",
                    routePath = "/theme",
                    documentResource = "dev/korafx/sample/docs/theme.md",
                    artifactName = "korafx-framework",
                    tags = listOf("tokens", "css", "presets"),
                ),
                ModuleShowcase(
                    id = "navigation",
                    title = "Navigation",
                    category = ModuleCategory.CORE,
                    summary = "PathRoute navigation, history, query/hash support and route-scoped restoration helpers.",
                    routePath = "/navigation",
                    documentResource = "dev/korafx/sample/docs/navigation.md",
                    artifactName = "korafx-navigation",
                    tags = listOf("routes", "history", "restoration"),
                ),
                ModuleShowcase(
                    id = "components",
                    title = "Components",
                    category = ModuleCategory.CORE,
                    summary = "Optional base workbench surfaces such as cards, toolbars, banners, sections and status bars.",
                    routePath = "/components",
                    documentResource = "dev/korafx/sample/docs/components.md",
                    artifactName = "korafx-components",
                    tags = listOf("surface", "layout", "feedback"),
                ),
                advancedComponent(
                    id = "source-editor",
                    title = "Source Editor",
                    summary = "Code, source and query editor surfaces with diagnostics and read-only preview modes.",
                    artifactName = "korafx-source-editor",
                    tags = listOf("code", "diagnostics", "query"),
                    documentResource = "dev/korafx/sample/docs/source-editor.md",
                ),
                advancedComponent(
                    id = "data-grid",
                    title = "Data Grid",
                    summary = "Editable table and data grid patterns for module catalogs and tabular resources.",
                    artifactName = "korafx-data-grid",
                    tags = listOf("table", "editing", "records"),
                    documentResource = "dev/korafx/sample/docs/data-grid.md",
                ),
                advancedComponent(
                    id = "resource-explorer",
                    title = "Resource Explorer",
                    summary = "Tree-backed resource browsing with status metadata and workspace open actions.",
                    artifactName = "korafx-resource-explorer",
                    tags = listOf("tree", "resources", "workspace"),
                    documentResource = "dev/korafx/sample/docs/resource-explorer.md",
                ),
                advancedComponent(
                    id = "workspace",
                    title = "Workspace",
                    summary = "Workspace layout and tabbed workbench surfaces for multi-document flows.",
                    artifactName = "korafx-components",
                    tags = listOf("tabs", "documents", "workspace"),
                    documentResource = "dev/korafx/sample/docs/workspace.md",
                ),
                advancedComponent(
                    id = "inspector-panel",
                    title = "Inspector Panel",
                    summary = "Property and metadata inspection panels for selected resources, rows or graph nodes.",
                    artifactName = "korafx-inspector-panel",
                    tags = listOf("properties", "selection", "metadata"),
                    documentResource = "dev/korafx/sample/docs/inspector-panel.md",
                ),
                advancedComponent(
                    id = "command-palette",
                    title = "Command Palette",
                    summary = "Searchable command launcher and command host integration for workbench actions.",
                    artifactName = "korafx-command-palette",
                    tags = listOf("commands", "search", "launcher"),
                    documentResource = "dev/korafx/sample/docs/command-palette.md",
                ),
                advancedComponent(
                    id = "graph-editor",
                    title = "Graph Editor",
                    summary = "Advanced node and edge editing showcase route reserved for graph-oriented workflows.",
                    artifactName = "korafx-graph-editor",
                    tags = listOf("graph", "nodes", "edges"),
                    documentResource = "dev/korafx/sample/docs/graph-editor.md",
                ),
                advancedComponent(
                    id = "virtual-list",
                    title = "Virtualization",
                    summary = "Virtualized list, table and terminal surfaces for large data and live logs.",
                    artifactName = "korafx-virtual-list",
                    tags = listOf("virtualization", "large-data", "terminal"),
                    documentResource = "dev/korafx/sample/docs/virtual-list.md",
                ),
            )

        val Overview = WorkbenchRoute(
            id = "overview",
            title = "Overview",
            path = "/",
            summary = "This workbench is the complete demo application for KoraFX DSL, MVVM, theme and component modules.",
            documentResource = "dev/korafx/sample/docs/overview.md",
            moduleId = null,
        )

        val Framework = routeFor("framework")
        val Dsl = routeFor("dsl")
        val Mvvm = routeFor("mvvm")
        val Theme = routeFor("theme")
        val Navigation = routeFor("navigation")
        val Components = routeFor("components")
        val SourceEditor = routeFor("source-editor")
        val DataGrid = routeFor("data-grid")
        val ResourceExplorer = routeFor("resource-explorer")
        val Workspace = routeFor("workspace")
        val InspectorPanel = routeFor("inspector-panel")
        val CommandPalette = routeFor("command-palette")
        val GraphEditor = routeFor("graph-editor")
        val VirtualList = routeFor("virtual-list")

        val moduleRoutes: List<WorkbenchRoute> = moduleDirectory.map(::fromModule)
        val all: List<WorkbenchRoute> = listOf(Overview) + moduleRoutes

        fun findModule(moduleId: String): ModuleShowcase? =
            moduleDirectory.firstOrNull { it.id == moduleId }

        fun findRoute(routeId: String): WorkbenchRoute? =
            all.firstOrNull { it.id == routeId }

        private fun routeFor(moduleId: String): WorkbenchRoute =
            fromModule(requireNotNull(findModule(moduleId)) { "Unknown module: $moduleId" })

        private fun fromModule(module: ModuleShowcase): WorkbenchRoute =
            WorkbenchRoute(
                id = module.id,
                title = module.title,
                path = module.routePath,
                summary = module.summary,
                documentResource = module.documentResource,
                moduleId = module.id,
            )

        private fun advancedComponent(
            id: String,
            title: String,
            summary: String,
            artifactName: String,
            tags: List<String>,
            documentResource: String,
        ): ModuleShowcase =
            ModuleShowcase(
                id = id,
                title = title,
                category = ModuleCategory.ADVANCED_COMPONENT,
                summary = summary,
                routePath = "/components/$id",
                documentResource = documentResource,
                artifactName = artifactName,
                tags = tags,
                sourceSnippets = listOf(
                    SourceSnippet(
                        id = "$id-route-action",
                        module = title.replace(" ", ""),
                        title = "$title route",
                        description = "Navigate to the $title showcase route from any command or module list.",
                        language = "kotlin",
                        routeIds = setOf(id, "components"),
                        code = "viewModel.dispatch(WorkbenchAction.NavigateModule(\"$id\"))",
                    ),
                ),
            )
    }
}
