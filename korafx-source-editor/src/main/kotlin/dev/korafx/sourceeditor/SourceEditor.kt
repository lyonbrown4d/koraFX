package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

data class SourceDiagnostic(
    val line: Int,
    val column: Int,
    val message: String,
    val tone: ComponentTone = ComponentTone.WARNING,
)

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

        diagnosticsPane.apply {
            styleClass("source-editor-diagnostics")
            isVisible = false
            isManaged = false
            children += diagnosticsHeader.apply {
                styleClass("source-editor-diagnostics-title")
            }
            children += diagnosticsList.apply {
                styleClass("source-editor-diagnostics-list")
            }
        }

        resultPane.apply {
            styleClass("source-editor-result")
            isVisible = false
            isManaged = false
            children += resultHeader.apply {
                styleClass("source-editor-result-title")
            }
            children += resultContent.apply {
                styleClass("source-editor-result-content")
            }
        }

        children += toolbar
        children += editor
        children += diagnosticsPane
        children += resultPane

        setDiagnostics(diagnostics)
    }

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

    fun setResult(
        node: Node?,
        title: String = "Result",
    ) {
        resultContent.children.clear()
        resultHeader.text = title
        if (node != null) {
            node.styleClass("source-editor-result-node")
            resultContent.children += node
        }
        refreshResultVisibility()
    }

    fun addResultNode(node: Node): Node =
        node.apply {
            styleClass("source-editor-result-node")
            resultContent.children += this
            refreshResultVisibility()
        }

    fun clearResult() {
        resultContent.children.clear()
        refreshResultVisibility()
    }

    private fun diagnosticRow(diagnostic: SourceDiagnostic): HBox =
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

    private fun refreshToolbarVisibility() {
        val visible = toolbar.children.isNotEmpty()
        toolbar.isVisible = visible
        toolbar.isManaged = visible
    }

    private fun refreshDiagnosticsVisibility() {
        val visible = diagnosticsList.children.isNotEmpty()
        diagnosticsPane.isVisible = visible
        diagnosticsPane.isManaged = visible
    }

    private fun refreshResultVisibility() {
        val visible = resultContent.children.isNotEmpty()
        resultPane.isVisible = visible
        resultPane.isManaged = visible
    }
}

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

fun sourceEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onTextChange: ((String) -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: SourceEditorBuilder.() -> Unit = {},
): SourceEditor =
    SourceEditor(
        title = title,
        text = text,
        language = language,
        readOnly = readOnly,
        placeholder = placeholder,
        showLineNumbers = showLineNumbers,
        showSearch = showSearch,
        wrapText = wrapText,
        diagnostics = diagnostics,
        onTextChange = onTextChange,
    ).apply(init).apply {
        SourceEditorBuilder(this).content()
    }

fun queryEditor(
    title: String? = "Query.sql",
    text: String = "",
    placeholder: String? = "Write SQL...",
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onRun: ((String) -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: QueryEditorBuilder.() -> Unit = {},
): SourceEditor =
    SourceEditor(
        title = title,
        text = text,
        language = "sql",
        readOnly = false,
        placeholder = placeholder,
        showLineNumbers = showLineNumbers,
        showSearch = showSearch,
        wrapText = wrapText,
        diagnostics = diagnostics,
        onTextChange = null,
    ).apply(init).apply {
        val builder = SourceEditorBuilder(this)
        if (onRun != null) {
            builder.action("Run") {
                onRun(this.editor.textArea.text.orEmpty())
            }
        }
        if (onStop != null) {
            builder.action("Stop") {
                onStop()
            }
        }
        QueryEditorBuilder(this).content()
    }

fun NodeContainerBuilder.sourceEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onTextChange: ((String) -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: SourceEditorBuilder.() -> Unit = {},
): SourceEditor =
    add(
        dev.korafx.sourceeditor.sourceEditor(
            title = title,
            text = text,
            language = language,
            readOnly = readOnly,
            placeholder = placeholder,
            showLineNumbers = showLineNumbers,
            showSearch = showSearch,
            wrapText = wrapText,
            diagnostics = diagnostics,
            onTextChange = onTextChange,
            init = init,
            content = content,
        ),
    )

fun NodeContainerBuilder.queryEditor(
    title: String? = "Query.sql",
    text: String = "",
    placeholder: String? = "Write SQL...",
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onRun: ((String) -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: QueryEditorBuilder.() -> Unit = {},
): SourceEditor =
    add(
        dev.korafx.sourceeditor.queryEditor(
            title = title,
            text = text,
            placeholder = placeholder,
            showLineNumbers = showLineNumbers,
            showSearch = showSearch,
            wrapText = wrapText,
            diagnostics = diagnostics,
            onRun = onRun,
            onStop = onStop,
            init = init,
            content = content,
        ),
    )
