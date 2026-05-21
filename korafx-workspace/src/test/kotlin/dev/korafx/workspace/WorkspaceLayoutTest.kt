package dev.korafx.workspace

import dev.korafx.dsl.label
import dev.korafx.dsl.panel
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class WorkspaceLayoutTest {
    @Test
    fun `workspace layout assigns workbench slots and overlay nodes`() {
        FxTestSupport.runOnFxThread {
            val topBar = label("Top")
            val navigation = label("Navigation")
            val content = label("Content")
            val details = label("Details")
            val status = label("Status")
            val overlay = label("Overlay")
            val margin = Insets(20.0)

            val workspace = workspaceLayout {
                topBar(topBar)
                navigation(navigation)
                content(content)
                details(details)
                status(status)
                overlay(overlay, alignment = Pos.TOP_RIGHT, margin = margin)
            }

            assertTrue("workspace-layout" in workspace.styleClass)
            assertTrue("workspace-layout-frame" in workspace.frame.styleClass)
            assertTrue("workspace-layout-body" in workspace.body.styleClass)
            assertSame(topBar, workspace.frame.top)
            assertSame(navigation, workspace.body.left)
            assertSame(content, workspace.body.center)
            assertSame(details, workspace.body.right)
            assertSame(status, workspace.frame.bottom)
            assertSame(overlay, workspace.overlayLayer.children.single())
            assertEquals(Pos.TOP_RIGHT, javafx.scene.layout.StackPane.getAlignment(overlay))
            assertEquals(margin, javafx.scene.layout.StackPane.getMargin(overlay))
            assertTrue("workspace-layout-top-bar" in topBar.styleClass)
            assertTrue("workspace-layout-navigation" in navigation.styleClass)
            assertTrue("workspace-layout-content" in content.styleClass)
            assertTrue("workspace-layout-details" in details.styleClass)
            assertTrue("workspace-layout-status" in status.styleClass)
            assertTrue("workspace-layout-overlay-item" in overlay.styleClass)
        }
    }

    @Test
    fun `workspace layout can hide navigation and details slots`() {
        FxTestSupport.runOnFxThread {
            val workspace = workspaceLayout {
                navigation { label("Navigation") }
                details { label("Details") }
                navigationVisible(false)
                detailsVisible(false)
            }

            assertFalse(workspace.navigationNode!!.isVisible)
            assertFalse(workspace.navigationNode!!.isManaged)
            assertFalse(workspace.detailsNode!!.isVisible)
            assertFalse(workspace.detailsNode!!.isManaged)
        }
    }

    @Test
    fun `workspace layout can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                workspaceLayout {
                    content { label("Repository") }
                }
            }
            val workspace = assertIs<WorkspaceLayout>(root.children.single())

            assertEquals("Repository", assertIs<Label>(workspace.body.center).text)
        }
    }
}
