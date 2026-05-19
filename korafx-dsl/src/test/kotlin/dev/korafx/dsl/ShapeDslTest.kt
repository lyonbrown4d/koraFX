package dev.korafx.dsl

import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class ShapeDslTest {
    @Test
    fun `rectangle factory configures size and style`() {
        val rectangle = rectangle(width = 24.0, height = 16.0) {
            fill = Color.RED
            stroke = Color.BLUE
        }

        assertEquals(24.0, rectangle.width)
        assertEquals(16.0, rectangle.height)
        assertEquals(Color.RED, rectangle.fill)
        assertEquals(Color.BLUE, rectangle.stroke)
    }

    @Test
    fun `rectangle can be added from node container builder`() {
        val root = hbox {
            rectangle(10.0, 12.0)
        }

        val rectangle = assertIs<Rectangle>(root.children.single())

        assertSame(rectangle, root.children.single())
        assertEquals(10.0, rectangle.width)
        assertEquals(12.0, rectangle.height)
    }
}
