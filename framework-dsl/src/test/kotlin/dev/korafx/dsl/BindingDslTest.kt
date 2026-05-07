package dev.korafx.dsl

import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import kotlin.test.Test

class BindingDslTest {
    @Test
    fun `bindText updates labeled control on fx thread`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow("initial")
        val label = fx { Label() }

        try {
            label.bindText(scope, state)
            state.value = "updated"

            FxTestSupport.waitForFxCondition {
                label.text == "updated"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `text input bidirectional binding syncs state and control`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow("from-state")
        val field = fx { TextField() }

        try {
            field.bindTextBidirectional(scope, state)

            FxTestSupport.waitForFxCondition {
                field.text == "from-state"
            }

            FxTestSupport.runOnFxThread {
                field.text = "from-control"
            }

            FxTestSupport.waitForFxCondition {
                state.value == "from-control"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `toggle selected bidirectional binding syncs state and control`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val state = MutableStateFlow(false)
        val button = fx { ToggleButton("Enabled") }

        try {
            button.bindSelectedBidirectional(scope, state)

            state.value = true
            FxTestSupport.waitForFxCondition {
                button.isSelected
            }

            FxTestSupport.runOnFxThread {
                button.isSelected = false
            }

            FxTestSupport.waitForFxCondition {
                !state.value
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `combo and choice selected item bindings sync state and controls`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val comboState = MutableStateFlow<String?>("DSL")
        val choiceState = MutableStateFlow<String?>("Manual")
        val combo = fx { comboBox(items = listOf("DSL", "MVVM")) }
        val choice = fx { choiceBox(items = listOf("Manual", "Auto")) }

        try {
            combo.bindSelectedItemBidirectional(scope, comboState)
            choice.bindSelectedItemBidirectional(scope, choiceState)

            FxTestSupport.waitForFxCondition {
                combo.selectionModel.selectedItem == "DSL" &&
                    choice.selectionModel.selectedItem == "Manual"
            }

            FxTestSupport.runOnFxThread {
                combo.selectionModel.select("MVVM")
                choice.selectionModel.select("Auto")
            }

            FxTestSupport.waitForFxCondition {
                comboState.value == "MVVM" && choiceState.value == "Auto"
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `spinner and date picker value bindings sync state and controls`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val count = MutableStateFlow(2)
        val date = MutableStateFlow<LocalDate?>(LocalDate.of(2026, 5, 7))
        val spinner = fx { intSpinner(min = 0, max = 10) }
        val datePicker = fx { datePicker() }

        try {
            spinner.bindValueBidirectional(scope, count)
            datePicker.bindValueBidirectional(scope, date)

            FxTestSupport.waitForFxCondition {
                spinner.value == 2 && datePicker.value == LocalDate.of(2026, 5, 7)
            }

            FxTestSupport.runOnFxThread {
                spinner.valueFactory.value = 5
                datePicker.value = LocalDate.of(2026, 5, 8)
            }

            FxTestSupport.waitForFxCondition {
                count.value == 5 && date.value == LocalDate.of(2026, 5, 8)
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `node bindings update visibility and style classes`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val visible = MutableStateFlow(true)
        val highlighted = MutableStateFlow(false)
        val node = fx { Region() }

        try {
            node.bindVisible(scope, visible)
            node.bindStyleClass(scope, "highlighted", highlighted)

            visible.value = false
            highlighted.value = true

            FxTestSupport.waitForFxCondition {
                !node.isVisible && !node.isManaged && "highlighted" in node.styleClass
            }

            visible.value = true
            highlighted.value = false

            FxTestSupport.waitForFxCondition {
                node.isVisible && node.isManaged && "highlighted" !in node.styleClass
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `validation and invalid bindings update form controls`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val message = MutableStateFlow<String?>(null)
        val invalid = MutableStateFlow(false)
        val label = fx { Label() }
        val field = fx { TextField() }

        try {
            label.bindValidation(scope, message)
            field.bindInvalid(scope, invalid)

            FxTestSupport.waitForFxCondition {
                !label.isVisible && !label.isManaged && label.text == "" && "invalid" !in field.styleClass
            }

            message.value = "Project name is required."
            invalid.value = true

            FxTestSupport.waitForFxCondition {
                label.isVisible &&
                    label.isManaged &&
                    label.text == "Project name is required." &&
                    "invalid" in field.styleClass
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `bind children creates nodes on fx thread`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val items = MutableStateFlow(emptyList<String>())
        val box = fx { VBox() }

        try {
            box.bindChildren(scope, items) { item ->
                check(javafx.application.Platform.isFxApplicationThread()) {
                    "Node factory must run on the JavaFX Application Thread."
                }
                Label(item)
            }

            items.value = listOf("DSL", "MVVM")

            FxTestSupport.waitForFxCondition {
                box.children.map { node -> (node as Label).text } == listOf("DSL", "MVVM")
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `bindItems updates JavaFX item controls`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val items = MutableStateFlow(listOf("DSL"))
        val listView = fx { ListView<String>() }
        val comboBox = fx { ComboBox<String>() }
        val choiceBox = fx { ChoiceBox<String>() }
        val tableView = fx { TableView<String>() }

        try {
            listView.bindItems(scope, items)
            comboBox.bindItems(scope, items)
            choiceBox.bindItems(scope, items)
            tableView.bindItems(scope, items)

            FxTestSupport.waitForFxCondition {
                listView.items.toList() == listOf("DSL") &&
                    comboBox.items.toList() == listOf("DSL") &&
                    choiceBox.items.toList() == listOf("DSL") &&
                    tableView.items.toList() == listOf("DSL")
            }

            items.value = listOf("DSL", "MVVM", "Components")

            FxTestSupport.waitForFxCondition {
                listView.items.toList() == listOf("DSL", "MVVM", "Components") &&
                    comboBox.items.toList() == listOf("DSL", "MVVM", "Components") &&
                    choiceBox.items.toList() == listOf("DSL", "MVVM", "Components") &&
                    tableView.items.toList() == listOf("DSL", "MVVM", "Components")
            }
        } finally {
            scope.cancel()
        }
    }

    private fun <T : Any> fx(factory: () -> T): T {
        lateinit var result: T
        FxTestSupport.runOnFxThread {
            result = factory()
        }
        return result
    }
}
