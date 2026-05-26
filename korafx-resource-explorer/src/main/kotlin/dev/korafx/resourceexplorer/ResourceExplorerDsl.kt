package dev.korafx.resourceexplorer

import dev.korafx.dsl.NodeContainerBuilder
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.MouseButton

class ResourceExplorerBuilder<T> internal constructor(
    private val explorer: ResourceExplorer<T>,
) {
    fun items(items: Iterable<T>) {
        explorer.setItems(items)
    }

    fun children(childrenOf: (T) -> Iterable<T>) {
        explorer.setChildrenProvider(childrenOf)
    }

    fun text(textOf: (T) -> String) {
        explorer.setTextRenderer(textOf)
    }

    fun graphic(graphicOf: (T) -> Node?) {
        explorer.setGraphicRenderer(graphicOf)
    }

    fun secondaryText(secondaryTextOf: (T) -> String?) {
        explorer.setSecondaryTextRenderer(secondaryTextOf)
    }

    fun status(statusTextOf: (T) -> String?) {
        explorer.setStatusTextRenderer(statusTextOf)
    }

    fun emptyState(text: String) {
        explorer.setEmptyStateText(text)
    }

    fun search(
        prompt: String = "Search resources...",
        visible: Boolean = true,
    ) {
        explorer.searchField.promptText = prompt
        explorer.setSearchVisible(visible)
    }

    fun hideSearch() {
        explorer.setSearchVisible(false)
    }

    fun breadcrumb(
        separator: String = " / ",
        visible: Boolean = true,
    ) {
        explorer.setBreadcrumbSeparator(separator)
        explorer.setBreadcrumbVisible(visible)
    }

    fun hideBreadcrumb() {
        explorer.setBreadcrumbVisible(false)
    }

    fun onSelect(handler: (T?) -> Unit) {
        explorer.onSelect(handler)
    }

    fun clearSelection() {
        explorer.clearSelection()
    }

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        explorer.rowAction(clickCount, mouseButton, handler)
    }

    fun contextMenuFor(provider: (T) -> ContextMenu?) {
        explorer.setContextMenuProvider(provider)
    }

    fun contextMenu(content: ResourceContextMenuBuilder<T>.(T) -> Unit) {
        explorer.setContextMenuProvider { item ->
            ResourceContextMenuBuilder(item).apply {
                content(item)
            }.build()
        }
    }
}

class ResourceContextMenuBuilder<T> internal constructor(
    private val resource: T,
) {
    private val items = mutableListOf<MenuItem>()

    fun item(menuItem: MenuItem): MenuItem =
        menuItem.also {
            items += it
        }

    fun actionItem(
        text: String,
        action: (T) -> Unit,
    ): MenuItem =
        item(
            MenuItem(text).apply {
                setOnAction {
                    action(resource)
                }
            },
        )

    fun separator() {
        items += SeparatorMenuItem()
    }

    fun build(): ContextMenu? =
        if (items.isEmpty()) {
            null
        } else {
            ContextMenu(*items.toTypedArray())
        }
}

fun <T> resourceExplorer(
    items: Iterable<T> = emptyList(),
    childrenOf: (T) -> Iterable<T> = { emptyList() },
    textOf: (T) -> String = { it.toString() },
    showSearch: Boolean = true,
    searchPrompt: String = "Search resources...",
    init: ResourceExplorer<T>.() -> Unit = {},
    content: ResourceExplorerBuilder<T>.() -> Unit = {},
): ResourceExplorer<T> =
    ResourceExplorer(
        items = items,
        childrenOf = childrenOf,
        textOf = textOf,
        showSearch = showSearch,
        searchPrompt = searchPrompt,
    ).apply(init).apply {
        ResourceExplorerBuilder(this).content()
    }

fun <T> NodeContainerBuilder.resourceExplorer(
    items: Iterable<T> = emptyList(),
    childrenOf: (T) -> Iterable<T> = { emptyList() },
    textOf: (T) -> String = { it.toString() },
    showSearch: Boolean = true,
    searchPrompt: String = "Search resources...",
    init: ResourceExplorer<T>.() -> Unit = {},
    content: ResourceExplorerBuilder<T>.() -> Unit = {},
): ResourceExplorer<T> =
    add(
        dev.korafx.resourceexplorer.resourceExplorer(
            items = items,
            childrenOf = childrenOf,
            textOf = textOf,
            showSearch = showSearch,
            searchPrompt = searchPrompt,
            init = init,
            content = content,
        ),
    )
