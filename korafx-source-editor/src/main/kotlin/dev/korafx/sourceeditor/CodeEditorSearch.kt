package dev.korafx.sourceeditor

import javafx.scene.control.Button

internal fun CodeEditor.findFrom(
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

internal fun CodeEditor.setReplaceControlsVisible(visible: Boolean) {
    searchBar.children
        .filter { node ->
            "code-editor-replace-label" in node.styleClass ||
                "code-editor-replace-field" in node.styleClass ||
                ("code-editor-search-button" in node.styleClass && node is Button && node.text in setOf("Replace", "All"))
        }
        .forEach { node ->
            node.isVisible = visible
            node.isManaged = visible
        }
}

internal fun CodeEditor.replaceNextInEditor(
    query: String,
    replacement: String,
    ignoreCase: Boolean,
): Boolean {
    if (!textArea.isEditable || query.isEmpty()) {
        return false
    }

    val selected = textArea.selectedText.orEmpty()
    val selectionMatches = selected.equals(query, ignoreCase = ignoreCase)
    val matchIndex = when {
        query == lastSearchQuery && lastSearchIndex >= 0 && matchesAt(lastSearchIndex, query, ignoreCase) -> {
            lastSearchIndex
        }
        selectionMatches -> {
            textArea.selection.start
        }
        else -> {
            find(query, textArea.caretPosition, ignoreCase)
        }
    }

    if (matchIndex < 0) {
        return false
    }

    textArea.replaceText(matchIndex, matchIndex + query.length, replacement)
    lastSearchQuery = query
    lastSearchIndex = matchIndex
    textArea.selectRange(matchIndex, matchIndex + replacement.length)
    updateSearchStatus()
    return true
}

internal fun CodeEditor.replaceAllInEditor(
    query: String,
    replacement: String,
    ignoreCase: Boolean,
): Int {
    if (!textArea.isEditable || query.isEmpty()) {
        return 0
    }

    val source = textArea.text.orEmpty()
    val searchableSource = if (ignoreCase) source.lowercase() else source
    val searchableQuery = if (ignoreCase) query.lowercase() else query
    var index = searchableSource.indexOf(searchableQuery)
    if (index < 0) {
        lastSearchQuery = query
        lastSearchIndex = -1
        updateSearchStatus()
        return 0
    }

    val result = StringBuilder(source.length)
    var consumed = 0
    var count = 0
    while (index >= 0) {
        result.append(source, consumed, index)
        result.append(replacement)
        consumed = index + query.length
        count += 1
        index = searchableSource.indexOf(searchableQuery, consumed)
    }
    result.append(source, consumed, source.length)

    textArea.text = result.toString()
    lastSearchQuery = query
    lastSearchIndex = -1
    updateSearchStatus()
    return count
}

internal fun CodeEditor.matchesAt(
    offset: Int,
    query: String,
    ignoreCase: Boolean,
): Boolean {
    val text = textArea.text.orEmpty()
    if (offset < 0 || offset + query.length > text.length) {
        return false
    }
    return text.regionMatches(offset, query, 0, query.length, ignoreCase = ignoreCase)
}

internal fun CodeEditor.updateSearchStatus() {
    val query = lastSearchQuery.ifEmpty {
        searchField.text.orEmpty()
    }
    searchResultLabel.text = when {
        query.isEmpty() -> ""
        lastSearchIndex >= 0 -> "Match at ${positionAt(lastSearchIndex).line}:${positionAt(lastSearchIndex).column}"
        else -> "No match"
    }
}

internal fun CodeEditor.selectedTextOrEmpty(): String =
    textArea.selectedText.orEmpty()
