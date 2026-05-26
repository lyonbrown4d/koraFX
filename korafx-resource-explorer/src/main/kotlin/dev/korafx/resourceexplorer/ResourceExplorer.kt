package dev.korafx.resourceexplorer

import dev.korafx.dsl.styleClass
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class ResourceExplorer<T> internal constructor(
    items: Iterable<T>,
    private var childrenOf: (T) -> Iterable<T>,
    internal var textOf: (T) -> String,
    showSearch: Boolean,
    searchPrompt: String,
) : VBox(8.0) {
    val searchField: TextField = TextField()
    val breadcrumbLabel: Label = Label()
    val treeView: TreeView<T> = TreeView()
    val emptyStateLabel: Label = Label("No resources")

    private val rootItem = TreeItem<T>().apply {
        isExpanded = true
    }
    private var roots: List<T> = items.toList()
    internal var graphicOf: ((T) -> Node?)? = null
    internal var secondaryTextOf: ((T) -> String?)? = null
    internal var statusTextOf: ((T) -> String?)? = null
    private var contextMenuProvider: ((T) -> ContextMenu?)? = null
    internal var rowActionHandler: ((T) -> Unit)? = null
    internal var rowClickCount: Int = 2
    internal var rowMouseButton: MouseButton = MouseButton.PRIMARY
    private var breadcrumbSeparator: String = " / "

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

        children += breadcrumbLabel.apply {
            styleClass("resource-explorer-breadcrumb")
            isVisible = false
            isManaged = false
            maxWidth = Double.MAX_VALUE
        }

        children += treeView.apply {
            styleClass("resource-explorer-tree")
            root = rootItem
            isShowRoot = false
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        children += emptyStateLabel.apply {
            styleClass("resource-explorer-empty-state")
            isVisible = false
            isManaged = false
            maxWidth = Double.MAX_VALUE
        }

        installCellFactory()
        treeView.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            updateBreadcrumb()
        }
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

    fun setSecondaryTextRenderer(secondaryTextOf: ((T) -> String?)?) {
        this.secondaryTextOf = secondaryTextOf
        treeView.refresh()
    }

    fun setStatusTextRenderer(statusTextOf: ((T) -> String?)?) {
        this.statusTextOf = statusTextOf
        treeView.refresh()
    }

    fun setEmptyStateText(text: String) {
        emptyStateLabel.text = text
    }

    fun setSearchVisible(visible: Boolean) {
        searchField.isVisible = visible
        searchField.isManaged = visible
    }

    fun setSearchText(text: String) {
        searchField.text = text
    }

    fun setBreadcrumbVisible(visible: Boolean) {
        breadcrumbLabel.isVisible = visible
        breadcrumbLabel.isManaged = visible
        updateBreadcrumb()
    }

    fun setBreadcrumbSeparator(separator: String) {
        breadcrumbSeparator = separator
        updateBreadcrumb()
    }

    fun selectedItem(): T? =
        treeView.selectionModel.selectedItem?.value

    fun selectedPath(): List<T> =
        pathOf(treeView.selectionModel.selectedItem)

    fun selectedPathText(separator: String = breadcrumbSeparator): String =
        selectedPath().joinToString(separator) { textOf(it) }

    fun clearSelection() {
        treeView.selectionModel.clearSelection()
    }

    fun isEmpty(): Boolean =
        roots.isEmpty()

    fun selectPath(path: Iterable<T>): Boolean {
        val target = findTreeItem(path.toList()) ?: return false
        expandAncestors(target)
        treeView.selectionModel.select(target)
        treeView.scrollTo(treeView.getRow(target))
        return true
    }

    fun expandAll() {
        rootItem.expandRecursively()
    }

    fun collapseAll() {
        rootItem.children.forEach { it.collapseRecursively() }
        rootItem.isExpanded = true
    }

    fun expandSelected() {
        treeView.selectionModel.selectedItem?.expandRecursively()
    }

    fun collapseSelected() {
        treeView.selectionModel.selectedItem?.collapseRecursively()
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

    private fun rebuildTree() {
        val query = searchField.text.orEmpty().trim()
        rootItem.children.setAll(roots.mapNotNull { buildTreeItem(it, query) })
        rootItem.isExpanded = true
        updateEmptyState()
        updateBreadcrumb()
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

    private fun TreeItem<T>.collapseRecursively() {
        children.forEach { it.collapseRecursively() }
        isExpanded = false
    }

    private fun updateBreadcrumb() {
        breadcrumbLabel.text = selectedPathText()
    }

    private fun updateEmptyState() {
        val empty = rootItem.children.isEmpty()
        emptyStateLabel.isVisible = empty
        emptyStateLabel.isManaged = empty
        treeView.isVisible = !empty
        treeView.isManaged = !empty
    }

    private fun pathOf(item: TreeItem<T>?): List<T> {
        if (item == null || item === rootItem) {
            return emptyList()
        }

        val path = mutableListOf<T>()
        var current: TreeItem<T>? = item
        while (current != null && current !== rootItem) {
            current.value?.let(path::add)
            current = current.parent
        }
        return path.asReversed()
    }

    private fun findTreeItem(path: List<T>): TreeItem<T>? {
        var current = rootItem
        for (pathItem in path) {
            current = current.children.firstOrNull { it.value == pathItem } ?: return null
        }
        return current
    }

    private fun expandAncestors(item: TreeItem<T>) {
        var current = item.parent
        while (current != null) {
            current.isExpanded = true
            current = current.parent
        }
    }
}
