package dev.korafx.grapheditor

import dev.korafx.dsl.panel
import dev.korafx.test.FxTestSupport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GraphEditorTest {
    @Test
    fun `graph editor syncs node and edge models through view updates`() {
        FxTestSupport.runOnFxThread {
            val editor = graphEditor()
            val source = editor.addNode(label = "Source", x = 40.0, y = 32.0)
            val target = editor.addNode(label = "Target", x = 220.0, y = 120.0)
            val link = editor.addEdge(source, target, label = "flow")

            assertEquals(2, editor.nodeViewIds().size)
            assertEquals(1, editor.graph.edges.size)

            editor.graph.selectEdge(link)
            assertEquals(link, editor.graph.selectedEdge)
            assertTrue(editor.graph.deleteSelected())
            assertEquals(0, editor.graph.edges.size)

            editor.graph.selectNode(source)
            assertEquals(source, editor.graph.selectedNode)
            assertTrue(editor.graph.deleteSelected())
            assertEquals(1, editor.graph.nodes.size)
        }
    }

    @Test
    fun `builder can be added through dsl`() {
        FxTestSupport.runOnFxThread {
            val host = panel {
                graphEditor {
                    val source = node("source", "Source", x = 56.0, y = 56.0)
                    val target = node("target", "Target", x = 240.0, y = 56.0)
                    edge(source, target)
                }
            }

            val editor = assertIs<GraphEditor>(assertNotNull(host.children.singleOrNull()))
            assertEquals(2, editor.graph.nodes.size)
            assertEquals(1, editor.graph.edges.size)
        }
    }
}
