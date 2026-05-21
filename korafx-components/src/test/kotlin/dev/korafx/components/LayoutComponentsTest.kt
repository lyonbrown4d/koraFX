package dev.korafx.components

import dev.korafx.dsl.label
import dev.korafx.dsl.panel
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LayoutComponentsTest {
    @Test
    fun `border layout applies semantic slot style classes`() {
        FxTestSupport.runOnFxThread {
            val layout = borderLayout {
                header { label("Header") }
                sidebar { label("Sidebar") }
                content { label("Content") }
                right { label("Tools") }
                footer { label("Footer") }
            }

            assertTrue("border-layout" in layout.styleClass)
            assertTrue("border-layout-top" in layout.top.styleClass)
            assertTrue("border-layout-left" in layout.left.styleClass)
            assertTrue("border-layout-center" in layout.center.styleClass)
            assertTrue("border-layout-right" in layout.right.styleClass)
            assertTrue("border-layout-bottom" in layout.bottom.styleClass)
            assertEquals("Header", assertIs<Label>(layout.top).text)
        }
    }

    @Test
    fun `border layout can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                borderLayout {
                    center { label("Dashboard") }
                }
            }
            val layout = assertIs<javafx.scene.layout.BorderPane>(root.children.single())

            assertTrue("border-layout" in layout.styleClass)
            assertEquals("Dashboard", assertIs<Label>(layout.center).text)
        }
    }
}
