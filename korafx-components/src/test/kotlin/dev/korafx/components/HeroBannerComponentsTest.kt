package dev.korafx.components

import dev.korafx.dsl.button
import dev.korafx.dsl.label
import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HeroBannerComponentsTest {
    @Test
    fun `hero banner renders icon copy actions and content slots`() {
        FxTestSupport.runOnFxThread {
            val banner = heroBanner(
                title = "KoraFX Workbench",
                subtitle = "Compose a polished workspace from reusable modules.",
                eyebrow = "Preview",
                icon = BootstrapIcons.STARS,
                actions = {
                    button("Open")
                },
                content = {
                    label("Use this area for metrics, notes, or richer sample content.")
                },
            )

            assertTrue("hero-banner" in banner.styleClass)
            assertEquals(2, banner.children.size)

            val header = assertIs<HBox>(banner.children[0])
            assertTrue("hero-banner-header" in header.styleClass)
            assertEquals(3, header.children.size)

            val icon = assertIs<StackPane>(header.children[0])
            assertTrue("hero-banner-icon" in icon.styleClass)
            assertEquals(BootstrapIcons.STARS, assertIs<FontIcon>(icon.children.single()).iconCode)

            val copy = assertIs<VBox>(header.children[1])
            assertTrue("hero-banner-copy" in copy.styleClass)
            val eyebrow = assertIs<Label>(copy.children[0])
            val title = assertIs<Label>(copy.children[1])
            val subtitle = assertIs<Label>(copy.children[2])
            assertEquals("Preview", eyebrow.text)
            assertTrue("hero-banner-eyebrow" in eyebrow.styleClass)
            assertEquals("KoraFX Workbench", title.text)
            assertTrue("hero-banner-title" in title.styleClass)
            assertEquals("Compose a polished workspace from reusable modules.", subtitle.text)
            assertTrue("hero-banner-subtitle" in subtitle.styleClass)

            val actions = assertIs<HBox>(header.children[2])
            assertTrue("hero-banner-actions" in actions.styleClass)
            assertTrue("action-bar" in actions.styleClass)
            assertEquals("Open", assertIs<Button>(actions.children.single()).text)

            val content = assertIs<VBox>(banner.children[1])
            assertTrue("hero-banner-content" in content.styleClass)
            assertEquals(
                "Use this area for metrics, notes, or richer sample content.",
                assertIs<Label>(content.children.single()).text,
            )
        }
    }

    @Test
    fun `hero banner keeps stable slots when optional content is omitted`() {
        FxTestSupport.runOnFxThread {
            val banner = heroBanner(title = "Simple")

            val header = assertIs<HBox>(banner.children[0])
            val copy = assertIs<VBox>(header.children[0])
            val actions = assertIs<HBox>(header.children[1])
            val content = assertIs<VBox>(banner.children[1])

            assertEquals(2, header.children.size)
            assertEquals("Simple", assertIs<Label>(copy.children.single()).text)
            assertTrue("hero-banner-actions" in actions.styleClass)
            assertTrue(actions.children.isEmpty())
            assertTrue("hero-banner-content" in content.styleClass)
            assertTrue(content.children.isEmpty())
        }
    }

    @Test
    fun `hero banner can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                heroBanner(
                    title = "Dashboard",
                    eyebrow = "Sample",
                    actions = {
                        button("Refresh")
                    },
                    content = {
                        label("Status")
                    },
                )
            }

            val banner = assertIs<VBox>(root.children.single())
            assertTrue("hero-banner" in banner.styleClass)
            val header = assertIs<HBox>(banner.children[0])
            val copy = assertIs<VBox>(header.children[0])
            assertEquals("Sample", assertIs<Label>(copy.children[0]).text)
            assertEquals("Dashboard", assertIs<Label>(copy.children[1]).text)
        }
    }
}
