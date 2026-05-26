package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.WorkspaceLayout
import dev.korafx.components.appToolbar
import dev.korafx.components.card
import dev.korafx.components.badge
import dev.korafx.components.section
import dev.korafx.components.statusBar
import dev.korafx.components.statusItem
import dev.korafx.components.tabWorkspace
import dev.korafx.components.workspaceLayout
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.inspector.InspectorPanel
import dev.korafx.inspector.inspectorPanel
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClasses
import dev.korafx.sample.navigation.WorkbenchRoute
import dev.korafx.sample.ui.WorkbenchIcons
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sourceeditor.queryEditor
import dev.korafx.sourceeditor.sourceEditor
import dev.korafx.components.TabWorkspace
import javafx.scene.Node
import javafx.scene.control.Button
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.ArrayDeque

private const val WorkspaceTabReadme = "workspace-readme"
private const val WorkspaceTabQuery = "workspace-query"
private const val WorkspaceTabActivity = "workspace-activity"
private const val WorkspaceTabLog = "workspace-log"

private const val WorkspaceDefaultLogLimit = 8
private val workspaceQueryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

private object WorkspacePageState {
    var activeTabId: String = WorkspaceTabReadme
    var currentTarget: String = "README.md"
    var executedQueryCount: Int = 0
    var scratchOpenCount: Int = 0
    val recentQueries: ArrayDeque<String> = ArrayDeque()

    fun trackQuery(sql: String) {
        executedQueryCount += 1
        recentQueries.addFirst("${LocalTime.now().format(workspaceQueryFormatter)} · ${sql.trim().take(160)}")
        while (recentQueries.size > WorkspaceDefaultLogLimit) {
            recentQueries.removeLast()
        }
    }

    fun currentTab(workspace: TabWorkspace): String =
        if (workspace.workspaceTabs.any { workspace.tabId(it) == activeTabId }) {
            activeTabId
        } else {
            workspace.workspaceTabs
                .firstOrNull()
                ?.let { workspace.tabId(it) }
                ?: WorkspaceTabReadme
        }
}

private fun tabTitle(tabId: String): String =
    when (tabId) {
        WorkspaceTabReadme -> "README.md"
        WorkspaceTabQuery -> "query.sql"
        WorkspaceTabActivity -> "Activity Feed"
        WorkspaceTabLog -> "Query Log"
        else -> tabId
    }

private fun buildReadmeTab(): Node =
    sourceEditor(
        title = "README.md",
        language = "markdown",
        readOnly = true,
        showLineNumbers = false,
        showSearch = false,
        wrapText = true,
        text =
            """
            # KoraFX Workspace Demo

            这是 `korafx-components` 的 `workspaceLayout` + `tabWorkspace` 示例。

            - 左侧是导航与操作栏；
            - 中间是文档/查询工作区，多标签；
            - 右侧是 inspectorPanel；
            - 底部是统一状态区域。
            """.trimIndent(),
        init = {
            prefHeight = 340.0
        },
    )

private fun buildQueryTab(context: WorkbenchPageContext): Node =
    queryEditor(
        title = "query.sql",
        text = "select name, owner, status from modules;",
        onRun = { sql ->
            WorkspacePageState.trackQuery(sql)
            context.viewModel.dispatch(
                WorkbenchAction.UpdateDraft(
                    "Executed #${WorkspacePageState.executedQueryCount}: ${sql.lines().firstOrNull()?.trim().orEmpty()}",
                ),
            )
        },
        init = {
            prefHeight = 340.0
        },
    )

private fun buildActivityTab(context: WorkbenchPageContext): Node =
    dev.korafx.dsl.vbox(spacing = 10.0) {
        label("Activity feed (${context.catalog.activityEvents.size + WorkspacePageState.recentQueries.size} item(s))")

        context.catalog.activityEvents.forEach { event ->
            card {
                label(event.title) {
                    styleClasses(ThemeStyleClass.Headline)
                }
                label("${event.group} · ${event.time}") {
                    styleClasses(ThemeStyleClass.Muted)
                }
                label(event.message)
            }
        }

        WorkspacePageState.recentQueries.forEach { query ->
            card {
                label("Query event") {
                    styleClasses(ThemeStyleClass.Headline)
                }
                label(query) {
                    styleClasses(ThemeStyleClass.Muted)
                }
            }
        }

        if (context.catalog.activityEvents.isEmpty() && WorkspacePageState.recentQueries.isEmpty()) {
            label("No activity yet.") {
                styleClasses(ThemeStyleClass.Muted)
            }
        }
    }

private fun buildLogTab(context: WorkbenchPageContext): Node =
    sourceEditor(
        title = "Query Log",
        language = "text",
        readOnly = true,
        showLineNumbers = true,
        showSearch = false,
        wrapText = false,
        text =
            buildString {
                appendLine("KoraFX workspace query history")
                appendLine()
                WorkspacePageState.recentQueries.forEach { query ->
                    appendLine(query)
                }
                if (WorkspacePageState.recentQueries.isEmpty()) {
                    appendLine("No queries in current session.")
                }
            },
        init = {
            prefHeight = 340.0
        },
    )

private fun openOrSelectWorkspaceTab(
    workspace: TabWorkspace,
    context: WorkbenchPageContext,
    inspectorPanel: InspectorPanel?,
    id: String,
    title: String,
    select: Boolean,
    dirty: Boolean = false,
    closable: Boolean = true,
    content: () -> Node,
) {
    workspace.openTab(
        id = id,
        title = title,
        dirty = dirty,
        closable = closable,
        select = select,
        content = content,
    )

    if (select) {
        workspace.selectTab(id)
        WorkspacePageState.activeTabId = id
        WorkspacePageState.currentTarget = tabTitle(id)
        inspectorPanel?.let {
            refreshWorkspaceInspector(it, context, workspace, id)
        }
    }
}

private fun openScratchLogTab(
    workspace: TabWorkspace,
    context: WorkbenchPageContext,
    inspectorPanel: InspectorPanel?,
) {
    val existed = workspace.workspaceTabs.any { workspace.tabId(it) == WorkspaceTabLog }
    if (!existed) {
        WorkspacePageState.scratchOpenCount += 1
    }
    openOrSelectWorkspaceTab(
        workspace = workspace,
        context = context,
        inspectorPanel = inspectorPanel,
        id = WorkspaceTabLog,
        title = "Query Log",
        select = true,
        content = { buildLogTab(context) },
    )
}

private fun selectWorkspaceTab(
    workspace: TabWorkspace,
    context: WorkbenchPageContext,
    inspectorPanel: InspectorPanel?,
    tabId: String,
) {
    if (!workspace.selectTab(tabId)) {
        return
    }
    WorkspacePageState.activeTabId = tabId
    WorkspacePageState.currentTarget = tabTitle(tabId)
    inspectorPanel?.let {
        refreshWorkspaceInspector(it, context, workspace, tabId)
    }
    context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Workspace active: ${tabTitle(tabId)}"))
}

private fun closeWorkspaceTab(
    workspace: TabWorkspace,
    context: WorkbenchPageContext,
    inspectorPanel: InspectorPanel?,
    tabId: String,
) {
    if (tabId == WorkspaceTabReadme) {
        context.viewModel.dispatch(WorkbenchAction.UpdateDraft("README is pinned."))
        return
    }
    if (!workspace.closeTab(tabId)) {
        context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Close tab failed: $tabId"))
        return
    }

    val fallbackTabId = WorkspacePageState.currentTab(workspace)
    selectWorkspaceTab(
        workspace = workspace,
        context = context,
        inspectorPanel = inspectorPanel,
        tabId = fallbackTabId,
    )
    context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Workspace tab closed: $tabId"))
}

private fun refreshWorkspaceInspector(
    panel: InspectorPanel,
    context: WorkbenchPageContext,
    workspace: TabWorkspace,
    activeTabId: String,
) {
    val activeTabs = workspace.workspaceTabs

    panel.clearMetadata()
    panel.clearBody()
    panel.clearActions()

    panel.setHeader("Workspace Inspector", "Active workspace state")
    panel.addMetadata(
        badge("Theme: ${context.themeManager.currentTheme().displayName}", ComponentTone.INFO),
    )
    panel.addMetadata(
        badge("Tabs: ${activeTabs.size}", ComponentTone.SUCCESS),
    )
    panel.addProperty("Active tab", tabTitle(activeTabId))
    panel.addProperty("Target", WorkspacePageState.currentTarget)
    panel.addProperty("Route", WorkbenchRoute.Workspace.path)
    panel.addProperty("Executed queries", WorkspacePageState.executedQueryCount.toString())
    panel.addProperty("Log openings", WorkspacePageState.scratchOpenCount.toString())

    panel.addSection("Runtime") {
        property("Workspace tabs", if (activeTabs.isEmpty()) "-" else activeTabs.joinToString(", ") { workspace.tabId(it) ?: "unknown" })
        property("Latest query", WorkspacePageState.recentQueries.firstOrNull() ?: "No query yet.")
    }

    panel.addAction(
        Button("Open DataGrid").apply {
            onAction {
                context.viewModel.dispatch(WorkbenchAction.NavigateModule("data-grid"))
            }
        },
    )
    panel.addAction(
        Button("Open Resource Explorer").apply {
            onAction {
                context.viewModel.dispatch(WorkbenchAction.NavigateModule("resource-explorer"))
            }
        },
    )
    panel.addAction(
        Button("Run Navigation Demo").apply {
            onAction {
                context.viewModel.dispatch(WorkbenchAction.NavigateModule("navigation"))
            }
        },
    )
}

fun NodeContainerBuilder.workspacePage(context: WorkbenchPageContext) {
    var workspaceRef: TabWorkspace? = null
    var inspectorRef: InspectorPanel? = null
    var layoutRef: WorkspaceLayout? = null
    var showNavigation = true
    var showDetails = true

    fun refreshStatusState(): Array<String> {
        val activeTab = workspaceRef?.let(WorkspacePageState::currentTab) ?: WorkspacePageState.activeTabId
        return arrayOf(
            "Workspace: ${workspaceRef?.workspaceTabs?.size ?: 0} tab(s)",
            "Active: ${tabTitle(activeTab)}",
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
                    appToolbar(
                        title = "KoraFX Workspace",
                        subtitle = "Document and query workspace",
                        icon = WorkbenchIcons.Workspace,
                        actions = {
                            button("Toggle Navigation") {
                                onAction {
                                    showNavigation = !showNavigation
                                    layoutRef?.setNavigationVisible(showNavigation)
                                    context.viewModel.dispatch(
                                        WorkbenchAction.UpdateDraft(
                                            if (showNavigation) {
                                                "Navigation panel shown"
                                            } else {
                                                "Navigation panel hidden"
                                            },
                                        ),
                                    )
                                }
                            }
                            button("Toggle Details") {
                                onAction {
                                    showDetails = !showDetails
                                    layoutRef?.setDetailsVisible(showDetails)
                                    context.viewModel.dispatch(
                                        WorkbenchAction.UpdateDraft(
                                            if (showDetails) {
                                                "Details panel shown"
                                            } else {
                                                "Details panel hidden"
                                            },
                                        ),
                                    )
                                }
                            }
                            button("Readme") {
                                onAction {
                                    workspaceRef?.let { workspace ->
                                        openOrSelectWorkspaceTab(
                                            workspace = workspace,
                                            context = context,
                                            inspectorPanel = inspectorRef,
                                            id = WorkspaceTabReadme,
                                            title = "README.md",
                                            select = true,
                                            closable = false,
                                            content = ::buildReadmeTab,
                                        )
                                    }
                                }
                            }
                            button("Query") {
                                onAction {
                                    workspaceRef?.let { workspace ->
                                        openOrSelectWorkspaceTab(
                                            workspace = workspace,
                                            context = context,
                                            inspectorPanel = inspectorRef,
                                            id = WorkspaceTabQuery,
                                            title = "query.sql",
                                            select = true,
                                            dirty = true,
                                            content = { buildQueryTab(context) },
                                        )
                                    }
                                }
                            }
                            button("Activity") {
                                onAction {
                                    workspaceRef?.let { workspace ->
                                        openOrSelectWorkspaceTab(
                                            workspace = workspace,
                                            context = context,
                                            inspectorPanel = inspectorRef,
                                            id = WorkspaceTabActivity,
                                            title = "Activity Feed",
                                            select = true,
                                            content = { buildActivityTab(context) },
                                        )
                                    }
                                }
                            }
                            button("Scratch Log") {
                                onAction {
                                    workspaceRef?.let { workspace ->
                                        openScratchLogTab(
                                            workspace = workspace,
                                            context = context,
                                            inspectorPanel = inspectorRef,
                                        )
                                    }
                                }
                            }
                        },
                    )
                }

                navigation {
                    section("Navigation") {
                        button("Focus Query") {
                            onAction {
                                workspaceRef?.let { workspace ->
                                    selectWorkspaceTab(
                                        workspace = workspace,
                                        context = context,
                                        inspectorPanel = inspectorRef,
                                        tabId = WorkspaceTabQuery,
                                    )
                                }
                            }
                        }
                        button("Close Active Tab") {
                            onAction {
                                workspaceRef?.let { workspace ->
                                    closeWorkspaceTab(
                                        workspace = workspace,
                                        context = context,
                                        inspectorPanel = inspectorRef,
                                        tabId = WorkspacePageState.activeTabId,
                                    )
                                }
                            }
                        }
                    }
                }

                content {
                    val workspace =
                        tabWorkspace(
                            emptyText = "Open a tab...",
                            init = {
                                maxWidth = Double.MAX_VALUE
                            },
                        ) {
                            onSelect { tabId ->
                                WorkspacePageState.activeTabId = tabId
                                WorkspacePageState.currentTarget = tabTitle(tabId)
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

                    workspaceRef = workspace
                    val activeTab = WorkspacePageState.currentTab(workspace)
                    WorkspacePageState.activeTabId = activeTab
                    WorkspacePageState.currentTarget = tabTitle(activeTab)
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
                        statusItem(
                            text = status[0],
                            tone = ComponentTone.NEUTRAL,
                        )
                        statusItem(
                            text = status[1],
                            tone = ComponentTone.INFO,
                        )
                        statusItem(
                            text = status[2],
                            tone = ComponentTone.WARNING,
                        )
                    }
                }
            },
        )

        layoutRef = layout
    }
}
