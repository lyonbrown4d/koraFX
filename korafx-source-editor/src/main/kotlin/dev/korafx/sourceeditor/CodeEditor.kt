package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

class CodeEditor internal constructor(
    title: String?,
    text: String,
    language: String?,
    readOnly: Boolean,
    placeholder: String?,
    showToolbar: Boolean,
    showStatus: Boolean,
    private val onTextChange: ((String) -> Unit)?,
) : VBox(0.0) {
    val textArea: TextArea = TextArea(text)

    private val statusLabel = Label()
    private val dirtyBadge = Label("Modified").apply {
        styleClasses("badge", "code-editor-dirty", ComponentTone.WARNING.styleClass)
        isVisible = false
        isManaged = false
    }
    private var cleanText = text
    private var indentSize = 4
    private var suppressTextCallback = false

    val isDirty: Boolean
        get() = textArea.text.orEmpty() != cleanText

    init {
        styleClass("code-editor")
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE

        if (showToolbar) {
            children += toolbar(title, language)
        }

        children += textArea.apply {
            styleClass("code-editor-area")
            promptText = placeholder
            isEditable = !readOnly
            isWrapText = false
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        if (showStatus) {
            children += statusBar()
        }

        installTabIndent()
        textArea.textProperty().addListener { _, _, newValue ->
            updateDirtyState()
            updateStatus()
            if (!suppressTextCallback) {
                onTextChange?.invoke(newValue.orEmpty())
            }
        }
        textArea.caretPositionProperty().addListener { _, _, _ ->
            updateStatus()
        }
        updateDirtyState()
        updateStatus()
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
        updateStatus()
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

    private fun statusBar(): HBox =
        HBox(8.0).apply {
            styleClass("code-editor-status")
            alignment = Pos.CENTER_LEFT
            children += statusLabel.apply {
                styleClass("code-editor-status-text")
            }
        }

    private fun installTabIndent() {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            if (event.code == KeyCode.TAB && textArea.isEditable) {
                textArea.insertText(textArea.caretPosition, " ".repeat(indentSize))
                event.consume()
            }
        }
    }

    private fun updateDirtyState() {
        val dirty = isDirty
        dirtyBadge.isVisible = dirty
        dirtyBadge.isManaged = dirty
    }

    private fun updateStatus() {
        val text = textArea.text.orEmpty()
        val caret = textArea.caretPosition.coerceIn(0, text.length)
        val beforeCaret = text.substring(0, caret)
        val line = beforeCaret.count { it == '\n' } + 1
        val column = beforeCaret.substringAfterLast('\n').length + 1
        val lines = text.count { it == '\n' } + 1

        statusLabel.text = "Ln $line, Col $column | $lines lines | ${text.length} chars"
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
            onTextChange = onTextChange,
            init = init,
        ),
    )
