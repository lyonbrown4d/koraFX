package dev.korafx.sample.data

import dev.korafx.sample.domain.SourceSnippet

internal fun advancedWorkbenchSourceSnippets(): List<SourceSnippet> =
    listOf(
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
