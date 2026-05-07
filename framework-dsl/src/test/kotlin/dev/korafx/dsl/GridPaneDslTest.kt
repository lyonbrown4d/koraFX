package dev.korafx.dsl

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GridPaneDslTest {
    @Test
    fun `grid pane configures constraints and cell metadata`() {
        val margin = Insets(4.0)
        lateinit var firstCell: Region
        lateinit var secondCell: Region

        val grid = gridPane(hgap = 12.0, vgap = 8.0) {
            column(prefWidth = 120.0, grow = Priority.NEVER, alignment = HPos.RIGHT)
            column(percentWidth = 100.0, grow = Priority.ALWAYS, fillWidth = true)
            row(prefHeight = 32.0, alignment = VPos.CENTER)

            firstCell = cell(
                column = 0,
                row = 0,
                horizontalAlignment = HPos.RIGHT,
                verticalAlignment = VPos.CENTER,
                margin = margin,
                node = Region(),
            )

            secondCell = cell(
                column = 1,
                row = 0,
                horizontalGrow = Priority.ALWAYS,
            ) {
                Region()
            }
        }

        assertEquals(12.0, grid.hgap)
        assertEquals(8.0, grid.vgap)
        assertEquals(2, grid.columnConstraints.size)
        assertEquals(120.0, grid.columnConstraints[0].prefWidth)
        assertEquals(Priority.ALWAYS, grid.columnConstraints[1].hgrow)
        assertEquals(1, grid.rowConstraints.size)
        assertEquals(VPos.CENTER, grid.rowConstraints[0].valignment)
        assertEquals(2, grid.children.size)
        assertSame(firstCell, grid.children[0])
        assertSame(secondCell, grid.children[1])
        assertEquals(0, GridPane.getColumnIndex(firstCell))
        assertEquals(1, GridPane.getColumnIndex(secondCell))
        assertEquals(HPos.RIGHT, GridPane.getHalignment(firstCell))
        assertEquals(VPos.CENTER, GridPane.getValignment(firstCell))
        assertSame(margin, GridPane.getMargin(firstCell))
        assertEquals(Priority.ALWAYS, GridPane.getHgrow(secondCell))
    }
}
