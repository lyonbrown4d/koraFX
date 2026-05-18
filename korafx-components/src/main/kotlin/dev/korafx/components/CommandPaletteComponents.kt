package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

data class CommandPaletteCommand(
    val id: String,
    val title: String,
    val description: String? = null,
    val group: String? = null,
    val action: () -> Unit = {},
)

class CommandPaletteHost(
    commands: Iterable<CommandPaletteCommand> = emptyList(),
) {
    private val visible = SimpleBooleanProperty(false)

    val commands = FXCollections.observableArrayList<CommandPaletteCommand>()

    val visibleProperty: ReadOnlyBooleanProperty
        get() = visible

    val isVisible: Boolean
        get() = visible.get()

    init {
        setCommands(commands)
    }

    fun show() {
        visible.set(true)
    }

    fun hide() {
        visible.set(false)
    }

    fun toggle() {
        visible.set(!visible.get())
    }

    fun setCommands(commands: Iterable<CommandPaletteCommand>) {
        this.commands.setAll(commands.toList())
    }

    fun addCommand(command: CommandPaletteCommand): CommandPaletteCommand =
        command.also {
            commands += it
        }

    fun removeCommand(id: String): Boolean =
        commands.removeIf { it.id == id }
}

class CommandPalette internal constructor(
    val host: CommandPaletteHost,
    emptyText: String,
    searchPrompt: String,
) : StackPane() {
    val scrim: StackPane = StackPane()
    val card: VBox = VBox(10.0)
    val searchField: TextField = TextField()
    val results: VBox = VBox(4.0)
    val emptyLabel: Label = Label(emptyText)

    private var filteredCommands: List<CommandPaletteCommand> = emptyList()
    private var selectedIndex = -1
    private var emptyText = emptyText

    init {
        styleClass("command-palette")
        isPickOnBounds = false
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE
        isVisible = host.isVisible
        isManaged = host.isVisible

        scrim.apply {
            styleClass("command-palette-scrim")
            isPickOnBounds = true
            setOnMouseClicked {
                host.hide()
            }
        }

        card.apply {
            styleClass("command-palette-card")
            maxWidth = 560.0
            alignment = Pos.TOP_LEFT
            StackPane.setAlignment(this, Pos.TOP_CENTER)
            StackPane.setMargin(this, Insets(48.0, 16.0, 0.0, 16.0))
        }

        searchField.apply {
            styleClass("command-palette-search")
            promptText = searchPrompt
            maxWidth = Double.MAX_VALUE
            textProperty().addListener { _, _, _ ->
                rebuild()
            }
            setOnKeyPressed { event ->
                when (event.code) {
                    KeyCode.DOWN -> {
                        selectNext()
                        event.consume()
                    }
                    KeyCode.UP -> {
                        selectPrevious()
                        event.consume()
                    }
                    KeyCode.ENTER -> {
                        executeSelected()
                        event.consume()
                    }
                    KeyCode.ESCAPE -> {
                        host.hide()
                        event.consume()
                    }
                    else -> Unit
                }
            }
        }

        emptyLabel.apply {
            styleClass("command-palette-empty")
            isWrapText = true
        }
        results.styleClass("command-palette-results")

        card.children += searchField
        card.children += results
        children += scrim
        children += card

        host.visibleProperty.addListener { _, _, visible ->
            isVisible = visible
            isManaged = visible
            if (visible) {
                searchField.requestFocus()
            } else {
                searchField.clear()
            }
        }
        host.commands.addListener(ListChangeListener {
            rebuild()
        })

        rebuild()
    }

    fun setSearchText(text: String) {
        searchField.text = text
    }

    fun setEmptyText(text: String) {
        emptyText = text
        emptyLabel.text = text
        rebuild()
    }

    fun selectNext() {
        if (filteredCommands.isEmpty()) {
            selectedIndex = -1
            return
        }
        selectedIndex = (selectedIndex + 1).floorMod(filteredCommands.size)
        refreshSelectionStyles()
    }

    fun selectPrevious() {
        if (filteredCommands.isEmpty()) {
            selectedIndex = -1
            return
        }
        selectedIndex = (selectedIndex - 1).floorMod(filteredCommands.size)
        refreshSelectionStyles()
    }

    fun executeSelected(): Boolean {
        val command = filteredCommands.getOrNull(selectedIndex) ?: return false
        execute(command)
        return true
    }

    fun execute(commandId: String): Boolean {
        val command = host.commands.firstOrNull { it.id == commandId } ?: return false
        execute(command)
        return true
    }

    private fun rebuild() {
        val query = searchField.text.orEmpty().trim()
        filteredCommands =
            if (query.isBlank()) {
                host.commands.toList()
            } else {
                host.commands.filter { command ->
                    command.title.contains(query, ignoreCase = true) ||
                        command.description.orEmpty().contains(query, ignoreCase = true) ||
                        command.group.orEmpty().contains(query, ignoreCase = true)
                }
            }
        selectedIndex =
            if (filteredCommands.isEmpty()) {
                -1
            } else {
                selectedIndex.coerceIn(0, filteredCommands.lastIndex).takeIf { selectedIndex >= 0 } ?: 0
            }

        results.children.clear()
        if (filteredCommands.isEmpty()) {
            results.children += emptyLabel
            return
        }

        var currentGroup: String? = null
        filteredCommands.forEachIndexed { index, command ->
            val group = command.group
            if (!group.isNullOrBlank() && group != currentGroup) {
                currentGroup = group
                results.children += Label(group).apply {
                    styleClass("command-palette-group")
                }
            }
            results.children += row(command, selected = index == selectedIndex)
        }
    }

    private fun row(
        command: CommandPaletteCommand,
        selected: Boolean,
    ): Button =
        Button().apply {
            styleClass("command-palette-row")
            if (selected) {
                styleClass("command-palette-row-selected")
            }
            maxWidth = Double.MAX_VALUE
            contentDisplay = javafx.scene.control.ContentDisplay.GRAPHIC_ONLY
            graphic = HBox(10.0).apply {
                styleClass("command-palette-row-content")
                alignment = Pos.CENTER_LEFT
                children += VBox(3.0).apply {
                    styleClass("command-palette-row-text")
                    HBox.setHgrow(this, Priority.ALWAYS)
                    children += Label(command.title).apply {
                        styleClass("command-palette-title")
                        isWrapText = true
                    }
                    val description = command.description
                    if (!description.isNullOrBlank()) {
                        children += Label(description).apply {
                            styleClass("command-palette-description")
                            isWrapText = true
                        }
                    }
                }
                children += Label(command.id).apply {
                    styleClass("command-palette-id")
                }
            }
            onAction {
                execute(command)
            }
        }

    private fun execute(command: CommandPaletteCommand) {
        command.action()
        host.hide()
    }

    private fun refreshSelectionStyles() {
        var commandIndex = 0
        results.children.forEach { node ->
            if (node is Button && "command-palette-row" in node.styleClass) {
                node.styleClass.remove("command-palette-row-selected")
                if (commandIndex == selectedIndex) {
                    node.styleClass("command-palette-row-selected")
                }
                commandIndex += 1
            }
        }
    }

    private fun Int.floorMod(size: Int): Int =
        ((this % size) + size) % size
}

class CommandPaletteBuilder internal constructor(
    private val host: CommandPaletteHost,
    private val palette: CommandPalette,
) {
    fun command(
        id: String,
        title: String,
        description: String? = null,
        group: String? = null,
        action: () -> Unit = {},
    ): CommandPaletteCommand =
        host.addCommand(
            CommandPaletteCommand(
                id = id,
                title = title,
                description = description,
                group = group,
                action = action,
            ),
        )

    fun commands(commands: Iterable<CommandPaletteCommand>) {
        host.setCommands(commands)
    }

    fun emptyState(text: String) {
        palette.setEmptyText(text)
    }
}

fun commandPalette(
    host: CommandPaletteHost,
    emptyText: String = "No commands found",
    searchPrompt: String = "Search commands...",
    init: CommandPalette.() -> Unit = {},
    content: CommandPaletteBuilder.() -> Unit = {},
): CommandPalette =
    CommandPalette(
        host = host,
        emptyText = emptyText,
        searchPrompt = searchPrompt,
    ).apply(init).apply {
        CommandPaletteBuilder(host, this).content()
    }

fun NodeContainerBuilder.commandPalette(
    host: CommandPaletteHost,
    emptyText: String = "No commands found",
    searchPrompt: String = "Search commands...",
    init: CommandPalette.() -> Unit = {},
    content: CommandPaletteBuilder.() -> Unit = {},
): CommandPalette =
    add(
        dev.korafx.components.commandPalette(
            host = host,
            emptyText = emptyText,
            searchPrompt = searchPrompt,
            init = init,
            content = content,
        ),
    )
