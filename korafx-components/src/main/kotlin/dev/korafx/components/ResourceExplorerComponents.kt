package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.styleClass
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TextField
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class ResourceExplorer<T> internal constructor(
    items: Iterable<T>,
    private var childrenOf: (T) -> Iterable<T>,
    private var textOf: (T) -> String,
    showSearch: Boolean,
    searchPrompt: String,
) : VBox(8.0) {
    val searchField: TextField = TextField()
    val treeView: TreeView<T> = TreeView()

    private val rootItem = TreeItem<T>().apply {
        isExpanded = true
    }
    private var roots: List<T> = items.toList()
    private var graphicOf: ((T) -> Node?)? = null
    private var contextMenuProvider: ((T) -> ContextMenu?)? = null
    private var rowActionHandler: ((T) -> Unit)? = null
    private var rowClickCount: Int = 2
    private var rowMouseButton: MouseButton = MouseButton.PRIMARY

    init {
        styleClass("resource-explorer")
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE

        children += searchField.apply {
            styleClass("resource-explorer-search")
            promptText = searchPrompt
            isVisible = showSearch
            isManaged = showSearch
            maxWidth = Double.MAX_VALUE
            textProperty().addListener { _, _, _ ->
                rebuildTree()
            }
        }

        children += treeView.apply {
            styleClass("resource-explorer-tree")
            root = rootItem
            isShowRoot = false
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        installCellFactory()
        rebuildTree()
    }

    fun setItems(items: Iterable<T>) {
        roots = items.toList()
        rebuildTree()
    }

    fun setChildrenProvider(childrenOf: (T) -> Iterable<T>) {
        this.childrenOf = childrenOf
        rebuildTree()
    }

    fun setTextRenderer(textOf: (T) -> String) {
        this.textOf = textOf
        rebuildTree()
        treeView.refresh()
    }

    fun setGraphicRenderer(graphicOf: ((T) -> Node?)?) {
        this.graphicOf = graphicOf
        rebuildTree()
        treeView.refresh()
    }

    fun setSearchVisible(visible: Boolean) {
        searchField.isVisible = visible
        searchField.isManaged = visible
    }

    fun setSearchText(text: String) {
        searchField.text = text
    }

    fun expandAll() {
        rootItem.expandRecursively()
    }

    fun onSelect(handler: (T?) -> Unit) {
        treeView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue?.value)
        }
    }

    fun rowAction(
        clickCount: Int = 2,
        mouseButton: MouseButton = MouseButton.PRIMARY,
        handler: (T) -> Unit,
    ) {
        rowClickCount = clickCount
        rowMouseButton = mouseButton
        rowActionHandler = handler
    }

    fun setContextMenuProvider(provider: ((T) -> ContextMenu?)?) {
        contextMenuProvider = provider
        treeView.refresh()
    }

    fun createContextMenu(item: T): ContextMenu? =
        contextMenuProvider?.invoke(item)

    private fun installCellFactory() {
        treeView.setCellFactory {
            object : TreeCell<T>() {
                init {
                    styleClass("resource-explorer-cell")
                    setOnMouseClicked { event ->
                        val rowItem = item
                        if (
                            event.button == rowMouseButton &&
                            event.clickCount == rowClickCount &&
                            rowItem != null &&
                            !isEmpty
                        ) {
                            rowActionHandler?.invoke(rowItem)
                        }
                    }
                }

                override fun updateItem(
                    item: T?,
                    empty: Boolean,
                ) {
                    super.updateItem(item, empty)

                    if (empty || item == null) {
                        text = null
                        graphic = null
                        contextMenu = null
                        return
                    }

                    text = textOf(item)
                    graphic = graphicOf?.invoke(item)
                    contextMenu = createContextMenu(item)
                }
            }
        }
    }

    private fun rebuildTree() {
        val query = searchField.text.orEmpty().trim()
        rootItem.children.setAll(roots.mapNotNull { buildTreeItem(it, query) })
        rootItem.isExpanded = true
    }

    private fun buildTreeItem(
        item: T,
        query: String,
    ): TreeItem<T>? {
        val children = childrenOf(item).toList()
        val childItems = children.mapNotNull { child -> buildTreeItem(child, query) }
        val matches = query.isBlank() || textOf(item).contains(query, ignoreCase = true)

        if (!matches && childItems.isEmpty()) {
            return null
        }

        return TreeItem(item, graphicOf?.invoke(item)).apply {
            isExpanded = query.isNotBlank()
            this.children.setAll(childItems)
        }
    }

    private fun TreeItem<T>.expandRecursively() {
        isExpanded = true
        children.forEach { it.expandRecursively() }
    }
}

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

    fun onSelect(handler: (T?) -> Unit) {
        explorer.onSelect(handler)
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
        dev.korafx.components.resourceExplorer(
            items = items,
            childrenOf = childrenOf,
            textOf = textOf,
            showSearch = showSearch,
            searchPrompt = searchPrompt,
            init = init,
            content = content,
        ),
    )
