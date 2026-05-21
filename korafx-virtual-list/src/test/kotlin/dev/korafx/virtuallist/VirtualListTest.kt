package dev.korafx.virtuallist

import dev.korafx.dsl.vbox
import javafx.scene.control.Label
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class VirtualListTest {
    private fun pageLoader(
        total: Int,
        requests: MutableList<Pair<Long, Int>>,
    ): VirtualListDataLoader<String> = { offset, limit ->
        requests += offset to limit
        val start = offset.toInt()
        val end = min(start + limit, total)
        if (start >= end) {
            emptyList()
        } else {
            (start until end).map { index -> "item-$index" }
        }
    }

    @Test
    fun `virtual list loads fixed pages and supports DSL item builder`() {
        val requests = mutableListOf<Pair<Long, Int>>()

        val list =
            FxTestSupport.callOnFxThread {
                virtualList(
                    dataLoader = pageLoader(7, requests),
                    totalCountEstimate = { 7 },
                    pageSize = 3,
                    rowHeight = 41.0,
                ) {
                    selectionMode(VirtualSelectionMode.MULTIPLE)
                    item {
                        label("value: $item")
                    }
                }
            }

        FxTestSupport.waitForFxCondition { list.listView.items.size == 3 }

        FxTestSupport.runOnFxThread {
            assertEquals(41.0, list.listView.fixedCellSize)
            assertEquals(3, list.items.size)
            assertEquals("item-0", list.items[0])
            assertEquals(listOf(0L to 3), requests.take(1))
            assertEquals(3, list.state.itemCount)
            assertTrue(list.styleClass.contains("virtual-list"))

            val selectedRows = mutableListOf<List<String>>()
            list.selectionModel.onSelect { selectedRows += it }
            list.selectionModel.mode = VirtualSelectionMode.SINGLE
            list.listView.selectionModel.select(0)

            assertEquals(listOf(listOf("item-0")), selectedRows)
            assertEquals(3, list.items.size)
        }
    }

    @Test
    fun `virtual list can request additional pages manually`() {
        val requests = mutableListOf<Pair<Long, Int>>()

        val list =
            FxTestSupport.callOnFxThread {
                virtualList(
                    dataLoader = pageLoader(7, requests),
                    pageSize = 2,
                )
            }

        FxTestSupport.waitForFxCondition { list.items.size == 2 }
        FxTestSupport.runOnFxThread {
            assertEquals(listOf("item-0", "item-1"), list.items.toList())
            assertEquals(listOf(0L to 2), requests.take(1))
        }

        FxTestSupport.callOnFxThread { list.loadMore() }
        FxTestSupport.waitForFxCondition { list.items.size == 4 }
        FxTestSupport.runOnFxThread {
            assertEquals("item-3", list.items[3])
            assertEquals(listOf(2L to 2), requests.drop(1).take(1))
        }

        FxTestSupport.callOnFxThread { list.loadMore() }
        FxTestSupport.waitForFxCondition { list.items.size == 6 }

        FxTestSupport.callOnFxThread { list.loadMore() }
        FxTestSupport.waitForFxCondition { list.state.isAtEnd || list.items.size == 7 }
        FxTestSupport.runOnFxThread {
            assertEquals(7, list.items.size)
            assertEquals(listOf(2L to 2, 4L to 2, 6L to 2), requests.takeLast(3))
            assertTrue(list.state.isAtEnd)
        }
    }

    @Test
    fun `virtual list shows error placeholder when loader fails`() {
        var firstCall = true

        val list =
            FxTestSupport.callOnFxThread {
                virtualList(
                    dataLoader = { _, _ ->
                        if (firstCall) {
                            firstCall = false
                            error("boom")
                        }
                        emptyList<String>()
                    },
                )
            }

        FxTestSupport.waitForFxCondition {
            (list.listView.placeholder as? Label)?.text == "Failed to load items"
        }
        FxTestSupport.runOnFxThread {
            assertIs<Label>(list.listView.placeholder)

            assertEquals(
                "Failed to load items",
                assertIs<Label>(list.listView.placeholder).text,
            )
            assertEquals(0, list.items.size)
        }
    }

    @Test
    fun `node container virtual list delegates to factory without recursion`() {
        val list =
            FxTestSupport.callOnFxThread {
                var created: VirtualList<String>? = null
                val root =
                    vbox {
                        created =
                            virtualList(
                                dataLoader = { _, _ -> emptyList() },
                                pageSize = 1,
                            )
                    }

                assertEquals(1, root.children.size)
                assertTrue(root.children.first() === created)
                checkNotNull(created)
            }

        FxTestSupport.runOnFxThread {
            assertTrue("virtual-list" in list.styleClass)
        }
    }
}
