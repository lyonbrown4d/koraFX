package dev.korafx.sample.ui.pages

import dev.korafx.components.TabWorkspace
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.ArrayDeque

internal const val WorkspaceTabReadme = "workspace-readme"
internal const val WorkspaceTabQuery = "workspace-query"
internal const val WorkspaceTabActivity = "workspace-activity"
internal const val WorkspaceTabLog = "workspace-log"

private const val WorkspaceDefaultLogLimit = 8
private val workspaceQueryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

internal object WorkspacePageState {
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

internal fun workspaceTabTitle(tabId: String): String =
    when (tabId) {
        WorkspaceTabReadme -> "README.md"
        WorkspaceTabQuery -> "query.sql"
        WorkspaceTabActivity -> "Activity Feed"
        WorkspaceTabLog -> "Query Log"
        else -> tabId
    }
