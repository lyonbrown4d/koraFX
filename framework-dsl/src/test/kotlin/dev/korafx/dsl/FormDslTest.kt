package dev.korafx.dsl

import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FormDslTest {
    @Test
    fun `form can be nested and build common fields`() {
        FxTestSupport.runOnFxThread {
            lateinit var nameField: TextField
            lateinit var descriptionField: TextArea
            lateinit var enabledField: CheckBox
            lateinit var saveButton: Button

            val root = vbox {
                form {
                    nameField = textField(
                        label = "Name",
                        helper = "Shown in the workbench title.",
                        text = "KoraFX",
                    )
                    descriptionField = textArea("Description")
                    enabledField = checkBox(
                        label = "Enabled",
                        text = "Enable feature",
                    )
                    submitBar {
                        secondaryButton("Cancel")
                        saveButton = primaryButton("Save")
                    }
                }
            }

            val form = root.children.single() as VBox
            assertTrue("form" in form.styleClass)
            assertEquals(4, form.children.size)
            assertEquals("KoraFX", nameField.text)
            assertSame(descriptionField, (form.children[1] as VBox).children[1])
            assertEquals("Enable feature", enabledField.text)
            assertEquals("Save", saveButton.text)
        }
    }

    @Test
    fun `submit bar can align actions at start or end`() {
        FxTestSupport.runOnFxThread {
            val startAligned = form {
                submitBar(alignEnd = false) {
                    primaryButton("Save")
                }
            }

            val endAligned = form {
                submitBar {
                    primaryButton("Save")
                }
            }

            val startBar = startAligned.children.single() as HBox
            val endBar = endAligned.children.single() as HBox

            assertEquals(1, startBar.children.size)
            assertEquals(2, endBar.children.size)
            assertEquals(Priority.ALWAYS, HBox.getHgrow(endBar.children.first()))
        }
    }

    @Test
    fun `form validation message can bind nullable message flow`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val message = MutableStateFlow<String?>(null)

        try {
            lateinit var field: TextField
            lateinit var root: VBox

            FxTestSupport.runOnFxThread {
                root = form {
                    item("Name") {
                        field = textField()
                        validationMessage(scope, message)
                    }
                }
            }

            val item = root.children.single() as VBox
            val validation = item.children[2] as javafx.scene.control.Label

            FxTestSupport.waitForFxCondition {
                !validation.isVisible && field.text == ""
            }

            message.value = "Name is required."

            FxTestSupport.waitForFxCondition {
                validation.isVisible && validation.text == "Name is required."
            }
        } finally {
            scope.cancel()
        }
    }
}
