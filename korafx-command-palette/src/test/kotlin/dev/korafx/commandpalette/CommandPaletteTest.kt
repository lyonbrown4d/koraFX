package dev.korafx.commandpalette

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CommandPaletteTest {
    @Test
    fun `command palette filters and groups commands`() {
        FxTestSupport.runOnFxThread {
            val host = CommandPaletteHost(
                listOf(
                    CommandPaletteCommand("open-file", "Open File", "Open repository file", "Navigation"),
                    CommandPaletteCommand("run-query", "Run Query", "Execute SQL", "Database"),
                    CommandPaletteCommand("toggle-theme", "Toggle Theme", "Switch preset", "View"),
                ),
            )
            val palette = commandPalette(host)

            palette.setSearchText("query")

            assertEquals(2, palette.results.children.size)
            assertEquals("Database", assertIs<Label>(palette.results.children[0]).text)
            val row = assertIs<Button>(palette.results.children[1])
            assertTrue("command-palette-row-selected" in row.styleClass)
            assertTrue("command-palette-row" in row.styleClass)
        }
    }

    @Test
    fun `command palette executes selected command and closes host`() {
        FxTestSupport.runOnFxThread {
            var executed = ""
            val host = CommandPaletteHost()
            val palette = commandPalette(host) {
                command(
                    id = "open-file",
                    title = "Open File",
                    group = "Navigation",
                ) {
                    executed = "open-file"
                }
            }

            host.show()

            assertTrue(host.isVisible)
            assertTrue(palette.isVisible)
            assertTrue(palette.executeSelected())

            assertEquals("open-file", executed)
            assertFalse(host.isVisible)
            assertFalse(palette.isVisible)
        }
    }

    @Test
    fun `command palette shows empty state and supports explicit close`() {
        FxTestSupport.runOnFxThread {
            val host = CommandPaletteHost()
            val palette = commandPalette(host, emptyText = "No matching command")

            host.show()
            palette.setSearchText("missing")

            assertEquals(palette.emptyLabel, palette.results.children.single())
            assertEquals("No matching command", palette.emptyLabel.text)

            host.hide()

            assertFalse(palette.isVisible)
            assertFalse(palette.isManaged)
        }
    }

    @Test
    fun `command palette can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val host = CommandPaletteHost()
            val root = panel {
                commandPalette(host) {
                    command("run", "Run Query")
                }
            }
            val palette = assertIs<CommandPalette>(root.children.single())

            assertEquals(1, host.commands.size)
            assertEquals("run", host.commands.single().id)
            assertTrue("command-palette" in palette.styleClass)
        }
    }
}
