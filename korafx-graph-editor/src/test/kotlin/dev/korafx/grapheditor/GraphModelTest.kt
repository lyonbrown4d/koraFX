package dev.korafx.grapheditor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraphModelTest {
    @Test
    fun `adds nodes with stable unique ids`() {
        val graph = Graph()
        val first = graph.addNode(label = "Start")
        val second = graph.addNode(label = "Process")
        val explicit = graph.addNode(id = "start", label = "Explicit")

        assertEquals("Start", first.label)
        assertEquals("Process", second.label)
        assertEquals("start", explicit.id)
        assertEquals(setOf("node-1", "node-2", "start"), graph.nodes.map { it.id }.toSet())
        assertEquals(3, graph.nodes.size)
    }

    @Test
    fun `creates and removes node-edge relations`() {
        val graph = Graph()
        val source = graph.addNode("source", label = "Source")
        val target = graph.addNode("target", label = "Target")
        val link = graph.addEdge(source, target, label = "flow")

        assertNotNull(graph.edgeOf(link.id))
        assertEquals(1, graph.edges.size)

        graph.removeNode(source)

        assertEquals(1, graph.nodes.size)
        assertEquals(0, graph.edges.size)
        assertNull(graph.edgeOf(link.id))
        assertNull(graph.nodeOf("source"))
    }

    @Test
    fun `selection state keeps single active target`() {
        val graph = Graph()
        val source = graph.addNode("source")
        val target = graph.addNode("target")
        val edge = graph.addEdge(source, target)

        graph.selectNode(source)
        assertEquals(source, graph.selectedNode)
        assertNull(graph.selectedEdge)

        graph.selectEdge(edge)
        assertEquals(edge, graph.selectedEdge)
        assertNull(graph.selectedNode)

        assertTrue(graph.deleteSelected())
        assertEquals(0, graph.edges.size)
        assertNull(graph.selectedEdge)
        assertEquals(2, graph.nodes.size)
        assertFalse(graph.deleteSelected())
    }

    @Test
    fun `handles delete selected node and connected links`() {
        val graph = Graph()
        val source = graph.addNode("source")
        val middle = graph.addNode("middle")
        val target = graph.addNode("target")

        graph.addEdge(source, middle)
        graph.addEdge(middle, target)
        graph.selectNode(middle)

        assertTrue(graph.deleteSelected())
        assertEquals(2, graph.nodes.size)
        assertEquals(0, graph.edges.size)
        assertNull(graph.selectedNode)
    }
}
