package dev.korafx.sourceeditor

data class CodeEditorPosition(
    val line: Int,
    val column: Int,
    val offset: Int,
)

internal fun CodeEditor.positionFor(
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
