package dev.korafx.sample.domain

import dev.korafx.components.ComponentTone

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
