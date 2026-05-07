package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputDialog
import java.util.Optional

fun alert(
    type: Alert.AlertType = Alert.AlertType.INFORMATION,
    init: Alert.() -> Unit = {},
    content: AlertBuilder.() -> Unit = {},
): Alert =
    Alert(type).apply(init).apply {
        AlertBuilder(this).content()
    }

fun confirmation(
    init: Alert.() -> Unit = {},
    content: AlertBuilder.() -> Unit = {},
): Alert =
    alert(Alert.AlertType.CONFIRMATION, init, content)

fun textInputDialog(
    defaultValue: String = "",
    init: TextInputDialog.() -> Unit = {},
    content: TextInputDialogBuilder.() -> Unit = {},
): TextInputDialog =
    TextInputDialog(defaultValue).apply(init).apply {
        TextInputDialogBuilder(this).content()
    }

fun <R> customDialog(
    init: Dialog<R>.() -> Unit = {},
    content: CustomDialogBuilder<R>.() -> Unit = {},
): Dialog<R> =
    Dialog<R>().apply(init).apply {
        CustomDialogBuilder(this).content()
    }

class AlertBuilder internal constructor(
    private val alert: Alert,
) {
    fun title(value: String) {
        alert.title = value
    }

    fun header(value: String?) {
        alert.headerText = value
    }

    fun message(value: String?) {
        alert.contentText = value
    }

    fun buttons(vararg buttonTypes: ButtonType) {
        alert.buttonTypes.setAll(buttonTypes.toList())
    }

    fun buttonTypes(vararg buttonTypes: ButtonType) {
        buttons(*buttonTypes)
    }

    fun expandable(node: Node) {
        alert.dialogPane.expandableContent = node
        alert.dialogPane.isExpanded = true
    }

    fun expandableText(
        value: String,
        init: TextArea.() -> Unit = {},
    ) {
        expandable(
            TextArea(value).apply {
                isEditable = false
                isWrapText = true
                prefRowCount = 8
                init()
            },
        )
    }
}

class TextInputDialogBuilder internal constructor(
    private val dialog: TextInputDialog,
) {
    fun title(value: String) {
        dialog.title = value
    }

    fun header(value: String?) {
        dialog.headerText = value
    }

    fun message(value: String?) {
        dialog.contentText = value
    }

    fun prompt(value: String) {
        dialog.editor.promptText = value
    }

    fun text(value: String) {
        dialog.editor.text = value
    }

    fun buttons(vararg buttonTypes: ButtonType) {
        dialog.dialogPane.buttonTypes.setAll(buttonTypes.toList())
    }

    fun buttonTypes(vararg buttonTypes: ButtonType) {
        buttons(*buttonTypes)
    }
}

class CustomDialogBuilder<R> internal constructor(
    private val dialog: Dialog<R>,
) {
    fun title(value: String) {
        dialog.title = value
    }

    fun header(value: String?) {
        dialog.headerText = value
    }

    fun content(node: Node) {
        dialog.dialogPane.content = node
    }

    fun content(factory: () -> Node) {
        content(factory())
    }

    fun buttons(vararg buttonTypes: ButtonType) {
        dialog.dialogPane.buttonTypes.setAll(buttonTypes.toList())
    }

    fun buttonTypes(vararg buttonTypes: ButtonType) {
        buttons(*buttonTypes)
    }

    fun result(converter: (ButtonType) -> R?) {
        dialog.setResultConverter(converter)
    }

    fun resultByButton(vararg results: Pair<ButtonType, R>) {
        val mappedResults = results.toMap()
        result { button ->
            mappedResults[button]
        }
    }
}

fun <R> Dialog<R>.showResult(): R? =
    showAndWait().orNull()

fun Alert.showConfirmed(acceptedButton: ButtonType = ButtonType.OK): Boolean =
    showAndWait().orElse(ButtonType.CANCEL) == acceptedButton

fun TextInputDialog.showText(
    trim: Boolean = false,
    blankAsNull: Boolean = false,
): String? {
    val value = showAndWait().orNull()
    val normalized = if (trim) value?.trim() else value
    return if (blankAsNull && normalized.isNullOrBlank()) {
        null
    } else {
        normalized
    }
}

private fun <T> Optional<T>.orNull(): T? =
    orElse(null)
