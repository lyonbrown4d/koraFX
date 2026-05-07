package dev.korafx.dsl

import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.Text
import javafx.scene.text.TextFlow

fun textFlow(
    init: TextFlow.() -> Unit = {},
    content: TextFlowBuilder.() -> Unit = {},
): TextFlow =
    TextFlow().apply(init).apply {
        TextFlowBuilder(this).content()
    }

fun text(
    value: String,
    init: Text.() -> Unit = {},
): Text =
    Text(value).apply(init)

fun imageView(
    image: Image? = null,
    init: ImageView.() -> Unit = {},
): ImageView =
    ImageView(image).apply(init)

fun tooltip(
    text: String = "",
    init: Tooltip.() -> Unit = {},
): Tooltip =
    Tooltip(text).apply(init)

fun <T : Control> T.attachTooltip(
    text: String,
    init: Tooltip.() -> Unit = {},
): T =
    apply {
        tooltip = dev.korafx.dsl.tooltip(text, init)
    }

class TextFlowBuilder internal constructor(
    private val textFlow: TextFlow,
) : NodeContainerBuilder() {
    override fun append(node: javafx.scene.Node) {
        textFlow.children += node
    }

    fun text(
        value: String,
        init: Text.() -> Unit = {},
    ): Text = add(dev.korafx.dsl.text(value, init))

    fun lineBreak(): Text = text("\n")
}

fun NodeContainerBuilder.textFlow(
    init: TextFlow.() -> Unit = {},
    content: TextFlowBuilder.() -> Unit = {},
): TextFlow = add(dev.korafx.dsl.textFlow(init, content))

fun NodeContainerBuilder.imageView(
    image: Image? = null,
    init: ImageView.() -> Unit = {},
): ImageView = add(dev.korafx.dsl.imageView(image, init))
