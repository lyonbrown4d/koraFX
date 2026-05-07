# DSL Examples

## Settings Form

```kotlin
val form = panel {
    label("Settings") {
        styleClasses("headline")
    }

    gridPane(hgap = 12.0, vgap = 10.0) {
        column(prefWidth = 120.0, alignment = HPos.RIGHT)
        column(grow = Priority.ALWAYS, fillWidth = true)

        label(0, 0, "Project")
        textField(1, 0, "KoraFX") {
            maxWidth = Double.MAX_VALUE
        }

        label(0, 1, "Mode")
        checkBox(1, 1, "Kotlin-first JavaFX DSL") {
            isSelected = true
        }

        label(0, 2, "Actions")
        cell(1, 2, horizontalGrow = Priority.ALWAYS) {
            hbox(spacing = 8.0) {
                button("Apply") {
                    onAction {
                        println("Applied")
                    }
                }
                ghostButton("Reset")
            }
        }
    }
}
```

## Layout Helpers

```kotlin
val page = vbox(spacing = 16.0) {
    alignment(Pos.TOP_LEFT)
    fillWidth()

    hbox(spacing = 8.0) {
        alignment(Pos.CENTER_LEFT)

        label("Workspace") {
            margin(right = 8.0, top = 0.0, bottom = 0.0, left = 0.0)
        }

        textField("KoraFX") {
            maxWidth = Double.MAX_VALUE
            growHorizontal()
        }
    }

    stackPane {
        alignment(Pos.CENTER)

        label("Pinned") {
            align(Pos.BOTTOM_RIGHT)
            marginAll(12.0)
        }
    }
}
```

## Form Builder

```kotlin
val scope = MainScope()
val projectName = MutableStateFlow("KoraFX")
val projectError = projectName.map { value ->
    if (value.isBlank()) "Project name is required." else null
}

val view = form {
    item(
        label = "Project",
        helper = "Used in the window title and recent projects list.",
    ) {
        textField {
            maxWidth = Double.MAX_VALUE
            bindTextBidirectional(scope, projectName)
            bindInvalid(scope, projectError.map { it != null })
        }
        validationMessage(scope, projectError)
    }

    textArea(
        label = "Description",
        helper = "Wraps normal JavaFX controls in a consistent form item.",
    ) {
        prefRowCount = 4
    }

    submitBar(alignEnd = false) {
        secondaryButton("Reset") {
            onAction {
                projectName.value = "KoraFX"
            }
        }
        primaryButton("Save")
    }
}
```

## Input Bindings

```kotlin
val scope = MainScope()
val mode = MutableStateFlow<String?>("DSL First")
val runtime = MutableStateFlow<String?>("Manual JavaFX")
val workers = MutableStateFlow(2)
val targetDate = MutableStateFlow<LocalDate?>(LocalDate.now().plusWeeks(1))

val inputs = form {
    item("Mode") {
        comboBox(items = listOf("DSL First", "MVVM Ready", "Component Polish")) {
            select("DSL First")
        }.bindSelectedItemBidirectional(scope, mode)
    }

    item("Runtime") {
        choiceBox(items = listOf("Manual JavaFX", "Custom Factory", "External DI")) {
            select("Manual JavaFX")
        }.bindSelectedItemBidirectional(scope, runtime)
    }

    item("Parallel Work") {
        intSpinner(min = 1, max = 8, initialValue = workers.value) {
            isEditable = true
            bindValueBidirectional(scope, workers)
        }
    }

    item("Target Date") {
        datePicker {
            bindValueBidirectional(scope, targetDate)
        }
    }
}
```

## Menu And Tree

```kotlin
val root = vbox(spacing = 12.0) {
    menuBar {
        menu("Navigate") {
            actionItem("Overview") {
                println("Open overview")
            }
            actionItem("Settings") {
                println("Open settings")
            }
        }
    }

    treeView<String> {
        root("Routes") {
            item("Overview")
            item("Settings")
            item("About")
        }
        render { "• $it" }
        rowAction { route ->
            println("Open $route")
        }
    }
}
```

## TableView Actions

```kotlin
data class ModuleRow(
    val name: String,
    val purpose: String,
)

val rows = listOf(
    ModuleRow("framework-dsl", "Kotlin-first JavaFX API"),
    ModuleRow("framework-mvvm", "StateFlow ViewModel helpers"),
)

val table = tableView(items = rows) {
    constrainedResize()
    placeholder("No modules")

    textColumn("Module") { it.name }
    textColumn("Purpose") { it.purpose }
    actionColumn(title = "Action", text = "Open") { row ->
        println("Open ${row.name}")
    }
}
```

## Dialogs

```kotlin
val confirm = confirmation {
    title("Delete route")
    header("Remove this route?")
    message("The navigation registry will no longer include it.")
    buttonTypes(ButtonType.OK, ButtonType.CANCEL)
    expandableText("Route id: settings")
}

if (confirm.showConfirmed()) {
    println("Deleted")
}

val input = textInputDialog {
    title("New route")
    header(null)
    message("Route title")
    prompt("Settings")
}

val routeTitle = input.showText(trim = true, blankAsNull = true)

val custom = customDialog<String> {
    title("Route visibility")
    buttonTypes(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
    resultByButton(
        ButtonType.YES to "visible",
        ButtonType.NO to "hidden",
    )
}

val visibility = custom.showResult()
```
