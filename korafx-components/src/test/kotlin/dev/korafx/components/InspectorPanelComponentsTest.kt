package dev.korafx.components

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InspectorPanelComponentsTest {
    @Test
    fun `inspector panel renders header metadata properties sections and actions`() {
        FxTestSupport.runOnFxThread {
            var opened = false
            val panel = inspectorPanel(
                title = "users",
                subtitle = "public.users table",
            ) {
                badge("Selected", ComponentTone.INFO)
                property("Rows", "128")
                section("Columns") {
                    property("id", "uuid")
                    badge("indexed", ComponentTone.SUCCESS)
                }
                actions {
                    action("Open") {
                        opened = true
                    }
                }
            }

            assertTrue("inspector-panel" in panel.styleClass)
            assertEquals("users", panel.titleLabel.text)
            assertEquals("public.users table", panel.subtitleLabel.text)
            assertEquals("Selected", assertIs<Label>(panel.metadataBar.children.single()).text)

            val property = assertIs<HBox>(panel.body.children.first())
            assertTrue("inspector-panel-property" in property.styleClass)
            assertEquals("Rows", assertIs<Label>(property.children.first()).text)
            assertEquals("128", assertIs<Label>(property.children[1]).text)

            val section = assertIs<VBox>(panel.body.children[1])
            assertTrue("inspector-panel-section" in section.styleClass)
            assertEquals("Columns", assertIs<Label>(section.children.first()).text)

            assertTrue(panel.actions.isVisible)
            assertIs<Button>(panel.actions.children.single()).fire()
            assertTrue(opened)
        }
    }

    @Test
    fun `inspector panel supports empty state and clears it when content is added`() {
        FxTestSupport.runOnFxThread {
            val panel = inspectorPanel(emptyText = "Select a resource")

            assertEquals(panel.emptyLabel, panel.body.children.single())
            assertEquals("Select a resource", panel.emptyLabel.text)

            panel.addProperty("Name", "Repository")

            assertFalse(panel.emptyLabel in panel.body.children)
            assertEquals("Name", assertIs<Label>(assertIs<HBox>(panel.body.children.single()).children.first()).text)

            panel.showEmpty("No selection")

            assertEquals(panel.emptyLabel, panel.body.children.single())
            assertEquals("No selection", panel.emptyLabel.text)
        }
    }

    @Test
    fun `inspector panel can hide header and action regions when empty`() {
        FxTestSupport.runOnFxThread {
            val panel = inspectorPanel {
                property("Branch", "main")
            }

            assertFalse(panel.header.isVisible)
            assertFalse(panel.header.isManaged)
            assertFalse(panel.actions.isVisible)
            assertFalse(panel.actions.isManaged)
        }
    }

    @Test
    fun `inspector panel can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                inspectorPanel(title = "Commit") {
                    property("Hash", "abc123")
                }
            }
            val inspector = assertIs<InspectorPanel>(root.children.single())

            assertEquals("Commit", inspector.titleLabel.text)
            assertEquals("Hash", assertIs<Label>(assertIs<HBox>(inspector.body.children.single()).children.first()).text)
        }
    }
}
