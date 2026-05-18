package dev.korafx.components

import dev.korafx.dsl.button
import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Region
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SurfaceComponentsTest {
    @Test
    fun `card renders styled container`() {
        FxTestSupport.runOnFxThread {
            val card = card {
                label("Content")
            }

            assertTrue("card" in card.styleClass)
            assertEquals(1, card.children.size)
            assertEquals("Content", (card.children.single() as Label).text)
        }
    }

    @Test
    fun `section renders title description and content`() {
        FxTestSupport.runOnFxThread {
            val section = section(
                title = "General",
                description = "Basic settings.",
            ) {
                button("Apply")
            }

            val labels = section.children.filterIsInstance<Label>()

            assertTrue("card" in section.styleClass)
            assertTrue("section" in section.styleClass)
            assertEquals("General", labels[0].text)
            assertTrue("section-title" in labels[0].styleClass)
            assertEquals("Basic settings.", labels[1].text)
            assertTrue("section-description" in labels[1].styleClass)
            assertIs<Button>(section.children.last())
        }
    }

    @Test
    fun `action bar inserts leading spacer when aligned to end`() {
        FxTestSupport.runOnFxThread {
            val bar = actionBar {
                button("Save")
            }

            assertTrue("action-bar" in bar.styleClass)
            assertIs<Region>(bar.children.first())
            assertEquals("Save", (bar.children.last() as Button).text)
        }
    }

    @Test
    fun `surface components can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                card {
                    label("Card")
                }
                section("Section")
                actionBar {
                    button("Apply")
                }
            }

            assertEquals(3, root.children.size)
            assertTrue("card" in root.children[0].styleClass)
            assertTrue("section" in root.children[1].styleClass)
            assertTrue("action-bar" in root.children[2].styleClass)
        }
    }
}
