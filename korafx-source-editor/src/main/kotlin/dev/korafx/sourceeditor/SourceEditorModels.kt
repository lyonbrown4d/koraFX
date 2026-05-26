package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone

data class SourceDiagnostic(
    val line: Int,
    val column: Int,
    val message: String,
    val tone: ComponentTone = ComponentTone.WARNING,
)

enum class SourceEditorExecutionPhase {
    IDLE,
    RUNNING,
    SUCCESS,
    ERROR,
}

data class SourceEditorExecutionState(
    val phase: SourceEditorExecutionPhase = SourceEditorExecutionPhase.IDLE,
    val message: String = "Ready",
    val tone: ComponentTone = when (phase) {
        SourceEditorExecutionPhase.IDLE -> ComponentTone.INFO
        SourceEditorExecutionPhase.RUNNING -> ComponentTone.INFO
        SourceEditorExecutionPhase.SUCCESS -> ComponentTone.SUCCESS
        SourceEditorExecutionPhase.ERROR -> ComponentTone.DANGER
    },
) {
    fun toBadgeText(): String =
        when (phase) {
            SourceEditorExecutionPhase.IDLE -> "Ready"
            SourceEditorExecutionPhase.RUNNING -> "Running"
            SourceEditorExecutionPhase.SUCCESS -> "Success"
            SourceEditorExecutionPhase.ERROR -> "Error"
        }
}
