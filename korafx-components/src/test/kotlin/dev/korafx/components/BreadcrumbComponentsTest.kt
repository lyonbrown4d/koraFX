package dev.korafx.components

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BreadcrumbComponentsTest {
    @Test
    fun `breadcrumb renders links separators and current item`() {
        FxTestSupport.runOnFxThread {
            var selected: String? = null
            val breadcrumb = breadcrumb(
                items = listOf(
                    breadcrumbItem("workspace", "Workspace", icon = BootstrapIcons.HOUSE),
                    breadcrumbItem("repo", "Repository"),
                    breadcrumbItem("file", "Main.kt", current = true),
                ),
                onSelect = { selected = it },
            )

            assertTrue("breadcrumb" in breadcrumb.styleClass)
            assertEquals(5, breadcrumb.children.size)
            val first = assertIs<Hyperlink>(breadcrumb.children[0])
            val separator = assertIs<Label>(breadcrumb.children[1])
            val current = assertIs<Label>(breadcrumb.children[4])

            assertEquals("Workspace", first.text)
            assertTrue("breadcrumb-item-link" in first.styleClass)
            assertEquals(BootstrapIcons.HOUSE, assertIs<FontIcon>(first.graphic).iconCode)
            assertEquals("/", separator.text)
            assertTrue("breadcrumb-separator" in separator.styleClass)
            assertEquals("Main.kt", current.text)
            assertTrue("breadcrumb-item-current" in current.styleClass)

            first.fire()

            assertEquals("workspace", selected)
        }
    }

    @Test
    fun `page header renders title subtitle actions and content`() {
        FxTestSupport.runOnFxThread {
            val header = pageHeader(
                title = "Repository",
                subtitle = "main branch",
                eyebrow = "Workspace",
                icon = BootstrapIcons.DIAGRAM_3,
                actions = {
                    button("Refresh")
                },
            ) {
                badge("Connected", ComponentTone.SUCCESS)
            }

            assertTrue("page-header" in header.styleClass)
            assertEquals("Workspace", assertIs<Label>(header.children[0]).text)

            val titleRow = assertIs<HBox>(header.children[1])
            val title = assertIs<Label>(titleRow.children[0])
            val actions = assertIs<HBox>(titleRow.children[2])
            val subtitle = assertIs<Label>(header.children[2])
            val content = assertIs<VBox>(header.children[3])

            assertEquals("Repository", title.text)
            assertTrue("page-header-title" in title.styleClass)
            assertEquals(BootstrapIcons.DIAGRAM_3, assertIs<FontIcon>(title.graphic).iconCode)
            assertIs<Button>(actions.children.single())
            assertEquals("main branch", subtitle.text)
            assertTrue("page-header-subtitle" in subtitle.styleClass)
            assertTrue("badge" in assertIs<Label>(content.children.single()).styleClass)
        }
    }

    @Test
    fun `navigation surface components can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                breadcrumb(
                    items = listOf(
                        breadcrumbItem("home", "Home"),
                        breadcrumbItem("settings", "Settings", current = true),
                    ),
                )
                pageHeader("Settings")
            }

            assertTrue("breadcrumb" in assertIs<HBox>(root.children[0]).styleClass)
            assertTrue("page-header" in assertIs<VBox>(root.children[1]).styleClass)
        }
    }
}
