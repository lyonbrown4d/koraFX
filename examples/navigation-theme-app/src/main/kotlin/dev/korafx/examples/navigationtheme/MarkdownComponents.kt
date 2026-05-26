package dev.korafx.examples.navigationtheme

import dev.korafx.dsl.NodeContainerBuilder
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.scene.text.TextFlow

private data class MarkdownListItem(
    val marker: String,
    val text: String,
)

private val headingRegex = Regex("^(#{1,6})\\s+(.*)$")
private val unorderedListRegex = Regex("^\\s*[-*+]\\s+(.*)$")
private val orderedListRegex = Regex("^\\s*\\d+\\.\\s+(.*)$")
private val blockQuoteRegex = Regex("^>\\s?(.*)$")

private const val CODE_BLOCK_MARK = "```"
private val inlineCodeRegex = Regex("`([^`]+)`")

private const val DEFAULT_SPACING = 10.0
private const val MAX_LIST_DEPTH = 4

fun NodeContainerBuilder.markdownDocument(
    markdown: String,
    init: VBox.() -> Unit = {},
) {
    add(
        VBox(DEFAULT_SPACING).apply {
            init()
            styleClass.add("markdown-document")
            maxWidth = Double.MAX_VALUE
            parseMarkdown(markdown)
        },
    )
}

private fun VBox.parseMarkdown(rawText: String) {
    val lines = rawText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')

    var index = 0
    var inCodeBlock = false
    val codeLines = mutableListOf<String>()
    val paragraph = StringBuilder()
    children.clear()

    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotEmpty()) {
            children.add(markdownText(text))
        }
        paragraph.clear()
    }

    fun flushCodeBlock() {
        if (codeLines.isEmpty()) {
            return
        }

        children.add(
            VBox(4.0).apply {
                styleClass.add("markdown-code-block")
                codeLines.forEach { line ->
                    children.add(
                        Label(line.ifBlank { " " }).apply {
                            styleClass.add("markdown-code-line")
                        },
                    )
                }
            },
        )
        codeLines.clear()
    }

    while (index < lines.size) {
        val rawLine = lines[index]
        val line = rawLine.trimEnd()

        if (line.trim() == CODE_BLOCK_MARK) {
            if (inCodeBlock) {
                inCodeBlock = false
                flushCodeBlock()
            } else {
                flushParagraph()
                inCodeBlock = true
            }
            index++
            continue
        }

        if (inCodeBlock) {
            codeLines += line
            index++
            continue
        }

        if (line.isBlank()) {
            flushParagraph()
            index++
            continue
        }

        val headingMatch = headingRegex.matchEntire(line)
        if (headingMatch != null) {
            flushParagraph()
            children.add(markdownHeading(headingMatch.groupValues[2], headingMatch.groupValues[1].length))
            index++
            continue
        }

        val quoteMatch = blockQuoteRegex.matchEntire(line)
        if (quoteMatch != null) {
            flushParagraph()
            children.add(markdownQuote(quoteMatch.groupValues[1]))
            index++
            continue
        }

        val orderedItems = collectOrderedList(lines, index)
        if (orderedItems.isNotEmpty()) {
            flushParagraph()
            children.add(markdownList(orderedItems, ordered = true))
            index += orderedItems.size
            continue
        }

        val unorderedItems = collectUnorderedList(lines, index)
        if (unorderedItems.isNotEmpty()) {
            flushParagraph()
            children.add(markdownList(unorderedItems, ordered = false))
            index += unorderedItems.size
            continue
        }

        paragraph.append(rawLine.trim())
        paragraph.append(' ')
        index++
    }

    flushParagraph()
}

private fun collectUnorderedList(
    lines: List<String>,
    startIndex: Int,
): List<MarkdownListItem> =
    collectListItems(lines, startIndex, unorderedListRegex, "•")

private fun collectOrderedList(
    lines: List<String>,
    startIndex: Int,
): List<MarkdownListItem> =
    collectListItems(lines, startIndex, orderedListRegex, "#.")

private fun collectListItems(
    lines: List<String>,
    startIndex: Int,
    itemRegex: Regex,
    marker: String,
): List<MarkdownListItem> {
    val items = mutableListOf<MarkdownListItem>()
    var index = startIndex
    var count = 0

    while (index < lines.size) {
        val line = lines[index].trim()
        val match = itemRegex.matchEntire(line) ?: break
        val value = match.groupValues[1].trim()
        val itemMarker = if (marker == "#.") "${count + 1}." else marker
        items += MarkdownListItem(itemMarker, value)
        count++
        index++

        if (count >= MAX_LIST_DEPTH * 2000) {
            break
        }
    }

    return items
}

private fun markdownHeading(
    text: String,
    level: Int,
): Label =
    Label(text).apply {
        styleClass += when (level.coerceIn(1, 6)) {
            1 -> "markdown-h1"
            2 -> "markdown-h2"
            3 -> "markdown-h3"
            4 -> "markdown-h4"
            5 -> "markdown-h5"
            else -> "markdown-h6"
        }
        isWrapText = true
    }

private fun markdownText(text: String): TextFlow =
    TextFlow().apply {
        styleClass.add("markdown-paragraph")
        maxWidth = Double.MAX_VALUE
        textAlignment = TextAlignment.LEFT
        children.addAll(parseMarkdownInline(text))
    }

private fun parseMarkdownInline(text: String): List<Node> {
    val segments = mutableListOf<Node>()
    var cursor = 0

    inlineCodeRegex.findAll(text).forEach { match ->
        if (match.range.first > cursor) {
            segments += Text(text.substring(cursor, match.range.first)).apply {
                styleClass.add("markdown-inline")
            }
        }

        segments += Text(match.groupValues[1]).apply {
            styleClass.add("markdown-inline-code")
        }

        cursor = match.range.last + 1
    }

    if (cursor < text.length) {
        segments += Text(text.substring(cursor)).apply {
            styleClass.add("markdown-inline")
        }
    }

    if (segments.isEmpty()) {
        segments += Text(text).apply {
            styleClass.add("markdown-inline")
        }
    }

    return segments
}

private fun markdownQuote(text: String): TextFlow =
    TextFlow().apply {
        styleClass.add("markdown-quote")
        children.addAll(parseMarkdownInline(text))
    }

private fun markdownList(
    items: List<MarkdownListItem>,
    ordered: Boolean,
): VBox =
    VBox(6.0).apply {
        styleClass.add("markdown-list")
        items.forEachIndexed { index, item ->
            val marker = if (ordered) "${index + 1}." else item.marker
            children.add(
                HBox(8.0).apply {
                    styleClass.add("markdown-list-item")
                    children.add(
                        Label(marker).apply {
                            styleClass.add("markdown-list-marker")
                        },
                    )
                    children.add(
                        Label(item.text).apply {
                            styleClass.add("markdown-list-item-text")
                            isWrapText = true
                        },
                    )
                },
            )
        }
    }
