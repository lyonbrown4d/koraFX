package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SourceEditorTest {
    @Test
    fun `source editor renders actions diagnostics and result slot`() {
        FxTestSupport.runOnFxThread {
            var formatted = false
            var selectedDiagnostic: SourceDiagnostic? = null
            val editor = sourceEditor(
                title = "Main.kt",
                text = "fun main() {}",
                language = "kotlin",
                diagnostics = listOf(
                    SourceDiagnostic(1, 5, "Unused function", ComponentTone.WARNING),
                ),
            ) {
                action("Format") {
                    formatted = true
                }
                onDiagnosticSelected { diagnostic ->
                    selectedDiagnostic = diagnostic
                }
                result("Output", Label("Build succeeded"))
            }

            assertTrue("source-editor" in editor.styleClass)
            assertTrue("source-editor-code" in editor.editor.styleClass)
            assertEquals("fun main() {}", editor.editor.textArea.text)
            assertTrue(editor.toolbar.isVisible)
            assertIs<Button>(editor.toolbar.children.single()).fire()
            assertTrue(formatted)

            val diagnostic = assertIs<HBox>(editor.diagnosticsList.children.single())
            assertTrue("source-editor-diagnostic" in diagnostic.styleClass)
            assertEquals("1:5", assertIs<Label>(diagnostic.children.first()).text)
            assertEquals("Unused function", assertIs<Label>(diagnostic.children[1]).text)
            editor.jumpToDiagnostic(SourceDiagnostic(1, 5, "Unused function", ComponentTone.WARNING))
            assertEquals(1, editor.editor.currentLine)
            assertEquals(5, editor.editor.currentColumn)
            assertEquals("Unused function", selectedDiagnostic?.message)

            assertTrue(editor.resultPane.isVisible)
            assertEquals("Output", editor.resultHeader.text)
            assertEquals("Build succeeded", assertIs<Label>(editor.resultContent.children.single()).text)
            assertEquals(Double.MAX_VALUE, editor.resultPane.maxWidth)
            assertEquals(Double.MAX_VALUE, editor.resultContent.maxWidth)
        }
    }

    @Test
    fun `source editor result nodes fill the result slot`() {
        FxTestSupport.runOnFxThread {
            val result = Region()
            val editor = sourceEditor(text = "select 1") {
                result("Rows", result)
            }

            assertTrue("source-editor-result-node" in result.styleClass)
            assertEquals(Double.MAX_VALUE, result.maxWidth)
            assertEquals(javafx.scene.layout.Priority.SOMETIMES, VBox.getVgrow(result))
            assertEquals(result, editor.resultContent.children.single())
        }
    }

    @Test
    fun `source editor delegates dirty state to code editor`() {
        FxTestSupport.runOnFxThread {
            val editor = sourceEditor(text = "select 1")

            assertFalse(editor.isDirty)

            editor.editor.textArea.appendText(";")

            assertTrue(editor.isDirty)

            editor.markClean()

            assertFalse(editor.isDirty)
        }
    }

    @Test
    fun `source editor exposes replace and selection state through dsl`() {
        FxTestSupport.runOnFxThread {
            val editor = sourceEditor(
                text = "where status = active\nand status = active",
            ) {
                showReplace("active", "archived")
                replaceNext()
            }

            editor.editor.textArea.selectRange(0, 21)

            assertEquals("where status = archived\nand status = active", editor.editor.textArea.text)
            assertEquals(21, editor.selectionLength)
            assertEquals(1, editor.selectedLineCount)
            assertEquals(1, editor.replaceAll())
            assertEquals("where status = archived\nand status = archived", editor.editor.textArea.text)
        }
    }

    @Test
    fun `query editor wires run and stop actions to current query text`() {
        FxTestSupport.runOnFxThread {
            var query = ""
            var stopped = false
            val editor = queryEditor(
                text = "select * from users",
                onRun = { query = it },
                onStop = { stopped = true },
            )

            assertEquals("sql", assertIs<Label>(
                editor.editor.children
                    .filterIsInstance<javafx.scene.Parent>()
                    .flatMap { it.childrenUnmodifiable.filterIsInstance<Label>() }
                    .first { it.text == "sql" },
            ).text)

            assertIs<Button>(editor.toolbar.children[0]).fire()
            assertIs<Button>(editor.toolbar.children[1]).fire()

            assertEquals("select * from users", query)
            assertTrue(stopped)
        }
    }

    @Test
    fun `source editor can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                sourceEditor(
                    title = "Config",
                    text = "key=value",
                    readOnly = true,
                    showSearch = true,
                    wrapText = true,
                ) {
                    diagnostic(1, 1, "Read-only preview", ComponentTone.INFO)
                    showSearch("key")
                }
            }
            val editor = assertIs<SourceEditor>(root.children.single())

            assertFalse(editor.editor.textArea.isEditable)
            assertTrue(editor.editor.searchBar.isVisible)
            assertTrue(editor.editor.textArea.isWrapText)
            assertEquals("key=value", editor.editor.textArea.text)
            assertEquals("Read-only preview", assertIs<Label>(
                assertIs<HBox>(editor.diagnosticsList.children.single()).children[1],
            ).text)
        }
    }
}
