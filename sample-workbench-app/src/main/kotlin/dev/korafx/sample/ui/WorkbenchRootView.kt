package dev.korafx.sample.ui

import dev.korafx.components.actionBar
import dev.korafx.components.activityTimeline
import dev.korafx.components.alertBanner
import dev.korafx.components.appToolbar
import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.components.borderLayout
import dev.korafx.components.breadcrumb
import dev.korafx.components.breadcrumbItem
import dev.korafx.components.card
import dev.korafx.components.chip
import dev.korafx.commandpalette.commandPalette
import dev.korafx.datagrid.dataGrid
import dev.korafx.components.emptyState
import dev.korafx.components.heroBanner
import dev.korafx.sourceeditor.codeEditor
import dev.korafx.sourceeditor.queryEditor
import dev.korafx.sourceeditor.sourceEditor
import dev.korafx.sourceeditor.SourceDiagnostic
import dev.korafx.inspector.inspectorPanel
import dev.korafx.components.metricCard
import dev.korafx.components.pageHeader
import dev.korafx.resourceexplorer.resourceExplorer
import dev.korafx.components.section
import dev.korafx.components.setKoraIcon
import dev.korafx.components.statusBar
import dev.korafx.components.statusItem
import dev.korafx.navigation.navigationRail
import dev.korafx.navigation.pathButton
import dev.korafx.navigation.pathLink
import dev.korafx.navigation.routeButton
import dev.korafx.navigation.routeLink
import dev.korafx.navigation.routeScrollRestoration
import dev.korafx.navigation.routeSelectionRestoration
import dev.korafx.workspace.TabWorkspace
import dev.korafx.workspace.tabWorkspace
import dev.korafx.workspace.workspaceLayout
import dev.korafx.dsl.accordion
import dev.korafx.dsl.bindInvalid
import dev.korafx.dsl.bindSelectedItem
import dev.korafx.dsl.bindSelectedItemBidirectional
import dev.korafx.dsl.bindTextBidirectional
import dev.korafx.dsl.bindValueBidirectional
import dev.korafx.dsl.choiceBox
import dev.korafx.dsl.colorPicker
import dev.korafx.dsl.comboBox
import dev.korafx.dsl.datePicker
import dev.korafx.dsl.form
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.gridPane
import dev.korafx.dsl.growVertical
import dev.korafx.dsl.hyperlink
import dev.korafx.dsl.intSpinner
import dev.korafx.dsl.listView
import dev.korafx.dsl.menuButton
import dev.korafx.dsl.menuBar
import dev.korafx.dsl.onAction
import dev.korafx.dsl.panel
import dev.korafx.dsl.pagination
import dev.korafx.dsl.passwordField
import dev.korafx.dsl.progressBar
import dev.korafx.dsl.radioButton
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.slider
import dev.korafx.dsl.stateDisable
import dev.korafx.dsl.stateList
import dev.korafx.dsl.stateText
import dev.korafx.dsl.stateVisible
import dev.korafx.dsl.stackPane
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.splitMenuButton
import dev.korafx.dsl.tableView
import dev.korafx.dsl.tabPane
import dev.korafx.dsl.toggleButton
import dev.korafx.dsl.treeView
import dev.korafx.dsl.vbox
import dev.korafx.dsl.workbenchLayout
import dev.korafx.sample.di.WorkbenchAppGraph
import dev.korafx.sample.domain.ExplorerResource
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sample.viewmodel.WorkbenchEvent
import dev.korafx.framework.theme.KoraTheme
import dev.korafx.framework.theme.ThemeStyleClass
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WorkbenchRootView(
  private val graph: WorkbenchAppGraph,
) {
  private val moduleSummaries = graph.catalog.moduleSummaries
  private val editableModules = graph.catalog.editableModules
  private val explorerResources = graph.catalog.explorerResources
  private val activityEvents = graph.catalog.activityEvents
  private val dslProjectName = MutableStateFlow(graph.catalog.initialProjectName)
  private val dslProjectNameError = dslProjectName.map { value ->
    if (value.isBlank()) "Project name is required." else null
  }
  private val dslModeOptions = graph.catalog.dslModeOptions
  private val dslRuntimeOptions = graph.catalog.dslRuntimeOptions
  private val dslMode = MutableStateFlow<String?>(graph.catalog.initialDslMode)
  private val dslRuntime = MutableStateFlow<String?>(graph.catalog.initialDslRuntime)
  private val dslParallelism = MutableStateFlow(graph.catalog.initialDslParallelism)
  private val dslTargetDate = MutableStateFlow<LocalDate?>(graph.catalog.initialDslTargetDate)
  private val uiScope = graph.uiScope
  private val themeManager = graph.themeManager
  private val navigator = graph.navigator
  private val viewModel = graph.viewModel
  private val commandPaletteHost = graph.commandPaletteHost

  fun buildRoot() =
    stackPane {
      add(
        workbenchLayout {
          lateinit var feedbackLabel: Label
          lateinit var workspaceTabs: TabWorkspace

          fun openResourceTab(resource: ExplorerResource) {
            workspaceTabs.openTab(
              id = "resource:${resource.name}",
              title = resource.name,
              dirty = resource.name == "Theme.kt",
            ) {
              sourceEditor(
                title = resource.name,
                text = "// ${resource.name}\n// Opened from ResourceExplorer into TabWorkspace.",
                language = if (resource.name.endsWith(".kt")) "kotlin" else "text",
                readOnly = true,
                diagnostics = listOf(
                  SourceDiagnostic(1, 1, "Opened as a read-only preview.", ComponentTone.INFO),
                ),
              )
            }
            feedbackLabel.text = "State: Opened ${resource.name} in tab workspace."
          }

          topBar {
            appToolbar(
              title = "KoraFX",
              subtitle = "Framework workbench sample",
              icon = WorkbenchIcons.Stable,
              actions = {
                comboBox<KoraTheme>(
                  items = themeManager.availableThemes,
                  init = {
                    prefWidth = 180.0
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
                ghostButton("Toggle Theme") {
                  setKoraIcon(WorkbenchIcons.Theme)
                  onAction {
                    viewModel.dispatch(WorkbenchAction.ToggleTheme)
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
          }

          navigation {
            navigationRail(
              scope = uiScope,
              navigator = navigator,
              icon = WorkbenchIcons::route,
            )
          }

          content {
            scrollPane(
              init = {
                isFitToWidth = true
              },
            ) {
              content {
                panel {
                  label {
                    styleClasses(ThemeStyleClass.Headline)
                  }.stateText(uiScope, viewModel.state) { it.title }
                  label {
                    isWrapText = true
                    styleClasses(ThemeStyleClass.Muted)
                  }.stateText(uiScope, viewModel.state) { it.summary }
                  textArea {
                    isEditable = false
                    prefRowCount = 16
                  }.stateText(uiScope, viewModel.state) { it.document }
                  label {
                    styleClasses(ThemeStyleClass.Muted)
                  }.stateText(uiScope, viewModel.state) { "Theme: ${it.currentThemeName}" }
                  feedbackLabel = label {
                    styleClasses(ThemeStyleClass.Muted)
                  }.stateText(uiScope, viewModel.state) { "State: ${it.feedbackMessage}" }
                  label {
                    styleClasses(ThemeStyleClass.Muted)
                  }.stateText(uiScope, viewModel.events) { event ->
                    when (event) {
                      is WorkbenchEvent.Feedback -> "Last event: ${event.message}"
                    }
                  }
                  vbox(spacing = 16.0) {
                    section(
                      title = "Router Showcase",
                      description = "The workbench uses PathRoute plus Navigator state: route buttons keep active state, path navigation supports query/hash, and scroll or selection restoration is explicit.",
                    ) {
                      pageHeader(
                        title = "Router and Navigation Surface",
                        subtitle = "Breadcrumbs and page headers share the same Material token stylesheet as the route controls.",
                        eyebrow = "Workbench / Navigation",
                        icon = WorkbenchIcons.Route,
                        actions = {
                          button("Open DevTools") {
                            onAction {
                              commandPaletteHost.show()
                            }
                          }
                        },
                      ) {
                        breadcrumb(
                          items = listOf(
                            breadcrumbItem(WorkbenchRoute.Overview.path, "Workbench", icon = WorkbenchIcons.Home),
                            breadcrumbItem(WorkbenchRoute.Components.path, "Components"),
                            breadcrumbItem(navigator.currentLocation.fullPath, "Router", current = true),
                          ),
                          onSelect = { path ->
                            viewModel.dispatch(WorkbenchAction.NavigatePath(path))
                          },
                        )
                      }
                      label {
                        styleClasses(ThemeStyleClass.Muted)
                      }.stateText(uiScope, navigator.state) { state ->
                        "Current location: ${state.currentLocation.fullPath}"
                      }
                      label {
                        styleClasses(ThemeStyleClass.Muted)
                      }.stateText(uiScope, navigator.state) { state ->
                        "History: back=${state.backStack.size}, forward=${state.forwardStack.size}"
                      }

                      hbox(spacing = 8.0) {
                        add(routeButton(uiScope, navigator, WorkbenchRoute.Overview))
                        add(pathButton(uiScope, navigator, WorkbenchRoute.Components.path, text = "Components path"))
                        add(routeLink(uiScope, navigator, WorkbenchRoute.Mvvm))
                        add(pathLink(uiScope, navigator, "${WorkbenchRoute.Theme.path}?preview=palette", text = "Theme query"))
                      }

                      actionBar {
                        ghostButton("Add query") {
                          onAction {
                            uiScope.launch {
                              navigator.navigatePathAsync(
                                navigator.currentLocation.withQuery(
                                  "view" to "quickstart",
                                  "page" to 1,
                                ),
                              )
                            }
                          }
                        }
                        ghostButton("Set hash") {
                          onAction {
                            uiScope.launch {
                              navigator.navigatePathAsync(navigator.currentLocation.withHash("router-showcase"))
                            }
                          }
                        }
                        ghostButton("Back") {
                          onAction {
                            uiScope.launch {
                              navigator.backAsync()
                            }
                          }
                        }
                        ghostButton("Forward") {
                          onAction {
                            uiScope.launch {
                              navigator.forwardAsync()
                            }
                          }
                        }
                      }

                      scrollPane(
                        init = {
                          isFitToWidth = true
                          prefHeight = 180.0
                          routeScrollRestoration(
                            scope = uiScope,
                            navigator = navigator,
                            key = "overview-router-showcase",
                          )
                        },
                      ) {
                        content {
                          vbox(spacing = 10.0) {
                            label("Route list with selection restoration") {
                              styleClasses(ThemeStyleClass.Headline)
                            }
                            listView(
                              items = WorkbenchRoute.all,
                              init = {
                                prefHeight = 130.0
                                routeSelectionRestoration(
                                  scope = uiScope,
                                  navigator = navigator,
                                  key = "overview-route-list",
                                  keyOf = { route -> route.path },
                                )
                              },
                            ) {
                              render { route -> "${route.title} -> ${route.path}" }
                              onSelect { route ->
                                if (route != null) {
                                  viewModel.dispatch(WorkbenchAction.NavigatePath(route.path))
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }.stateVisible(uiScope, viewModel.state) {
                    it.currentRouteId == WorkbenchRoute.Overview.id
                  }
                  vbox(spacing = 16.0) {
                    section(
                      title = "DSL Composition",
                      description = "This route focuses on the base Kotlin DSL: layout builders, control builders, binding helpers and native JavaFX access.",
                    ) {
                      borderLayout(
                        init = {
                          prefHeight = 220.0
                          maxWidth = Double.MAX_VALUE
                        },
                      ) {
                        header {
                          label("borderLayout { header / sidebar / content / footer }") {
                            styleClasses(ThemeStyleClass.Headline)
                          }
                        }
                        sidebar {
                          vbox(spacing = 8.0) {
                            label("Layouts")
                            label("Controls")
                            label("Bindings")
                          }
                        }
                        content {
                          gridPane(hgap = 12.0, vgap = 10.0) {
                            column(prefWidth = 120.0, alignment = HPos.RIGHT)
                            column(grow = Priority.ALWAYS, fillWidth = true)

                            label(0, 0, "TextField")
                            textField(1, 0, "Native JavaFX node") {
                              maxWidth = Double.MAX_VALUE
                            }
                            label(0, 1, "Choice")
                            comboBox(
                              items = dslModeOptions,
                              init = {
                                maxWidth = Double.MAX_VALUE
                              },
                            ) {
                              select(graph.catalog.initialDslMode)
                            }
                          }
                        }
                        footer {
                          label("All slots are still plain JavaFX nodes.")
                        }
                      }
                    }

                    section(
                      title = "State Binding",
                      description = "Bindings are explicit at component property level, so views stay readable without reflection or generated UI models.",
                    ) {
                      form {
                        item(
                          label = "Project",
                          helper = "MutableStateFlow drives text and validation state.",
                        ) {
                          textField {
                            maxWidth = Double.MAX_VALUE
                            bindTextBidirectional(uiScope, dslProjectName)
                            bindInvalid(uiScope, dslProjectNameError.map { it != null })
                          }
                          validationMessage(uiScope, dslProjectNameError)
                        }
                        item(label = "Parallel Work") {
                          intSpinner(
                            min = 1,
                            max = 8,
                            initialValue = dslParallelism.value,
                          ) {
                            isEditable = true
                            bindValueBidirectional(uiScope, dslParallelism)
                          }
                        }
                      }
                    }
                  }.stateVisible(uiScope, viewModel.state) {
                    it.currentRouteId == WorkbenchRoute.Dsl.id
                  }
                  vbox(spacing = 16.0) {
                    menuBar {
                      menu("DSL Actions") {
                        actionItem("Toggle Theme") {
                          viewModel.dispatch(WorkbenchAction.ToggleTheme)
                        }
                        actionItem("Open MVVM Route") {
                          viewModel.dispatch(WorkbenchAction.NavigatePath(WorkbenchRoute.Mvvm.path))
                        }
                        actionItem("Open Components Route") {
                          viewModel.dispatch(WorkbenchAction.NavigatePath(WorkbenchRoute.Components.path))
                        }
                        actionItem("Open Theme Route") {
                          viewModel.dispatch(WorkbenchAction.NavigatePath(WorkbenchRoute.Theme.path))
                        }
                      }
                    }

                    section(
                      title = "Component Gallery",
                      description = "A paged showcase keeps each custom component independent, like a desktop component banner deck.",
                    ) {
                      heroBanner(
                        title = "KoraFX Component Gallery",
                        subtitle = "Flip through focused component pages instead of scanning one oversized demo route.",
                        eyebrow = "Showcase / Hero Banner",
                        icon = WorkbenchIcons.Samples,
                        actions = {
                          ghostButton("Open Commands") {
                            setKoraIcon(WorkbenchIcons.Commands)
                            onAction {
                              commandPaletteHost.show()
                            }
                          }
                        },
                      ) {
                        hbox(spacing = 8.0) {
                          badge("Paged", ComponentTone.PRIMARY, icon = WorkbenchIcons.Route)
                          badge("Independent", ComponentTone.SUCCESS, icon = WorkbenchIcons.Stable)
                          badge("Theme-covered", ComponentTone.INFO, icon = WorkbenchIcons.Theme)
                        }
                        hbox(spacing = 12.0) {
                          metricCard("Advanced modules", "6", "Published as focused artifacts", ComponentTone.PRIMARY, init = {
                            maxWidth = Double.MAX_VALUE
                            HBox.setHgrow(this, Priority.ALWAYS)
                          })
                          metricCard("Showcase pages", "6", "One component family per page", ComponentTone.INFO, init = {
                            maxWidth = Double.MAX_VALUE
                            HBox.setHgrow(this, Priority.ALWAYS)
                          })
                          metricCard("Sample pattern", "MVVM", "Koin + Navigator + DSL", ComponentTone.SUCCESS, init = {
                            maxWidth = Double.MAX_VALUE
                            HBox.setHgrow(this, Priority.ALWAYS)
                          })
                        }
                      }

                      add(
                        pagination(
                          pageCount = 6,
                          init = {
                            maxWidth = Double.MAX_VALUE
                            prefHeight = 620.0
                            styleClass += "component-gallery-pagination"
                          },
                        ) { pageIndex ->
                          panel {
                            when (pageIndex) {
                              0 -> {
                                pageHeader(
                                  title = "Surface Kit",
                                  subtitle = "Base workbench surfaces stay small: card, section, actionBar, badge, chip and metricCard.",
                                  eyebrow = "Page 1 / Base Components",
                                  icon = WorkbenchIcons.Stable,
                                )
                                gridPane(
                                  hgap = 12.0,
                                  vgap = 10.0,
                                ) {
                                  column(prefWidth = 120.0, alignment = HPos.RIGHT)
                                  column(grow = Priority.ALWAYS, fillWidth = true)

                                  label(0, 0, "Project")
                                  textField(1, 0, "KoraFX") {
                                    maxWidth = Double.MAX_VALUE
                                  }
                                  label(0, 1, "Mode")
                                  checkBox(1, 1, "Kotlin-first JavaFX DSL") {
                                    isSelected = true
                                  }
                                }
                                actionBar {
                                  ghostButton("Secondary") {
                                    onAction {
                                      feedbackLabel.text = "State: Secondary action from gallery."
                                    }
                                  }
                                  button("Primary") {
                                    onAction {
                                      feedbackLabel.text = "State: Primary action from gallery."
                                    }
                                  }
                                }
                                hbox(spacing = 8.0) {
                                  badge("Stable", ComponentTone.SUCCESS, icon = WorkbenchIcons.Stable)
                                  badge("Theme", ComponentTone.INFO, icon = WorkbenchIcons.Theme)
                                  chip("DSL", ComponentTone.PRIMARY, selected = true, icon = WorkbenchIcons.Dsl)
                                  chip("Samples", ComponentTone.NEUTRAL, icon = WorkbenchIcons.Samples)
                                }
                              }

                              1 -> {
                                pageHeader(
                                  title = "Source Editor",
                                  subtitle = "CodeEditor now demonstrates line numbers, search, diagnostics and cursor navigation.",
                                  eyebrow = "Page 2 / korafx-source-editor",
                                  icon = WorkbenchIcons.Editor,
                                )
                                sourceEditor(
                                  title = "RepositoryConfig.kt",
                                  text = "data class RepositoryConfig(\n    val branch: String,\n    val remote: String,\n)",
                                  language = "kotlin",
                                  readOnly = true,
                                  showSearch = true,
                                  diagnostics = listOf(
                                    SourceDiagnostic(2, 9, "Click diagnostics to jump to a source position.", ComponentTone.INFO),
                                  ),
                                  init = {
                                    prefHeight = 420.0
                                  },
                                ) {
                                  showSearch("RepositoryConfig")
                                  onDiagnosticSelected { diagnostic ->
                                    feedbackLabel.text = "State: Gallery jumped to ${diagnostic.line}:${diagnostic.column}."
                                  }
                                  action("Open File") {
                                    feedbackLabel.text = "State: Source editor gallery action."
                                  }
                                }
                              }

                              2 -> {
                                pageHeader(
                                  title = "Workspace Shell",
                                  subtitle = "WorkspaceLayout composes navigation, tab workspace, inspector and status surfaces.",
                                  eyebrow = "Page 3 / korafx-workspace",
                                  icon = WorkbenchIcons.Workspace,
                                )
                                workspaceLayout(
                                  init = {
                                    prefHeight = 430.0
                                    maxWidth = Double.MAX_VALUE
                                  },
                                ) {
                                  topBar {
                                    hbox(spacing = 10.0) {
                                      label("Git / Database Workspace") {
                                        styleClasses(ThemeStyleClass.Headline)
                                      }
                                      badge("WorkspaceLayout", ComponentTone.INFO)
                                    }
                                  }
                                  navigation {
                                    resourceExplorer(
                                      items = explorerResources,
                                      childrenOf = { it.children },
                                      textOf = { it.name },
                                      init = {
                                        prefWidth = 240.0
                                        maxHeight = Double.MAX_VALUE
                                      },
                                    ) {
                                      search(prompt = "Search repository or database...")
                                      breadcrumb(separator = " > ")
                                      rowAction { resource ->
                                        openResourceTab(resource)
                                      }
                                    }
                                  }
                                  content {
                                    tabWorkspace(
                                      emptyText = "Open a repository file or database object...",
                                      init = {
                                        workspaceTabs = this
                                      },
                                    ) {
                                      tab("welcome", "Welcome", closable = false, select = true) {
                                        card(spacing = 8.0, padding = 12.0) {
                                          label("TabWorkspace") {
                                            styleClasses(ThemeStyleClass.Headline)
                                          }
                                          label("Double-click an explorer item to open a reusable source preview tab.")
                                        }
                                      }
                                    }
                                  }
                                  details {
                                    inspectorPanel(
                                      title = "Repository",
                                      subtitle = "Selection details for Git and database resources.",
                                    ) {
                                      badge("Connected", ComponentTone.SUCCESS, icon = WorkbenchIcons.Connected)
                                      property("Branch", "main")
                                      property("Connection", "local")
                                    }
                                  }
                                  status {
                                    statusBar {
                                      statusItem("Ready", ComponentTone.SUCCESS, icon = WorkbenchIcons.Connected)
                                      spacer()
                                      statusItem("Gallery workspace", ComponentTone.INFO)
                                    }
                                  }
                                }
                              }

                              3 -> {
                                pageHeader(
                                  title = "Data Grid",
                                  subtitle = "DataGrid is now isolated from base components and can evolve into editable database-style grids.",
                                  eyebrow = "Page 4 / korafx-data-grid",
                                  icon = WorkbenchIcons.Database,
                                )
                                dataGrid(
                                  items = editableModules,
                                  searchPrompt = "Search modules...",
                                  init = {
                                    prefHeight = 420.0
                                    maxWidth = Double.MAX_VALUE
                                  },
                                ) {
                                  search(textOf = { "${it.name} ${it.owner} ${it.status}" })
                                  selectionMode(SelectionMode.MULTIPLE)
                                  selectionSummary()
                                  dirtyRows { it.status == "Draft" }
                                  footer("${editableModules.size} modules - draft rows are marked")
                                  toolbar {
                                    action("Refresh") {
                                      feedbackLabel.text = "State: Data grid gallery refresh."
                                    }
                                    batchAction("Archive selected") { rows ->
                                      feedbackLabel.text = "State: Prepared ${rows.size} module rows for archive."
                                    }
                                  }
                                  constrainedResize()
                                  editableTextColumn("Module", valueOf = { it.name }) { row, value ->
                                    row.name = value
                                    feedbackLabel.text = "State: Gallery renamed module to $value."
                                  }
                                  editableTextColumn("Owner", valueOf = { it.owner }) { row, value ->
                                    row.owner = value
                                    feedbackLabel.text = "State: ${row.name} owner changed to $value."
                                  }
                                  readOnlyTextColumn("Status") { it.status }
                                }
                              }

                              4 -> {
                                pageHeader(
                                  title = "Activity And Tree",
                                  subtitle = "Timeline, TreeView and feedback states cover history, navigation trees and empty/loading/error surfaces.",
                                  eyebrow = "Page 5 / Runtime Surfaces",
                                  icon = WorkbenchIcons.Samples,
                                )
                                activityTimeline(
                                  events = activityEvents,
                                  emptyText = "No activity yet",
                                ) {
                                  groupBy { it.group }
                                  timeOf { it.time }
                                  titleOf { it.title }
                                  messageOf { it.message }
                                  toneOf { it.tone }
                                  action("Open") { event ->
                                    feedbackLabel.text = "State: Open activity ${event.title}."
                                  }
                                }
                                emptyState(
                                  title = "Feedback components",
                                  message = "emptyState, loadingState and errorState are plain JavaFX nodes.",
                                  actionText = "Mark Reviewed",
                                  onAction = {
                                    feedbackLabel.text = "State: Feedback component action from gallery."
                                  },
                                ) {
                                  prefHeight = 140.0
                                }
                              }

                              else -> {
                                pageHeader(
                                  title = "Command Palette",
                                  subtitle = "CommandPalette is a dedicated module for keyboard-first application actions.",
                                  eyebrow = "Page 6 / korafx-command-palette",
                                  icon = WorkbenchIcons.Commands,
                                )
                                alertBanner(
                                  title = "Try the command overlay",
                                  message = "The sample registers commands through Koin-backed app services and renders the palette as an overlay.",
                                  tone = ComponentTone.INFO,
                                  actionText = "Open Commands",
                                  onAction = {
                                  commandPaletteHost.show()
                                  },
                                )
                                card {
                                  label("Registered commands") {
                                    styleClasses(ThemeStyleClass.Headline)
                                  }
                                  label("Toggle theme, navigate routes, and trigger sample feedback from one command surface.")
                                  actionBar(alignEnd = false) {
                                    button("Open Commands") {
                                      setKoraIcon(WorkbenchIcons.Commands)
                                      onAction {
                                        commandPaletteHost.show()
                                      }
                                    }
                                    ghostButton("Go To Theme") {
                                      onAction {
                                        viewModel.dispatch(WorkbenchAction.NavigatePath(WorkbenchRoute.Theme.path))
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        },
                      )
                    }

                    section(
                      title = "Surface Components",
                      description = "section, card and actionBar are lightweight containers built on top of the DSL.",
                    ) {
                      gridPane(
                        hgap = 12.0,
                        vgap = 10.0,
                      ) {
                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                        column(grow = Priority.ALWAYS, fillWidth = true)

                        label(0, 0, "Project")
                        textField(1, 0, "KoraFX") {
                          maxWidth = Double.MAX_VALUE
                        }

                        label(0, 1, "Mode")
                        checkBox(1, 1, "Kotlin-first JavaFX DSL") {
                          isSelected = true
                        }
                      }

                      actionBar {
                        ghostButton("Secondary") {
                          onAction {
                            feedbackLabel.text = "State: Secondary action from actionBar."
                          }
                        }
                        button("Primary") {
                          onAction {
                            feedbackLabel.text = "State: Primary action from actionBar."
                          }
                        }
                      }

                      hbox(spacing = 8.0) {
                        badge("Stable", ComponentTone.SUCCESS, icon = WorkbenchIcons.Stable)
                        badge("Theme", ComponentTone.INFO, icon = WorkbenchIcons.Theme)
                        chip("DSL", ComponentTone.PRIMARY, selected = true, icon = WorkbenchIcons.Dsl)
                        chip("Samples", ComponentTone.NEUTRAL, icon = WorkbenchIcons.Samples) {
                          onAction {
                            feedbackLabel.text = "State: Chip action."
                          }
                        }
                      }
                    }

                    section(
                      title = "Editor Components",
                      description = "CodeEditor adds line numbers, find, cursor status and dirty tracking; SourceEditor and QueryEditor add actions, diagnostics and result slots.",
                    ) {
                      codeEditor(
                        title = "Kotlin Scratch",
                        text = "fun main() {\n    println(\"KoraFX\")\n}",
                        language = "kotlin",
                        placeholder = "Start typing...",
                        showSearch = true,
                        onTextChange = { text ->
                          feedbackLabel.text = "State: Editor changed, ${text.length} chars."
                        },
                      ) {
                        prefHeight = 220.0
                      }
                      sourceEditor(
                        title = "RepositoryConfig.kt",
                        text = "data class RepositoryConfig(\n    val branch: String,\n    val remote: String,\n)",
                        language = "kotlin",
                        readOnly = true,
                        diagnostics = listOf(
                          SourceDiagnostic(2, 9, "Preview mode keeps source read-only.", ComponentTone.INFO),
                        ),
                        init = {
                          prefHeight = 260.0
                        },
                      ) {
                        showSearch("RepositoryConfig")
                        onDiagnosticSelected { diagnostic ->
                          feedbackLabel.text = "State: Jumped to ${diagnostic.line}:${diagnostic.column}."
                        }
                        action("Open File") {
                          feedbackLabel.text = "State: Open source file requested."
                        }
                      }
                      queryEditor(
                        text = "select id, name, owner from modules;",
                        showSearch = true,
                        onRun = { sql ->
                          feedbackLabel.text = "State: Run query with ${sql.length} chars."
                        },
                        onStop = {
                          feedbackLabel.text = "State: Query stopped."
                        },
                        init = {
                          prefHeight = 420.0
                        },
                      ) {
                        diagnostics(
                          listOf(
                            SourceDiagnostic(
                              1,
                              8,
                              "Demo diagnostic: query is not executed against a real database.",
                              ComponentTone.WARNING
                            ),
                          ),
                        )
                        result(
                          title = "Query Result",
                          node = dataGrid(
                            items = editableModules,
                            showSearch = false,
                            init = {
                              prefHeight = 160.0
                              maxWidth = Double.MAX_VALUE
                            },
                          ) {
                            constrainedResize()
                            readOnlyTextColumn("Module") { it.name }
                            readOnlyTextColumn("Owner") { it.owner }
                            readOnlyTextColumn("Status") { it.status }
                          },
                        )
                      }
                    }

                    section(
                      title = "Layout And Data Grid Components",
                      description = "Semantic workbench slots, resource browsing, and searchable editable grids cover common tool surfaces.",
                    ) {
                      workspaceLayout(
                        init = {
                          prefHeight = 260.0
                          maxWidth = Double.MAX_VALUE
                        },
                      ) {
                        topBar {
                          hbox(spacing = 10.0) {
                            label("Git / Database Workspace") {
                              styleClasses(ThemeStyleClass.Headline)
                            }
                            badge("WorkspaceLayout", ComponentTone.INFO)
                          }
                        }
                        navigation {
                          resourceExplorer(
                            items = explorerResources,
                            childrenOf = { it.children },
                            textOf = { it.name },
                            init = {
                              prefWidth = 240.0
                              maxHeight = Double.MAX_VALUE
                            },
                          ) {
                            search(prompt = "Search repository or database...")
                            breadcrumb(separator = " > ")
                            onSelect { resource ->
                              if (resource != null) {
                                feedbackLabel.text = "State: Selected resource ${resource.name}."
                              }
                            }
                            rowAction { resource ->
                              openResourceTab(resource)
                            }
                            contextMenu { resource ->
                              actionItem("Open") {
                                openResourceTab(resource)
                              }
                              actionItem("Inspect") {
                                feedbackLabel.text = "State: Inspect ${resource.name}."
                              }
                            }
                          }
                        }
                        content {
                          tabWorkspace(
                            emptyText = "Open a repository file or database object...",
                            init = {
                              workspaceTabs = this
                            },
                          ) {
                            onSelect { id ->
                              feedbackLabel.text = "State: Selected tab $id."
                            }
                            onClose { id ->
                              feedbackLabel.text = "State: Closed tab $id."
                            }
                            tab("welcome", "Welcome", closable = false, select = true) {
                              card(spacing = 8.0, padding = 12.0) {
                                label("TabWorkspace") {
                                  styleClasses(ThemeStyleClass.Headline)
                                }
                                label("Double-click an explorer item to open a reusable source preview tab.")
                              }
                            }
                            tab("query:modules", "Modules Query", dirty = true) {
                              queryEditor(
                                text = "select name, owner, status from modules;",
                                onRun = { sql ->
                                  feedbackLabel.text = "State: Workspace query run with ${sql.length} chars."
                                },
                              ) {
                                result(
                                  title = "Modules",
                                  node = dataGrid(
                                    items = editableModules,
                                    showSearch = false,
                                    init = {
                                      prefHeight = 150.0
                                      maxWidth = Double.MAX_VALUE
                                    },
                                  ) {
                                    constrainedResize()
                                    readOnlyTextColumn("Module") { it.name }
                                    readOnlyTextColumn("Owner") { it.owner }
                                    readOnlyTextColumn("Status") { it.status }
                                  },
                                )
                              }
                            }
                          }
                        }
                        details {
                          inspectorPanel(
                            title = "Repository",
                            subtitle = "Selection details for Git and database resources.",
                          ) {
                            badge("Connected", ComponentTone.SUCCESS, icon = WorkbenchIcons.Connected)
                            property("Branch", "main")
                            property("Connection", "local")
                            section("Metadata") {
                              property("Schema", "public")
                              property("Dirty rows", "1")
                            }
                            actions {
                              action("Inspect") {
                                feedbackLabel.text = "State: Inspector action requested."
                              }
                            }
                          }
                        }
                        status {
                          statusBar {
                            statusItem("Ready", ComponentTone.SUCCESS, icon = WorkbenchIcons.Connected)
                            spacer()
                            statusItem("3 modules loaded", ComponentTone.INFO)
                          }
                        }
                        overlay(alignment = Pos.BOTTOM_RIGHT) {
                          badge("Overlay slot", ComponentTone.SUCCESS, icon = WorkbenchIcons.Overlay)
                        }
                      }

                      borderLayout(
                        init = {
                          prefHeight = 220.0
                          maxWidth = Double.MAX_VALUE
                        },
                      ) {
                        header {
                          label("Workspace Layout") {
                            styleClasses(ThemeStyleClass.Headline)
                          }
                        }
                        sidebar {
                          vbox(spacing = 8.0) {
                            label("Overview")
                            label("Modules")
                            label("Settings")
                          }
                        }
                        content {
                          card(spacing = 8.0, padding = 12.0) {
                            label("BorderLayout") {
                              styleClasses(ThemeStyleClass.Headline)
                            }
                            label("Header, sidebar, content and footer are plain JavaFX nodes with stable theme slot classes.")
                          }
                        }
                        footer {
                          label("Footer slot keeps its own themed boundary.")
                        }
                      }

                      dataGrid(
                        items = editableModules,
                        searchPrompt = "Search modules...",
                        init = {
                          prefHeight = 230.0
                          maxWidth = Double.MAX_VALUE
                          growVertical(Priority.SOMETIMES)
                        },
                      ) {
                        search(textOf = { "${it.name} ${it.owner} ${it.status}" })
                        selectionMode(SelectionMode.MULTIPLE)
                        selectionSummary()
                        dirtyRows { it.status == "Draft" }
                        emptyState("No modules match the current filter")
                        footer("${editableModules.size} modules - draft rows are marked")
                        toolbar {
                          action("Refresh") {
                            feedbackLabel.text = "State: Data grid refresh requested."
                          }
                          batchAction("Archive selected") { rows ->
                            feedbackLabel.text = "State: Prepared ${rows.size} module rows for archive."
                          }
                        }
                        constrainedResize()
                        editableTextColumn("Module", valueOf = { it.name }) { row, value ->
                          row.name = value
                          feedbackLabel.text = "State: Renamed module to $value."
                        }
                        editableTextColumn("Owner", valueOf = { it.owner }) { row, value ->
                          row.owner = value
                          feedbackLabel.text = "State: ${row.name} owner changed to $value."
                        }
                        readOnlyTextColumn("Status") { it.status }
                        actionColumn(title = "Action", text = "Open") { row ->
                          feedbackLabel.text = "State: Open data grid row ${row.name}."
                        }
                      }
                    }

                    section(
                      title = "Activity Timeline Component",
                      description = "A grouped event surface for Git history, query execution history and background task logs.",
                    ) {
                      activityTimeline(
                        events = activityEvents,
                        emptyText = "No activity yet",
                      ) {
                        groupBy { it.group }
                        timeOf { it.time }
                        titleOf { it.title }
                        messageOf { it.message }
                        toneOf { it.tone }
                        action("Open") { event ->
                          feedbackLabel.text = "State: Open activity ${event.title}."
                        }
                      }
                    }

                    section(
                      title = "Form And Table DSL",
                      description = "Common controls stay close to JavaFX while removing repetitive layout code.",
                    ) {
                      form {
                        lateinit var projectField: TextField

                        item(
                          label = "Project",
                          helper = "Validation is just Flow binding; no form model is required.",
                        ) {
                          projectField = textField {
                            maxWidth = Double.MAX_VALUE
                            bindTextBidirectional(uiScope, dslProjectName)
                            bindInvalid(
                              uiScope,
                              dslProjectNameError.map { it != null },
                            )
                          }
                          validationMessage(uiScope, dslProjectNameError)
                        }

                        checkBox(
                          label = "Options",
                          text = "Keep MVVM independent from DI",
                        ) {
                          isSelected = true
                        }

                        item(
                          label = "Mode",
                          helper = "ComboBox selection can bind directly to MutableStateFlow.",
                        ) {
                          comboBox(
                            items = dslModeOptions,
                            init = {
                              maxWidth = Double.MAX_VALUE
                            },
                          ) {
                            select("DSL First")
                          }.bindSelectedItemBidirectional(uiScope, dslMode)
                        }

                        item(label = "Runtime") {
                          choiceBox(items = dslRuntimeOptions) {
                            select(graph.catalog.initialDslRuntime)
                          }.bindSelectedItemBidirectional(uiScope, dslRuntime)
                        }

                        item(label = "Parallel Work") {
                          intSpinner(
                            min = 1,
                            max = 8,
                            initialValue = dslParallelism.value,
                          ) {
                            isEditable = true
                            bindValueBidirectional(uiScope, dslParallelism)
                          }
                        }

                        item(label = "Target Date") {
                          datePicker {
                            bindValueBidirectional(uiScope, dslTargetDate)
                          }
                        }

                        submitBar {
                          secondaryButton("Reset") {
                            onAction {
                              dslProjectName.value = graph.catalog.initialProjectName
                              dslMode.value = graph.catalog.initialDslMode
                              dslRuntime.value = graph.catalog.initialDslRuntime
                              dslParallelism.value = graph.catalog.initialDslParallelism
                              dslTargetDate.value = graph.catalog.initialDslTargetDate
                            }
                          }
                          primaryButton("Apply") {
                            onAction {
                              val project = projectField.text.trim()
                              feedbackLabel.text =
                                if (project.isEmpty()) {
                                  "State: Form validation blocked submit."
                                } else {
                                  "State: $project uses ${dslMode.value} with ${dslParallelism.value} workers."
                                }
                            }
                          }
                        }
                      }

                      tableView(
                        items = moduleSummaries,
                        init = {
                          prefHeight = 180.0
                          maxWidth = Double.MAX_VALUE
                          growVertical(Priority.SOMETIMES)
                        },
                      ) {
                        constrainedResize()
                        placeholder("No modules")
                        textColumn("Module") { it.name }
                        textColumn("Responsibility") { it.responsibility }
                        actionColumn(title = "Action", text = "Inspect") { row ->
                          feedbackLabel.text = "State: Inspect ${row.name}."
                        }
                      }
                    }

                    card {
                      label("Composable TreeView") {
                        styleClasses(ThemeStyleClass.Headline)
                      }
                      treeView<String>(
                        init = {
                          prefHeight = 150.0
                        },
                      ) {
                        root("DSL Coverage") {
                          item("Layouts")
                          item("Controls")
                          item("Bindings")
                          item("Menus")
                          item("Components")
                        }
                        showRoot(true)
                        render { "• $it" }
                        rowAction { item ->
                          feedbackLabel.text = "State: Tree row action: $item"
                        }
                      }
                    }

                    emptyState(
                      title = "Feedback components",
                      message = "emptyState, loadingState and errorState are plain JavaFX nodes.",
                      actionText = "Mark Reviewed",
                      onAction = {
                        feedbackLabel.text = "State: Feedback component action."
                      },
                    ) {
                      prefHeight = 180.0
                    }
                  }.stateVisible(uiScope, viewModel.state) {
                    it.currentRouteId == WorkbenchRoute.Components.id
                  }
                  vbox(spacing = 16.0) {
                    section(
                      title = "StateFlow-backed ViewModel",
                      description = "Buttons dispatch actions. The ViewModel updates state and emits feedback events.",
                    ) {
                      label {
                        styleClasses(ThemeStyleClass.Headline)
                      }.stateText(uiScope, viewModel.state) { "Count: ${it.mvvmCount}" }

                      actionBar(alignEnd = false) {
                        alignment(Pos.CENTER_LEFT)

                        button("-1") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.DecrementCounter)
                          }
                        }
                        button("+1") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.IncrementCounter)
                          }
                        }
                        ghostButton("Reset") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.ResetCounter)
                          }
                        }
                      }
                    }

                    section(
                      title = "Action / Event Flow",
                      description = "Text input updates ViewModel state. Submit emits a one-shot event and renders notes from state.",
                    ) {
                      gridPane(
                        hgap = 12.0,
                        vgap = 10.0,
                      ) {
                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                        column(grow = Priority.ALWAYS, fillWidth = true)

                        label(0, 0, "Draft")
                        textField(1, 0, "") {
                          promptText = "Type a note for the ViewModel"
                          maxWidth = Double.MAX_VALUE
                        }.stateText(
                          scope = uiScope,
                          state = viewModel.state,
                          onTextChange = { nextValue ->
                            viewModel.dispatch(WorkbenchAction.UpdateDraft(nextValue))
                          }
                        ) { it.mvvmDraft }

                        label(0, 1, "Notes")
                        cell(1, 1, horizontalGrow = Priority.ALWAYS) {
                          vbox(
                            spacing = 10.0,
                            init = {
                              minHeight = 150.0
                            },
                          ) {
                          }.stateList(
                            scope = uiScope,
                            state = viewModel.state,
                            items = { it.mvvmNotes },
                            empty = {
                              emptyState(
                                title = "No notes yet",
                                message = "Submit the draft to prove state-driven rendering.",
                              ) {
                                prefHeight = 130.0
                              }
                            },
                          ) { note ->
                            ghostButton(note) {
                              maxWidth = Double.MAX_VALUE
                              onAction {
                                viewModel.dispatch(WorkbenchAction.RecallNote(note))
                              }
                            }
                          }
                        }
                      }

                      actionBar {
                        ghostButton("Clear Notes") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.ClearNotes)
                          }
                        }
                        button("Submit Draft") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.SubmitDraft)
                          }
                        }.stateDisable(uiScope, viewModel.state) {
                          it.mvvmDraft.isBlank()
                        }
                      }
                    }
                  }.stateVisible(uiScope, viewModel.state) {
                    it.currentRouteId == WorkbenchRoute.Mvvm.id
                  }
                  vbox(spacing = 16.0) {
                    section(
                      title = "Theme Presets",
                      description = "Choose any built-in theme. The Scene stylesheet is regenerated from typed tokens.",
                    ) {
                      gridPane(
                        hgap = 12.0,
                        vgap = 10.0,
                      ) {
                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                        column(grow = Priority.ALWAYS, fillWidth = true)

                        label(0, 0, "Preset")
                        cell(1, 0, horizontalGrow = Priority.ALWAYS) {
                          comboBox<KoraTheme>(
                            items = themeManager.availableThemes,
                            init = {
                              maxWidth = Double.MAX_VALUE
                            },
                          ) {
                            render { it.displayName }
                            onSelect { theme ->
                              if (theme != null) {
                                viewModel.dispatch(WorkbenchAction.SelectTheme(theme.id))
                              }
                            }
                          }.also {
                            it.bindSelectedItem(uiScope, themeManager.theme)
                          }
                        }

                        label(0, 1, "Current")
                        label(1, 1) {
                          styleClasses(ThemeStyleClass.Muted)
                        }.stateText(uiScope, themeManager.theme) { "${it.displayName} (${it.id})" }
                      }

                      actionBar(alignEnd = false) {
                        ghostButton("Previous") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.PreviousTheme)
                          }
                        }
                        button("Next") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.NextTheme)
                          }
                        }
                        ghostButton("Light / Dark") {
                          onAction {
                            viewModel.dispatch(WorkbenchAction.ToggleTheme)
                          }
                        }
                      }
                    }

                    section(
                      title = "Current Theme Tokens",
                      description = "These values are the source for generated JavaFX CSS.",
                    ) {
                      gridPane(
                        hgap = 12.0,
                        vgap = 10.0,
                      ) {
                        column(prefWidth = 120.0, alignment = HPos.RIGHT)
                        column(grow = Priority.ALWAYS, fillWidth = true)

                        label(0, 0, "Primary")
                        label(1, 0).stateText(uiScope, themeManager.theme) { it.tokens.colors.primary }
                        label(0, 1, "Surface")
                        label(1, 1).stateText(uiScope, themeManager.theme) { it.tokens.colors.surface }
                        label(0, 2, "Muted Surface")
                        label(1, 2).stateText(uiScope, themeManager.theme) { it.tokens.colors.surfaceMuted }
                        label(0, 3, "Text")
                        label(1, 3).stateText(uiScope, themeManager.theme) {
                          "${it.tokens.colors.textPrimary} / ${it.tokens.colors.textSecondary}"
                        }
                        label(0, 4, "Semantic")
                        label(1, 4).stateText(uiScope, themeManager.theme) {
                          "success ${it.tokens.colors.success}, warning ${it.tokens.colors.warning}, danger ${it.tokens.colors.danger}, info ${it.tokens.colors.info}"
                        }
                        label(0, 5, "Typography")
                        label(1, 5).stateText(uiScope, themeManager.theme) {
                          "${it.tokens.typography.fontFamily}, ${it.tokens.typography.baseSize}px / ${it.tokens.typography.headlineSize}px"
                        }
                        label(0, 6, "Radius")
                        label(1, 6).stateText(uiScope, themeManager.theme) { "${it.tokens.radius}px" }
                        label(0, 7, "Radii")
                        label(1, 7).stateText(uiScope, themeManager.theme) {
                          "small ${it.tokens.radii.small}px, medium ${it.tokens.radii.medium}px, large ${it.tokens.radii.large}px"
                        }
                        label(0, 8, "Spacing")
                        label(1, 8).stateText(uiScope, themeManager.theme) {
                          "sm ${it.tokens.spacing.sm}px, md ${it.tokens.spacing.md}px, xl ${it.tokens.spacing.xl}px"
                        }
                        label(0, 9, "States")
                        label(1, 9).stateText(uiScope, themeManager.theme) {
                          "hover ${it.tokens.states.surfaceHover}, selected ${it.tokens.states.selected}, disabled ${it.tokens.states.disabledOpacity}"
                        }
                      }
                    }

                    section(
                      title = "Theme Control Gallery",
                      description = "Switch presets above and check whether common JavaFX controls keep a consistent surface, radius and interaction style.",
                    ) {
                      vbox(spacing = 16.0) {
                        flowPane(
                          hgap = 10.0,
                          vgap = 10.0,
                        ) {
                          button("Primary")
                          ghostButton("Ghost")
                          toggleButton("Toggle") {
                            isSelected = true
                          }
                          menuButton("Menu") {
                            actionItem("Refresh") {
                              feedbackLabel.text = "State: Menu action."
                            }
                            actionItem("Export") {
                              feedbackLabel.text = "State: Export action."
                            }
                          }
                          splitMenuButton("Split Action") {
                            actionItem("Run now") {
                              feedbackLabel.text = "State: Split action."
                            }
                            actionItem("Schedule") {
                              feedbackLabel.text = "State: Schedule action."
                            }
                          }
                          hyperlink("Documentation") {
                            onAction {
                              feedbackLabel.text = "State: Hyperlink action."
                            }
                          }
                          badge("Success", ComponentTone.SUCCESS, icon = WorkbenchIcons.Stable)
                          badge("Warning", ComponentTone.WARNING, icon = WorkbenchIcons.Warning)
                          badge("Danger", ComponentTone.DANGER)
                          chip("Selected", ComponentTone.PRIMARY, selected = true, icon = WorkbenchIcons.Dsl)
                          chip("Info", ComponentTone.INFO, icon = WorkbenchIcons.Theme)
                        }

                        hbox(spacing = 12.0) {
                          metricCard(
                            label = "Presets",
                            value = themeManager.availableThemes.size.toString(),
                            helper = "Built-in themes",
                            tone = ComponentTone.INFO,
                          )
                          metricCard(
                            label = "Tones",
                            value = ComponentTone.entries.size.toString(),
                            helper = "Semantic component states",
                            tone = ComponentTone.SUCCESS,
                          )
                        }

                        alertBanner(
                          title = "Semantic theme coverage",
                          message = "Badges, chips, metric cards and alert banners share the same tone classes.",
                          tone = ComponentTone.WARNING,
                          icon = WorkbenchIcons.Warning,
                          actionText = "Switch Theme",
                          onAction = {
                            viewModel.dispatch(WorkbenchAction.NextTheme)
                          },
                        )

                        gridPane(
                          hgap = 12.0,
                          vgap = 10.0,
                        ) {
                          val toneGroup = javafx.scene.control.ToggleGroup()

                          column(prefWidth = 120.0, alignment = HPos.RIGHT)
                          column(grow = Priority.ALWAYS, fillWidth = true)
                          column(grow = Priority.ALWAYS, fillWidth = true)

                          label(0, 0, "Text")
                          textField(1, 0, "Editable value") {
                            maxWidth = Double.MAX_VALUE
                          }
                          cell(2, 0, horizontalGrow = Priority.ALWAYS) {
                            passwordField("secret") {
                              maxWidth = Double.MAX_VALUE
                            }
                          }

                          label(0, 1, "Selection")
                          cell(1, 1, horizontalGrow = Priority.ALWAYS) {
                            comboBox(
                              items = listOf("Kotlin", "JavaFX", "StateFlow"),
                              init = {
                                maxWidth = Double.MAX_VALUE
                              },
                            ) {
                              select("Kotlin")
                            }
                          }
                          cell(2, 1, horizontalGrow = Priority.ALWAYS) {
                            choiceBox(
                              items = listOf("Light", "Dark", "Brand"),
                              init = {
                                maxWidth = Double.MAX_VALUE
                              },
                            ) {
                              select("Brand")
                            }
                          }

                          label(0, 2, "Pickers")
                          cell(1, 2, horizontalGrow = Priority.ALWAYS) {
                            datePicker(LocalDate.now()) {
                              maxWidth = Double.MAX_VALUE
                            }
                          }
                          cell(2, 2, horizontalGrow = Priority.ALWAYS) {
                            colorPicker(Color.web("#246BFD")) {
                              maxWidth = Double.MAX_VALUE
                            }
                          }

                          label(0, 3, "Inputs")
                          cell(1, 3, horizontalGrow = Priority.ALWAYS) {
                            intSpinner(
                              min = 1,
                              max = 12,
                              initialValue = 4,
                            ) {
                              isEditable = true
                              maxWidth = Double.MAX_VALUE
                            }
                          }
                          cell(2, 3, horizontalGrow = Priority.ALWAYS) {
                            hbox(spacing = 12.0) {
                              checkBox("Enabled") {
                                isSelected = true
                              }
                              radioButton("A", toneGroup) {
                                isSelected = true
                              }
                              radioButton("B", toneGroup)
                            }
                          }
                        }

                        tabPane {
                          tab("Controls") {
                            vbox(
                              spacing = 10.0,
                              init = {
                                prefHeight = 160.0
                              },
                            ) {
                              label("Progress")
                              progressBar(0.68) {
                                maxWidth = Double.MAX_VALUE
                              }
                              label("Slider")
                              slider(
                                min = 0.0,
                                max = 100.0,
                                value = 68.0,
                              ) {
                                maxWidth = Double.MAX_VALUE
                              }
                            }
                          }
                          tab("Navigation") {
                            accordion {
                              pane("Accordion Pane", expanded = true) {
                                vbox(spacing = 8.0) {
                                  label("Accordion, titled panes and tabs share the same theme tokens.")
                                  pagination(
                                    pageCount = 3,
                                    init = {
                                      maxPageIndicatorCount = 3
                                    },
                                  ) { pageIndex ->
                                    Label("Preview page ${pageIndex + 1}").apply {
                                      styleClasses(ThemeStyleClass.Muted)
                                    }
                                  }
                                }
                              }
                            }
                          }
                          tab("Lists") {
                            listView(
                              items = listOf("List item", "Selected row style", "Hover row style"),
                              init = {
                                prefHeight = 160.0
                                selectionModel.select("Selected row style")
                              },
                            ) {
                              render { it }
                            }
                          }
                        }
                      }
                    }

                    section(
                      title = "Built-In Theme Catalog",
                      description = "The catalog is plain data, so applications can expose all presets or only a curated subset.",
                    ) {
                      tableView(
                        items = themeManager.availableThemes,
                        init = {
                          prefHeight = 240.0
                          maxWidth = Double.MAX_VALUE
                          growVertical(Priority.SOMETIMES)
                        },
                      ) {
                        constrainedResize()
                        textColumn("Theme") { it.displayName }
                        textColumn("ID") { it.id }
                        textColumn("Primary") { it.tokens.colors.primary }
                        textColumn("Radius") { "${it.tokens.radius}px" }
                        actionColumn(title = "Action", text = "Use") { theme ->
                          viewModel.dispatch(WorkbenchAction.SelectTheme(theme.id))
                        }
                      }
                    }
                  }.stateVisible(uiScope, viewModel.state) {
                    it.currentRouteId == WorkbenchRoute.Theme.id
                  }
                }
              }
            }
          }

          footer {
            statusBar {
            }.stateList(
              scope = uiScope,
              state = viewModel.state,
              items = { it.statusItems },
            ) { item ->
              statusItem(item)
            }
          }
        },
      )
      commandPalette(commandPaletteHost) {
        emptyState("No commands match the current search.")
      }
    }
}
