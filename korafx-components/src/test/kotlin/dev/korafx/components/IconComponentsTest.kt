package dev.korafx.components

import dev.korafx.dsl.hbox
import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IconComponentsTest {
    @Test
    fun `kora icon wraps ikonli font icon with framework style class`() {
        FxTestSupport.runOnFxThread {
            val icon = koraIcon(BootstrapIcons.ALARM, size = 20) {
                styleClass += "custom-icon"
            }

            assertEquals(BootstrapIcons.ALARM, icon.iconCode)
            assertEquals(20, icon.iconSize)
            assertTrue("ikonli-font-icon" in icon.styleClass)
            assertTrue("korafx-icon" in icon.styleClass)
            assertTrue("custom-icon" in icon.styleClass)
        }
    }

    @Test
    fun `icon button configures graphic and text layout`() {
        FxTestSupport.runOnFxThread {
            val button = iconButton(BootstrapIcons.ALARM, text = "Notify", size = 18)
            val icon = assertIs<FontIcon>(button.graphic)

            assertEquals("Notify", button.text)
            assertEquals(BootstrapIcons.ALARM, icon.iconCode)
            assertEquals(18, icon.iconSize)
            assertEquals(ContentDisplay.LEFT, button.contentDisplay)
            assertEquals(6.0, button.graphicTextGap)
            assertTrue("icon-button" in button.styleClass)
            assertFalse("icon-only-button" in button.styleClass)
        }
    }

    @Test
    fun `icon only button uses compact style class`() {
        FxTestSupport.runOnFxThread {
            val button = iconButton(BootstrapIcons.ALARM)

            assertEquals("", button.text)
            assertTrue("icon-button" in button.styleClass)
            assertTrue("icon-only-button" in button.styleClass)
        }
    }

    @Test
    fun `labeled controls can set and clear kora icon`() {
        FxTestSupport.runOnFxThread {
            val button = Button("Refresh")

            button.setKoraIcon(
                icon = BootstrapIcons.ALARM,
                size = 22,
                display = ContentDisplay.RIGHT,
                gap = 10.0,
            )

            val icon = assertIs<FontIcon>(button.graphic)
            assertEquals(BootstrapIcons.ALARM, icon.iconCode)
            assertEquals(22, icon.iconSize)
            assertEquals(ContentDisplay.RIGHT, button.contentDisplay)
            assertEquals(10.0, button.graphicTextGap)

            button.clearKoraIcon()

            assertNull(button.graphic)
        }
    }

    @Test
    fun `icons can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                hbox {
                    koraIcon(BootstrapIcons.ALARM)
                    iconButton(BootstrapIcons.ALARM, "Alert")
                }
            }

            val row = assertIs<javafx.scene.layout.HBox>(root.children.single())

            assertIs<FontIcon>(row.children[0])
            assertIs<Button>(row.children[1])
        }
    }
}
