package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.dsl.styleClass
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

internal fun CodeEditor.initializeCodeEditor(
    title: String?,
    language: String?,
    readOnly: Boolean,
    placeholder: String?,
    showToolbar: Boolean,
    showStatus: Boolean,
    showSearch: Boolean,
    wrapText: Boolean,
    onTextChange: ((String) -> Unit)?,
) {
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
    installEditorListeners(onTextChange)
    refreshMetrics()
    updateDirtyState()
    updateLineNumbers()
    updateSearchStatus()
}

internal fun CodeEditor.toolbar(
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

internal fun CodeEditor.createSearchBar(): HBox =
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
        addReplaceControls()
        addSearchNavigationControls()
    }

internal fun CodeEditor.statusBar(): HBox =
    HBox(8.0).apply {
        styleClass("code-editor-status")
        alignment = Pos.CENTER_LEFT
        children += statusLabel.apply {
            styleClass("code-editor-status-text")
        }
    }

internal fun CodeEditor.installKeyboardShortcuts() {
    textArea.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
        when {
            event.isShortcutDown && event.code == KeyCode.F -> {
                showSearchBar()
                event.consume()
            }
            event.isShortcutDown && event.code == KeyCode.H -> {
                showReplaceBar()
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

private fun CodeEditor.installEditorListeners(onTextChange: ((String) -> Unit)?) {
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
    textArea.selectedTextProperty().addListener { _, _, _ ->
        refreshMetrics()
    }
}

private fun CodeEditor.addReplaceControls() {
    searchBar.children += Label("Replace").apply {
        styleClass("code-editor-replace-label")
        isVisible = false
        isManaged = false
    }
    searchBar.children += replaceField.apply {
        styleClass("code-editor-replace-field")
        promptText = "Replace..."
        isVisible = false
        isManaged = false
        setOnAction {
            replaceNext()
        }
    }
    searchBar.children += Button("Replace").apply {
        styleClass("code-editor-search-button")
        isVisible = false
        isManaged = false
        setOnAction {
            replaceNext()
        }
    }
    searchBar.children += Button("All").apply {
        styleClass("code-editor-search-button")
        isVisible = false
        isManaged = false
        setOnAction {
            replaceAll()
        }
    }
}

private fun CodeEditor.addSearchNavigationControls() {
    searchBar.children += Button("Prev").apply {
        styleClass("code-editor-search-button")
        setOnAction {
            findPrevious()
        }
    }
    searchBar.children += Button("Next").apply {
        styleClass("code-editor-search-button")
        setOnAction {
            findNext()
        }
    }
    searchBar.children += searchResultLabel.apply {
        styleClass("code-editor-search-result")
    }
    searchBar.children += Button("Close").apply {
        styleClass("code-editor-search-button")
        setOnAction {
            hideSearchBar()
        }
    }
}
