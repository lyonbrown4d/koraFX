package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.MenuButton
import javafx.scene.control.SplitMenuButton
import javafx.util.StringConverter

class ComboBoxBuilder<T> internal constructor(
    private val comboBox: ComboBox<T>,
) {
    fun items(items: Iterable<T>) {
        comboBox.items.setAll(items.toList())
    }

    fun items(vararg items: T) {
        comboBox.items.setAll(items.toList())
    }

    fun select(item: T) {
        comboBox.selectionModel.select(item)
    }

    fun <R> render(textOf: (T) -> R) {
        comboBox.converter = itemStringConverter(comboBox.items, textOf)
    }

    fun onSelect(handler: (T?) -> Unit) {
        comboBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue)
        }
    }
}

class ChoiceBoxBuilder<T> internal constructor(
    private val choiceBox: ChoiceBox<T>,
) {
    fun items(items: Iterable<T>) {
        choiceBox.items.setAll(items.toList())
    }

    fun items(vararg items: T) {
        choiceBox.items.setAll(items.toList())
    }

    fun select(item: T) {
        choiceBox.selectionModel.select(item)
    }

    fun <R> render(textOf: (T) -> R) {
        choiceBox.converter = itemStringConverter(choiceBox.items, textOf)
    }

    fun onSelect(handler: (T?) -> Unit) {
        choiceBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue)
        }
    }
}

private fun <T, R> itemStringConverter(
    items: Iterable<T>,
    textOf: (T) -> R,
): StringConverter<T> =
    object : StringConverter<T>() {
        override fun toString(item: T?): String =
            item?.let { textOf(it).toString() }.orEmpty()

        override fun fromString(value: String?): T? =
            items.firstOrNull { item -> textOf(item).toString() == value }
    }

class ContextMenuBuilder internal constructor(
    private val contextMenu: ContextMenu,
) : MenuItemsBuilder(contextMenu.items) {
    fun customItem(
        hideOnClick: Boolean = true,
        content: () -> Node,
    ): CustomMenuItem =
        CustomMenuItem(content(), hideOnClick).also {
            contextMenu.items += it
        }
}

class MenuButtonBuilder internal constructor(
    private val menuButton: MenuButton,
) : MenuItemsBuilder(menuButton.items)

class SplitMenuButtonBuilder internal constructor(
    private val splitMenuButton: SplitMenuButton,
) : MenuItemsBuilder(splitMenuButton.items)
