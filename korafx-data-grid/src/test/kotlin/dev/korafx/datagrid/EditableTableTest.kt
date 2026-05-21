package dev.korafx.datagrid

import dev.korafx.dsl.panel
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EditableTableTest {
    private data class Row(
        var name: String,
        var owner: String,
    )

    @Test
    fun `editable table configures columns and table edit mode`() {
        FxTestSupport.runOnFxThread {
            val rows = listOf(Row("DSL", "Core"))
            val table = editableTable(rows) {
                placeholder("No rows")
                readOnlyTextColumn("Name") { it.name }
                editableTextColumn("Owner", valueOf = { it.owner }) { row, value ->
                    row.owner = value
                }
                actionColumn(title = "Action", text = "Open") {}
            }

            assertTrue(table.isEditable)
            assertTrue("editable-table" in table.styleClass)
            assertEquals(rows, table.items.toList())
            assertEquals(3, table.columns.size)
            val placeholder = assertIs<Label>(table.placeholder)
            assertEquals("No rows", placeholder.text)
            assertTrue("editable-table-placeholder" in placeholder.styleClass)
            assertTrue("editable-table-text-column" in table.columns[0].styleClass)
            assertTrue(table.columns[1].isEditable)
            assertTrue("editable-table-column" in table.columns[1].styleClass)
            assertTrue("editable-table-editable-text-column" in table.columns[1].styleClass)
            assertTrue("editable-table-action-column" in table.columns[2].styleClass)
        }
    }

    @Test
    fun `editable table marks placeholder nodes and node columns`() {
        FxTestSupport.runOnFxThread {
            val placeholder = Label("Nothing")
            val table = editableTable(listOf(Row("DSL", "Core"))) {
                placeholder(placeholder)
                columnNode("Owner", valueOf = { it.owner }) { Label(it) }
            }

            assertTrue("editable-table-placeholder" in placeholder.styleClass)
            assertTrue("editable-table-node-column" in table.columns.single().styleClass)
        }
    }

    @Test
    fun `editable text column invokes commit callback`() {
        FxTestSupport.runOnFxThread {
            val row = Row("DSL", "Core")
            val commits = mutableListOf<String>()
            val table = editableTable(listOf(row)) {
                editableTextColumn("Owner", valueOf = { it.owner }) { item, value ->
                    item.owner = value
                    commits += value
                }
            }
            @Suppress("UNCHECKED_CAST")
            val column = table.columns.single() as TableColumn<Row, String>
            val event =
                TableColumn.CellEditEvent(
                    table,
                    TablePosition(table, 0, column),
                    TableColumn.editCommitEvent(),
                    "Docs",
                )

            column.onEditCommit.handle(event)

            assertEquals("Docs", row.owner)
            assertEquals(listOf("Docs"), commits)
        }
    }

    @Test
    fun `editable table can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                editableTable(listOf(Row("Theme", "Design"))) {
                    readOnlyTextColumn("Name") { it.name }
                }
            }
            val table = assertIs<TableView<Row>>(root.children.single())

            assertTrue(table.isEditable)
            assertEquals("Theme", table.items.single().name)
        }
    }
}
