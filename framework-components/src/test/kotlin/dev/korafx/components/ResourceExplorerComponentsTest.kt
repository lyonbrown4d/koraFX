package dev.korafx.components

import dev.korafx.dsl.panel
import javafx.scene.control.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ResourceExplorerComponentsTest {
    private data class Resource(
        val name: String,
        val children: List<Resource> = emptyList(),
    )

    private val resources =
        listOf(
            Resource(
                "repository",
                listOf(
                    Resource("src", listOf(Resource("Main.kt"), Resource("Theme.kt"))),
                    Resource("README.md"),
                ),
            ),
            Resource(
                "database",
                listOf(
                    Resource("public", listOf(Resource("users"), Resource("orders"))),
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
}
