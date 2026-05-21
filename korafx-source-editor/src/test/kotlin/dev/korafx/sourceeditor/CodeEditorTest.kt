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
            assertTrue("code-editor-frame" in editor.editorFrame.styleClass)
            assertTrue("code-editor-line-numbers" in editor.lineNumberGutter.styleClass)
            assertEquals("fun main() {\n}", editor.textArea.text)
            assertEquals(2, editor.lineNumberGutter.children.size)
            assertTrue(labels.any { it.text == "Document" && "code-editor-title" in it.styleClass })
            assertTrue(labels.any { it.text == "kotlin" && "badge" in it.styleClass })
            assertTrue(labels.any { it.text.startsWith("Ln 1, Col 1") })
            assertEquals(1, editor.currentLine)
            assertEquals(1, editor.currentColumn)
            assertEquals(2, editor.lineCount)
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

    @Test
    fun `code editor supports search and navigation`() {
        FxTestSupport.runOnFxThread {
            val editor = codeEditor(
                text = "fun main() {\n    println(\"KoraFX\")\n}",
                showSearch = true,
            )

            val match = editor.find("println", startAt = 0)

            assertEquals(17, match)
            assertEquals("println", editor.textArea.selectedText)
            assertEquals("Match at 2:5", editor.searchResultLabel.text)

            val position = editor.goTo(2, 5)

            assertEquals(CodeEditorPosition(line = 2, column = 5, offset = 17), position)
            assertEquals(2, editor.currentLine)
            assertEquals(5, editor.currentColumn)
        }
    }

    @Test
    fun `code editor supports replace next and replace all`() {
        FxTestSupport.runOnFxThread {
            val editor = codeEditor(
                text = "select foo\nselect foo",
            )

            editor.showReplaceBar("foo", "bar")

            assertTrue(editor.replaceField.isVisible)
            assertTrue(editor.replaceNext())
            assertEquals("select bar\nselect foo", editor.textArea.text)
            assertEquals("bar", editor.textArea.selectedText)
            assertTrue(editor.isDirty)

            assertEquals(1, editor.replaceAll())
            assertEquals("select bar\nselect bar", editor.textArea.text)
        }
    }

    @Test
    fun `code editor tracks selected character and line counts`() {
        FxTestSupport.runOnFxThread {
            val editor = codeEditor(
                text = "alpha\nbeta\ngamma",
            )

            editor.textArea.selectRange(0, 10)

            assertEquals(10, editor.selectionLength)
            assertEquals(2, editor.selectedLineCount)
            assertTrue("10 selected across 2 lines" in editor.statusLabel.text)
        }
    }

    @Test
    fun `code editor does not replace while read only`() {
        FxTestSupport.runOnFxThread {
            val editor = codeEditor(
                text = "alpha alpha",
                readOnly = true,
            )

            editor.showReplaceBar("alpha", "beta")

            assertFalse(editor.replaceNext())
            assertEquals(0, editor.replaceAll())
            assertEquals("alpha alpha", editor.textArea.text)
        }
    }

    @Test
    fun `code editor can toggle line numbers and wrapping`() {
        FxTestSupport.runOnFxThread {
            val editor = codeEditor(
                text = "alpha\nbeta",
                showLineNumbers = false,
                wrapText = true,
            )

            assertFalse(editor.lineNumberGutter.isVisible)
            assertTrue(editor.textArea.isWrapText)

            editor.setLineNumbersVisible(true)
            editor.setWrapText(false)

            assertTrue(editor.lineNumberGutter.isVisible)
            assertFalse(editor.textArea.isWrapText)
        }
    }
}
