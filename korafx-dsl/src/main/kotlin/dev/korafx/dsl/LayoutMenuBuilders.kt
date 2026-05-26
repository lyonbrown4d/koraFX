package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.ToolBar

class ToolBarBuilder internal constructor(
    private val toolBar: ToolBar,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        toolBar.items += node
    }
}

class ScrollPaneBuilder internal constructor(
    private val scrollPane: ScrollPane,
) {
    fun content(node: Node) {
        scrollPane.content = node
    }

    fun content(factory: () -> Node) {
        content(factory())
    }
}

class SplitPaneBuilder internal constructor(
    private val splitPane: SplitPane,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        splitPane.items += node
    }
}

class TabPaneBuilder internal constructor(
    private val tabPane: TabPane,
) {
    fun tab(
        title: String,
        closable: Boolean = false,
        init: Tab.() -> Unit = {},
        content: () -> Node,
    ): Tab =
        Tab(title).apply {
            isClosable = closable
            init()
            this.content = content()
        }.also {
            tabPane.tabs += it
        }
}

class MenuBarBuilder internal constructor(
    private val menuBar: MenuBar,
) {
    fun menu(
        text: String,
        init: MenuBuilder.() -> Unit,
    ): Menu =
        Menu(text).also {
            MenuBuilder(it).init()
            menuBar.menus += it
        }
}

open class MenuItemsBuilder internal constructor(
    private val items: MutableList<MenuItem>,
) {
    fun item(
        text: String,
        init: MenuItem.() -> Unit = {},
    ): MenuItem =
        MenuItem(text).apply(init).also {
            items += it
        }

    fun actionItem(
        text: String,
        action: () -> Unit,
    ): MenuItem =
        item(text) {
            onAction(action)
        }

    fun actionItem(
        text: String,
        init: MenuItem.() -> Unit,
        action: () -> Unit,
    ): MenuItem =
        item(text) {
            init()
            onAction(action)
        }

    fun checkItem(
        text: String,
        init: CheckMenuItem.() -> Unit = {},
    ): CheckMenuItem =
        CheckMenuItem(text).apply(init).also {
            items += it
        }

    fun radioItem(
        text: String,
        toggleGroup: javafx.scene.control.ToggleGroup? = null,
        init: RadioMenuItem.() -> Unit = {},
    ): RadioMenuItem =
        RadioMenuItem(text).apply {
            this.toggleGroup = toggleGroup
            init()
        }.also {
            items += it
        }

    fun submenu(
        text: String,
        init: MenuBuilder.() -> Unit,
    ): Menu =
        Menu(text).also {
            MenuBuilder(it).init()
            items += it
        }

    fun separator() {
        items += SeparatorMenuItem()
    }
}

class MenuBuilder internal constructor(
    private val menu: Menu,
) : MenuItemsBuilder(menu.items)
