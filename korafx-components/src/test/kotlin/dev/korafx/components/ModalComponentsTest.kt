package dev.korafx.components

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ModalComponentsTest {
    @Test
    fun `modal host stores and dismisses current request`() {
        val host = ModalHost()

        val request = host.show(
            title = "Edit workspace",
            message = "Update workspace settings.",
        )

        assertEquals(request, host.current.value)

        host.dismiss("other")

        assertSame(request, host.current.value)

        host.dismiss(request.id)

        assertNull(host.current.value)
    }

    @Test
    fun `modal host renders current request and wires actions`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val host = ModalHost()
        var saved = false

        try {
            val container = FxTestSupport.run {
                lateinit var result: StackPane
                runOnFxThread {
                    result = modalHost(scope, host)
                }
                result
            }

            host.show(
                title = "Edit workspace",
                message = "Save changes before leaving.",
                actions = listOf(
                    ModalAction("Cancel"),
                    ModalAction(
                        text = "Save",
                        role = ModalActionRole.PRIMARY,
                        onAction = {
                            saved = true
                        },
                    ),
                ),
            ) {
                label("Workspace name")
            }

            FxTestSupport.waitForFxCondition {
                container.isVisible &&
                    container.labels().contains("Edit workspace") &&
                    container.labels().contains("Workspace name")
            }

            val backdrop = container.children.single() as StackPane
            val card = backdrop.children.single() as VBox

            assertTrue("modal-host" in container.styleClass)
            assertTrue("modal-backdrop" in backdrop.styleClass)
            assertTrue("modal-card" in card.styleClass)

            FxTestSupport.runOnFxThread {
                container.buttons().first { it.text == "Save" }.fire()
            }

            assertTrue(saved)

            FxTestSupport.waitForFxCondition {
                !container.isVisible && host.current.value == null
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `modal host hides without current request`() {
        FxTestSupport.runOnFxThread {
            val host = ModalHost()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

            try {
                val container = modalHost(scope, host)

                assertFalse(container.isVisible)
                assertFalse(container.isManaged)
            } finally {
                scope.cancel()
            }
        }
    }

    private fun Node.labels(): List<String> =
        descendants().filterIsInstance<Label>().map(Label::getText)

    private fun Node.buttons(): List<Button> =
        descendants().filterIsInstance<Button>()

    private fun Node.descendants(): List<Node> {
        val result = mutableListOf<Node>()

        fun visit(node: Node) {
            result += node
            if (node is Pane) {
                node.children.forEach(::visit)
            }
        }

        visit(this)
        return result
    }
}
