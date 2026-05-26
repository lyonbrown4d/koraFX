package dev.korafx.sourceeditor

import dev.korafx.dsl.styleClass
import javafx.scene.control.Label

internal fun CodeEditor.refreshMetrics() {
    val text = textArea.text.orEmpty()
    val position = positionAt(textArea.caretPosition)
    val lines = computeLineCount(text)
    val selectedText = selectedTextOrEmpty()
    val selectedLines = computeSelectedLineCount(selectedText)

    currentLineValue.set(position.line)
    currentColumnValue.set(position.column)
    lineCountValue.set(lines)
    charCountValue.set(text.length)
    selectionLengthValue.set(selectedText.length)
    selectedLineCountValue.set(selectedLines)
    statusLabel.text = buildString {
        append("Ln ${position.line}, Col ${position.column} | $lines lines | ${text.length} chars")
        if (selectedText.isNotEmpty()) {
            append(" | ${selectedText.length} selected")
            if (selectedLines > 1) {
                append(" across $selectedLines lines")
            }
        }
    }
    updateActiveLineNumber(position.line)
}

internal fun CodeEditor.updateDirtyState() {
    val dirty = isDirty
    dirtyBadge.isVisible = dirty
    dirtyBadge.isManaged = dirty
}

internal fun CodeEditor.updateLineNumbers() {
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

internal fun CodeEditor.updateActiveLineNumber(line: Int) {
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

private fun computeLineCount(text: String): Int =
    if (text.isEmpty()) {
        1
    } else {
        text.count { it == '\n' } + 1
    }

private fun computeSelectedLineCount(text: String): Int =
    if (text.isEmpty()) {
        0
    } else {
        text.count { it == '\n' } + 1
    }
