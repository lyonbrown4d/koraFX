package dev.korafx.dsl

import javafx.scene.control.Button
import javafx.scene.control.Cell
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.layout.StackPane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TableViewDslTest {
    @Test
    fun `text column stringifies values and action column invokes row handler`() {
        FxTestSupport.runOnFxThread {
            val row = ModuleRow(name = "framework-dsl", status = null)
            var opened: ModuleRow? = null

            val table = tableView(items = listOf(row)) {
                textColumn("Module") { it.name }
                textColumn("Status") { it.status }
                actionColumn(title = "Action", text = "Open") {
                    opened = it
                }
            }

            @Suppress("UNCHECKED_CAST")
            val statusColumn = table.columns[1] as TableColumn<ModuleRow, String>
            @Suppress("UNCHECKED_CAST")
            val actionColumn = table.columns[2] as TableColumn<ModuleRow, ModuleRow>

            assertEquals(3, table.columns.size)
            assertEquals("", statusColumn.getCellObservableValue(row).value)

            val cell = actionColumn.cellFactory.call(actionColumn)
            cell.updateForTest(row, empty = false)

            val graphic = cell.graphic as StackPane
            val button = graphic.children.single() as Button
            button.fire()

            assertSame(row, opened)
        }
    }

    @Test
    fun `action column handler wins over init event handler`() {
        FxTestSupport.runOnFxThread {
            val row = ModuleRow(name = "framework-dsl", status = "ready")
            var opened: ModuleRow? = null

            val table = tableView(items = listOf(row)) {
                actionColumn(
                    title = "Action",
                    text = "Open",
                    init = {
                        onAction {
                            opened = ModuleRow(name = "wrong", status = null)
                        }
                    },
                ) {
                    opened = it
                }
            }

            @Suppress("UNCHECKED_CAST")
            val actionColumn = table.columns.single() as TableColumn<ModuleRow, ModuleRow>
            val cell = actionColumn.cellFactory.call(actionColumn)
            cell.updateForTest(row, empty = false)

            val graphic = cell.graphic as StackPane
            val button = graphic.children.single() as Button
            button.fire()

            assertSame(row, opened)
        }
    }

    private data class ModuleRow(
        val name: String,
        val status: String?,
    )

    private fun <T> TableCell<*, T>.updateForTest(item: T, empty: Boolean) {
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
