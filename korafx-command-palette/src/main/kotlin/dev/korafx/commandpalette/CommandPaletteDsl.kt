package dev.korafx.commandpalette

import dev.korafx.dsl.NodeContainerBuilder

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
        dev.korafx.commandpalette.commandPalette(
            host = host,
            emptyText = emptyText,
            searchPrompt = searchPrompt,
            init = init,
            content = content,
        ),
    )
