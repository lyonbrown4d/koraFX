package dev.korafx.dsl

import javafx.scene.control.Label
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertTrue

class RenderStateDslTest {
    @Test
    fun `bind list renders empty and item content on fx thread`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val items = MutableStateFlow(emptyList<String>())
        val box = fx { VBox() }

        try {
            box.bindList(
                scope = scope,
                flow = items,
                empty = {
                    label("No items")
                },
            ) { item ->
                label(item)
            }

            FxTestSupport.waitForFxCondition {
                box.labels() == listOf("No items")
            }

            items.value = listOf("DSL", "MVVM")

            FxTestSupport.waitForFxCondition {
                box.labels() == listOf("DSL", "MVVM")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `bind render state swaps loading empty failure and content`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow<RenderState<List<String>>>(RenderState.Loading)
        val box = fx { VBox() }

        try {
            box.bindRenderState(
                scope = scope,
                flow = state,
                loading = {
                    label("Loading modules")
                },
                empty = {
                    label("No modules")
                },
                failed = { failure ->
                    label("Failed: ${failure.message}")
                },
            ) { modules ->
                renderEach(modules) { module ->
                    label(module)
                }
            }

            FxTestSupport.waitForFxCondition {
                box.labels() == listOf("Loading modules")
            }

            state.value = RenderState.Empty
            FxTestSupport.waitForFxCondition {
                box.labels() == listOf("No modules")
            }

            state.value = RenderState.Failed("Offline")
            FxTestSupport.waitForFxCondition {
                box.labels() == listOf("Failed: Offline")
            }

            state.value = RenderState.Content(listOf("framework-dsl", "framework-mvvm"))
            FxTestSupport.waitForFxCondition {
                box.labels() == listOf("framework-dsl", "framework-mvvm")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `list can be converted to render state`() {
        assertTrue(emptyList<String>().asRenderState() is RenderState.Empty)
        assertTrue(listOf("KoraFX").asRenderState() is RenderState.Content)
    }

    private fun VBox.labels(): List<String> =
        children.map { node -> (node as Label).text }

    private fun <T : Any> fx(factory: () -> T): T {
        lateinit var result: T
        FxTestSupport.runOnFxThread {
            result = factory()
        }
        return result
    }
}
