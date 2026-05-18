package dev.korafx.dsl

import javafx.scene.control.Cell
import javafx.scene.control.ListCell
import javafx.scene.control.TreeCell
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CellFactoryDslTest {
    @Test
    fun `list view render and row action share one cell factory`() {
        FxTestSupport.runOnFxThread {
            val listView = listView(items = listOf("alpha")) {
                render { it.uppercase() }
                rowAction { }
            }

            val cell = listView.cellFactory.call(listView)
            cell.updateForTest("alpha", empty = false)

            assertEquals("ALPHA", cell.text)
            assertNotNull(cell.onMouseClicked)
        }
    }

    @Test
    fun `tree view render and row action share one cell factory`() {
        FxTestSupport.runOnFxThread {
            val treeView = treeView<String> {
                root("root") {
                    item("alpha")
                }
                render { "node:$it" }
                rowAction { }
            }

            val cell = treeView.cellFactory.call(treeView)
            cell.updateForTest("alpha", empty = false)

            assertEquals("node:alpha", cell.text)
            assertNotNull(cell.onMouseClicked)
        }
    }

    @Test
    fun `combo and choice boxes render item text and expose selection handler`() {
        data class Option(
            val id: String,
            val label: String,
        )

        val alpha = Option("a", "Alpha")
        val beta = Option("b", "Beta")

        FxTestSupport.runOnFxThread {
            val comboSelections = mutableListOf<Option?>()
            val choiceSelections = mutableListOf<Option?>()

            val combo = comboBox(items = listOf(alpha, beta)) {
                render { it.label }
                onSelect(comboSelections::add)
            }
            val choice = choiceBox(items = listOf(alpha, beta)) {
                render { it.label }
                onSelect(choiceSelections::add)
            }

            assertEquals("Alpha", combo.converter.toString(alpha))
            assertEquals(beta, combo.converter.fromString("Beta"))
            assertEquals("Alpha", choice.converter.toString(alpha))
            assertEquals(beta, choice.converter.fromString("Beta"))

            combo.selectionModel.select(beta)
            choice.selectionModel.select(alpha)

            assertEquals(beta, comboSelections.single())
            assertEquals(alpha, choiceSelections.single())
        }
    }

    private fun <T> ListCell<T>.updateForTest(item: T, empty: Boolean) {
        invokeUpdateItem(item, empty)
    }

    private fun <T> TreeCell<T>.updateForTest(item: T, empty: Boolean) {
        invokeUpdateItem(item, empty)
    }

    private fun <T> Cell<T>.invokeUpdateItem(item: T, empty: Boolean) {
        val method =
            generateSequence(javaClass as Class<*>?) { it.superclass }
                .mapNotNull { type ->
                    runCatching {
                        type.getDeclaredMethod(
                            "updateItem",
                            Any::class.java,
                            Boolean::class.javaPrimitiveType,
                        )
                    }.getOrNull()
                }
                .first()

        method.isAccessible = true
        method.invoke(this, item, empty)
    }
}
