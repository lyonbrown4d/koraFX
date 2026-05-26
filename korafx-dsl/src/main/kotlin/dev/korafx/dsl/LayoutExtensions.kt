@file:JvmName("LayoutDslKt")
@file:JvmMultifileClass

package dev.korafx.dsl

import javafx.css.PseudoClass
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.ButtonBase
import javafx.scene.control.MenuItem
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox

fun insets(value: Double): Insets =
    Insets(value)

fun insets(vertical: Double, horizontal: Double): Insets =
    Insets(vertical, horizontal, vertical, horizontal)

fun insets(
    top: Double,
    right: Double,
    bottom: Double,
    left: Double,
): Insets =
    Insets(top, right, bottom, left)

fun Region.paddingAll(value: Double) {
    padding = Insets(value)
}

fun Region.padding(vertical: Double, horizontal: Double) {
    padding = Insets(vertical, horizontal, vertical, horizontal)
}

fun Region.padding(
    top: Double,
    right: Double,
    bottom: Double,
    left: Double,
) {
    padding = Insets(top, right, bottom, left)
}

fun Region.prefSize(width: Double, height: Double) {
    prefWidth = width
    prefHeight = height
}

fun Region.minSize(width: Double, height: Double) {
    minWidth = width
    minHeight = height
}

fun Region.maxSize(width: Double, height: Double) {
    maxWidth = width
    maxHeight = height
}

fun Node.marginAll(value: Double) {
    margin(insets(value))
}

fun Node.margin(vertical: Double, horizontal: Double) {
    margin(insets(vertical, horizontal))
}

fun Node.margin(
    top: Double,
    right: Double,
    bottom: Double,
    left: Double,
) {
    margin(insets(top, right, bottom, left))
}

fun Node.margin(value: Insets) {
    HBox.setMargin(this, value)
    VBox.setMargin(this, value)
    GridPane.setMargin(this, value)
    StackPane.setMargin(this, value)
    FlowPane.setMargin(this, value)
    TilePane.setMargin(this, value)
    BorderPane.setMargin(this, value)
}

fun Node.styleClass(name: String) {
    if (!styleClass.contains(name)) {
        styleClass += name
    }
}

fun Node.styleClasses(vararg names: String) {
    names.forEach(::styleClass)
}

fun Node.removeStyleClass(name: String) {
    styleClass.remove(name)
}

fun Node.toggleStyleClass(name: String, enabled: Boolean) {
    if (enabled) {
        styleClass(name)
    } else {
        removeStyleClass(name)
    }
}

fun Node.pseudoClass(name: String, enabled: Boolean) {
    pseudoClassStateChanged(PseudoClass.getPseudoClass(name), enabled)
}

fun Node.invalidWhen(invalid: Boolean) {
    pseudoClass("invalid", invalid)
    toggleStyleClass("invalid", invalid)
}

fun Node.visibleWhen(visible: Boolean, manageWhenHidden: Boolean = true) {
    isVisible = visible
    if (manageWhenHidden) {
        isManaged = visible
    }
}

fun Node.disableWhen(disabled: Boolean) {
    isDisable = disabled
}

fun Node.growHorizontal(priority: Priority = Priority.ALWAYS) {
    HBox.setHgrow(this, priority)
    GridPane.setHgrow(this, priority)
}

fun Node.growVertical(priority: Priority = Priority.ALWAYS) {
    VBox.setVgrow(this, priority)
    GridPane.setVgrow(this, priority)
}

fun Node.align(value: Pos) {
    StackPane.setAlignment(this, value)
    BorderPane.setAlignment(this, value)
}

fun Node.gridAlign(
    horizontal: HPos? = null,
    vertical: VPos? = null,
) {
    horizontal?.let { GridPane.setHalignment(this, it) }
    vertical?.let { GridPane.setValignment(this, it) }
}

fun ButtonBase.onAction(handler: () -> Unit) {
    setOnAction { handler() }
}

fun MenuItem.onAction(handler: () -> Unit) {
    setOnAction { handler() }
}
