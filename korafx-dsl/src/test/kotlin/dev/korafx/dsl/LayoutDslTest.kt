package dev.korafx.dsl

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutDslTest {
    @Test
    fun `box builders configure common layout properties`() {
        lateinit var horizontalChild: Region
        lateinit var verticalChild: Region

        val row = hbox(spacing = 4.0) {
            alignment(Pos.CENTER_RIGHT)
            fillHeight(false)
            spacing(12.0)

            horizontalChild = region {
                margin(4.0, 8.0)
                growHorizontal()
            }
        }

        val column = vbox(spacing = 4.0) {
            alignment(Pos.BOTTOM_LEFT)
            fillWidth(false)
            spacing(10.0)

            verticalChild = region {
                marginAll(6.0)
                growVertical(Priority.SOMETIMES)
            }
        }

        assertEquals(Pos.CENTER_RIGHT, row.alignment)
        assertEquals(false, row.isFillHeight)
        assertEquals(12.0, row.spacing)
        assertEquals(Insets(4.0, 8.0, 4.0, 8.0), HBox.getMargin(horizontalChild))
        assertEquals(Priority.ALWAYS, HBox.getHgrow(horizontalChild))
        assertEquals(Priority.ALWAYS, GridPane.getHgrow(horizontalChild))

        assertEquals(Pos.BOTTOM_LEFT, column.alignment)
        assertEquals(false, column.isFillWidth)
        assertEquals(10.0, column.spacing)
        assertEquals(Insets(6.0), VBox.getMargin(verticalChild))
        assertEquals(Priority.SOMETIMES, VBox.getVgrow(verticalChild))
        assertEquals(Priority.SOMETIMES, GridPane.getVgrow(verticalChild))
    }

    @Test
    fun `node alignment and margins apply to common JavaFX parent constraints`() {
        lateinit var child: Region

        val stack = stackPane {
            alignment(Pos.CENTER)

            child = region {
                align(Pos.BOTTOM_RIGHT)
                gridAlign(horizontal = HPos.RIGHT, vertical = VPos.BOTTOM)
                margin(insets(top = 1.0, right = 2.0, bottom = 3.0, left = 4.0))
            }
        }

        assertEquals(Pos.CENTER, stack.alignment)
        assertEquals(Pos.BOTTOM_RIGHT, StackPane.getAlignment(child))
        assertEquals(Pos.BOTTOM_RIGHT, BorderPane.getAlignment(child))
        assertEquals(HPos.RIGHT, GridPane.getHalignment(child))
        assertEquals(VPos.BOTTOM, GridPane.getValignment(child))
        assertEquals(Insets(1.0, 2.0, 3.0, 4.0), StackPane.getMargin(child))
        assertEquals(Insets(1.0, 2.0, 3.0, 4.0), GridPane.getMargin(child))
        assertEquals(Insets(1.0, 2.0, 3.0, 4.0), insets(1.0, 2.0, 3.0, 4.0))
    }
}
