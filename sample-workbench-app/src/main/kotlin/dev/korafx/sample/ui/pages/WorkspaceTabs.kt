package dev.korafx.sample.ui.pages

import dev.korafx.components.TabWorkspace
import dev.korafx.components.card
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.inspector.InspectorPanel
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClasses
import dev.korafx.sample.viewmodel.WorkbenchAction
import dev.korafx.sourceeditor.queryEditor
import dev.korafx.sourceeditor.sourceEditor
import javafx.scene.Node

internal fun buildReadmeTab(): Node =
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

internal fun buildQueryTab(context: WorkbenchPageContext): Node =
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

internal fun buildActivityTab(context: WorkbenchPageContext): Node =
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

internal fun buildLogTab(context: WorkbenchPageContext): Node =
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

internal fun openOrSelectWorkspaceTab(
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
    workspace.openTab(id = id, title = title, dirty = dirty, closable = closable, select = select, content = content)

    if (select) {
        workspace.selectTab(id)
        WorkspacePageState.activeTabId = id
        WorkspacePageState.currentTarget = workspaceTabTitle(id)
        inspectorPanel?.let {
            refreshWorkspaceInspector(it, context, workspace, id)
        }
    }
}

internal fun openScratchLogTab(
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

internal fun selectWorkspaceTab(
    workspace: TabWorkspace,
    context: WorkbenchPageContext,
    inspectorPanel: InspectorPanel?,
    tabId: String,
) {
    if (!workspace.selectTab(tabId)) {
        return
    }
    WorkspacePageState.activeTabId = tabId
    WorkspacePageState.currentTarget = workspaceTabTitle(tabId)
    inspectorPanel?.let {
        refreshWorkspaceInspector(it, context, workspace, tabId)
    }
    context.viewModel.dispatch(WorkbenchAction.UpdateDraft("Workspace active: ${workspaceTabTitle(tabId)}"))
}

internal fun closeWorkspaceTab(
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
