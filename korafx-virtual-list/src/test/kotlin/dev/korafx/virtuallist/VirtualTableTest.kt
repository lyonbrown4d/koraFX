package dev.korafx.virtuallist

import dev.korafx.dsl.vbox
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VirtualTableTest {
    data class Row(
        val id: Int,
        val name: String,
    )

    private fun pageLoader(
        total: Int,
        requests: MutableList<Pair<Long, Int>>,
    ): VirtualTableDataLoader<Row> = { offset, limit ->
        requests += offset to limit
        val start = offset.toInt()
        val end = min(start + limit, total)
        if (start >= end) {
            emptyList()
        } else {
            (start until end).map { index -> Row(index, "row-$index") }
        }
    }

    @Test
    fun `virtual table loads first page and builds columns`() {
        val requests = mutableListOf<Pair<Long, Int>>()

        val table =
            FxTestSupport.callOnFxThread {
                virtualTable(
                    dataLoader = pageLoader(5, requests),
                    totalCountEstimate = { 5 },
                    pageSize = 2,
                    selectionMode = VirtualSelectionMode.SINGLE,
                ) {
                    textColumn("ID", Row::id)
                    textColumn("Name", Row::name)
                }
            }

        FxTestSupport.waitForFxCondition { table.items.size == 2 }

        FxTestSupport.runOnFxThread {
            assertEquals(listOf(0L to 2), requests.take(1))
            assertEquals(2, table.items.size)
            assertEquals(2, table.tableView.columns.size)
            assertEquals("ID", table.tableView.columns[0].text)
            assertEquals("Name", table.tableView.columns[1].text)
            assertEquals(2, table.state.rowCount)
            assertTrue("virtual-table" in table.styleClass)
        }
    }

    @Test
    fun `virtual table supports selection callback and manual paging`() {
        val requests = mutableListOf<Pair<Long, Int>>()

        val table =
            FxTestSupport.callOnFxThread {
                virtualTable(
                    dataLoader = pageLoader(3, requests),
                    totalCountEstimate = { 3 },
                    pageSize = 2,
                ) {
                    textColumn("Name", Row::name)
                }
            }

        FxTestSupport.waitForFxCondition { table.items.size == 2 }

        val selectedRows = mutableListOf<List<Row>>()
        FxTestSupport.runOnFxThread {
            table.onSelect { selectedRows += it }
            table.selectionModel.mode = VirtualSelectionMode.SINGLE
            table.selectionModel.select(1)
        }

        FxTestSupport.callOnFxThread { table.loadMore() }
        FxTestSupport.waitForFxCondition { table.items.size == 3 }

        FxTestSupport.runOnFxThread {
            assertEquals(listOf(listOf(Row(1, "row-1"))), selectedRows)
            assertEquals(Row(2, "row-2"), table.items[2])
            assertEquals(listOf(0L to 2, 2L to 2), requests)
            assertTrue(table.state.isAtEnd)
        }
    }

    @Test
    fun `node container virtual table delegates to factory without recursion`() {
        val table =
            FxTestSupport.callOnFxThread {
                var created: VirtualTable<Row>? = null
                val root =
                    vbox {
                        created =
                            virtualTable(
                                dataLoader = { _, _ -> emptyList() },
                                pageSize = 1,
                            ) {
                                textColumn("Name", Row::name)
                            }
                    }

                assertEquals(1, root.children.size)
                assertTrue(root.children.first() === created)
                checkNotNull(created)
            }

        FxTestSupport.runOnFxThread {
            assertTrue("virtual-table" in table.styleClass)
            assertEquals(1, table.tableView.columns.size)
        }
    }
}
