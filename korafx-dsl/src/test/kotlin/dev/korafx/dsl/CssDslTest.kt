package dev.korafx.dsl

import javafx.scene.layout.Pane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CssDslTest {
    @Test
    fun `css style builder renders expected declaration map`() {
        val node = Pane()
        node.cssStyle {
            fontSize(18.0)
            fontWeight("bold")
            textFill("#111827")
            padding(6.0, 12.0)
            radius(8.0)
        }

        assertEquals(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-padding: 6px 12px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-background-insets: 0px;",
            node.style,
        )
    }

    @Test
    fun `css append can merge with existing style text`() {
        val node = Pane()
        node.style = "-fx-font-size: 12px;"
        node.cssAppend {
            textFill("#4B5563")
        }

        assertTrue(node.style.contains("-fx-font-size: 12px;"))
        assertTrue(node.style.contains("-fx-text-fill: #4B5563;"))
        assertFalse(node.style.contains(";;"))
    }

    @Test
    fun `raw css declarations can be parsed by builder`() {
        val node = Pane()
        node.cssStyle {
            raw("-fx-background-color: #fff; -fx-border-color: #eee;")
            fx("-fx-text-fill", "#111")
        }

        assertTrue(node.style.contains("-fx-background-color: #fff;"))
        assertTrue(node.style.contains("-fx-border-color: #eee;"))
        assertTrue(node.style.contains("-fx-text-fill: #111;"))
    }

    @Test
    fun `css property names are normalized when using builder helpers`() {
        val node = Pane()
        node.cssStyle {
            fontSize(12.0)
            fx("text-fill", "#333")
            fx("-fx-alignment", "center")
        }

        assertEquals(
            "-fx-font-size: 12px; -fx-text-fill: #333; -fx-alignment: center;",
            node.style,
        )
    }

    @Test
    fun `background helper applies independent declarations`() {
        val node = Pane()
        node.cssStyle {
            background(
                background = null,
                border = "#D9E0EE",
                radius = 10.0,
            )
        }

        assertEquals(
            "-fx-border-color: #D9E0EE; -fx-background-radius: 10px; -fx-border-radius: 10px; -fx-background-insets: 0px;",
            node.style,
        )
    }

    @Test
    fun `cssStyle can be reused across nodes`() {
        val headingStyle = cssStyleOf {
            textFill("#0F172A")
            fontWeight("700")
            radius(12.0)
        }
        val first = Pane().apply { cssStyle(headingStyle) }
        val second = Pane().apply { cssStyle(headingStyle) }

        assertEquals(headingStyle, cssStyleOf { textFill("#0F172A"); fontWeight("700"); radius(12.0) })
        assertEquals(headingStyle.asCssText(), first.style)
        assertEquals(first.style, second.style)
        assertNotNull(first.style)
    }
}
