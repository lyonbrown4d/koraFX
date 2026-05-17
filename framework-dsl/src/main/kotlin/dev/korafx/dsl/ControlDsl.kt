package dev.korafx.dsl

import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.DatePicker
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.PasswordField
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.RadioButton
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.Slider
import javafx.scene.control.SplitMenuButton
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.util.StringConverter
import java.time.LocalDate

fun <T> listView(
    items: Iterable<T> = emptyList(),
    init: ListView<T>.() -> Unit = {},
    content: ListViewBuilder<T>.() -> Unit = {},
): ListView<T> =
    ListView<T>().apply {
        this.items.setAll(items.toList())
        init()
        ListViewBuilder(this).content()
    }

fun <T> comboBox(
    items: Iterable<T> = emptyList(),
    init: ComboBox<T>.() -> Unit = {},
    content: ComboBoxBuilder<T>.() -> Unit = {},
): ComboBox<T> =
    ComboBox<T>().apply {
        this.items.setAll(items.toList())
        init()
        ComboBoxBuilder(this).content()
    }

fun <T> choiceBox(
    items: Iterable<T> = emptyList(),
    init: ChoiceBox<T>.() -> Unit = {},
    content: ChoiceBoxBuilder<T>.() -> Unit = {},
): ChoiceBox<T> =
    ChoiceBox<T>().apply {
        this.items.setAll(items.toList())
        init()
        ChoiceBoxBuilder(this).content()
    }

fun passwordField(
    text: String = "",
    init: PasswordField.() -> Unit = {},
): PasswordField =
    PasswordField().apply {
        this.text = text
        init()
    }

fun slider(
    min: Double = 0.0,
    max: Double = 100.0,
    value: Double = min,
    init: Slider.() -> Unit = {},
): Slider =
    Slider(min, max, value).apply(init)

fun datePicker(
    value: LocalDate? = null,
    init: DatePicker.() -> Unit = {},
): DatePicker =
    DatePicker(value).apply(init)

fun colorPicker(
    value: Color = Color.WHITE,
    init: ColorPicker.() -> Unit = {},
): ColorPicker =
    ColorPicker(value).apply(init)

fun radioButton(
    text: String,
    toggleGroup: ToggleGroup? = null,
    init: RadioButton.() -> Unit = {},
): RadioButton =
    RadioButton(text).apply {
        this.toggleGroup = toggleGroup
        init()
    }

fun toggleButton(
    text: String,
    toggleGroup: ToggleGroup? = null,
    init: ToggleButton.() -> Unit = {},
): ToggleButton =
    ToggleButton(text).apply {
        this.toggleGroup = toggleGroup
        init()
    }

fun toggleGroup(
    init: ToggleGroup.() -> Unit = {},
): ToggleGroup = ToggleGroup().apply(init)

fun contextMenu(
    init: ContextMenuBuilder.() -> Unit,
): ContextMenu =
    ContextMenu().apply {
        ContextMenuBuilder(this).init()
    }

fun menuButton(
    text: String,
    init: MenuButton.() -> Unit = {},
    content: MenuButtonBuilder.() -> Unit = {},
): MenuButton =
    MenuButton(text).apply(init).apply {
        MenuButtonBuilder(this).content()
    }

fun splitMenuButton(
    text: String,
    init: SplitMenuButton.() -> Unit = {},
    content: SplitMenuButtonBuilder.() -> Unit = {},
): SplitMenuButton =
    SplitMenuButton().apply {
        this.text = text
    }.apply(init).apply {
        SplitMenuButtonBuilder(this).content()
    }

class ListViewBuilder<T> internal constructor(
    private val listView: ListView<T>,
) {
    private var textRenderer: ((T) -> String)? = null
    private var nodeRenderer: (CellContentBuilder.(T) -> Unit)? = null
    private var rowClickCount: Int = 2
    private var rowMouseButton: MouseButton = MouseButton.PRIMARY
    private var rowActionHandler: ((T) -> Unit)? = null

    fun items(items: Iterable<T>) {
        listView.items.setAll(items.toList())
    }

    fun items(vararg items: T) {
        listView.items.setAll(items.toList())
    }

    fun <R> render(textOf: (T) -> R) {
        textRenderer = { item -> textOf(item).toString() }
        nodeRenderer = null
        installCellFactory()
    }

    fun cell(content: CellContentBuilder.(T) -> Unit) {
        nodeRenderer = content
        textRenderer = null
        installCellFactory()
    }

    fun selectionMode(mode: SelectionMode) {
        listView.selectionModel.selectionMode = mode
    }

    fun placeholder(text: String, init: javafx.scene.control.Label.() -> Unit = {}) {
        listView.placeholder =
            javafx.scene.control.Label(text).apply(init)
    }

    fun placeholder(node: javafx.scene.Node) {
        listView.placeholder = node
    }

    fun onSelect(handler: (T?) -> Unit) {
        listView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            handler(newValue)
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
        installCellFactory()
    }

    private fun installCellFactory() {
        listView.setCellFactory {
            object : ListCell<T>() {
                init {
                    setOnMouseClicked { event ->
                        if (
                            event.button == rowMouseButton &&
                            event.clickCount == rowClickCount &&
                            item != null &&
                            !isEmpty
                        ) {
                            rowActionHandler?.invoke(item)
                        }
                    }
                }

                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        graphic = null
                        return
                    }

                    val content = nodeRenderer
                    if (content != null) {
                        text = null
                        graphic = StackPane().apply {
                            CellContentBuilder(this).content(item)
                        }
                        return
                    }

                    text = textRenderer?.invoke(item) ?: item.toString()
                    graphic = null
                }
            }
        }
    }
}

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
        content: () -> javafx.scene.Node,
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

fun <T> NodeContainerBuilder.listView(
    items: Iterable<T> = emptyList(),
    init: ListView<T>.() -> Unit = {},
    content: ListViewBuilder<T>.() -> Unit = {},
): ListView<T> = add(dev.korafx.dsl.listView(items, init, content))

fun <T> NodeContainerBuilder.comboBox(
    items: Iterable<T> = emptyList(),
    init: ComboBox<T>.() -> Unit = {},
    content: ComboBoxBuilder<T>.() -> Unit = {},
): ComboBox<T> = add(dev.korafx.dsl.comboBox(items, init, content))

fun <T> NodeContainerBuilder.choiceBox(
    items: Iterable<T> = emptyList(),
    init: ChoiceBox<T>.() -> Unit = {},
    content: ChoiceBoxBuilder<T>.() -> Unit = {},
): ChoiceBox<T> = add(dev.korafx.dsl.choiceBox(items, init, content))

fun NodeContainerBuilder.passwordField(
    text: String = "",
    init: PasswordField.() -> Unit = {},
): PasswordField = add(dev.korafx.dsl.passwordField(text, init))

fun NodeContainerBuilder.slider(
    min: Double = 0.0,
    max: Double = 100.0,
    value: Double = min,
    init: Slider.() -> Unit = {},
): Slider = add(dev.korafx.dsl.slider(min, max, value, init))

fun NodeContainerBuilder.datePicker(
    value: LocalDate? = null,
    init: DatePicker.() -> Unit = {},
): DatePicker = add(dev.korafx.dsl.datePicker(value, init))

fun NodeContainerBuilder.colorPicker(
    value: Color = Color.WHITE,
    init: ColorPicker.() -> Unit = {},
): ColorPicker = add(dev.korafx.dsl.colorPicker(value, init))

fun NodeContainerBuilder.radioButton(
    text: String,
    toggleGroup: ToggleGroup? = null,
    init: RadioButton.() -> Unit = {},
): RadioButton = add(dev.korafx.dsl.radioButton(text, toggleGroup, init))

fun NodeContainerBuilder.toggleButton(
    text: String,
    toggleGroup: ToggleGroup? = null,
    init: ToggleButton.() -> Unit = {},
): ToggleButton = add(dev.korafx.dsl.toggleButton(text, toggleGroup, init))

fun NodeContainerBuilder.menuButton(
    text: String,
    init: MenuButton.() -> Unit = {},
    content: MenuButtonBuilder.() -> Unit = {},
): MenuButton = add(dev.korafx.dsl.menuButton(text, init, content))

fun NodeContainerBuilder.splitMenuButton(
    text: String,
    init: SplitMenuButton.() -> Unit = {},
    content: SplitMenuButtonBuilder.() -> Unit = {},
): SplitMenuButton = add(dev.korafx.dsl.splitMenuButton(text, init, content))
