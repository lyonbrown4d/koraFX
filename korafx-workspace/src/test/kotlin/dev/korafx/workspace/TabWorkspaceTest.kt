package dev.korafx.workspace

import dev.korafx.dsl.label
import dev.korafx.dsl.panel
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TabWorkspaceTest {
    @Test
    fun `tab workspace shows empty placeholder until tabs are opened`() {
        FxTestSupport.runOnFxThread {
            val workspace = tabWorkspace(emptyText = "Open a file")

            assertTrue("tab-workspace" in workspace.styleClass)
            assertEquals(1, workspace.tabs.size)
            assertEquals("Open a file", workspace.tabs.single().text)
            assertTrue("tab-workspace-empty-tab" in workspace.tabs.single().styleClass)
            assertEquals("Open a file", workspace.emptyLabel.text)

            workspace.openTab("readme", "README.md") {
                label("README")
            }

            assertEquals(1, workspace.workspaceTabs.size)
            assertEquals("readme", workspace.tabId(workspace.workspaceTabs.single()))
            assertFalse("tab-workspace-empty-tab" in workspace.workspaceTabs.single().styleClass)
        }
    }

    @Test
    fun `tab workspace tracks dirty marker selection and close callbacks`() {
        FxTestSupport.runOnFxThread {
            val selected = mutableListOf<String>()
            val closed = mutableListOf<String>()
            val workspace = tabWorkspace {
                onSelect(selected::add)
                onClose(closed::add)
                tab("query", "Query.sql", dirty = true) {
                    label("select 1")
                }
                tab("source", "Main.kt") {
                    label("fun main() {}")
                }
            }

            val query = workspace.workspaceTabs.first()
            assertTrue("tab-workspace-tab-dirty" in query.styleClass)
            assertEquals("*", assertIs<Label>(query.graphic).text)
            assertTrue(workspace.isTabDirty("query"))

            workspace.selectTab("source")
            workspace.selectTab("query")

            assertEquals(listOf("source", "query"), selected)

            workspace.setTabDirty("query", false)

            assertFalse("tab-workspace-tab-dirty" in query.styleClass)
            assertNull(query.graphic)
            assertFalse(workspace.isTabDirty("query"))

            assertTrue(workspace.closeTab("query"))

            assertEquals(listOf("query"), closed)
            assertEquals(listOf("source"), workspace.workspaceTabs.mapNotNull(workspace::tabId))
        }
    }

    @Test
    fun `tab workspace reuses existing tab id`() {
        FxTestSupport.runOnFxThread {
            val workspace = tabWorkspace {
                tab("query", "Query.sql") {
                    label("first")
                }
            }
            val first = workspace.workspaceTabs.single()
            val second = workspace.openTab("query", "Query.sql", dirty = true) {
                label("second")
            }

            assertEquals(first, second)
            assertEquals(1, workspace.workspaceTabs.size)
            assertTrue(workspace.isTabDirty("query"))
            assertEquals("first", assertIs<Label>(first.content).text)
        }
    }

    @Test
    fun `tab workspace can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                tabWorkspace {
                    tab("overview", "Overview", closable = false) {
                        Label("Dashboard")
                    }
                }
            }
            val workspace = assertIs<TabWorkspace>(root.children.single())

            assertEquals("overview", workspace.tabId(workspace.workspaceTabs.single()))
            assertEquals("Dashboard", assertIs<Label>(workspace.workspaceTabs.single().content).text)
        }
    }
}
