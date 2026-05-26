package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.dsl.onAction
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class SourceEditorBuilder internal constructor(
    private val sourceEditor: SourceEditor,
) {
    fun action(
        text: String,
        init: Button.() -> Unit = {},
        handler: () -> Unit,
    ): Button =
        Button(text).apply {
            init()
            onAction {
                handler()
            }
        }.also {
            sourceEditor.addAction(it)
        }

    fun nodeAction(node: Node): Node =
        sourceEditor.addAction(node)

    fun spacer(): Region =
        Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }.also {
            sourceEditor.addAction(it)
        }

    fun diagnostics(diagnostics: Iterable<SourceDiagnostic>) {
        sourceEditor.setDiagnostics(diagnostics)
    }

    fun onDiagnosticSelected(handler: (SourceDiagnostic) -> Unit) {
        sourceEditor.onDiagnosticSelected(handler)
    }

    fun markIdle(message: String = "Ready") {
        sourceEditor.markIdle(message)
    }

    fun markRunning(message: String = "Running...") {
        sourceEditor.markRunning(message)
    }

    fun markSuccess(message: String = "Completed") {
        sourceEditor.markSuccess(message)
    }

    fun markError(message: String = "Failed") {
        sourceEditor.markError(message)
    }

    fun executionState(state: SourceEditorExecutionState) {
        sourceEditor.setExecutionState(state)
    }

    fun status(
        message: String,
        tone: ComponentTone = ComponentTone.INFO,
        phase: SourceEditorExecutionPhase = SourceEditorExecutionPhase.IDLE,
    ) {
        sourceEditor.setStatus(message, tone, phase)
    }

    fun lineNumbers(visible: Boolean = true) {
        sourceEditor.setLineNumbersVisible(visible)
    }

    fun wrapText(enabled: Boolean = true) {
        sourceEditor.setWrapText(enabled)
    }

    fun showSearch(query: String = "") {
        sourceEditor.showSearch(query)
    }

    fun showReplace(
        query: String = "",
        replacement: String = "",
    ) {
        sourceEditor.showReplace(query, replacement)
    }

    fun replaceNext(
        query: String = sourceEditor.editor.searchField.text.orEmpty(),
        replacement: String = sourceEditor.editor.replaceField.text.orEmpty(),
    ): Boolean =
        sourceEditor.replaceNext(query, replacement)

    fun replaceAll(
        query: String = sourceEditor.editor.searchField.text.orEmpty(),
        replacement: String = sourceEditor.editor.replaceField.text.orEmpty(),
    ): Int =
        sourceEditor.replaceAll(query, replacement)

    fun goTo(
        line: Int,
        column: Int = 1,
    ): CodeEditorPosition =
        sourceEditor.goTo(line, column)

    fun diagnostic(
        line: Int,
        column: Int,
        message: String,
        tone: ComponentTone = ComponentTone.WARNING,
    ) {
        sourceEditor.addDiagnostic(SourceDiagnostic(line, column, message, tone))
    }

    fun result(
        title: String = "Result",
        node: Node,
    ) {
        sourceEditor.setResult(node, title)
    }

    fun resultNode(node: Node): Node =
        sourceEditor.addResultNode(node)
}

class QueryEditorBuilder internal constructor(
    private val sourceEditor: SourceEditor,
) {
    private val delegate = SourceEditorBuilder(sourceEditor)

    fun runAction(
        text: String = "Run",
        handler: (String) -> Unit,
    ): Button =
        delegate.action(text) {
            handler(sourceEditor.editor.textArea.text.orEmpty())
        }

    fun stopAction(
        text: String = "Stop",
        handler: () -> Unit,
    ): Button =
        delegate.action(text, handler = handler)

    fun action(
        text: String,
        init: Button.() -> Unit = {},
        handler: () -> Unit,
    ): Button =
        delegate.action(text, init, handler)

    fun markIdle(message: String = "Ready") {
        delegate.markIdle(message)
    }

    fun markRunning(message: String = "Running...") {
        delegate.markRunning(message)
    }

    fun markSuccess(message: String = "Completed") {
        delegate.markSuccess(message)
    }

    fun markError(message: String = "Failed") {
        delegate.markError(message)
    }

    fun executionState(state: SourceEditorExecutionState) {
        delegate.executionState(state)
    }

    fun status(
        message: String,
        tone: ComponentTone = ComponentTone.INFO,
        phase: SourceEditorExecutionPhase = SourceEditorExecutionPhase.IDLE,
    ) {
        delegate.status(message, tone, phase)
    }

    fun diagnostics(diagnostics: Iterable<SourceDiagnostic>) {
        delegate.diagnostics(diagnostics)
    }

    fun onDiagnosticSelected(handler: (SourceDiagnostic) -> Unit) {
        delegate.onDiagnosticSelected(handler)
    }

    fun lineNumbers(visible: Boolean = true) {
        delegate.lineNumbers(visible)
    }

    fun wrapText(enabled: Boolean = true) {
        delegate.wrapText(enabled)
    }

    fun showSearch(query: String = "") {
        delegate.showSearch(query)
    }

    fun showReplace(
        query: String = "",
        replacement: String = "",
    ) {
        delegate.showReplace(query, replacement)
    }

    fun replaceNext(
        query: String = sourceEditor.editor.searchField.text.orEmpty(),
        replacement: String = sourceEditor.editor.replaceField.text.orEmpty(),
    ): Boolean =
        delegate.replaceNext(query, replacement)

    fun replaceAll(
        query: String = sourceEditor.editor.searchField.text.orEmpty(),
        replacement: String = sourceEditor.editor.replaceField.text.orEmpty(),
    ): Int =
        delegate.replaceAll(query, replacement)

    fun goTo(
        line: Int,
        column: Int = 1,
    ): CodeEditorPosition =
        delegate.goTo(line, column)

    fun result(
        title: String = "Result",
        node: Node,
    ) {
        delegate.result(title, node)
    }

    fun resultNode(node: Node): Node =
        delegate.resultNode(node)
}
