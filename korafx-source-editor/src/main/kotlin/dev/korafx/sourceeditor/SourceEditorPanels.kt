package dev.korafx.sourceeditor

import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

internal fun SourceEditor.initializeSourceEditor(diagnostics: Iterable<SourceDiagnostic>) {
    styleClass("source-editor")
    maxWidth = Double.MAX_VALUE
    maxHeight = Double.MAX_VALUE

    toolbar.apply {
        styleClass("source-editor-toolbar")
        alignment = Pos.CENTER_LEFT
        isVisible = false
        isManaged = false
    }

    editor.styleClass("source-editor-code")
    VBox.setVgrow(editor, Priority.ALWAYS)
    initializeDiagnosticsPane()
    initializeResultPane()

    statusBar.apply {
        styleClass("source-editor-status")
        maxWidth = Double.MAX_VALUE
        alignment = Pos.CENTER_LEFT
        children += statusBadge
        children += statusLabel
    }

    children += toolbar
    children += editor
    children += statusBar
    children += diagnosticsPane
    children += resultPane

    refreshStatus()
    setDiagnostics(diagnostics)
}

internal fun SourceEditor.diagnosticRow(diagnostic: SourceDiagnostic): HBox =
    HBox(8.0).apply {
        styleClasses("source-editor-diagnostic", diagnostic.tone.styleClass)
        alignment = Pos.TOP_LEFT
        setOnMouseClicked {
            jumpToDiagnostic(diagnostic)
        }
        children += Label("${diagnostic.line}:${diagnostic.column}").apply {
            styleClass("source-editor-diagnostic-location")
        }
        children += Label(diagnostic.message).apply {
            styleClass("source-editor-diagnostic-message")
            isWrapText = true
            maxWidth = Double.MAX_VALUE
            HBox.setHgrow(this, Priority.ALWAYS)
        }
    }

internal fun SourceEditor.refreshToolbarVisibility() {
    val visible = toolbar.children.isNotEmpty()
    toolbar.isVisible = visible
    toolbar.isManaged = visible
}

internal fun SourceEditor.refreshDiagnosticsVisibility() {
    val visible = diagnosticsList.children.isNotEmpty()
    diagnosticsPane.isVisible = visible
    diagnosticsPane.isManaged = visible
}

internal fun SourceEditor.refreshStatus() {
    val state = executionState
    statusBadge.styleClasses("badge", state.tone.styleClass, "source-editor-status-badge")
    statusBadge.text = state.toBadgeText()
    statusLabel.text = state.message
    statusLabel.styleClass("source-editor-status-message")
}

internal fun SourceEditor.refreshResultVisibility() {
    val visible = resultContent.children.isNotEmpty()
    resultPane.isVisible = visible
    resultPane.isManaged = visible
}

internal fun SourceEditor.prepareResultNode(node: Node): Node =
    node.apply {
        styleClass("source-editor-result-node")
        if (this is Region) {
            maxWidth = Double.MAX_VALUE
            VBox.setVgrow(this, Priority.SOMETIMES)
        }
    }

private fun SourceEditor.initializeDiagnosticsPane() {
    diagnosticsPane.apply {
        styleClass("source-editor-diagnostics")
        maxWidth = Double.MAX_VALUE
        isVisible = false
        isManaged = false
        children += diagnosticsHeader.apply {
            styleClass("source-editor-diagnostics-title")
        }
        children += diagnosticsList.apply {
            styleClass("source-editor-diagnostics-list")
            maxWidth = Double.MAX_VALUE
        }
    }
}

private fun SourceEditor.initializeResultPane() {
    resultPane.apply {
        styleClass("source-editor-result")
        maxWidth = Double.MAX_VALUE
        isVisible = false
        isManaged = false
        children += resultHeader.apply {
            styleClass("source-editor-result-title")
        }
        children += resultContent.apply {
            styleClass("source-editor-result-content")
            maxWidth = Double.MAX_VALUE
        }
    }
}
