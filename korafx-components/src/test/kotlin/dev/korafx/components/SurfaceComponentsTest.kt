package dev.korafx.components

import dev.korafx.dsl.button
import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Region
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
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
    fun `status bar renders status items`() {
        FxTestSupport.runOnFxThread {
            val bar = statusBar {
                statusItem("Ready", ComponentTone.SUCCESS, icon = BootstrapIcons.CHECK_CIRCLE)
                spacer()
                statusItem("main", ComponentTone.INFO)
            }

            assertTrue("status-bar" in bar.styleClass)
            val first = assertIs<Label>(bar.children.first())
            val last = assertIs<Label>(bar.children.last())

            assertEquals("Ready", first.text)
            assertTrue("status-item" in first.styleClass)
            assertTrue("tone-success" in first.styleClass)
            assertEquals(BootstrapIcons.CHECK_CIRCLE, assertIs<FontIcon>(first.graphic).iconCode)
            assertIs<Region>(bar.children[1])
            assertEquals("main", last.text)
            assertTrue("tone-info" in last.styleClass)
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
                statusBar {
                    statusItem("Ready")
                }
            }

            assertEquals(4, root.children.size)
            assertTrue("card" in root.children[0].styleClass)
            assertTrue("section" in root.children[1].styleClass)
            assertTrue("action-bar" in root.children[2].styleClass)
            assertTrue("status-bar" in root.children[3].styleClass)
        }
    }
}
