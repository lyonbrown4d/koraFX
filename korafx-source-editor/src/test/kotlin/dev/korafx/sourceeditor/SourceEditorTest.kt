package dev.korafx.sourceeditor

import dev.korafx.components.ComponentTone
import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
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

            assertTrue(editor.resultPane.isVisible)
            assertEquals("Output", editor.resultHeader.text)
            assertEquals("Build succeeded", assertIs<Label>(editor.resultContent.children.single()).text)
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
                ) {
                    diagnostic(1, 1, "Read-only preview", ComponentTone.INFO)
                }
            }
            val editor = assertIs<SourceEditor>(root.children.single())

            assertFalse(editor.editor.textArea.isEditable)
            assertEquals("key=value", editor.editor.textArea.text)
            assertEquals("Read-only preview", assertIs<Label>(
                assertIs<HBox>(editor.diagnosticsList.children.single()).children[1],
            ).text)
        }
    }
}
