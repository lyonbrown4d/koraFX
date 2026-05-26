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

    override val sourceSnippets = coreWorkbenchSourceSnippets() + advancedWorkbenchSourceSnippets()

    override val dslModeOptions = listOf("DSL First", "MVVM Ready", "Component Polish")
    override val dslRuntimeOptions = listOf("Manual JavaFX", "Custom Factory", "External DI")
    override val initialProjectName = "KoraFX"
    override val initialDslMode = "DSL First"
    override val initialDslRuntime = "Manual JavaFX"
    override val initialDslParallelism = 2
    override val initialDslTargetDate: LocalDate = LocalDate.now().plusWeeks(1)
}
