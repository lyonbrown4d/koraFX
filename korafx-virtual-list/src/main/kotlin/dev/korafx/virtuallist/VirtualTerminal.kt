package dev.korafx.virtuallist

import dev.korafx.dsl.NodeContainerBuilder
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import javafx.util.Callback
import kotlin.math.max

data class VirtualTerminalLine(
    val text: String,
    val styleClasses: List<String> = emptyList(),
)

class VirtualTerminal(
    maxLines: Int = 1_000,
    autoScroll: Boolean = true,
) : VBox() {
    private var maxLinesValue = max(1, maxLines)
    private var lineRenderer: (VirtualTerminalLine) -> Node = { line ->
        Label(line.text).apply {
            styleClass += "virtual-terminal-line-label"
            styleClass += line.styleClasses
        }
    }

    val listView: ListView<VirtualTerminalLine> = ListView<VirtualTerminalLine>()

    var autoScroll: Boolean = autoScroll

    var maxLines: Int
        get() = maxLinesValue
        set(value) {
            maxLinesValue = max(1, value)
            trimToMaxLines()
        }

    val lines: ObservableList<VirtualTerminalLine>
        get() = listView.items

    init {
        styleClass += "virtual-terminal"
        children += listView

        listView.styleClass += "virtual-terminal-list-view"
        listView.prefWidth = Double.MAX_VALUE
        listView.prefHeight = Double.MAX_VALUE
        installCellFactory()
    }

    fun appendLine(
        text: String,
        vararg styleClasses: String,
    ): VirtualTerminalLine =
        appendLine(VirtualTerminalLine(text, styleClasses.toList()))

    fun appendLine(line: VirtualTerminalLine): VirtualTerminalLine {
        lines += line
        trimToMaxLines()
        scrollToEndIfNeeded()
        return line
    }

    fun appendLines(values: Iterable<String>) {
        values.forEach { appendLine(it) }
    }

    fun clear() {
        lines.clear()
    }

    fun setLineRenderer(renderer: (VirtualTerminalLine) -> Node) {
        lineRenderer = renderer
        listView.refresh()
    }

    private fun installCellFactory() {
        listView.setCellFactory(
            Callback {
                object : ListCell<VirtualTerminalLine>() {
                    override fun updateItem(item: VirtualTerminalLine?, empty: Boolean) {
                        super.updateItem(item, empty)

                        if (empty || item == null) {
                            text = null
                            graphic = null
                            styleClass.removeAll(previousStyleClasses)
                            previousStyleClasses = emptyList()
                            return
                        }

                        styleClass.removeAll(previousStyleClasses)
                        previousStyleClasses = item.styleClasses
                        styleClass += item.styleClasses
                        text = null
                        graphic = lineRenderer(item)
                    }

                    private var previousStyleClasses: List<String> = emptyList()
                }
            },
        )
    }

    private fun trimToMaxLines() {
        val overflow = lines.size - maxLinesValue
        if (overflow > 0) {
            lines.remove(0, overflow)
        }
    }

    private fun scrollToEndIfNeeded() {
        if (!autoScroll || lines.isEmpty()) {
            return
        }

        if (Platform.isFxApplicationThread()) {
            listView.scrollTo(lines.size - 1)
        } else {
            Platform.runLater {
                if (lines.isNotEmpty()) {
                    listView.scrollTo(lines.size - 1)
                }
            }
        }
    }
}

class VirtualTerminalBuilder internal constructor(
    private val terminal: VirtualTerminal,
) {
    fun maxLines(value: Int) {
        terminal.maxLines = value
    }

    fun autoScroll(value: Boolean) {
        terminal.autoScroll = value
    }

    fun lineRenderer(renderer: (VirtualTerminalLine) -> Node) {
        terminal.setLineRenderer(renderer)
    }

    fun line(
        text: String,
        vararg styleClasses: String,
    ): VirtualTerminalLine = terminal.appendLine(text, *styleClasses)

    fun clear() {
        terminal.clear()
    }
}

fun virtualTerminal(
    maxLines: Int = 1_000,
    autoScroll: Boolean = true,
    init: VirtualTerminal.() -> Unit = {},
    content: VirtualTerminalBuilder.() -> Unit = {},
): VirtualTerminal =
    VirtualTerminal(maxLines = maxLines, autoScroll = autoScroll)
        .apply(init)
        .apply {
            VirtualTerminalBuilder(this).content()
        }

fun NodeContainerBuilder.virtualTerminal(
    maxLines: Int = 1_000,
    autoScroll: Boolean = true,
    init: VirtualTerminal.() -> Unit = {},
    content: VirtualTerminalBuilder.() -> Unit = {},
): VirtualTerminal =
    add(
        dev.korafx.virtuallist.virtualTerminal(
            maxLines = maxLines,
            autoScroll = autoScroll,
            init = init,
            content = content,
        ),
    )
