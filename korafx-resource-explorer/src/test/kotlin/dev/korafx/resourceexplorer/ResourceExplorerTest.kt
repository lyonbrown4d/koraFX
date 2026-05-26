package dev.korafx.resourceexplorer

import dev.korafx.dsl.panel
import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ResourceExplorerTest {
    private data class Resource(
        val name: String,
        val status: String? = null,
        val secondary: String? = null,
        val children: List<Resource> = emptyList(),
    )

    private val resources =
        listOf(
            Resource(
                "repository",
                children = listOf(
                    Resource("src", children = listOf(Resource("Main.kt"), Resource("Theme.kt"))),
                    Resource("README.md"),
                ),
            ),
            Resource(
                "database",
                children = listOf(
                    Resource("public", children = listOf(Resource("users"), Resource("orders"))),
                ),
            ),
        )

    @Test
    fun `resource explorer renders tree and filters matching descendants`() {
        FxTestSupport.runOnFxThread {
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                )

            assertTrue("resource-explorer" in explorer.styleClass)
            assertTrue("resource-explorer-search" in explorer.searchField.styleClass)
            assertTrue("resource-explorer-tree" in explorer.treeView.styleClass)
            assertFalse(explorer.treeView.isShowRoot)
            assertEquals(2, explorer.treeView.root.children.size)

            explorer.setSearchText("users")

            assertEquals(1, explorer.treeView.root.children.size)
            assertEquals("database", explorer.treeView.root.children[0].value.name)
            assertEquals("public", explorer.treeView.root.children[0].children[0].value.name)
            assertEquals("users", explorer.treeView.root.children[0].children[0].children[0].value.name)
        }
    }

    @Test
    fun `resource explorer exposes selection and context menu callbacks`() {
        FxTestSupport.runOnFxThread {
            val selections = mutableListOf<Resource?>()
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                ) {
                    onSelect(selections::add)
                    contextMenu { resource ->
                        actionItem("Open") {
                            selections += resource
                        }
                    }
                }
            val selected = explorer.treeView.root.children.first().value

            explorer.treeView.selectionModel.select(explorer.treeView.root.children.first())
            val menu = explorer.createContextMenu(selected)

            assertEquals(listOf(selected), selections)
            assertEquals("Open", menu!!.items.single().text)

            menu.items.single().fire()

            assertEquals(listOf(selected, selected), selections)
        }
    }

    @Test
    fun `resource explorer can hide search and render graphics`() {
        FxTestSupport.runOnFxThread {
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                    showSearch = false,
                ) {
                    graphic { Label(it.name.take(1)) }
                }

            assertFalse(explorer.searchField.isVisible)
            assertFalse(explorer.searchField.isManaged)
            assertIs<Label>(explorer.treeView.root.children.first().graphic)
        }
    }

    @Test
    fun `resource explorer renders secondary text and status rows`() {
        FxTestSupport.runOnFxThread {
            val changedFile = Resource(
                name = "UserRepository.kt",
                status = "M",
                secondary = "src/main/kotlin/data",
            )
            val explorer =
                resourceExplorer(
                    items = listOf(changedFile),
                    textOf = { it.name },
                ) {
                    graphic { Label("KT") }
                    secondaryText { it.secondary }
                    status { it.status }
            }
            val cell = explorer.treeView.cellFactory.call(explorer.treeView)

            cell.render(explorer.treeView, explorer.treeView.root.children.single())

            val row = assertIs<HBox>(cell.graphic)
            val labels = row.descendantLabels()

            assertEquals(null, cell.text)
            assertTrue("resource-explorer-row" in row.styleClass)
            assertEquals(listOf("KT", "UserRepository.kt", "src/main/kotlin/data", "M"), labels.map { it.text })
            assertTrue("resource-explorer-row-icon" in labels[0].styleClass)
            assertTrue("resource-explorer-row-primary" in labels[1].styleClass)
            assertTrue("resource-explorer-row-secondary" in labels[2].styleClass)
            assertTrue("resource-explorer-row-status" in labels[3].styleClass)
        }
    }

    @Test
    fun `resource explorer shows empty state for empty and filtered trees`() {
        FxTestSupport.runOnFxThread {
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                ) {
                    emptyState("No matching resources")
                }

            assertFalse(explorer.emptyStateLabel.isVisible)
            assertTrue(explorer.treeView.isVisible)

            explorer.setSearchText("missing-resource")

            assertTrue(explorer.emptyStateLabel.isVisible)
            assertTrue(explorer.emptyStateLabel.isManaged)
            assertFalse(explorer.treeView.isVisible)
            assertFalse(explorer.treeView.isManaged)
            assertEquals("No matching resources", explorer.emptyStateLabel.text)
            assertTrue("resource-explorer-empty-state" in explorer.emptyStateLabel.styleClass)
        }
    }

    @Test
    fun `resource explorer exposes selected path and breadcrumb text`() {
        FxTestSupport.runOnFxThread {
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                ) {
                    breadcrumb(separator = " > ")
                }
            val sourceFile = resources[0].children[0].children[0]

            assertTrue(explorer.selectPath(listOf(resources[0], resources[0].children[0], sourceFile)))

            assertTrue(explorer.breadcrumbLabel.isVisible)
            assertTrue(explorer.breadcrumbLabel.isManaged)
            assertEquals(sourceFile, explorer.selectedItem())
            assertEquals(listOf(resources[0], resources[0].children[0], sourceFile), explorer.selectedPath())
            assertEquals("repository > src > Main.kt", explorer.selectedPathText())
            assertEquals("repository > src > Main.kt", explorer.breadcrumbLabel.text)
        }
    }

    @Test
    fun `resource explorer can collapse and expand tree selections`() {
        FxTestSupport.runOnFxThread {
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                )
            val repository = explorer.treeView.root.children.first()

            explorer.expandAll()

            assertTrue(repository.isExpanded)
            assertTrue(repository.children.first().isExpanded)

            explorer.collapseAll()

            assertFalse(repository.isExpanded)
            assertFalse(repository.children.first().isExpanded)
            assertTrue(explorer.treeView.root.isExpanded)

            explorer.treeView.selectionModel.select(repository)
            explorer.expandSelected()

            assertTrue(repository.isExpanded)
            assertTrue(repository.children.first().isExpanded)

            explorer.collapseSelected()

            assertFalse(repository.isExpanded)
            assertFalse(repository.children.first().isExpanded)
        }
    }

    @Test
    fun `resource explorer select path reports missing paths`() {
        FxTestSupport.runOnFxThread {
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                )

            assertFalse(explorer.selectPath(listOf(resources[0], resources[1])))
            assertEquals(emptyList(), explorer.selectedPath())
        }
    }

    @Test
    fun `resource explorer can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root =
                panel {
                    resourceExplorer(
                        items = resources,
                        childrenOf = { it.children },
                        textOf = { it.name },
                    )
                }
            val explorer = assertIs<ResourceExplorer<Resource>>(root.children.single())

            assertEquals("repository", explorer.treeView.root.children.first().value.name)
        }
    }

    @Test
    fun `resource explorer can clear selection and report emptiness`() {
        FxTestSupport.runOnFxThread {
            val explorer =
                resourceExplorer(
                    items = resources,
                    childrenOf = { it.children },
                    textOf = { it.name },
                )
            val repository = explorer.treeView.root.children.first()
            explorer.treeView.selectionModel.select(repository)

            assertFalse(explorer.isEmpty())
            assertEquals(repository.value, explorer.selectedItem())

            explorer.clearSelection()

            assertEquals(null, explorer.selectedItem())
            assertTrue(explorer.selectedPath().isEmpty())
        }
    }

    private fun <T> TreeCell<T>.render(
        treeView: TreeView<T>,
        item: TreeItem<T>,
    ) {
        updateTreeView(treeView)
        updateTreeItem(item)
        updateIndex(0)
    }

    private fun HBox.descendantLabels(): List<Label> =
        children.flatMap { child ->
            when (child) {
                is Label -> listOf(child)
                is VBox -> child.children.filterIsInstance<Label>()
                else -> emptyList()
            }
        }
}
