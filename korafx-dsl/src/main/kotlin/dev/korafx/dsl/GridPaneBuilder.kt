package dev.korafx.dsl

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints

class GridPaneBuilder internal constructor(
    private val pane: GridPane,
) {
    fun column(
        minWidth: Double? = null,
        prefWidth: Double? = null,
        maxWidth: Double? = null,
        percentWidth: Double? = null,
        grow: Priority? = null,
        fillWidth: Boolean? = null,
        alignment: HPos? = null,
    ): ColumnConstraints =
        ColumnConstraints().apply {
            minWidth?.let { this.minWidth = it }
            prefWidth?.let { this.prefWidth = it }
            maxWidth?.let { this.maxWidth = it }
            percentWidth?.let { this.percentWidth = it }
            grow?.let { hgrow = it }
            fillWidth?.let { isFillWidth = it }
            alignment?.let { halignment = it }
        }.also {
            pane.columnConstraints += it
        }

    fun columns(
        count: Int,
        minWidth: Double? = null,
        prefWidth: Double? = null,
        maxWidth: Double? = null,
        percentWidth: Double? = null,
        grow: Priority? = null,
        fillWidth: Boolean? = null,
        alignment: HPos? = null,
    ): List<ColumnConstraints> {
        require(count >= 0) {
            "GridPane column count must be non-negative."
        }

        return List(count) {
            column(
                minWidth = minWidth,
                prefWidth = prefWidth,
                maxWidth = maxWidth,
                percentWidth = percentWidth,
                grow = grow,
                fillWidth = fillWidth,
                alignment = alignment,
            )
        }
    }

    fun row(
        minHeight: Double? = null,
        prefHeight: Double? = null,
        maxHeight: Double? = null,
        percentHeight: Double? = null,
        grow: Priority? = null,
        fillHeight: Boolean? = null,
        alignment: VPos? = null,
    ): RowConstraints =
        RowConstraints().apply {
            minHeight?.let { this.minHeight = it }
            prefHeight?.let { this.prefHeight = it }
            maxHeight?.let { this.maxHeight = it }
            percentHeight?.let { this.percentHeight = it }
            grow?.let { vgrow = it }
            fillHeight?.let { isFillHeight = it }
            alignment?.let { valignment = it }
        }.also {
            pane.rowConstraints += it
        }

    fun rows(
        count: Int,
        minHeight: Double? = null,
        prefHeight: Double? = null,
        maxHeight: Double? = null,
        percentHeight: Double? = null,
        grow: Priority? = null,
        fillHeight: Boolean? = null,
        alignment: VPos? = null,
    ): List<RowConstraints> {
        require(count >= 0) {
            "GridPane row count must be non-negative."
        }

        return List(count) {
            row(
                minHeight = minHeight,
                prefHeight = prefHeight,
                maxHeight = maxHeight,
                percentHeight = percentHeight,
                grow = grow,
                fillHeight = fillHeight,
                alignment = alignment,
            )
        }
    }

    fun <T : Node> cell(
        column: Int,
        row: Int,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        node: T,
        horizontalAlignment: HPos? = null,
        verticalAlignment: VPos? = null,
        horizontalGrow: Priority? = null,
        verticalGrow: Priority? = null,
        margin: Insets? = null,
    ): T =
        node.also {
            pane.add(it, column, row, columnSpan, rowSpan)
            horizontalAlignment?.let { alignment -> GridPane.setHalignment(it, alignment) }
            verticalAlignment?.let { alignment -> GridPane.setValignment(it, alignment) }
            horizontalGrow?.let { grow -> GridPane.setHgrow(it, grow) }
            verticalGrow?.let { grow -> GridPane.setVgrow(it, grow) }
            margin?.let { value -> GridPane.setMargin(it, value) }
        }

    fun <T : Node> cell(
        column: Int,
        row: Int,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        horizontalAlignment: HPos? = null,
        verticalAlignment: VPos? = null,
        horizontalGrow: Priority? = null,
        verticalGrow: Priority? = null,
        margin: Insets? = null,
        factory: () -> T,
    ): T =
        cell(
            column = column,
            row = row,
            columnSpan = columnSpan,
            rowSpan = rowSpan,
            node = factory(),
            horizontalAlignment = horizontalAlignment,
            verticalAlignment = verticalAlignment,
            horizontalGrow = horizontalGrow,
            verticalGrow = verticalGrow,
            margin = margin,
        )

    fun label(
        column: Int,
        row: Int,
        text: String = "",
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: Label.() -> Unit = {},
    ): Label =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.label(text, init))

    fun button(
        column: Int,
        row: Int,
        text: String,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: Button.() -> Unit = {},
    ): Button =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.button(text, init))

    fun checkBox(
        column: Int,
        row: Int,
        text: String,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: CheckBox.() -> Unit = {},
    ): CheckBox =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.checkBox(text, init))

    fun textField(
        column: Int,
        row: Int,
        text: String = "",
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: TextField.() -> Unit = {},
    ): TextField =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.textField(text, init))

    fun textArea(
        column: Int,
        row: Int,
        text: String = "",
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        init: TextArea.() -> Unit = {},
    ): TextArea =
        cell(column, row, columnSpan, rowSpan, dev.korafx.dsl.textArea(text, init))
}
