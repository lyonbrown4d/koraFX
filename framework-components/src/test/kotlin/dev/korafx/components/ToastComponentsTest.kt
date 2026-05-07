package dev.korafx.components

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToastComponentsTest {
    @Test
    fun `toast host stores and dismisses messages`() {
        val host = ToastHost()

        val first = host.show("Saved", title = "Done", tone = ToastTone.SUCCESS)
        val second = host.show("Offline", tone = ToastTone.ERROR)

        assertEquals(listOf(first, second), host.messages.value)

        host.dismiss(first.id)

        assertEquals(listOf(second), host.messages.value)

        host.clear()

        assertTrue(host.messages.value.isEmpty())
    }

    @Test
    fun `toast host renders messages and wires actions`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val host = ToastHost()
        var actionInvoked = false

        try {
            val container = FxTestSupport.run {
                lateinit var result: VBox
                runOnFxThread {
                    result = toastHost(scope, host)
                }
                result
            }

            val toast = host.show(
                message = "Saved successfully",
                title = "Saved",
                tone = ToastTone.SUCCESS,
                actionText = "Undo",
                onAction = {
                    actionInvoked = true
                },
            )

            FxTestSupport.waitForFxCondition {
                container.children.size == 1 &&
                    container.labels().contains("Saved successfully")
            }

            val snackbar = container.children.single()
            assertTrue(snackbar.styleClass.contains("snackbar"))
            assertTrue(snackbar.styleClass.contains("toast-success"))

            FxTestSupport.runOnFxThread {
                container.buttons().first { it.text == "Undo" }.fire()
            }

            assertTrue(actionInvoked)

            FxTestSupport.runOnFxThread {
                container.buttons().first { it.text == "Dismiss" }.fire()
            }

            FxTestSupport.waitForFxCondition {
                container.children.isEmpty() && host.messages.value.none { it.id == toast.id }
            }
        } finally {
            scope.cancel()
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
