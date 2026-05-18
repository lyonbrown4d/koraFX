package dev.korafx.components

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppShellComponentsTest {
    @Test
    fun `app shell assigns layout slots and overlay nodes`() {
        FxTestSupport.runOnFxThread {
            val top = Label("top")
            val navigation = Label("navigation")
            val content = Label("content")
            val footer = Label("footer")
            val overlay = Label("overlay")
            val margin = Insets(24.0)

            val shell = appShell {
                topBar(top)
                navigation(navigation)
                content(content)
                footer(footer)
                overlay(overlay, alignment = Pos.TOP_RIGHT, margin = margin)
            }

            val layout = shell.children[0] as BorderPane
            val overlayPane = shell.children[1] as StackPane

            assertTrue(shell.styleClass.contains("app-shell"))
            assertSame(top, layout.top)
            assertSame(navigation, layout.left)
            assertSame(content, layout.center)
            assertSame(footer, layout.bottom)
            assertSame(overlay, overlayPane.children.single())
            assertEquals(Pos.TOP_RIGHT, StackPane.getAlignment(overlay))
            assertEquals(margin, StackPane.getMargin(overlay))
        }
    }
}
