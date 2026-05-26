package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class CodeEditor internal constructor(
    title: String?,
    text: String,
    language: String?,
    readOnly: Boolean,
    placeholder: String?,
    showToolbar: Boolean,
    showStatus: Boolean,
    showLineNumbers: Boolean,
    showSearch: Boolean,
    wrapText: Boolean,
    private val onTextChange: ((String) -> Unit)?,
) : VBox(0.0) {
    val searchBar: HBox = HBox(8.0)
    val searchField: TextField = TextField()
    val replaceField: TextField = TextField()
    val searchResultLabel: Label = Label()
    val editorFrame: HBox = HBox(0.0)
    val lineNumberGutter: VBox = VBox(0.0)
    val textArea: TextArea = TextArea(text)
    val statusLabel: Label = Label()
    val dirtyBadge: Label = Label("Modified").apply {
        styleClasses("badge", "code-editor-dirty", ComponentTone.WARNING.styleClass)
        isVisible = false
        isManaged = false
    }

    internal val currentLineValue = SimpleIntegerProperty(1)
    internal val currentColumnValue = SimpleIntegerProperty(1)
    internal val lineCountValue = SimpleIntegerProperty(1)
    internal val charCountValue = SimpleIntegerProperty(text.length)
    internal val selectionLengthValue = SimpleIntegerProperty(0)
    internal val selectedLineCountValue = SimpleIntegerProperty(0)

    val currentLineProperty: ReadOnlyIntegerProperty
        get() = currentLineValue

    val currentColumnProperty: ReadOnlyIntegerProperty
        get() = currentColumnValue

    val lineCountProperty: ReadOnlyIntegerProperty
        get() = lineCountValue

    val charCountProperty: ReadOnlyIntegerProperty
        get() = charCountValue

    val selectionLengthProperty: ReadOnlyIntegerProperty
        get() = selectionLengthValue

    val selectedLineCountProperty: ReadOnlyIntegerProperty
        get() = selectedLineCountValue

    val currentLine: Int
        get() = currentLineValue.get()

    val currentColumn: Int
        get() = currentColumnValue.get()

    val lineCount: Int
        get() = lineCountValue.get()

    val charCount: Int
        get() = charCountValue.get()

    val selectionLength: Int
        get() = selectionLengthValue.get()

    val selectedLineCount: Int
        get() = selectedLineCountValue.get()

    val isDirty: Boolean
        get() = textArea.text.orEmpty() != cleanText

    var searchIgnoreCase: Boolean = true

    internal var cleanText = text
    internal var indentSize = 4
    internal var suppressTextCallback = false
    internal var lineNumbersVisible = showLineNumbers
    internal var lastSearchQuery = ""
    internal var lastSearchIndex = -1

    init {
        initializeCodeEditor(
            title = title,
            language = language,
            readOnly = readOnly,
            placeholder = placeholder,
            showToolbar = showToolbar,
            showStatus = showStatus,
            showSearch = showSearch,
            wrapText = wrapText,
            onTextChange = onTextChange,
        )
    }

    fun setText(
        value: String,
        markClean: Boolean = false,
    ) {
        suppressTextCallback = true
        textArea.text = value
        suppressTextCallback = false

        if (markClean) {
            cleanText = value
        }

        updateDirtyState()
        refreshMetrics()
        updateLineNumbers()
        updateSearchStatus()
    }

    fun markClean() {
        cleanText = textArea.text.orEmpty()
        updateDirtyState()
    }

    fun tabSize(size: Int) {
        require(size > 0) {
            "Tab size must be positive."
        }
        indentSize = size
    }

    fun setLineNumbersVisible(visible: Boolean) {
        lineNumbersVisible = visible
        lineNumberGutter.isVisible = visible
        lineNumberGutter.isManaged = visible
    }

    fun setWrapText(enabled: Boolean) {
        textArea.isWrapText = enabled
    }

    fun setReadOnly(readOnly: Boolean) {
        textArea.isEditable = !readOnly
    }

    fun goTo(
        line: Int,
        column: Int = 1,
    ): CodeEditorPosition {
        val position = positionFor(line, column)
        textArea.positionCaret(position.offset)
        textArea.requestFocus()
        refreshMetrics()
        return position
    }

    fun selectLine(line: Int): CodeEditorPosition {
        val lineStart = positionFor(line, 1)
        val text = textArea.text.orEmpty()
        val lineEnd = text.indexOf('\n', lineStart.offset).let { index ->
            if (index < 0) text.length else index
        }
        textArea.selectRange(lineStart.offset, lineEnd)
        textArea.requestFocus()
        refreshMetrics()
        return lineStart
    }

    fun showSearchBar(query: String = textArea.selectedText.orEmpty()) {
        setReplaceControlsVisible(false)
        searchBar.isVisible = true
        searchBar.isManaged = true
        if (query.isNotEmpty()) {
            searchField.text = query
            find(query, startAt = 0)
        }
        searchField.requestFocus()
        searchField.selectAll()
    }

    fun showReplaceBar(
        query: String = textArea.selectedText.orEmpty(),
        replacement: String = replaceField.text.orEmpty(),
    ) {
        setReplaceControlsVisible(true)
        searchBar.isVisible = true
        searchBar.isManaged = true
        if (query.isNotEmpty()) {
            searchField.text = query
            find(query, startAt = 0)
        }
        replaceField.text = replacement
        replaceField.requestFocus()
        replaceField.selectAll()
    }

    fun hideSearchBar() {
        searchBar.isVisible = false
        searchBar.isManaged = false
        textArea.requestFocus()
    }

    fun clearSearch() {
        lastSearchQuery = ""
        lastSearchIndex = -1
        searchField.clear()
        textArea.deselect()
        updateSearchStatus()
    }

    fun find(
        query: String = searchField.text.orEmpty(),
        startAt: Int = textArea.caretPosition,
        ignoreCase: Boolean = searchIgnoreCase,
    ): Int =
        findFrom(
            query = query,
            startAt = startAt,
            forward = true,
            ignoreCase = ignoreCase,
            requestEditorFocus = true,
        )

    fun findNext(
        query: String = searchField.text.orEmpty(),
        ignoreCase: Boolean = searchIgnoreCase,
    ): Int {
        val start = if (query == lastSearchQuery && lastSearchIndex >= 0) {
            lastSearchIndex + query.length
        } else {
            textArea.caretPosition
        }
        return findFrom(query, start, forward = true, ignoreCase = ignoreCase, requestEditorFocus = true)
    }

    fun findPrevious(
        query: String = searchField.text.orEmpty(),
        ignoreCase: Boolean = searchIgnoreCase,
    ): Int {
        val start = if (query == lastSearchQuery && lastSearchIndex >= 0) {
            lastSearchIndex - 1
        } else {
            textArea.caretPosition - 1
        }
        return findFrom(query, start, forward = false, ignoreCase = ignoreCase, requestEditorFocus = true)
    }

    fun replaceNext(
        query: String = searchField.text.orEmpty(),
        replacement: String = replaceField.text.orEmpty(),
        ignoreCase: Boolean = searchIgnoreCase,
    ): Boolean =
        replaceNextInEditor(query, replacement, ignoreCase)

    fun replaceAll(
        query: String = searchField.text.orEmpty(),
        replacement: String = replaceField.text.orEmpty(),
        ignoreCase: Boolean = searchIgnoreCase,
    ): Int =
        replaceAllInEditor(query, replacement, ignoreCase)

    fun positionAt(offset: Int): CodeEditorPosition {
        val text = textArea.text.orEmpty()
        val safeOffset = offset.coerceIn(0, text.length)
        val beforeOffset = text.substring(0, safeOffset)
        return CodeEditorPosition(
            line = beforeOffset.count { it == '\n' } + 1,
            column = beforeOffset.substringAfterLast('\n').length + 1,
            offset = safeOffset,
        )
    }
}
