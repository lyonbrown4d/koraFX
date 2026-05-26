package dev.korafx.sample.ui

import dev.korafx.commandpalette.commandPalette
import dev.korafx.components.ToastHost
import dev.korafx.components.appShell
import dev.korafx.components.appToolbar
import dev.korafx.components.emptyState
import dev.korafx.components.pageHeader
import dev.korafx.components.setKoraIcon
import dev.korafx.components.statusBar
import dev.korafx.components.statusItem
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.bindSelectedItem
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.growVertical
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.panel
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.stateList
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.tabPane
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.bindContentWithTransition
import dev.korafx.sample.di.WorkbenchAppGraph
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.ui.pages.WorkbenchPageContext
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sample.viewmodel.WorkbenchState
import javafx.geometry.Insets
import javafx.geometry.Pos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class WorkbenchRootView(
    private val graph: WorkbenchAppGraph,
) {
    private val dslProjectName = MutableStateFlow(graph.catalog.initialProjectName)
    private val uiScope = graph.uiScope
    private val themeManager = graph.themeManager
    private val navigator = graph.navigator
    private val viewModel = graph.viewModel
    private val commandPaletteHost = graph.commandPaletteHost
    private val transitionMode = MutableStateFlow(WorkbenchTransitionMode.Fade)
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
            navigation { workbenchModuleSidebar(uiScope, navigator) }
            content { mainContent() }
            details {
                detailsPane().apply {
                    minWidth = 360.0
                    prefWidth = 440.0
                }
            }
            footer { statusFooter() }
            overlay(alignment = Pos.CENTER, margin = Insets.EMPTY) {
                commandPalette(commandPaletteHost) {
                    emptyState("No commands match the current search.")
                }
            }
        }

    private fun mainContent() =
        scrollPane(
            init = {
                isFitToWidth = true
            },
        ) {
            content {
                showcasePane()
            }
        }

    private fun detailsPane() =
        tabPane {
            tab("Documentation", closable = false) {
                documentationPane()
            }
            tab("Source", closable = false) {
                sourceCodePane()
            }
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

                comboBox(
                    items = WorkbenchTransitionMode.entries.toList(),
                    init = {
                        prefWidth = 180.0
                    },
                ) {
                    render { it.label }
                    onSelect { mode ->
                        transitionMode.value = mode ?: WorkbenchTransitionMode.Fade
                    }
                    select(transitionMode.value)
                }
            },
        )

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
                    container.bindContentWithTransition(
                        scope = uiScope,
                        state = viewModel.state,
                        transition = transitionMode.map { it.transition },
                    ) { state ->
                        renderShowcase(state)
                    }
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
                    container.bindContentWithTransition(
                        scope = uiScope,
                        state = viewModel.state,
                        transition = transitionMode.map { it.transition },
                    ) { state ->
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
                    container.bindContentWithTransition(
                        scope = uiScope,
                        state = viewModel.state,
                        transition = transitionMode.map { it.transition },
                    ) { state ->
                        renderWorkbenchSourceCode(state)
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

        renderWorkbenchModuleBadges(module)
        renderWorkbenchRoute(state, pageContext)
    }
}
