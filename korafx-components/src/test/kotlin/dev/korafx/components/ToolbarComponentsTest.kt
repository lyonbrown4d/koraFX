package dev.korafx.components

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ToolbarComponentsTest {
    @Test
    fun `app toolbar renders navigation title content spacer and actions`() {
        FxTestSupport.runOnFxThread {
            val toolbar = appToolbar(
                title = "KoraFX",
                subtitle = "Workbench",
                icon = BootstrapIcons.BOX,
                navigation = {
                    button("Back")
                },
                content = {
                    button("Project")
                },
                actions = {
                    button("Commands")
                },
            )

            assertTrue("app-toolbar" in toolbar.styleClass)
            val navigation = assertIs<HBox>(toolbar.children[0])
            val titleStack = assertIs<VBox>(toolbar.children[1])
            val content = assertIs<HBox>(toolbar.children[2])
            val spacer = assertIs<Region>(toolbar.children[3])
            val actions = assertIs<HBox>(toolbar.children[4])
            val title = assertIs<Label>(titleStack.children[0])
            val subtitle = assertIs<Label>(titleStack.children[1])

            assertTrue("app-toolbar-navigation" in navigation.styleClass)
            assertTrue("app-toolbar-title-stack" in titleStack.styleClass)
            assertTrue("app-toolbar-content" in content.styleClass)
            assertTrue("app-toolbar-actions" in actions.styleClass)
            assertEquals("Back", assertIs<Button>(navigation.children.single()).text)
            assertEquals("KoraFX", title.text)
            assertEquals(BootstrapIcons.BOX, assertIs<FontIcon>(title.graphic).iconCode)
            assertEquals("Workbench", subtitle.text)
            assertTrue(spacer.isManaged)
            assertEquals("Project", assertIs<Button>(content.children.single()).text)
            assertEquals("Commands", assertIs<Button>(actions.children.single()).text)
        }
    }

    @Test
    fun `toolbar components can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                appToolbar("KoraFX")
                toolbarGroup {
                    button("Action")
                }
            }

            assertTrue("app-toolbar" in assertIs<HBox>(root.children[0]).styleClass)
            assertTrue("toolbar-group" in assertIs<HBox>(root.children[1]).styleClass)
        }
    }
}
