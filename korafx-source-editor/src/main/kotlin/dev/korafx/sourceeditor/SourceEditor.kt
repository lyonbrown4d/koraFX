package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.dsl.styleClass
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class SourceEditor internal constructor(
    title: String?,
    text: String,
    language: String?,
    readOnly: Boolean,
    placeholder: String?,
    showLineNumbers: Boolean,
    showSearch: Boolean,
    wrapText: Boolean,
    diagnostics: Iterable<SourceDiagnostic>,
    onTextChange: ((String) -> Unit)?,
) : VBox(10.0) {
    val toolbar: HBox = HBox(8.0)
    val editor: CodeEditor = codeEditor(
        title = title,
        text = text,
        language = language,
        readOnly = readOnly,
        placeholder = placeholder,
        showLineNumbers = showLineNumbers,
        showSearch = showSearch,
        wrapText = wrapText,
        onTextChange = onTextChange,
    )
    val diagnosticsPane: VBox = VBox(6.0)
    val diagnosticsHeader: Label = Label("Diagnostics")
    val diagnosticsList: VBox = VBox(4.0)
    val resultPane: VBox = VBox(8.0)
    val resultHeader: Label = Label("Result")
    val resultContent: VBox = VBox(8.0)
    val statusBar: HBox = HBox(8.0)
    val statusBadge: Label = Label(SourceEditorExecutionState().toBadgeText()).apply {
        styleClass("badge")
    }
    val statusLabel: Label = Label(SourceEditorExecutionState().message)
    private val executionStateValue = SimpleObjectProperty(SourceEditorExecutionState())

    val isDirty: Boolean
        get() = editor.isDirty

    val currentLine: Int
        get() = editor.currentLine

    val currentColumn: Int
        get() = editor.currentColumn

    val selectionLength: Int
        get() = editor.selectionLength

    val selectedLineCount: Int
        get() = editor.selectedLineCount

    private val currentDiagnostics = mutableListOf<SourceDiagnostic>()
    private var diagnosticSelectionHandler: ((SourceDiagnostic) -> Unit)? = null

    init {
        initializeSourceEditor(diagnostics)
    }

    val executionState: SourceEditorExecutionState
        get() = executionStateValue.value

    val executionStateProperty: ReadOnlyObjectProperty<SourceEditorExecutionState>
        get() = executionStateValue

    val isRunning: Boolean
        get() = executionState.phase == SourceEditorExecutionPhase.RUNNING

    fun setText(
        value: String,
        markClean: Boolean = false,
    ) {
        editor.setText(value, markClean)
    }

    fun markClean() {
        editor.markClean()
    }

    fun tabSize(size: Int) {
        editor.tabSize(size)
    }

    fun setLineNumbersVisible(visible: Boolean) {
        editor.setLineNumbersVisible(visible)
    }

    fun setWrapText(enabled: Boolean) {
        editor.setWrapText(enabled)
    }

    fun showSearch(query: String = "") {
        editor.showSearchBar(query)
    }

    fun showReplace(
        query: String = "",
        replacement: String = "",
    ) {
        editor.showReplaceBar(query, replacement)
    }

    fun hideSearch() {
        editor.hideSearchBar()
    }

    fun findNext(query: String = editor.searchField.text.orEmpty()): Int =
        editor.findNext(query)

    fun findPrevious(query: String = editor.searchField.text.orEmpty()): Int =
        editor.findPrevious(query)

    fun replaceNext(
        query: String = editor.searchField.text.orEmpty(),
        replacement: String = editor.replaceField.text.orEmpty(),
    ): Boolean =
        editor.replaceNext(query, replacement)

    fun replaceAll(
        query: String = editor.searchField.text.orEmpty(),
        replacement: String = editor.replaceField.text.orEmpty(),
    ): Int =
        editor.replaceAll(query, replacement)

    fun goTo(
        line: Int,
        column: Int = 1,
    ): CodeEditorPosition =
        editor.goTo(line, column)

    fun selectLine(line: Int): CodeEditorPosition =
        editor.selectLine(line)

    fun onDiagnosticSelected(handler: (SourceDiagnostic) -> Unit) {
        diagnosticSelectionHandler = handler
    }

    fun jumpToDiagnostic(diagnostic: SourceDiagnostic): CodeEditorPosition =
        editor.goTo(diagnostic.line, diagnostic.column).also {
            diagnosticSelectionHandler?.invoke(diagnostic)
        }

    fun addAction(node: Node): Node =
        node.also {
            it.styleClass("source-editor-action")
            toolbar.children += it
            refreshToolbarVisibility()
        }

    fun clearActions() {
        toolbar.children.clear()
        refreshToolbarVisibility()
    }

    fun setDiagnostics(diagnostics: Iterable<SourceDiagnostic>) {
        currentDiagnostics.clear()
        currentDiagnostics += diagnostics
        diagnosticsList.children.setAll(currentDiagnostics.map(::diagnosticRow))
        refreshDiagnosticsVisibility()
    }

    fun addDiagnostic(diagnostic: SourceDiagnostic) {
        currentDiagnostics += diagnostic
        diagnosticsList.children += diagnosticRow(diagnostic)
        refreshDiagnosticsVisibility()
    }

    fun clearDiagnostics() {
        currentDiagnostics.clear()
        diagnosticsList.children.clear()
        refreshDiagnosticsVisibility()
    }

    fun setExecutionState(state: SourceEditorExecutionState) {
        executionStateValue.value = state
        refreshStatus()
    }

    fun setStatus(
        message: String,
        tone: ComponentTone,
        phase: SourceEditorExecutionPhase,
    ) {
        setExecutionState(
            SourceEditorExecutionState(
                phase = phase,
                message = message,
                tone = tone,
            ),
        )
    }

    fun markIdle(message: String = "Ready") {
        setExecutionState(
            SourceEditorExecutionState(
                phase = SourceEditorExecutionPhase.IDLE,
                message = message,
                tone = ComponentTone.INFO,
            ),
        )
    }

    fun markRunning(message: String = "Running...") {
        setExecutionState(
            SourceEditorExecutionState(
                phase = SourceEditorExecutionPhase.RUNNING,
                message = message,
                tone = ComponentTone.INFO,
            ),
        )
    }

    fun markSuccess(message: String = "Completed") {
        setExecutionState(
            SourceEditorExecutionState(
                phase = SourceEditorExecutionPhase.SUCCESS,
                message = message,
                tone = ComponentTone.SUCCESS,
            ),
        )
    }

    fun markError(message: String = "Failed") {
        setExecutionState(
            SourceEditorExecutionState(
                phase = SourceEditorExecutionPhase.ERROR,
                message = message,
                tone = ComponentTone.DANGER,
            ),
        )
    }

    fun setResult(
        node: Node?,
        title: String = "Result",
    ) {
        resultContent.children.clear()
        resultHeader.text = title
        if (node != null) {
            resultContent.children += prepareResultNode(node)
        }
        refreshResultVisibility()
    }

    fun addResultNode(node: Node): Node =
        prepareResultNode(node).apply {
            resultContent.children += this
            refreshResultVisibility()
        }

    fun clearResult() {
        resultContent.children.clear()
        refreshResultVisibility()
    }
}
