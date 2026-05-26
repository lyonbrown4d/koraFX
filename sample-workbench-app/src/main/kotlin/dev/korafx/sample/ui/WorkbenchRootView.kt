package dev.korafx.sample.ui

import dev.korafx.commandpalette.commandPalette
import dev.korafx.components.ComponentTone
import dev.korafx.components.appShell
import dev.korafx.components.appToolbar
import dev.korafx.components.badge
import dev.korafx.components.chip
import dev.korafx.components.emptyState
import dev.korafx.components.pageHeader
import dev.korafx.components.section
import dev.korafx.components.setKoraIcon
import dev.korafx.components.statusBar
import dev.korafx.components.statusItem
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.bindContent
import dev.korafx.dsl.bindSelectedItem
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.growVertical
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.hbox
import dev.korafx.dsl.onAction
import dev.korafx.dsl.panel
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.splitPane
import dev.korafx.dsl.stateList
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.vbox
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.routeButton
import dev.korafx.sample.di.WorkbenchAppGraph
import dev.korafx.sample.domain.ModuleCategory
import dev.korafx.sample.domain.SourceSnippet
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.ui.pages.WorkbenchPageContext
import dev.korafx.sample.ui.pages.commandPalettePage
import dev.korafx.sample.ui.pages.componentsPage
import dev.korafx.sample.ui.pages.dataGridPage
import dev.korafx.sample.ui.pages.dslPage
import dev.korafx.sample.ui.pages.frameworkPage
import dev.korafx.sample.ui.pages.graphEditorPage
import dev.korafx.sample.ui.pages.inspectorPage
import dev.korafx.sample.ui.pages.mvvmPage
import dev.korafx.sample.ui.pages.navigationPage
import dev.korafx.sample.ui.pages.overviewPage
import dev.korafx.sample.ui.pages.resourceExplorerPage
import dev.korafx.sample.ui.pages.sourceEditorPage
import dev.korafx.sample.ui.pages.themePage
import dev.korafx.sample.ui.pages.virtualListPage
import dev.korafx.sample.ui.pages.workspacePage
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sample.viewmodel.WorkbenchState
import dev.korafx.sourceeditor.codeEditor
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.SplitPane
import kotlinx.coroutines.flow.MutableStateFlow

class WorkbenchRootView(
    private val graph: WorkbenchAppGraph,
) {
    private val dslProjectName = MutableStateFlow(graph.catalog.initialProjectName)
    private val uiScope = graph.uiScope
    private val themeManager = graph.themeManager
    private val navigator = graph.navigator
    private val viewModel = graph.viewModel
    private val commandPaletteHost = graph.commandPaletteHost
    private val pageContext = WorkbenchPageContext(
        uiScope = graph.uiScope,
        catalog = graph.catalog,
        themeManager = graph.themeManager,
        navigator = graph.navigator,
        viewModel = graph.viewModel,
        commandPaletteHost = graph.commandPaletteHost,
        dslProjectName = dslProjectName,
    )

    fun buildRoot() =
        appShell(
            init = {
                overlayLayer.isMouseTransparent = !commandPaletteHost.isVisible
                commandPaletteHost.visibleProperty.addListener { _, _, visible ->
                    overlayLayer.isMouseTransparent = !visible
                }
            },
        ) {
            topBar { this@WorkbenchRootView.topBar() }
            content { mainContent() }
            footer { statusFooter() }
            overlay(alignment = Pos.CENTER, margin = Insets.EMPTY) {
                commandPalette(commandPaletteHost) {
                    emptyState("No commands match the current search.")
                }
            }
        }

    private fun mainContent() =
        splitPane(
            orientation = Orientation.HORIZONTAL,
            init = {
                setDividerPositions(0.24)
                minWidth = 760.0
                minHeight = 520.0
            },
        ) {
            val sidebar = moduleSidebar()
            val workArea =
                splitPane(
                    orientation = Orientation.VERTICAL,
                    init = {
                        setDividerPositions(0.64)
                        minWidth = 420.0
                    },
                ) {
                    add(showcasePane())
                    add(docsAndSourcePane())
                }

            SplitPane.setResizableWithParent(sidebar, false)
            SplitPane.setResizableWithParent(workArea, true)
            add(sidebar)
            add(workArea)
        }

    private fun statusFooter() =
        statusBar {
        }.stateList(
            scope = uiScope,
            state = viewModel.state,
            items = { it.statusItems },
        ) { item ->
            statusItem(item)
        }

    private fun topBar() =
        appToolbar(
            title = "KoraFX",
            subtitle = "Module showcase",
            icon = WorkbenchIcons.Stable,
            actions = {
                comboBox<KoraTheme>(
                    items = themeManager.availableThemes,
                    init = {
                        prefWidth = 190.0
                    },
                ) {
                    render { it.displayName }
                    onSelect { theme ->
                        if (theme != null) {
                            viewModel.dispatch(WorkbenchAction.SelectTheme(theme.id))
                        }
                    }
                }.bindSelectedItem(uiScope, themeManager.theme)

                ghostButton("Next Theme") {
                    setKoraIcon(WorkbenchIcons.NextTheme)
                    onAction {
                        viewModel.dispatch(WorkbenchAction.NextTheme)
                    }
                }

                ghostButton("Commands") {
                    setKoraIcon(WorkbenchIcons.Commands)
                    onAction {
                        commandPaletteHost.show()
                    }
                }
            },
        )

    private fun moduleSidebar() =
        scrollPane(
            init = {
                prefWidth = 300.0
                minWidth = 220.0
                maxWidth = 480.0
                isFitToWidth = true
            },
        ) {
            content {
                vbox(
                    spacing = 12.0,
                    init = {
                        styleClass += "korafx-workbench-module-nav"
                    },
                ) {
                    label("Modules") {
                        styleClasses(ThemeStyleClass.Headline)
                    }

                    add(
                        routeButton(
                            scope = uiScope,
                            navigator = navigator,
                            route = WorkbenchRoute.Overview,
                            text = "Overview",
                        ) {
                            maxWidth = Double.MAX_VALUE
                        },
                    )

                    ModuleCategory.entries.forEach { category ->
                        label(category.title) {
                            styleClasses(ThemeStyleClass.Muted)
                        }

                        WorkbenchRoute.moduleRoutes
                            .filter { route -> WorkbenchRoute.findModule(route.id)?.category == category }
                            .forEach { route ->
                                add(
                                    routeButton(
                                        scope = uiScope,
                                        navigator = navigator,
                                        route = route,
                                        text = route.title,
                                    ) {
                                        maxWidth = Double.MAX_VALUE
                                    },
                                )
                            }
                    }
                }
            }
        }

    private fun showcasePane() =
        scrollPane(
            init = {
                isFitToWidth = true
                minHeight = 260.0
                maxWidth = Double.MAX_VALUE
            },
        ) {
            content {
                panel(
                    spacing = 18.0,
                    padding = 22.0,
                    init = {
                        maxWidth = Double.MAX_VALUE
                    },
                ) {
                }.also { container ->
                    container.growVertical()
                    container.bindContent(uiScope, viewModel.state) { state ->
                        renderShowcase(state)
                    }
                }
            }
        }

    private fun docsAndSourcePane() =
        scrollPane(
            init = {
                isFitToWidth = true
                minHeight = 180.0
                maxWidth = Double.MAX_VALUE
            },
        ) {
            content {
                splitPane(
                    orientation = Orientation.HORIZONTAL,
                    init = {
                        setDividerPositions(0.5)
                    },
                ) {
                    add(documentationPane())
                    add(sourceCodePane())
                }
            }
        }

    private fun documentationPane() =
        scrollPane(
            init = {
                isFitToWidth = true
            },
        ) {
            content {
                panel(
                    spacing = 12.0,
                    padding = 18.0,
                    init = {
                        maxWidth = Double.MAX_VALUE
                    },
                ) {
                    label("Documentation") {
                        styleClasses(ThemeStyleClass.Headline)
                    }
                }.also { container ->
                    container.growVertical()
                    container.bindContent(uiScope, viewModel.state) { state ->
                        markdownDocument(state.document)
                    }
                }
            }
        }

    private fun sourceCodePane() =
        scrollPane(
            init = {
                isFitToWidth = true
            },
        ) {
            content {
                panel(
                    spacing = 12.0,
                    padding = 18.0,
                    init = {
                        maxWidth = Double.MAX_VALUE
                    },
                ) {
                    label("Source Code") {
                        styleClasses(ThemeStyleClass.Headline)
                    }
                }.also { container ->
                    container.growVertical()
                    container.bindContent(uiScope, viewModel.state) { state ->
                        renderSourceCode(state)
                    }
                }
            }
        }

    private fun NodeContainerBuilder.renderShowcase(state: WorkbenchState) {
        val module = state.currentModule
        val title = module?.title ?: "Overview"
        val artifact = module?.artifactName ?: "sample-workbench-app"

        pageHeader(
            title = title,
            subtitle = state.summary,
            eyebrow = artifact,
            icon = WorkbenchIcons.route(WorkbenchRoute.findRoute(state.currentRouteId) ?: WorkbenchRoute.Overview),
            actions = {
                ghostButton("Toggle Theme") {
                    onAction {
                        viewModel.dispatch(WorkbenchAction.ToggleTheme)
                    }
                }
            },
        )

        if (module != null) {
            hbox(spacing = 10.0) {
                badge(module.category.title, ComponentTone.INFO)
                module.tags.take(4).forEach { tag ->
                    chip(tag, ComponentTone.NEUTRAL)
                }
            }
        }

        renderRoute(state)
    }

    private fun NodeContainerBuilder.renderRoute(state: WorkbenchState) {
        when (state.currentRouteId) {
            WorkbenchRoute.Framework.id -> frameworkPage(pageContext)
            WorkbenchRoute.Dsl.id -> dslPage(pageContext)
            WorkbenchRoute.Mvvm.id -> mvvmPage(pageContext, state)
            WorkbenchRoute.Theme.id -> themePage(pageContext)
            WorkbenchRoute.Navigation.id -> navigationPage(pageContext, state)
            WorkbenchRoute.Components.id -> componentsPage(pageContext)
            WorkbenchRoute.SourceEditor.id -> sourceEditorPage(pageContext)
            WorkbenchRoute.DataGrid.id -> dataGridPage(pageContext)
            WorkbenchRoute.ResourceExplorer.id -> resourceExplorerPage(pageContext)
            WorkbenchRoute.Workspace.id -> workspacePage(pageContext)
            WorkbenchRoute.InspectorPanel.id -> inspectorPage(pageContext)
            WorkbenchRoute.CommandPalette.id -> commandPalettePage(pageContext)
            WorkbenchRoute.GraphEditor.id -> graphEditorPage()
            WorkbenchRoute.VirtualList.id -> virtualListPage()
            else -> overviewPage(pageContext, state)
        }
    }

    private fun NodeContainerBuilder.renderSourceCode(state: WorkbenchState) {
        val snippets = state.sourceSnippets.ifEmpty {
            listOf(
                SourceSnippet(
                    title = "Navigate to module",
                    language = "kotlin",
                    code = """viewModel.dispatch(WorkbenchAction.NavigateModule("${state.currentRouteId}"))""",
                    description = "Every module in the left rail is a typed KoraFX route.",
                ),
            )
        }

        snippets.forEach { snippet ->
            section(
                title = snippet.title,
                description = snippet.description.takeIf { it.isNotBlank() },
                padding = 12.0,
            ) {
                codeEditor(
                    title = "${snippet.id}.${snippet.language}",
                    text = snippet.code,
                    language = snippet.language,
                    readOnly = true,
                    showSearch = true,
                    wrapText = false,
                    init = {
                        prefHeight = 220.0
                        maxWidth = Double.MAX_VALUE
                    },
                )
            }
        }
    }
}
