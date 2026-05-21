package dev.korafx.datagrid

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DataGridTest {
    private data class Row(
        var name: String,
        var owner: String,
        val status: String,
    )

    @Test
    fun `data grid configures shell and delegates table columns`() {
        FxTestSupport.runOnFxThread {
            val rows = listOf(Row("DSL", "Core", "Ready"))
            val grid = dataGrid(rows) {
                constrainedResize()
                emptyState("No query results")
                footer("1 row")
                toolbar {
                    action("Refresh") {}
                }
                editableTextColumn("Name", valueOf = { it.name }) { row, value ->
                    row.name = value
                }
                readOnlyTextColumn("Owner") { it.owner }
                actionColumn(title = "Action", text = "Open") {}
            }

            assertTrue("data-grid" in grid.styleClass)
            assertTrue("data-grid-toolbar" in grid.toolbar.styleClass)
            assertTrue("data-grid-search" in grid.searchField.styleClass)
            assertTrue("data-grid-table" in grid.tableView.styleClass)
            assertTrue("editable-table" in grid.tableView.styleClass)
            assertTrue(grid.tableView.isEditable)
            assertEquals(rows, grid.tableView.items.toList())
            assertEquals(3, grid.tableView.columns.size)
            assertEquals("1 row", grid.footerLabel.text)
            assertTrue(grid.footer.isVisible)
            assertIs<Button>(grid.toolbar.children.last())
        }
    }

    @Test
    fun `data grid filters rows through search index`() {
        FxTestSupport.runOnFxThread {
            val rows = listOf(
                Row("DSL", "Core", "Ready"),
                Row("Theme", "Design", "Review"),
                Row("Components", "Product", "Draft"),
            )
            val grid = dataGrid(rows) {
                search(textOf = { "${it.name} ${it.owner} ${it.status}" })
                searchText("design")
                readOnlyTextColumn("Name") { it.name }
            }

            assertEquals(listOf("Theme"), grid.tableView.items.map { it.name })

            grid.setSearchText("missing")

            assertTrue(grid.tableView.items.isEmpty())
            assertEquals("No rows", assertIs<Label>(grid.tableView.placeholder).text)
        }
    }

    @Test
    fun `data grid tracks dirty rows and loading state`() {
        FxTestSupport.runOnFxThread {
            val rows = listOf(
                Row("DSL", "Core", "Ready"),
                Row("Components", "Product", "Draft"),
            )
            val grid = dataGrid(rows) {
                dirtyRows { it.status == "Draft" }
                readOnlyTextColumn("Name") { it.name }
            }
            val row = TableRow<Row>()

            grid.updateRowStyle(row, rows[1], empty = false)

            assertTrue(grid.isDirty(rows[1]))
            assertTrue("data-grid-row-dirty" in row.styleClass)

            grid.updateRowStyle(row, rows[0], empty = false)

            assertFalse("data-grid-row-dirty" in row.styleClass)

            grid.setLoading(true, "Fetching rows...")

            assertTrue(grid.tableView.items.isEmpty())
            assertEquals("Fetching rows...", assertIs<Label>(grid.tableView.placeholder).text)
        }
    }

    @Test
    fun `data grid reports selection summary`() {
        FxTestSupport.runOnFxThread {
            val rows = listOf(
                Row("DSL", "Core", "Ready"),
                Row("Theme", "Design", "Review"),
                Row("Components", "Product", "Draft"),
            )
            val grid = dataGrid(rows) {
                selectionMode(SelectionMode.MULTIPLE)
                search(textOf = { it.owner })
                selectionSummary()
                readOnlyTextColumn("Name") { it.name }
            }

            assertEquals("3 rows", grid.selectionSummaryLabel.text)
            assertTrue(grid.footer.isVisible)

            grid.tableView.selectionModel.selectIndices(0, 2)

            assertEquals("2 selected of 3 rows", grid.selectionSummaryLabel.text)

            grid.setSearchText("Design")

            assertEquals("1 row", grid.selectionSummaryLabel.text)
        }
    }

    @Test
    fun `data grid keeps footer hidden without footer content`() {
        FxTestSupport.runOnFxThread {
            val rows = listOf(Row("DSL", "Core", "Ready"))
            val grid = dataGrid(rows, showSearch = false) {
                readOnlyTextColumn("Name") { it.name }
            }

            assertFalse(grid.footer.isVisible)
            assertFalse(grid.footer.isManaged)
            assertFalse(grid.footerLabel.isVisible)
            assertFalse(grid.selectionSummaryLabel.isVisible)
        }
    }

    @Test
    fun `data grid toolbar batch action receives selected rows`() {
        FxTestSupport.runOnFxThread {
            val rows = listOf(
                Row("DSL", "Core", "Ready"),
                Row("Theme", "Design", "Review"),
            )
            val captured = mutableListOf<Row>()
            lateinit var action: Button
            val grid = dataGrid(rows) {
                selectionMode(SelectionMode.MULTIPLE)
                action = toolbarBatchAction("Archive") { selectedRows ->
                    captured += selectedRows
                }
                readOnlyTextColumn("Name") { it.name }
            }

            assertTrue(action.isDisable)

            grid.tableView.selectionModel.select(1)

            assertFalse(action.isDisable)

            action.fire()

            assertEquals(listOf(rows[1]), captured)
            assertTrue("data-grid-toolbar-batch-action" in action.styleClass)
        }
    }

    @Test
    fun `data grid editable column invokes commit callback`() {
        FxTestSupport.runOnFxThread {
            val row = Row("DSL", "Core", "Ready")
            val grid = dataGrid(listOf(row)) {
                editableTextColumn("Owner", valueOf = { it.owner }) { item, value ->
                    item.owner = value
                }
            }
            @Suppress("UNCHECKED_CAST")
            val column = grid.tableView.columns.single() as TableColumn<Row, String>
            val event =
                TableColumn.CellEditEvent(
                    grid.tableView,
                    javafx.scene.control.TablePosition(grid.tableView, 0, column),
                    TableColumn.editCommitEvent(),
                    "Docs",
                )

            column.onEditCommit.handle(event)

            assertEquals("Docs", row.owner)
        }
    }

    @Test
    fun `data grid can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                dataGrid(listOf(Row("Theme", "Design", "Review"))) {
                    readOnlyTextColumn("Name") { it.name }
                }
            }
            val grid = assertIs<DataGrid<Row>>(root.children.single())

            assertEquals("Theme", grid.tableView.items.single().name)
        }
    }
}
