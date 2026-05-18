package dev.korafx.dsl

import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

fun form(
    spacing: Double = 16.0,
    init: VBox.() -> Unit = {},
    content: FormBuilder.() -> Unit,
): VBox =
    vbox(
        spacing = spacing,
        init = {
            styleClass("form")
            init()
        },
    ) {
        FormBuilder(this).content()
    }

fun NodeContainerBuilder.form(
    spacing: Double = 16.0,
    init: VBox.() -> Unit = {},
    content: FormBuilder.() -> Unit,
): VBox =
    add(dev.korafx.dsl.form(spacing, init, content))

class FormBuilder internal constructor(
    private val container: VBoxBuilder,
) {
    fun item(
        label: String,
        helper: String? = null,
        content: FormItemBuilder.() -> Unit,
    ): VBox =
        container.vbox(
            spacing = 8.0,
            init = {
                styleClass("form-item")
            },
        ) {
            this.label(label) {
                styleClass("form-label")
            }

            if (helper != null) {
                this.label(helper) {
                    styleClass("form-helper")
                    isWrapText = true
                }
            }

            FormItemBuilder(this).content()
        }

    fun <T : Node> field(
        label: String,
        helper: String? = null,
        content: FormItemBuilder.() -> T,
    ): T {
        var result: T? = null
        item(label, helper) {
            result = content()
        }
        return checkNotNull(result) {
            "Form field content did not return a node."
        }
    }

    fun textField(
        label: String,
        helper: String? = null,
        text: String = "",
        init: TextField.() -> Unit = {},
    ): TextField =
        field(label, helper) {
            textField(text, init)
        }

    fun textArea(
        label: String,
        helper: String? = null,
        text: String = "",
        init: TextArea.() -> Unit = {},
    ): TextArea =
        field(label, helper) {
            textArea(text, init)
        }

    fun checkBox(
        label: String,
        helper: String? = null,
        text: String = label,
        init: CheckBox.() -> Unit = {},
    ): CheckBox =
        field(label, helper) {
            checkBox(text, init)
        }

    fun submitBar(
        spacing: Double = 12.0,
        alignEnd: Boolean = true,
        content: SubmitBarBuilder.() -> Unit,
    ): HBox =
        container.hbox(
            spacing = spacing,
            init = {
                styleClass("submit-bar")
            },
        ) {
            if (alignEnd) {
                spacer()
            }
            SubmitBarBuilder(this).content()
        }
}

class FormItemBuilder internal constructor(
    private val container: VBoxBuilder,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        container.add(node)
    }

    fun validationMessage(text: String, init: Label.() -> Unit = {}): Label =
        label(text) {
            styleClass("validation-message")
            init()
        }

    fun validationMessage(
        scope: CoroutineScope,
        flow: Flow<String?>,
        init: Label.() -> Unit = {},
    ): Label =
        validationMessage("", init).also {
            it.bindValidation(scope, flow)
        }
}

class SubmitBarBuilder internal constructor(
    private val container: HBoxBuilder,
) : NodeContainerBuilder() {
    override fun append(node: Node) {
        container.add(node)
    }

    fun primaryButton(text: String, init: javafx.scene.control.Button.() -> Unit = {}) =
        button(text, init)

    fun secondaryButton(text: String, init: javafx.scene.control.Button.() -> Unit = {}) =
        ghostButton(text, init)
}
