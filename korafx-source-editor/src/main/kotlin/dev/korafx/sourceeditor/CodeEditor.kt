package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

data class CodeEditorPosition(
    val line: Int,
    val column: Int,
    val offset: Int,
)

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

    private val currentLineValue = SimpleIntegerProperty(1)
    private val currentColumnValue = SimpleIntegerProperty(1)
    private val lineCountValue = SimpleIntegerProperty(1)
    private val charCountValue = SimpleIntegerProperty(text.length)

    val currentLineProperty: ReadOnlyIntegerProperty
        get() = currentLineValue

    val currentColumnProperty: ReadOnlyIntegerProperty
        get() = currentColumnValue

    val lineCountProperty: ReadOnlyIntegerProperty
        get() = lineCountValue

    val charCountProperty: ReadOnlyIntegerProperty
        get() = charCountValue

    val currentLine: Int
        get() = currentLineValue.get()

    val currentColumn: Int
        get() = currentColumnValue.get()

    val lineCount: Int
        get() = lineCountValue.get()

    val charCount: Int
        get() = charCountValue.get()

    val isDirty: Boolean
        get() = textArea.text.orEmpty() != cleanText

    var searchIgnoreCase: Boolean = true

    private var cleanText = text
    private var indentSize = 4
    private var suppressTextCallback = false
    private var lineNumbersVisible = showLineNumbers
    private var lastSearchQuery = ""
    private var lastSearchIndex = -1

    init {
        styleClass("code-editor")
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE

        if (showToolbar) {
            children += toolbar(title, language)
        }

        children += createSearchBar().apply {
            isVisible = showSearch
            isManaged = showSearch
        }

        children += editorFrame.apply {
            styleClass("code-editor-frame")
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            VBox.setVgrow(this, Priority.ALWAYS)
            children += lineNumberGutter.apply {
                styleClass("code-editor-line-numbers")
                isVisible = lineNumbersVisible
                isManaged = lineNumbersVisible
            }
            children += textArea.apply {
                styleClass("code-editor-area")
                promptText = placeholder
                isEditable = !readOnly
                isWrapText = wrapText
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
                HBox.setHgrow(this, Priority.ALWAYS)
            }
        }

        if (showStatus) {
            children += statusBar()
        }

        installKeyboardShortcuts()
        textArea.textProperty().addListener { _, _, newValue ->
            updateDirtyState()
            refreshMetrics()
            updateLineNumbers()
            if (!suppressTextCallback) {
                onTextChange?.invoke(newValue.orEmpty())
            }
        }
        textArea.caretPositionProperty().addListener { _, _, _ ->
            refreshMetrics()
        }
        refreshMetrics()
        updateDirtyState()
        updateLineNumbers()
        updateSearchStatus()
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

    fun showSearchBar(query: String = selectedTextOrEmpty()) {
        searchBar.isVisible = true
        searchBar.isManaged = true
        if (query.isNotEmpty()) {
            searchField.text = query
            find(query)
        }
        searchField.requestFocus()
        searchField.selectAll()
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

    private fun toolbar(
        title: String?,
        language: String?,
    ): HBox =
        HBox(8.0).apply {
            styleClass("code-editor-toolbar")
            alignment = Pos.CENTER_LEFT
            if (title != null) {
                children += Label(title).apply {
                    styleClass("code-editor-title")
                }
            }
            children += Region().apply {
                HBox.setHgrow(this, Priority.ALWAYS)
            }
            if (language != null) {
                children += badge(language, ComponentTone.INFO) {
                    styleClass("code-editor-language")
                }
            }
            children += dirtyBadge
        }

    private fun createSearchBar(): HBox =
        searchBar.apply {
            styleClass("code-editor-search")
            alignment = Pos.CENTER_LEFT
            children += Label("Find").apply {
                styleClass("code-editor-search-label")
            }
            children += searchField.apply {
                styleClass("code-editor-search-field")
                promptText = "Search..."
                HBox.setHgrow(this, Priority.ALWAYS)
                setOnAction {
                    findNext()
                }
                textProperty().addListener { _, _, value ->
                    if (value.isNullOrEmpty()) {
                        updateSearchStatus()
                    } else {
                        findFrom(
                            query = value,
                            startAt = 0,
                            forward = true,
                            ignoreCase = searchIgnoreCase,
                            requestEditorFocus = false,
                        )
                    }
                }
            }
            children += Button("Prev").apply {
                styleClass("code-editor-search-button")
                setOnAction {
                    findPrevious()
                }
            }
            children += Button("Next").apply {
                styleClass("code-editor-search-button")
                setOnAction {
                    findNext()
                }
            }
            children += searchResultLabel.apply {
                styleClass("code-editor-search-result")
            }
            children += Button("Close").apply {
                styleClass("code-editor-search-button")
                setOnAction {
                    hideSearchBar()
                }
            }
        }

    private fun statusBar(): HBox =
        HBox(8.0).apply {
            styleClass("code-editor-status")
            alignment = Pos.CENTER_LEFT
            children += statusLabel.apply {
                styleClass("code-editor-status-text")
            }
        }

    private fun installKeyboardShortcuts() {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            when {
                event.isShortcutDown && event.code == KeyCode.F -> {
                    showSearchBar()
                    event.consume()
                }
                event.code == KeyCode.ESCAPE && searchBar.isVisible -> {
                    hideSearchBar()
                    event.consume()
                }
                event.code == KeyCode.F3 -> {
                    findNext()
                    event.consume()
                }
                event.code == KeyCode.TAB && textArea.isEditable -> {
                    textArea.insertText(textArea.caretPosition, " ".repeat(indentSize))
                    event.consume()
                }
            }
        }
    }

    private fun findFrom(
        query: String,
        startAt: Int,
        forward: Boolean,
        ignoreCase: Boolean,
        requestEditorFocus: Boolean,
    ): Int {
        if (query.isEmpty()) {
            lastSearchQuery = ""
            lastSearchIndex = -1
            updateSearchStatus()
            return -1
        }

        val text = textArea.text.orEmpty()
        val searchableText = if (ignoreCase) text.lowercase() else text
        val searchableQuery = if (ignoreCase) query.lowercase() else query
        val safeStart = startAt.coerceIn(0, text.length)
        val index = if (forward) {
            searchableText.indexOf(searchableQuery, safeStart).let { first ->
                if (first >= 0) first else searchableText.indexOf(searchableQuery, 0)
            }
        } else {
            searchableText.lastIndexOf(searchableQuery, safeStart).let { first ->
                if (first >= 0) first else searchableText.lastIndexOf(searchableQuery)
            }
        }

        lastSearchQuery = query
        lastSearchIndex = index
        if (index >= 0) {
            textArea.selectRange(index, index + query.length)
            if (requestEditorFocus) {
                textArea.requestFocus()
            }
        }
        updateSearchStatus()
        return index
    }

    private fun positionFor(
        line: Int,
        column: Int,
    ): CodeEditorPosition {
        require(line > 0) {
            "Line must be positive."
        }
        require(column > 0) {
            "Column must be positive."
        }

        val text = textArea.text.orEmpty()
        var currentLine = 1
        var lineStart = 0
        while (currentLine < line) {
            val nextBreak = text.indexOf('\n', lineStart)
            if (nextBreak < 0) {
                return positionAt(text.length)
            }
            lineStart = nextBreak + 1
            currentLine += 1
        }

        val lineEnd = text.indexOf('\n', lineStart).let { index ->
            if (index < 0) text.length else index
        }
        val offset = (lineStart + column - 1).coerceIn(lineStart, lineEnd)
        return positionAt(offset)
    }

    private fun refreshMetrics() {
        val text = textArea.text.orEmpty()
        val position = positionAt(textArea.caretPosition)
        val lines = computeLineCount(text)

        currentLineValue.set(position.line)
        currentColumnValue.set(position.column)
        lineCountValue.set(lines)
        charCountValue.set(text.length)
        statusLabel.text = "Ln ${position.line}, Col ${position.column} | $lines lines | ${text.length} chars"
        updateActiveLineNumber(position.line)
    }

    private fun updateDirtyState() {
        val dirty = isDirty
        dirtyBadge.isVisible = dirty
        dirtyBadge.isManaged = dirty
    }

    private fun updateLineNumbers() {
        val lines = lineCount
        lineNumberGutter.children.setAll(
            (1..lines).map { line ->
                Label(line.toString()).apply {
                    styleClass("code-editor-line-number")
                    if (line == currentLine) {
                        styleClass("code-editor-line-number-active")
                    }
                }
            },
        )
    }

    private fun updateActiveLineNumber(line: Int) {
        lineNumberGutter.children.forEachIndexed { index, node ->
            val active = index + 1 == line
            val styleClasses = node.styleClass
            if (active && "code-editor-line-number-active" !in styleClasses) {
                styleClasses += "code-editor-line-number-active"
            } else if (!active) {
                styleClasses.remove("code-editor-line-number-active")
            }
        }
    }

    private fun updateSearchStatus() {
        val query = lastSearchQuery.ifEmpty {
            searchField.text.orEmpty()
        }
        searchResultLabel.text = when {
            query.isEmpty() -> ""
            lastSearchIndex >= 0 -> "Match at ${positionAt(lastSearchIndex).line}:${positionAt(lastSearchIndex).column}"
            else -> "No match"
        }
    }

    private fun selectedTextOrEmpty(): String =
        textArea.selectedText.orEmpty()

    private fun computeLineCount(text: String): Int =
        if (text.isEmpty()) {
            1
        } else {
            text.count { it == '\n' } + 1
        }
}

fun codeEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showToolbar: Boolean = true,
    showStatus: Boolean = true,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    onTextChange: ((String) -> Unit)? = null,
    init: CodeEditor.() -> Unit = {},
): CodeEditor =
    CodeEditor(
        title = title,
        text = text,
        language = language,
        readOnly = readOnly,
        placeholder = placeholder,
        showToolbar = showToolbar,
        showStatus = showStatus,
        showLineNumbers = showLineNumbers,
        showSearch = showSearch,
        wrapText = wrapText,
        onTextChange = onTextChange,
    ).apply(init)

fun NodeContainerBuilder.codeEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showToolbar: Boolean = true,
    showStatus: Boolean = true,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    onTextChange: ((String) -> Unit)? = null,
    init: CodeEditor.() -> Unit = {},
): CodeEditor =
    add(
        dev.korafx.sourceeditor.codeEditor(
            title = title,
            text = text,
            language = language,
            readOnly = readOnly,
            placeholder = placeholder,
            showToolbar = showToolbar,
            showStatus = showStatus,
            showLineNumbers = showLineNumbers,
            showSearch = showSearch,
            wrapText = wrapText,
            onTextChange = onTextChange,
            init = init,
        ),
    )
