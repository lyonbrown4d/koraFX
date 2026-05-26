package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.WorkspaceLayout
import dev.korafx.components.appToolbar
import dev.korafx.components.emptyState
import dev.korafx.components.section
import dev.korafx.components.statusBar
import dev.korafx.components.statusItem
import dev.korafx.components.tabWorkspace
import dev.korafx.components.workspaceLayout
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.button
import dev.korafx.dsl.onAction
import dev.korafx.inspector.InspectorPanel
import dev.korafx.inspector.inspectorPanel
import dev.korafx.sample.ui.WorkbenchIcons
import dev.korafx.sample.viewmodel.WorkbenchAction

fun NodeContainerBuilder.workspacePage(context: WorkbenchPageContext) {
    var workspaceRef: dev.korafx.components.TabWorkspace? = null
    var inspectorRef: InspectorPanel? = null
    var layoutRef: WorkspaceLayout? = null
    var showNavigation = true
    var showDetails = true

    fun refreshStatusState(): Array<String> {
        val activeTab = workspaceRef?.let(WorkspacePageState::currentTab) ?: WorkspacePageState.activeTabId
        return arrayOf(
            "Workspace: ${workspaceRef?.workspaceTabs?.size ?: 0} tab(s)",
            "Active: ${workspaceTabTitle(activeTab)}",
            "Executed queries: ${WorkspacePageState.executedQueryCount}",
        )
    }

    section(
        title = "Workspace",
        description = "Tab + navigation + details + status 的工作区布局。支持查询执行、动态 inspector 和多标签操作。",
    ) {
        val layout = workspaceLayout(
            init = {
                minHeight = 520.0
            },
            content = {
                topBar {
                    workspaceToolbar(
                        context = context,
                        layoutProvider = { layoutRef },
                        workspaceProvider = { workspaceRef },
                        inspectorProvider = { inspectorRef },
                        visibility = WorkspaceVisibility(
                            navigation = { showNavigation },
                            details = { showDetails },
                            setNavigation = { showNavigation = it },
                            setDetails = { showDetails = it },
                        ),
                    )
                }

                navigation {
                    section("Navigation") {
                        button("Focus Query") {
                            onAction {
                                workspaceRef?.let { workspace ->
                                    selectWorkspaceTab(workspace, context, inspectorRef, WorkspaceTabQuery)
                                }
                            }
                        }
                        button("Close Active Tab") {
                            onAction {
                                workspaceRef?.let { workspace ->
                                    closeWorkspaceTab(workspace, context, inspectorRef, WorkspacePageState.activeTabId)
                                }
                            }
                        }
                    }
                }

                content {
                    val workspace = workspaceTabs(context, workspaceRef, inspectorRef)
                    workspaceRef = workspace
                    val activeTab = WorkspacePageState.currentTab(workspace)
                    WorkspacePageState.activeTabId = activeTab
                    WorkspacePageState.currentTarget = workspaceTabTitle(activeTab)
                    workspace.selectTab(activeTab)
                    inspectorRef?.let { panel ->
                        refreshWorkspaceInspector(panel, context, workspace, activeTab)
                    }
                    workspace
                }

                details {
                    val panel = inspectorPanel(
                        title = "Workspace Inspector",
                        subtitle = "Live workspace metadata",
                        init = {
                            prefWidth = 300.0
                        },
                    ) {
                        emptyState("Waiting for workspace...")
                    }

                    inspectorRef = panel
                    workspaceRef?.let { workspace ->
                        val activeTab = WorkspacePageState.currentTab(workspace)
                        refreshWorkspaceInspector(panel, context, workspace, activeTab)
                    }
                    panel
                }

                status {
                    val status = refreshStatusState()
                    statusBar {
                        statusItem(text = status[0], tone = ComponentTone.NEUTRAL)
                        statusItem(text = status[1], tone = ComponentTone.INFO)
                        statusItem(text = status[2], tone = ComponentTone.WARNING)
                    }
                }
            },
        )

        layoutRef = layout
    }
}

private fun NodeContainerBuilder.workspaceTabs(
    context: WorkbenchPageContext,
    workspaceRef: dev.korafx.components.TabWorkspace?,
    inspectorRef: InspectorPanel?,
) =
    tabWorkspace(
        emptyText = "Open a tab...",
        init = {
            maxWidth = Double.MAX_VALUE
        },
    ) {
        onSelect { tabId ->
            WorkspacePageState.activeTabId = tabId
            WorkspacePageState.currentTarget = workspaceTabTitle(tabId)
            inspectorRef?.let { panel ->
                refreshWorkspaceInspector(panel, context, workspaceRef ?: return@onSelect, tabId)
            }
        }

        onClose { tabId ->
            closeWorkspaceTab(
                workspace = workspaceRef ?: return@onClose,
                context = context,
                inspectorPanel = inspectorRef,
                tabId = tabId,
            )
        }

        tab(WorkspaceTabReadme, "README.md", closable = false, select = false) {
            buildReadmeTab()
        }
        tab(WorkspaceTabQuery, "query.sql", dirty = true, select = false) {
            buildQueryTab(context)
        }
        tab(WorkspaceTabActivity, "Activity Feed", select = false) {
            buildActivityTab(context)
        }
    }

private data class WorkspaceVisibility(
    val navigation: () -> Boolean,
    val details: () -> Boolean,
    val setNavigation: (Boolean) -> Unit,
    val setDetails: (Boolean) -> Unit,
)

private fun workspaceToolbar(
    context: WorkbenchPageContext,
    layoutProvider: () -> WorkspaceLayout?,
    workspaceProvider: () -> dev.korafx.components.TabWorkspace?,
    inspectorProvider: () -> InspectorPanel?,
    visibility: WorkspaceVisibility,
) =
    appToolbar(
        title = "KoraFX Workspace",
        subtitle = "Document and query workspace",
        icon = WorkbenchIcons.Workspace,
        actions = {
            button("Toggle Navigation") {
                onAction {
                    val next = !visibility.navigation()
                    visibility.setNavigation(next)
                    layoutProvider()?.setNavigationVisible(next)
                    context.viewModel.dispatch(WorkbenchAction.UpdateDraft(if (next) "Navigation panel shown" else "Navigation panel hidden"))
                }
            }
            button("Toggle Details") {
                onAction {
                    val next = !visibility.details()
                    visibility.setDetails(next)
                    layoutProvider()?.setDetailsVisible(next)
                    context.viewModel.dispatch(WorkbenchAction.UpdateDraft(if (next) "Details panel shown" else "Details panel hidden"))
                }
            }
            button("Readme") {
                onAction {
                    workspaceProvider()?.let { workspace ->
                        openOrSelectWorkspaceTab(workspace, context, inspectorProvider(), WorkspaceTabReadme, "README.md", true, closable = false, content = ::buildReadmeTab)
                    }
                }
            }
            button("Query") {
                onAction {
                    workspaceProvider()?.let { workspace ->
                        openOrSelectWorkspaceTab(workspace, context, inspectorProvider(), WorkspaceTabQuery, "query.sql", true, dirty = true, content = { buildQueryTab(context) })
                    }
                }
            }
            button("Activity") {
                onAction {
                    workspaceProvider()?.let { workspace ->
                        openOrSelectWorkspaceTab(workspace, context, inspectorProvider(), WorkspaceTabActivity, "Activity Feed", true, content = { buildActivityTab(context) })
                    }
                }
            }
            button("Scratch Log") {
                onAction {
                    workspaceProvider()?.let { workspace ->
                        openScratchLogTab(workspace, context, inspectorProvider())
                    }
                }
            }
        },
    )
