package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.StackPane

data class TabWorkspaceEntry(
    val id: String,
    val title: String,
    val dirty: Boolean,
)

class TabWorkspace internal constructor(
    emptyText: String,
) : TabPane() {
    val emptyLabel: Label = Label(emptyText)

    private val metadata = mutableMapOf<Tab, TabWorkspaceEntry>()
    private var emptyTab = createEmptyTab(emptyText)
    private var closeHandler: ((String) -> Unit)? = null
    private var selectHandler: ((String) -> Unit)? = null
    private var suppressSelectCallback = false

    val workspaceTabs: List<Tab>
        get() = tabs.filterNot(::isEmptyTab)

    init {
        addStyle("tab-workspace")
        selectionModel.selectedItemProperty().addListener { _, _, selected ->
            if (!suppressSelectCallback && selected != null && !isEmptyTab(selected)) {
                selectHandler?.invoke(metadata[selected]?.id ?: return@addListener)
            }
        }
        refreshEmptyState()
    }

    fun openTab(
        id: String,
        title: String,
        dirty: Boolean = false,
        closable: Boolean = true,
        select: Boolean = true,
        content: () -> Node,
    ): Tab {
        findTab(id)?.let { existing ->
            setTabDirty(id, dirty)
            if (select) {
                selectionModel.select(existing)
            }
            return existing
        }

        hideEmptyState()
        val tab = Tab(title).apply {
            addStyle("tab-workspace-tab")
            isClosable = closable
            this.content = content()
            setOnClosed {
                metadata.remove(this)
                closeHandler?.invoke(id)
                refreshEmptyState()
            }
        }
        metadata[tab] = TabWorkspaceEntry(id, title, dirty)
        applyDirty(tab, dirty)
        suppressSelectCallback = true
        tabs += tab
        suppressSelectCallback = false

        if (select) {
            selectionModel.select(tab)
        }
        refreshEmptyState()
        return tab
    }

    fun closeTab(id: String): Boolean {
        val tab = findTab(id) ?: return false
        tabs.remove(tab)
        metadata.remove(tab)
        closeHandler?.invoke(id)
        refreshEmptyState()
        return true
    }

    fun selectTab(id: String): Boolean {
        val tab = findTab(id) ?: return false
        selectionModel.select(tab)
        return true
    }

    fun setTabDirty(
        id: String,
        dirty: Boolean,
    ): Boolean {
        val tab = findTab(id) ?: return false
        val entry = metadata[tab] ?: return false
        metadata[tab] = entry.copy(dirty = dirty)
        applyDirty(tab, dirty)
        return true
    }

    fun isTabDirty(id: String): Boolean =
        metadata[findTab(id)]?.dirty == true

    fun tabId(tab: Tab): String? =
        metadata[tab]?.id

    fun onClose(handler: (String) -> Unit) {
        closeHandler = handler
    }

    fun onSelect(handler: (String) -> Unit) {
        selectHandler = handler
    }

    fun setEmptyText(text: String) {
        emptyLabel.text = text
        emptyTab.text = text
    }

    private fun findTab(id: String): Tab? =
        metadata.entries.firstOrNull { it.value.id == id }?.key

    private fun createEmptyTab(text: String): Tab =
        Tab(text).apply {
            addStyle("tab-workspace-empty-tab")
            isClosable = false
            isDisable = true
            content = StackPane(emptyLabel.apply {
                addStyle("tab-workspace-empty")
                isWrapText = true
            }).apply {
                addStyle("tab-workspace-empty-pane")
                alignment = Pos.CENTER
            }
        }

    private fun refreshEmptyState() {
        if (workspaceTabs.isEmpty()) {
            if (emptyTab !in tabs) {
                suppressSelectCallback = true
                tabs.setAll(emptyTab)
                selectionModel.select(emptyTab)
                suppressSelectCallback = false
            }
        } else {
            hideEmptyState()
        }
    }

    private fun hideEmptyState() {
        if (emptyTab in tabs) {
            suppressSelectCallback = true
            tabs.remove(emptyTab)
            suppressSelectCallback = false
        }
    }

    private fun isEmptyTab(tab: Tab): Boolean =
        tab === emptyTab

    private fun applyDirty(
        tab: Tab,
        dirty: Boolean,
    ) {
        val dirtyStyle = "tab-workspace-tab-dirty"
        tab.styleClass.remove(dirtyStyle)
        if (dirty) {
            tab.styleClass += dirtyStyle
            tab.graphic = Label("*").apply {
                addStyle("tab-workspace-dirty-marker")
            }
        } else {
            tab.graphic = null
        }
    }
}

class TabWorkspaceBuilder internal constructor(
    private val workspace: TabWorkspace,
) {
    fun tab(
        id: String,
        title: String,
        dirty: Boolean = false,
        closable: Boolean = true,
        select: Boolean = false,
        content: () -> Node,
    ): Tab =
        workspace.openTab(
            id = id,
            title = title,
            dirty = dirty,
            closable = closable,
            select = select,
            content = content,
        )

    fun close(id: String): Boolean =
        workspace.closeTab(id)

    fun select(id: String): Boolean =
        workspace.selectTab(id)

    fun dirty(
        id: String,
        dirty: Boolean = true,
    ): Boolean =
        workspace.setTabDirty(id, dirty)

    fun emptyState(text: String) {
        workspace.setEmptyText(text)
    }

    fun onClose(handler: (String) -> Unit) {
        workspace.onClose(handler)
    }

    fun onSelect(handler: (String) -> Unit) {
        workspace.onSelect(handler)
    }
}

fun tabWorkspace(
    emptyText: String = "No open tabs",
    init: TabWorkspace.() -> Unit = {},
    content: TabWorkspaceBuilder.() -> Unit = {},
): TabWorkspace =
    TabWorkspace(emptyText).apply(init).apply {
        TabWorkspaceBuilder(this).content()
    }

fun NodeContainerBuilder.tabWorkspace(
    emptyText: String = "No open tabs",
    init: TabWorkspace.() -> Unit = {},
    content: TabWorkspaceBuilder.() -> Unit = {},
): TabWorkspace =
    add(
        dev.korafx.components.tabWorkspace(
            emptyText = emptyText,
            init = init,
            content = content,
        ),
    )

private fun Tab.addStyle(styleClass: String) {
    if (styleClass !in this.styleClass) {
        this.styleClass += styleClass
    }
}

private fun Node.addStyle(styleClass: String) {
    if (styleClass !in this.styleClass) {
        this.styleClass += styleClass
    }
}
