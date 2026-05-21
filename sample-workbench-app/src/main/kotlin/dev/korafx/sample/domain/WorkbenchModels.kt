package dev.korafx.sample.domain

import dev.korafx.components.ComponentTone

enum class ModuleCategory(
    val title: String,
) {
    CORE("Core Modules"),
    ADVANCED_COMPONENT("Advanced Components"),
}

data class ModuleShowcase(
    val id: String,
    val title: String,
    val category: ModuleCategory,
    val summary: String,
    val routePath: String,
    val documentResource: String,
    val artifactName: String,
    val tags: List<String> = emptyList(),
    val sourceSnippets: List<SourceSnippet> = emptyList(),
)

data class ModuleSummary(
    val name: String,
    val responsibility: String,
)

data class EditableModule(
    var name: String,
    var owner: String,
    val status: String,
)

data class ExplorerResource(
    val name: String,
    val children: List<ExplorerResource> = emptyList(),
)

data class ActivityEvent(
    val title: String,
    val message: String,
    val time: String,
    val group: String,
    val tone: ComponentTone,
)

data class SourceSnippet(
    val title: String,
    val language: String,
    val code: String,
    val id: String = title.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'),
    val module: String = "",
    val description: String = "",
    val routeIds: Set<String> = emptySet(),
)
