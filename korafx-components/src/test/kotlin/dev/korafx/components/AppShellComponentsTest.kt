package dev.korafx.components

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppShellComponentsTest {
    @Test
    fun `app shell assigns application slots and overlay nodes`() {
        FxTestSupport.runOnFxThread {
            val top = Label("top")
            val navigation = Label("navigation")
            val content = Label("content")
            val details = Label("details")
            val footer = Label("footer")
            val overlay = Label("overlay")
            val margin = Insets(24.0)

            val shell = appShell {
                topBar(top)
                navigation(navigation)
                content(content)
                details(details)
                footer(footer)
                overlay(overlay, alignment = Pos.TOP_RIGHT, margin = margin)
            }

            assertTrue(shell.styleClass.contains("app-shell"))
            assertTrue(shell.frame.styleClass.contains("app-shell-frame"))
            assertTrue(shell.body.styleClass.contains("app-shell-body"))
            assertSame(top, shell.frame.top)
            assertSame(navigation, shell.body.left)
            assertSame(content, shell.body.center)
            assertSame(details, shell.body.right)
            assertSame(footer, shell.frame.bottom)
            assertSame(overlay, shell.overlayLayer.children.single())
            assertEquals(Pos.TOP_RIGHT, StackPane.getAlignment(overlay))
            assertEquals(margin, StackPane.getMargin(overlay))
            assertTrue(top.styleClass.contains("app-shell-top-bar"))
            assertTrue(navigation.styleClass.contains("app-shell-navigation"))
            assertTrue(content.styleClass.contains("app-shell-content"))
            assertTrue(details.styleClass.contains("app-shell-details"))
            assertTrue(footer.styleClass.contains("app-shell-footer"))
            assertTrue(overlay.styleClass.contains("app-shell-overlay-item"))
        }
    }

    @Test
    fun `app shell can hide navigation and details slots`() {
        FxTestSupport.runOnFxThread {
            val shell = appShell {
                navigation { Label("navigation") }
                details { Label("details") }
                navigationVisible(false)
                detailsVisible(false)
            }

            assertFalse(shell.navigationNode!!.isVisible)
            assertFalse(shell.navigationNode!!.isManaged)
            assertFalse(shell.detailsNode!!.isVisible)
            assertFalse(shell.detailsNode!!.isManaged)
        }
    }

    @Test
    fun `app shell can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = dev.korafx.dsl.panel {
                appShell {
                    content { Label("content") }
                }
            }
            val shell = assertIs<AppShell>(root.children.single())

            assertEquals("content", assertIs<Label>(shell.body.center).text)
        }
    }
}
