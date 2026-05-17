package dev.korafx.dsl

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StatefulDslTest {
    private data class ScreenState(
        val title: String = "Draft",
        val canSave: Boolean = false,
        val name: String = "",
        val route: String = "editor",
        val error: String? = null,
        val notes: List<String> = emptyList(),
    )

    @Test
    fun `stateful controls bind properties from state selectors`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow(ScreenState())

        try {
            val root = fx {
                stateful(scope, state) {
                    vbox {
                        label(text = { it.title })
                        button(text = "Save", disabled = { !it.canSave })
                        label("Only editor") {
                            stateVisible { it.route == "editor" }
                        }
                    }
                }
            }

            val title = root.children[0] as Label
            val save = root.children[1] as Button
            val editorOnly = root.children[2] as Label

            FxTestSupport.waitForFxCondition {
                title.text == "Draft" && save.isDisable && editorOnly.isVisible
            }

            state.value = ScreenState(
                title = "Published",
                canSave = true,
                route = "settings",
            )

            FxTestSupport.waitForFxCondition {
                title.text == "Published" &&
                    !save.isDisable &&
                    !editorOnly.isVisible &&
                    !editorOnly.isManaged
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `stateful can be nested inside regular dsl`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow(ScreenState(title = "Dynamic"))

        try {
            val root = fx {
                vbox {
                    label("Static")
                    stateful(scope, state) {
                        label(text = { it.title })
                    }
                }
            }

            FxTestSupport.waitForFxCondition {
                root.labels() == listOf("Static", "Dynamic")
            }

            state.value = ScreenState(title = "Updated")

            FxTestSupport.waitForFxCondition {
                root.labels() == listOf("Static", "Updated")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `state helpers can be used without stateful builder`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow(ScreenState(title = "Dynamic", name = "KoraFX"))
        val changes = mutableListOf<String>()

        try {
            val root = fx {
                vbox {
                    label("Static")
                    label().stateText(scope, state) { it.title }
                    textField().stateText(
                        scope = scope,
                        state = state,
                        onTextChange = changes::add,
                    ) { it.name }
                    vbox {
                    }.stateList(
                        scope = scope,
                        state = state,
                        items = { it.notes },
                        empty = {
                            label("No notes")
                        },
                    ) { note ->
                        label(note)
                    }
                }
            }

            val dynamic = root.children[1] as Label
            val field = root.children[2] as TextField
            val listBox = root.children[3] as VBox

            FxTestSupport.waitForFxCondition {
                dynamic.text == "Dynamic" &&
                    field.text == "KoraFX" &&
                    listBox.labels() == listOf("No notes")
            }
            assertTrue(changes.isEmpty())

            state.value = ScreenState(title = "Updated", name = "State update", notes = listOf("One"))

            FxTestSupport.waitForFxCondition {
                dynamic.text == "Updated" &&
                    field.text == "State update" &&
                    listBox.labels() == listOf("One")
            }
            assertTrue(changes.isEmpty())

            FxTestSupport.runOnFxThread {
                field.text = "User edit"
            }
            assertEquals(listOf("User edit"), changes)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `stateful text input sends user changes without echoing state updates`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow(ScreenState(name = "KoraFX"))
        val changes = mutableListOf<String>()

        try {
            val root = fx {
                stateful(scope, state) {
                    vbox {
                        textField(
                            text = { it.name },
                            onTextChange = changes::add,
                        )
                    }
                }
            }

            val field = root.children.single() as TextField

            FxTestSupport.waitForFxCondition {
                field.text == "KoraFX"
            }
            assertTrue(changes.isEmpty())

            state.value = ScreenState(name = "State update")
            FxTestSupport.waitForFxCondition {
                field.text == "State update"
            }
            assertTrue(changes.isEmpty())

            FxTestSupport.runOnFxThread {
                field.text = "User edit"
            }
            assertEquals(listOf("User edit"), changes)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `stateful list renders empty and item content`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow(ScreenState())

        try {
            val root = fx {
                stateful(scope, state) {
                    vbox {
                        list(
                            items = { it.notes },
                            empty = {
                                label("No notes")
                            },
                        ) { note ->
                            label(note)
                        }
                    }
                }
            }

            val listBox = assertIs<VBox>(root.children.single())

            FxTestSupport.waitForFxCondition {
                listBox.labels() == listOf("No notes")
            }

            state.value = ScreenState(notes = listOf("One", "Two"))

            FxTestSupport.waitForFxCondition {
                listBox.labels() == listOf("One", "Two")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `stateful validation binds label visibility and text`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow(ScreenState())

        try {
            val root = fx {
                stateful(scope, state) {
                    vbox {
                        label {
                            stateValidation { it.error }
                        }
                    }
                }
            }

            val label = root.children.single() as Label

            FxTestSupport.waitForFxCondition {
                !label.isVisible && !label.isManaged && label.text == ""
            }

            state.value = ScreenState(error = "Name is required.")

            FxTestSupport.waitForFxCondition {
                label.isVisible && label.isManaged && label.text == "Name is required."
            }
        } finally {
            scope.cancel()
        }
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
