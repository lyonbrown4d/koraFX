package dev.korafx.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class MenuDslTest {
    @Test
    fun `action item wires menu action`() {
        var executed = 0

        val menuBar = menuBar {
            menu("File") {
                actionItem("Run") {
                    executed += 1
                }
            }
        }

        menuBar.menus.single().items.single().fire()

        assertEquals(1, executed)
    }

    @Test
    fun `action item action wins over init event handler`() {
        var executed = 0

        val menuBar = menuBar {
            menu("File") {
                actionItem(
                    text = "Run",
                    init = {
                        onAction {
                            executed = -1
                        }
                    },
                ) {
                    executed = 1
                }
            }
        }

        menuBar.menus.single().items.single().fire()

        assertEquals(1, executed)
    }
}
