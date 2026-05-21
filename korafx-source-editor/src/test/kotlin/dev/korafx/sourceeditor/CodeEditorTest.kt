package dev.korafx.sourceeditor

import dev.korafx.dsl.panel
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CodeEditorTest {
    @Test
    fun `code editor renders toolbar text area and status`() {
        FxTestSupport.runOnFxThread {
            val editor = codeEditor(
                title = "Document",
                text = "fun main() {\n}",
                language = "kotlin",
            )
            val labels = editor.children.flatMap { child ->
                when (child) {
                    is Label -> listOf(child)
                    is javafx.scene.Parent -> child.childrenUnmodifiable.filterIsInstance<Label>()
                    else -> emptyList()
                }
            }

            assertTrue("code-editor" in editor.styleClass)
            assertTrue("code-editor-area" in editor.textArea.styleClass)
            assertEquals("fun main() {\n}", editor.textArea.text)
            assertTrue(labels.any { it.text == "Document" && "code-editor-title" in it.styleClass })
            assertTrue(labels.any { it.text == "kotlin" && "badge" in it.styleClass })
            assertTrue(labels.any { it.text.startsWith("Ln 1, Col 1") })
            assertFalse(editor.isDirty)
        }
    }

    @Test
    fun `code editor tracks dirty state and suppresses programmatic updates`() {
        FxTestSupport.runOnFxThread {
            val changes = mutableListOf<String>()
            val editor = codeEditor(
                text = "initial",
                onTextChange = changes::add,
            )

            editor.textArea.text = "changed"

            assertTrue(editor.isDirty)
            assertEquals(listOf("changed"), changes)

            editor.setText("from-state", markClean = true)

            assertFalse(editor.isDirty)
            assertEquals(listOf("changed"), changes)

            editor.textArea.appendText("!")

            assertTrue(editor.isDirty)
            assertEquals(listOf("changed", "from-state!"), changes)

            editor.markClean()

            assertFalse(editor.isDirty)
        }
    }

    @Test
    fun `code editor can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                codeEditor(
                    title = "Notes",
                    text = "KoraFX",
                    placeholder = "Start typing...",
                )
            }
            val editor = assertIs<CodeEditor>(root.children.single())

            assertEquals("KoraFX", editor.textArea.text)
            assertEquals("Start typing...", editor.textArea.promptText)
        }
    }
}
