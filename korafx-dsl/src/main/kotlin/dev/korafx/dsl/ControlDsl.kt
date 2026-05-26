@file:JvmName("ControlDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.DatePicker
import javafx.scene.control.ListView
import javafx.scene.control.MenuButton
import javafx.scene.control.PasswordField
import javafx.scene.control.RadioButton
import javafx.scene.control.Slider
import javafx.scene.control.SplitMenuButton
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.paint.Color
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
