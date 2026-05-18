package dev.korafx.dsl

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DialogDslTest {
    @Test
    fun `alert builder configures dialog content`() {
        FxTestSupport.runOnFxThread {
            val dialog = alert(type = Alert.AlertType.WARNING) {
                title("Delete module")
                header(null)
                message("This cannot be undone.")
                buttonTypes(ButtonType.OK, ButtonType.CANCEL)
                expandableText("Details")
            }

            assertEquals(Alert.AlertType.WARNING, dialog.alertType)
            assertEquals("Delete module", dialog.title)
            assertEquals(null, dialog.headerText)
            assertEquals("This cannot be undone.", dialog.contentText)
            assertEquals(listOf(ButtonType.OK, ButtonType.CANCEL), dialog.buttonTypes)
            assertEquals(true, dialog.dialogPane.isExpanded)
        }
    }

    @Test
    fun `custom dialog builder configures content and result converter`() {
        FxTestSupport.runOnFxThread {
            val content = Label("Body")
            val dialog = customDialog<String> {
                title("Input")
                header("Header")
                content(content)
                buttonTypes(ButtonType.OK, ButtonType.CANCEL)
                result { button ->
                    if (button == ButtonType.OK) "accepted" else null
                }
            }

            assertEquals("Input", dialog.title)
            assertEquals("Header", dialog.headerText)
            assertSame(content, dialog.dialogPane.content)
            assertEquals("accepted", dialog.resultConverter.call(ButtonType.OK))
            assertEquals(null, dialog.resultConverter.call(ButtonType.CANCEL))
        }
    }

    @Test
    fun `custom dialog can map button types to result values`() {
        FxTestSupport.runOnFxThread {
            val dialog = customDialog<String> {
                buttons(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                resultByButton(
                    ButtonType.YES to "yes",
                    ButtonType.NO to "no",
                )
            }

            assertEquals("yes", dialog.resultConverter.call(ButtonType.YES))
            assertEquals("no", dialog.resultConverter.call(ButtonType.NO))
            assertEquals(null, dialog.resultConverter.call(ButtonType.CANCEL))
        }
    }

    @Test
    fun `text input dialog supports button types alias`() {
        FxTestSupport.runOnFxThread {
            val dialog = textInputDialog {
                title("Route")
                prompt("settings")
                buttonTypes(ButtonType.OK, ButtonType.CANCEL)
            }

            assertEquals("Route", dialog.title)
            assertEquals("settings", dialog.editor.promptText)
            assertEquals(listOf(ButtonType.OK, ButtonType.CANCEL), dialog.dialogPane.buttonTypes)
        }
    }
}
