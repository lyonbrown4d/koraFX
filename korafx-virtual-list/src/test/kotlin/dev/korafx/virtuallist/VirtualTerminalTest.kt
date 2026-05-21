package dev.korafx.virtuallist

import dev.korafx.dsl.vbox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VirtualTerminalTest {
    @Test
    fun `virtual terminal appends trims and clears lines`() {
        val terminal =
            FxTestSupport.callOnFxThread {
                virtualTerminal(maxLines = 2, autoScroll = false) {
                    line("one")
                    line("two", "terminal-info")
                    line("three", "terminal-error")
                }
            }

        FxTestSupport.runOnFxThread {
            assertEquals(2, terminal.lines.size)
            assertEquals("two", terminal.lines[0].text)
            assertEquals(listOf("terminal-info"), terminal.lines[0].styleClasses)
            assertEquals("three", terminal.lines[1].text)
            assertEquals(listOf("terminal-error"), terminal.lines[1].styleClasses)

            terminal.clear()
            assertEquals(0, terminal.lines.size)
        }
    }

    @Test
    fun `virtual terminal supports custom line renderer api`() {
        val terminal =
            FxTestSupport.callOnFxThread {
                virtualTerminal(autoScroll = false) {
                    lineRenderer { line ->
                        javafx.scene.control.Label("log: ${line.text}")
                    }
                    line("ready")
                }
            }

        FxTestSupport.runOnFxThread {
            assertEquals(1, terminal.lines.size)
            assertEquals("ready", terminal.lines.single().text)
        }
    }

    @Test
    fun `node container virtual terminal delegates to factory without recursion`() {
        val terminal =
            FxTestSupport.callOnFxThread {
                var created: VirtualTerminal? = null
                val root =
                    vbox {
                        created =
                            virtualTerminal(maxLines = 3, autoScroll = false) {
                                line("boot")
                            }
                    }

                assertEquals(1, root.children.size)
                assertTrue(root.children.first() === created)
                checkNotNull(created)
            }

        FxTestSupport.runOnFxThread {
            assertTrue("virtual-terminal" in terminal.styleClass)
            assertEquals(listOf("boot"), terminal.lines.map { it.text })
        }
    }
}
